package org.javamaster.invocationlab.admin

import com.jcraft.jsch.*
import org.apache.commons.io.IOUtils
import org.springframework.util.Assert
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystemException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * @author yudong
 * @date 2019/12/16
 */
@Suppress("UNCHECKED_CAST")
class SftpClient {
    private var channelSftp: ChannelSftp? = null

    private var channelShell: ChannelShell? = null

    private var channelExec: ChannelExec? = null

    private var session: Session? = null

    /**
     * SFTP 登录用户名
     */
    private var username: String

    /**
     * SFTP 登录密码
     */
    private lateinit var password: String

    /**
     * 私钥
     */
    private var privateKey: String? = null

    /**
     * SFTP 服务器地址IP地址
     */
    private val host: String

    /**
     * SFTP 端口
     */
    private val port: Int

    /**
     * 构造基于密码认证的sftp对象
     */
    constructor(username: String, password: String, host: String, port: Int) {
        this.username = username
        this.password = password
        this.host = host
        this.port = port
    }

    /**
     * 构造基于秘钥认证的sftp对象
     */
    constructor(username: String, host: String, port: Int, privateKey: String?) {
        this.username = username
        this.host = host
        this.port = port
        this.privateKey = privateKey
    }

    /**
     * 连接sftp服务器
     */

    fun connect() {
        val jsch = JSch()
        if (privateKey != null) {
            // 设置私钥
            jsch.addIdentity(privateKey)
        }
        session = jsch.getSession(username, host, port)
        session!!.setPassword(password)
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        session!!.setConfig(config)
        session!!.connect(TIMEOUT)
    }


    fun exec(command: String): String {
        checkExecChannelConnect()
        channelExec!!.setCommand(command)
        channelExec!!.connect(TIMEOUT)
        return readResult(channelExec!!.inputStream)
    }


    fun shell(command: String): String {
        var newCommand = command
        checkShellChannelConnect()
        newCommand += "\n"
        val inputStream = channelShell!!.inputStream
        val out = channelShell!!.outputStream
        out.write(newCommand.toByteArray(StandardCharsets.UTF_8))
        out.flush()
        return readResult(inputStream)
    }


    private fun readResult(`in`: InputStream): String {
        val sb = StringBuilder()
        var beat = 0
        while (beat <= 3) {
            if (`in`.available() > 0) {
                val bytes = ByteArray(`in`.available())
                `in`.read(bytes)
                sb.append(String(bytes))
                beat = 0
            } else {
                if (sb.isNotEmpty()) {
                    ++beat
                }
                TimeUnit.SECONDS.sleep(1)
            }
        }
        return sb.toString()
    }

    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     *
     * @param basePath     服务器的基础路径
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     * @param input        输入流
     */

    fun upload(basePath: String, directory: String, sftpFileName: String, input: InputStream) {
        checkSftpChannelConnect()
        try {
            channelSftp!!.cd(basePath)
            channelSftp!!.cd(directory)
        } catch (e: SftpException) {
            //目录不存在，则创建文件夹
            val dirs = directory.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var tempPath = basePath
            for (dir in dirs) {
                if ("" == dir) {
                    continue
                }
                tempPath += "/$dir"
                try {
                    channelSftp!!.cd(tempPath)
                } catch (ex: SftpException) {
                    channelSftp!!.mkdir(tempPath)
                    channelSftp!!.cd(tempPath)
                }
            }
        }
        channelSftp!!.put(input, sftpFileName)
    }


    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的完整路径
     */

    private fun download(directory: String, downloadFile: String, saveFile: String) {
        checkSftpChannelConnect()
        Assert.hasText(directory, "directory can't be blank")
        channelSftp!!.cd(directory)
        channelSftp!!.get(downloadFile, FileOutputStream(saveFile))
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件名
     * @return 字节数组
     */

    fun download(directory: String, downloadFile: String): ByteArray {
        checkSftpChannelConnect()
        Assert.hasText(directory, "directory can't be blank")
        channelSftp!!.cd(directory)
        val `is` = channelSftp!![downloadFile]
        return IOUtils.toByteArray(`is`)
    }


    fun downloadLogFiles(logPath: String, savePath: String, currentFileName: String, historyFileNamePrefix: String) {
        checkSftpChannelConnect()
        val path = File(savePath)
        if (!path.exists()) {
            val success = path.mkdirs()
            if (!success) {
                throw FileSystemException("can't create path $password")
            }
        }
        var saveFile = "$savePath/$currentFileName"
        download(logPath, currentFileName, saveFile)
        log.info("download {} to {} finished", currentFileName, saveFile)
        val fileNames = getFileNames(logPath, historyFileNamePrefix)
        for (fileName in fileNames) {
            saveFile = "$savePath/$fileName"
            download(logPath, fileName, saveFile)
            log.info("download {} to {} finished%n", fileName, saveFile)
        }
    }

    private fun getFileNames(directory: String, prefix: String): List<String> {
        checkSftpChannelConnect()
        return listFiles(directory).stream()
            .filter { lsEntry ->
                lsEntry.filename.startsWith(
                    prefix
                )
            }
            .map { it.filename }
            .sorted()
            .collect(Collectors.toList())
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */

    fun delete(directory: String, deleteFile: String) {
        checkSftpChannelConnect()
        channelSftp!!.cd(directory)
        channelSftp!!.rm(deleteFile)
    }


    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     */

    private fun listFiles(directory: String): Vector<ChannelSftp.LsEntry> {
        checkSftpChannelConnect()
        return channelSftp!!.ls(directory) as Vector<ChannelSftp.LsEntry>
    }


    private fun checkSftpChannelConnect() {
        if (channelSftp != null) {
            return
        }
        channelSftp = session!!.openChannel("sftp") as ChannelSftp
        channelSftp!!.connect(TIMEOUT)
    }


    private fun checkShellChannelConnect() {
        if (channelShell != null) {
            return
        }
        channelShell = session!!.openChannel("shell") as ChannelShell
        channelShell!!.connect(TIMEOUT)
    }


    private fun checkExecChannelConnect() {
        if (channelExec != null) {
            return
        }
        channelExec = session!!.openChannel("exec") as ChannelExec
        channelExec!!.setErrStream(System.err)
    }

    /**
     * 关闭连接
     */
    fun disconnect() {
        if (channelSftp?.isConnected == true) {
            channelSftp?.disconnect()
        }
        if (channelShell?.isConnected == true) {
            channelShell?.disconnect()
        }
        if (channelExec?.isConnected == true) {
            channelExec?.disconnect()
        }
        if (session?.isConnected == true) {
            session?.disconnect()
        }
    }

    companion object {
        private const val TIMEOUT = 6000
    }
}
