package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.enums.CipherTypeEnum;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AesUtils {

    @SneakyThrows
    public static String encrypt(final String plainValue, CipherTypeEnum cipherType, String cipherKey) {
        if (null == plainValue) {
            return null;
        }
        try {
            byte[] result = getCipher(Cipher.ENCRYPT_MODE, cipherType, cipherKey)
                    .doFinal(plainValue.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printBase64Binary(result);
        } catch (Exception e) {
            return plainValue;
        }
    }

    @SneakyThrows
    public static String decrypt(final String cipherValue, CipherTypeEnum cipherType, String cipherKey) {
        if (null == cipherValue) {
            return null;
        }
        try {
            byte[] result = getCipher(Cipher.DECRYPT_MODE, cipherType, cipherKey)
                    .doFinal(DatatypeConverter.parseBase64Binary(cipherValue));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return cipherValue;
        }
    }

    @SneakyThrows
    private static Cipher getCipher(final int decryptMode, CipherTypeEnum cipherType, String cipherKey) {
        byte[] secretKey;
        switch (cipherType) {
            case MYSQL_AES:
                secretKey = Arrays.copyOf(cipherKey.getBytes(StandardCharsets.UTF_8), 16);
                break;
            case AES:
                secretKey = Arrays.copyOf(DigestUtils.sha1(cipherKey), 16);
                break;
            default:
                throw new IllegalArgumentException(cipherKey);
        }
        Cipher result = Cipher.getInstance(CipherTypeEnum.AES.type);
        result.init(decryptMode, new SecretKeySpec(secretKey, CipherTypeEnum.AES.type));
        return result;
    }

}
