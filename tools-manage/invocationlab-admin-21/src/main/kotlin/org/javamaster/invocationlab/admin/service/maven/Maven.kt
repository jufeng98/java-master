package org.javamaster.invocationlab.admin.service.maven

import org.apache.maven.cli.MavenCli
import org.javamaster.invocationlab.admin.io.RedirectConsoleOutputStream
import org.javamaster.invocationlab.admin.service.GAV
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author yudong
 * 处理从nexus下载api.jar
 * 然后通过embededmaven下载这个api.jar的依赖
 */
class Maven(private val nexusUrl: String, private val nexusPathReleases: String, private val fileBasePath: String) {
    fun dependency(serviceName: String, gav: GAV) {
        val versionDirName = gav.version!!.replace("\\.".toRegex(), "_")
        val serviceDirName = versionDirName + "_" + serviceName
        RedirectConsoleOutputStream(ByteArrayOutputStream()).use {
            val pomPath = downPomAndJar(serviceDirName, gav.groupID, gav.artifactID, gav.version, it)
            logger.info("构建完成10%...")
            mavenCopyDependencies(pomPath, it)
        }
    }

    private fun downPomAndJar(
        serviceName: String, groupId: String?, artifactId: String?, version: String?,
        resultPrintStream: RedirectConsoleOutputStream
    ): String {
        resultPrintStream.println("准备下载api.jar文件...")
        resultPrintStream.println("groupId:$groupId")
        resultPrintStream.println("artifactId:$artifactId")
        resultPrintStream.println("version:$version")

        val jarUrl = buildNexusUrl(groupId, artifactId, version, "jar")
        resultPrintStream.println("api.jar的url:$jarUrl")

        val pomUrl = buildNexusUrl(groupId, artifactId, version, "pom")
        resultPrintStream.println("构建api.jar的pom.xml文件的url:$pomUrl")

        val basePath = "$fileBasePath/$serviceName"

        val file = File(basePath)
        if (file.exists()) {
            file.delete()
        }
        file.mkdirs()
        val jarPath = "$fileBasePath/$serviceName/lib"
        val libfile = File(jarPath)
        if (libfile.exists()) {
            file.delete()
        }
        libfile.mkdirs()

        resultPrintStream.println("api.jar下载路径:$jarPath")
        resultPrintStream.println("pom.xml的下载路径:$basePath")
        resultPrintStream.println("开始下载:$artifactId.jar文件")
        doDownLoadFile(jarUrl, jarPath, "$artifactId.jar")

        resultPrintStream.println("下载:$artifactId.jar文件成功")
        resultPrintStream.println("开始下载:" + artifactId + "的pom.xml文件")
        doDownLoadFile(pomUrl, basePath, "pom.xml")

        resultPrintStream.println("下载:" + artifactId + "的pom.xml文件成功")

        return basePath
    }

    private fun mavenCopyDependencies(pomPath: String, resultPrintStream: RedirectConsoleOutputStream) {
        val cli = MavenCli()
        resultPrintStream.println(
            "处理api.jar的所有依赖,通过执行maven命令: 'mvn dependency:copy-dependencies"
                    + " -DoutputDirectory=./lib -DexcludeScope=provided -U'"
        )
        resultPrintStream.println("开始执行maven命令")
        System.setProperty("maven.multiModuleProjectDirectory", "./")
        val result = cli.doMain(
            arrayOf(
                "dependency:copy-dependencies",
                "-DoutputDirectory=./lib",
                "-DexcludeScope=provided ",
                "-U"
            ), pomPath, resultPrintStream, resultPrintStream
        )
        val success = (result == 0)
        logger.info("构建完成100%,构建结果:{}", success)
        if (!success) {
            val logByteArray = resultPrintStream.logByteArray
            throw RuntimeException(String(logByteArray, StandardCharsets.UTF_8))
        }
    }


    private fun doDownLoadFile(baseUrl: String, filePath: String, fileName: String) {
        val httpUrl = URI.create(baseUrl).toURL()
        val conn = httpUrl.openConnection() as HttpURLConnection
        conn.doInput = true
        conn.doOutput = true
        conn.connect()
        val inputStream = conn.inputStream
        val bis = BufferedInputStream(inputStream)
        val fileOut = FileOutputStream(getFilePath(filePath, fileName))
        val bos = BufferedOutputStream(fileOut)
        val buf = ByteArray(4096)
        var length = bis.read(buf)
        //保存文件
        while (length != -1) {
            bos.write(buf, 0, length)
            length = bis.read(buf)
        }
        bos.close()
        bis.close()
        conn.disconnect()
    }

    private fun getFilePath(filePath: String, fileName: String): String {
        //判断文件的保存路径后面是否以/结尾
        var newFilePath = filePath
        if (!newFilePath.endsWith("/")) {
            newFilePath += "/"
        }
        return newFilePath + fileName
    }

    private fun buildNexusUrl(groupId: String?, artifactId: String?, version: String?, type: String): String {
        val upperV = version!!.trim { it <= ' ' }.uppercase(Locale.getDefault())
        val suffixUrl: String
        if (upperV.endsWith("SNAPSHOT")) {
            suffixUrl = "?r=snapshots&g=$groupId&a=$artifactId&v=$version&e=$type"
            return nexusUrl + suffixUrl
        } else {
            suffixUrl = groupId!!.replace(".", "/") + "/" + artifactId + "/" + version + "/" +
                    artifactId + "-" + version + "." + type
            return nexusPathReleases + suffixUrl
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Maven::class.java)
    }
}
