package com.libsign.libsign;



/**
 * @Description : TODO
 * @Author : tuozhonghua
 * @Date: 2023/6/7 15:57
 * @Version : 1.0
 */
public class SU {
    static {
        try {
            System.loadLibrary("SU");
        } catch (Exception e) {
        }
    }

    public static native String ig();

    public static native String ig2(String str);
}