package com.libsign.libsign.service;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

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
 * date        : 2023/8/11 17:03
 * description :
 */
public interface MirozApi {
    @Multipart
    @POST("/144/")
    Observable<String> findVideoDetails(@PartMap Map<String, RequestBody> map);
    @Multipart
    @POST("/317/")
    Observable<String> findMLink(@PartMap Map<String, RequestBody> map);
    @FormUrlEncoded
    @POST("/87/")
    Observable<String> findInitParams(@FieldMap Map<String,String> map);

    @FormUrlEncoded
    @POST("/203/")
    Observable<String> findEps(@FieldMap Map<String,String> map);

    @Multipart
    @POST("/202/")
    Observable<String> findTTDetails(@PartMap Map<String, RequestBody> map);

    @Multipart
    @POST("/151/")
    Observable<String> findTTLink(@PartMap Map<String, RequestBody> map);
}
