package com.libsign.libsign;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Description : TODO
 * @Author : tuozhonghua
 * @Date: 2023/6/7 15:14
 * @Version : 1.0
 */
public class DecodeUtils {
    public static String decode(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        int length2 = bArr2.length;
        int i2 = 0;
        int i3 = 0;
        while (i2 < length) {
            if (i3 >= length2) {
                i3 = 0;
            }
            bArr[i2] = (byte) (bArr[i2] ^ bArr2[i3]);
            i2++;
            i3++;
        }
        return new String(bArr, StandardCharsets.UTF_8);
    }

    public static String d(byte[] bArr, String str) {
        try {
            if (!TextUtils.isEmpty(str)) {
                str = str.substring(5, 37);
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(str.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(1, secretKeySpec);
            int length = bArr.length;
            int i2 = 16 - (length % 16);
            if (i2 == 16) {
                i2 = 0;
            }
            int i3 = i2 + length;
            byte[] bArr2 = new byte[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                if (i4 < length) {
                    bArr2[i4] = bArr[i4];
                } else {
                    bArr2[i4] = 0;
                }
            }
            return new String(Base64.encode(cipher.doFinal(bArr2), 0));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return "";
        } catch (BadPaddingException e3) {
            e3.printStackTrace();
            return "";
        } catch (IllegalBlockSizeException e4) {
            e4.printStackTrace();
            return "";
        } catch (NoSuchPaddingException e5) {
            e5.printStackTrace();
            return "";
        }
    }

    public static String getCommonUb(){
        //52,138,138,242,138,24,63,63,59
        String[] tmpData = new String[]{"138","242","138","24","63","63"};
        StringBuilder sb = new StringBuilder();
        long offTime = System.currentTimeMillis() / 1000 - 15;
        sb.append("52,");
        sb.append(offTime).append(",");
        sb.append("138,");
        sb.append(offTime).append(",");
        for (int i = 0; i < tmpData.length; i++) {
            sb.append(tmpData[i]).append(",");
            sb.append(offTime + i + 1).append(",");
        }
        sb.append("59").append(",");
        sb.append(System.currentTimeMillis() / 1000 - 10).append(",");
        return sb.toString();
    }

    public static String getCode(String androidID,long time1) {
        String plus_android = "com.rivoto.plus-e82e3f91bcff2c97fa5d226b9c8ea51d-" + androidID;
//        System.out.println("plus_android -> " + plus_android);
        String plus_androidMD5 = string2MD5(plus_android);
//        System.out.println("plus_androidMD5 -> " + plus_androidMD5);


        String plus_androidMD5_time = plus_androidMD5 + "/" + time1;
//        System.out.println("plus_androidMD5_time -> " + plus_androidMD5_time);
        return string2MD5(plus_androidMD5_time);
    }


    public static String string2MD5(String str)
    {
        MessageDigest mdEncoder;
        String plus_androidMD5 = null;
        try {
            mdEncoder = MessageDigest.getInstance("MD5");
            mdEncoder.update(str.getBytes(StandardCharsets.US_ASCII), 0, str.length());
            StringBuilder hexString = new StringBuilder();
            for (byte b : mdEncoder.digest()) {
                hexString.append(String.format("%02x", b & 0xff));
            }
            plus_androidMD5 = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return plus_androidMD5;
    }
}
