package com.libsign.libsign;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.tb.moviestools.TheApp;

import java.text.DecimalFormat;


public class Utils {
    /**
     *
     * @return
     */
    public static String getAndroidId() {
        return Settings.System.getString(TheApp.Companion.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取时间格式
     *
     * @param time
     * @return
     */
    public static String getNormalTime(long time) {
        StringBuffer buffer = new StringBuffer();
        int sec = (int) (time / 1000);
        if (sec > 0) {
            if (sec > 60) {
                int min = sec / 60;
                if (min < 10) {
                    buffer.append(0);
                    buffer.append(min);
                    buffer.append(":");
                    if (sec % 60 < 10) {
                        buffer.append(0);
                        buffer.append(sec % 60);
                    } else {
                        buffer.append(sec % 60);
                    }
                } else {
                    buffer.append(min);
                    buffer.append(":");
                    if (sec % 60 < 10) {
                        buffer.append(0);
                        buffer.append(sec % 60);
                    } else {
                        buffer.append(sec % 60);
                    }
                }
            } else {
                buffer.append("00:");
                if (sec < 10) {
                    buffer.append(0);
                    buffer.append(sec);
                } else {
                    buffer.append(sec);
                }
            }
        } else {
            buffer.append("00:00");
        }
        return buffer.toString();
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("0.0");
        String fileSizeString = "";
        if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 转换码率
     *
     * @param bitrate
     * @return
     */
    public static String getBitrateStr(long bitrate) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format((double) bitrate / 1024) + "kbps";
    }

    /**
     * 判断字符串的是否为空或者空格
     * @param sc
     * @return
     */
    public static Boolean isBlank(CharSequence sc) {
        if (sc != null && sc.length() > 0) {
            for (int i = 0; i < sc.length(); i++) {
                if (!Character.isWhitespace(sc.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }


    public static String getMetrics(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int i2 = displayMetrics.widthPixels;
        int i3 = displayMetrics.heightPixels;
        return i3 + "*" + i2;
    }
    public static boolean isHasSim() {
        int simState = ((TelephonyManager) TheApp.Companion.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getSimState();
        return simState != 0 && simState != 1;
    }


    public static String deviceName(Context context) {
        try {
            String string = Settings.Global.getString(context.getContentResolver(), "device_name");
            return TextUtils.isEmpty(string) ? DecodeUtils.decode(new byte[]{70, 60, 99, 32, 104, 59, 99}, new byte[]{7, 82}) : string;
        } catch (Exception unused) {
            return DecodeUtils.decode(new byte[]{70, 60, 99, 32, 104, 59, 99}, new byte[]{7, 82});
        }
    }

}
