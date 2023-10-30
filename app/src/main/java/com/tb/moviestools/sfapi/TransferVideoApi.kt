package com.tb.moviestools.sfapi

import com.tb.moviestools.AppLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 *_    .--,       .--,
 *_   ( (  \\.---./  ) )
 *_    '.__/o   o\\__.'
 *_       {=  ^  =}
 *_        >  -  <
 *_       /       \\
 *_      //       \\\\
 *_     //|   .   |\\\\
 *_     \"'\\       /'\"_.-~^`'-.
 *_        \\  _  /--'         `
 *_      ___)( )(___
 *_     (((__) (__)))
 * author      : xue
 * date        : 2023/10/30 13:29
 * description :
 */
object TransferVideoApi {
    //https://www.movieson.net
    const val host = "https://movie.powerfulclean.net"

    private val retrofit by lazy { createRetrofit(host) }

    fun getList(count: Int, callback: (List<VideoDescInfo>) -> Unit) {
        val params = JSONObject()
        params.put("page_number", 1)
        params.put("page_size", count)
        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            params.toString()
        )
        val dis = createApiService()
            .getAllCreatedList(body)
            .map { parseList(it) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback.invoke(it)
            }, {
                AppLog.logE(it.message ?: "")
                callback.invoke(emptyList())
            })
    }


    fun crateM3u8(url: String, id: String, isMovie: Boolean, content: String): Boolean {
        try {
            val obj = JSONObject()
            obj.put("source_key", "68D23E4E2A7013E21D82C5A24D8E051A")
            if (isMovie) {
                obj.put("source_type_ordinal", "1")
            } else {
                obj.put("source_type_ordinal", "0")
            }
            obj.put("source_sequence", id)
            obj.put("url", url)
            obj.put("m3u8_text", content)
            val body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                obj.toString()
            )
            val okhttp: OkHttpClient = createCommonOkhttp()
            val httpRequest: Request = Request.Builder()
                .url("$host/v1/media/create").post(body)
                .build()
            val response = okhttp.newCall(httpRequest).execute()
            if (response.isSuccessful) {
                AppLog.logE("upload play_address api success " + response.code())
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            AppLog.logE("upload play_address api error " + e.message)
        }

        return false
    }

    private fun parseList(content: String): List<VideoDescInfo> {
        val json = JSONObject(content)
        val recordArray = json.getJSONArray("records")
        val list = ArrayList<VideoDescInfo>()
        for (i in 0 until recordArray.length()) {
            val item = recordArray.getJSONObject(i)
            val vdi = VideoDescInfo(
                item.optString("video_id"),
                item.optInt("source_type_ordinal")
            )
            list.add(vdi)
        }
        return list
    }

    fun createCommonOkhttp(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(15000, TimeUnit.MILLISECONDS)
            .readTimeout(15000, TimeUnit.MILLISECONDS)
            .hostnameVerifier(TrustAllHostnameVerifier())
            .proxy(Proxy.NO_PROXY)
        val sf = createSSLSocketFactory()
        if (sf != null) {
            client.sslSocketFactory(sf)
        }
        return client.build()
    }

    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).client(createCommonOkhttp())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    fun createApiService(): TfVideoService {
        return retrofit.create(TfVideoService::class.java)
    }

    private class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    public
    class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }

    fun createSSLSocketFactory(): SSLSocketFactory? {
        var ssfFactory: SSLSocketFactory? = null
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf(TrustAllCerts()), SecureRandom())
            ssfFactory = sc.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ssfFactory
    }


}