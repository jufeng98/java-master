package org.javamaster.invocationlab.admin

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeployTest {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun restartServers() {
        val name = "invocationlab-admin.jar"
        val file = java.io.File("target/$name")
        val sftpClient = SftpClient("root", "root", "192.168.240.6", 22)
        try {
            sftpClient.connect()
            log.info("开始上传")
            sftpClient.upload("/home/appadm", "rpc-postman", name, java.nio.file.Files.newInputStream(file.toPath()))
            log.info("上传成功")
            var command = "cd /home/appadm/rpc-postman"
            var res = sftpClient.shell(command)
            log.info("command:({}),res:({})", command, res)
            command = "./restart.sh"
            res = sftpClient.shell(command)
            log.info("command:({}),res:({})", command, res)
        } finally {
            sftpClient.disconnect()
        }
    }
}
