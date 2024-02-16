package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.enums.CipherTypeEnum
import org.apache.commons.codec.digest.DigestUtils
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AesUtils {
    @JvmStatic

    fun encrypt(plainValue: String?, cipherType: CipherTypeEnum, cipherKey: String): String? {
        if (null == plainValue) {
            return null
        }
        try {
            val result = getCipher(Cipher.ENCRYPT_MODE, cipherType, cipherKey)
                .doFinal(plainValue.toByteArray(StandardCharsets.UTF_8))
            return Base64.getEncoder().encodeToString(result)
        } catch (e: Exception) {
            return plainValue
        }
    }

    @JvmStatic

    fun decrypt(cipherValue: String?, cipherType: CipherTypeEnum, cipherKey: String): String? {
        if (null == cipherValue) {
            return null
        }
        try {
            val result = getCipher(Cipher.DECRYPT_MODE, cipherType, cipherKey)
                .doFinal(Base64.getDecoder().decode(cipherValue))
            return String(result, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return cipherValue
        }
    }


    private fun getCipher(decryptMode: Int, cipherType: CipherTypeEnum, cipherKey: String): Cipher {
        val secretKey = when (cipherType) {
            CipherTypeEnum.MYSQL_AES -> cipherKey.toByteArray(StandardCharsets.UTF_8).copyOf(16)
            CipherTypeEnum.AES -> DigestUtils.sha1(cipherKey).copyOf(16)
        }
        val result = Cipher.getInstance(CipherTypeEnum.AES.type)
        result.init(decryptMode, SecretKeySpec(secretKey, CipherTypeEnum.AES.type))
        return result
    }
}
