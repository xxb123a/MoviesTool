package com.tb.moviestools.api

import android.annotation.SuppressLint
import android.util.ArrayMap
import com.libsign.libsign.MirozDataSource
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.EpsItem
import com.tb.moviestools.SeasonItem
import com.tb.moviestools.VideoInfo
import okhttp3.*
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

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
 *_     (((__) (__)))    高山仰止,景行行止.虽不能至,心向往之。
 * author      : xue
 * date        : 2023/8/24 10:07
 * description :
 */
object DataApi {
    const val baseUrl = "https://www.downloader.world/api/"

    @SuppressLint("CheckResult")
    fun requestVideoList(
        page: Int,
        pageSize: Int,
        type: Int,
        callback: CommonCallback<List<VideoInfo>>
    ) {
        okRequestVideoList(page, pageSize, type, callback)
    }

    private fun okRequestVideoList(
        page: Int,
        pageSize: Int,
        type: Int,
        callback: CommonCallback<List<VideoInfo>>
    ) {
        commonRequest(
            "${baseUrl}video/",
            getListParams(page, pageSize, type),
            object : CommonCallback<String> {
                override fun onCall(value: String) {
                    try {
                        val dataObj = JSONObject(value).optJSONObject("data")
                        val dataType = dataObj?.optString("data_type")
                        val jsonArray = dataObj?.optJSONArray("minfo")
                        if (jsonArray == null) {
                            callback.onFailed("json error")
                        } else {
                            val resultList = ArrayList<VideoInfo>()
                            val len = jsonArray.length()
                            for (idx in 0 until len) {
                                val vi = parseVideoDetails(jsonArray.optJSONObject(idx))
                                if (dataType == "1") {
                                    vi.dataType = 0
                                } else if (dataType == "2") {
                                    vi.dataType = 1
                                }
                                resultList.add(vi)
                            }
                            callback.onCall(resultList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailed(e.message ?: "")
                    }
                }

                override fun onFailed(msg: String) {
                    callback.onFailed(msg)
                }
            })
    }


    fun getSeasonInfo(videoId: String, callback: CommonCallback<List<SeasonItem>>) {
        commonRequest(
            "${baseUrl}video_season/",
            getSeasonParams(videoId),
            object : CommonCallback<String> {
                override fun onCall(value: String) {
                    try {
                        val obj = JSONObject(value).optJSONArray("data")
                        if (obj == null || obj.length() == 0) {
                            callback.onFailed("json error")
                        } else {
                            val seasonList = ArrayList<SeasonItem>()
                            for (idx in 0 until obj.length()) {
                                val si = parseSeason(obj.optJSONObject(idx))
                                seasonList.add(si)
                            }
                            callback.onCall(seasonList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailed(e.message ?: "")
                    }
                }

                override fun onFailed(msg: String) {
                    callback.onFailed(msg)
                }
            })
    }


    fun getEpsInfo(
        page: Int,
        pageSize: Int,
        ssnId: String,
        callback: CommonCallback<List<EpsItem>>
    ) {
        commonRequest(
            "${baseUrl}video_ssn/",
            getEpsParams(page, pageSize, ssnId),
            object : CommonCallback<String> {
                override fun onFailed(msg: String) {
                    callback.onFailed(msg)
                }

                override fun onCall(value: String) {
                    try {
                        val epsList =
                            JSONObject(value).optJSONObject("data")?.optJSONArray("eps_list")
                        if (epsList == null) {
                            callback.onFailed("json error")
                        } else {
                            val list = ArrayList<EpsItem>()
                            for (idx in 0 until epsList.length()) {
                                val eps = parseEps(epsList.optJSONObject(idx))
                                list.add(eps)
                            }
                            callback.onCall(list)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailed(e.message ?: "")
                    }
                }
            })
    }


    private fun commonRequest(
        url: String,
        params: Map<String, String>,
        callback: CommonCallback<String>
    ) {
        val formBody = FormBody.Builder()
        for (entry in params.entries) {
            formBody.add(entry.key, entry.value)
        }
        val client = createCommonOkHttp()
        val request = Request.Builder().url(url).post(formBody.build()).build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailed(e.message ?: "")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body()?.string() ?: ""
                    callback.onCall(body)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onFailed(e.message ?: "")
                }
            }
        })
    }

    private fun parseSeason(obj: JSONObject): SeasonItem {
        val item = SeasonItem()
        item.id = obj.optString("id")
        item.title = obj.optString("title")
        return item
    }

    private fun parseEps(obj: JSONObject): EpsItem {
        val eps = EpsItem()
        eps.id = obj.optString("id")
        eps.title = obj.optString("title")
        eps.epsNum = obj.optString("eps_num")
        return eps
    }

    private fun parseVideoDetails(json: JSONObject): VideoInfo {
        val vi = VideoInfo()
        vi.id = json.optString("id")
        vi.name = json.optString("title")
        //1 tv 0 movie
        vi.dataType = if (json.optString("video_flag") == "2") 1 else 0
        return vi
    }

    private fun getSeasonParams(videoId: String): Map<String, String> {
        val params = getDownloaderRequestParams()
        params["tab"] = "1"
        params["id"] = videoId
        return params
    }

    private fun getEpsParams(page: Int, pageSize: Int, ssnId: String): Map<String, String> {
        val params = getDownloaderRequestParams()
        params["page"] = page.toString()
        params["page_size"] = pageSize.toString()
        params["type"] = "2"
        params["ssn_id"] = ssnId
        return params
    }

    private fun getListParams(page: Int, pageSize: Int, type: Int): Map<String, String> {
        val params = getDownloaderRequestParams()
        params["page"] = page.toString()
        params["page_size"] = pageSize.toString()
        params["type"] = type.toString() //"All":100,"Movies":1,"TV Series":2
        params["genre"] = "100" //电影分类，"All Genres":100,....
        params["orderby"] = "1" //排序方式,"Popular":1,"Rated":2,"Release":3
        params["cntyno"] = "100" //国家，"All Countries":100,....
        params["pubdate"] = "100" //更新日期，"All Release Years":100,....
        return params
    }

    private fun getDownloaderRequestParams(): MutableMap<String, String> {
        val params: MutableMap<String, String> = HashMap()
        params["app_id"] = "100"
        params["device_os"] = "android"
        params["lang"] = "en"
        params["device"] = "android"
        params["app_ver"] = "1.0.0"
        params["os_ver"] = "11.1.1"
        params["resolution"] = "800*600"
        params["deviceNo"] = "D5C27BB2-3272-4CA9-869F-771A5DA1DABB"
        params["token"] = "1"
        return params
    }

    fun createRetrofit(host: String): Retrofit {
        return Retrofit.Builder().baseUrl(host)
            .client(createCommonOkHttp())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    fun createCommonOkHttp(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(150000, TimeUnit.MILLISECONDS)
            .readTimeout(150000, TimeUnit.MILLISECONDS)
            .hostnameVerifier(TrustAllHostnameVerifier())
            .proxy(Proxy.NO_PROXY)
        val sf = createSSLSocketFactory()
        if (sf != null) {
            builder.sslSocketFactory(sf)
        }
        return builder.build()
    }

    class TrustAllCerts : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    fun createSSLSocketFactory(): SSLSocketFactory? {
        try {
            val ctx = SSLContext.getInstance("TLS")
            ctx.init(null, arrayOf<TrustManager>(TrustAllCerts()), SecureRandom())
            return ctx.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}