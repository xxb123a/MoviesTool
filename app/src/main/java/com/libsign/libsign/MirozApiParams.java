package com.libsign.libsign;

import android.os.Build;

import com.tb.moviestools.TheApp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.RequestBody;

/**
 * _    .--,       .--,
 * _   ( (  \\.---./  ) )
 * _    '.__/o   o\\__.'
 * _       {=  ^  =}
 * _        >  -  <
 * _       /       \\
 * _      //       \\\\
 * _     //|   .   |\\\\
 * _     \"'\\       /'\"_.-~^`'-.
 * _        \\  _  /--'         `
 * _      ___)( )(___
 * _     (((__) (__)))    高山仰止,景行行止.虽不能至,心向往之。
 * author      : xue
 * date        : 2023/8/11 16:55
 * description :
 */
public class MirozApiParams {
    public static Map<String, String> getCommonParams() {
        Map<String, String> map = new HashMap<>();
        map.put("sig2", DecodeUtils.getCode(Utils.getAndroidId(), System.currentTimeMillis() / 1000));
        map.put("unixtime1", System.currentTimeMillis() / 1000L + "");
        map.put("device", "android");
        map.put("app_ver", "1.0.6");
        map.put("os_ver", Build.VERSION.RELEASE);
        map.put("resolution", Utils.getMetrics(TheApp.Companion.getInstance()));
        map.put("deviceNo", Utils.getAndroidId());
        Locale locale = Locale.getDefault();
        map.put("country", locale.getCountry());
        map.put("lang", locale.getLanguage());
        map.put("app_id", "413");
        map.put("installTime", TheApp.Companion.getPackageInfo().firstInstallTime / 1000 + "");
        map.put("resolution_e", "15712*174");
        map.put("token", System.currentTimeMillis() + "");

        map.put("idfa", "");
        map.put("simcard", Utils.isHasSim() ? "1" : "0");
        map.put("brand", Build.BRAND);
        map.put("model", Build.MODEL);
        map.put("title", Utils.deviceName(TheApp.Companion.getInstance()));
        map.put("reg_id", "");
        map.put("vp", "0");
        return map;
    }

    private static String getCommonUb() {
        //52,138,138,242,138,24,63,63,59
        String[] tmpData = new String[]{"138", "242", "138", "24", "63", "63"};
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

    public static Map<String, String> getMediaDetailsParams(String vid) {
        Map<String, String> params = getCommonParams();
        params.put("m_id", vid);
        params.put("p1", "2");
        params.put("detail_from", "PlayerActivity_release");
        params.put("api_ver", "6");
        params.put("stageflag", "2");
        params.put("unixtime", (System.currentTimeMillis() / 1000) + "");

        String str = getCommonUb();
        String ub = DecodeUtils.d(str.getBytes(), SU.ig2(str));
        params.put("ub", ub);
        params.put("uid", "0");
        return params;
    }


    public static Map<String, String> getTTLinkParams(String tid) {
        Map<String, String> params = getCommonParams();
        params.put("tt_id", tid);
        params.put("api_ver", "6");
        params.put("unixtime", (System.currentTimeMillis() / 1000) + "");

        String str = getCommonUb();
        String ub = DecodeUtils.d(str.getBytes(), SU.ig2(str));
        params.put("ub", ub);
        return params;
    }

    public static Map<String, String> getTTDetailsParams(String tid) {
        Map<String, String> params = getCommonParams();
        params.put("tt_id", tid);
        params.put("p1", "1");
        params.put("uid", "0");
        params.put("api_ver", "6");

        String str = getCommonUb();
        String ub = DecodeUtils.d(str.getBytes(), SU.ig2(str));
        params.put("ub", ub);
        return params;
    }

    public static Map<String, String> getInitParams() {
        Map<String, String> params = getCommonParams();
        //1009692
        int timezone = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000;
        params.put("timezone", timezone + "");
        params.put("channel", "");
        params.put("device_type", "1");
        params.put("net", "dummy0,lo,wlan0,rmnet_data0,");
        params.put("mheader", "7");
        params.put("r2", "0");
        params.put("apk_name", "com.rivoto.plus");
        return params;
    }

    public static Map<String, String> getEpsParams(String ssnId) {
        Map<String, String> params = getCommonParams();
        params.put("ssn_id", ssnId);
        return params;
    }

    public static Map<String, RequestBody> map2MultiMapBody(Map<String, String> params) {
        Map<String, RequestBody> map = new HashMap<>();
        for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
            map.put(stringStringEntry.getKey(), RequestBody.create(null, stringStringEntry.getValue()));
        }
        return map;
    }
}
