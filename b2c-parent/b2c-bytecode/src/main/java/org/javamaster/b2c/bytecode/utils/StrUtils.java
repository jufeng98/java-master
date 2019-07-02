package org.javamaster.b2c.bytecode.utils;

import java.io.UTFDataFormatException;

/**
 * @author yudong
 * @date 2019/6/25
 */
public class StrUtils {

    public static String getStringValue(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        short length = (short) bytes.length;
        try {
            int c, char2, char3;
            int count = 0;
            int chararrCount = 0;
            char[] chararr = new char[length];

            while (count < length) {
                c = (int) bytes[count] & 0xff;
                if (c > 127) {
                    break;
                }
                count++;
                chararr[chararrCount++] = (char) c;
            }

            while (count < length) {
                c = (int) bytes[count] & 0xff;
                switch (c >> 4) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        /* 0xxxxxxx*/
                        count++;
                        chararr[chararrCount++] = (char) c;
                        break;
                    case 12:
                    case 13:
                        /* 110x xxxx   10xx xxxx*/
                        count += 2;
                        if (count > length) {
                            throw new UTFDataFormatException("malformed input: partial character at end");
                        }
                        char2 = (int) bytes[count - 1];
                        if ((char2 & 0xC0) != 0x80) {
                            throw new UTFDataFormatException("malformed input around byte " + count);
                        }
                        chararr[chararrCount++] = (char) (((c & 0x1F) << 6) |
                                (char2 & 0x3F));
                        break;
                    case 14:
                        /* 1110 xxxx  10xx xxxx  10xx xxxx */
                        count += 3;
                        if (count > length) {
                            throw new UTFDataFormatException("malformed input: partial character at end");
                        }
                        char2 = (int) bytes[count - 2];
                        char3 = (int) bytes[count - 1];
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                            throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                        }
                        chararr[chararrCount++] = (char) (((c & 0x0F) << 12) |
                                ((char2 & 0x3F) << 6) |
                                ((char3 & 0x3F) << 0));
                        break;
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new UTFDataFormatException("malformed input around byte " + count);
                }
            }
            // The number of chars produced may be less than utflen
            return new String(chararr, 0, chararrCount);
        } catch (Exception e) {
            throw new RuntimeException("transform failed", e);
        }
    }

}
