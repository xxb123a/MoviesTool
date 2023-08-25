package com.libsign.libsign

import android.text.TextUtils
import com.libsign.libsign.service.MirozApi
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.api.DataApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject

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
 * date        : 2023/8/25 10:46
 * description :
 */
object MirozDataSource {
    private var serverHost = "https://server.tunever.top"

    private fun requestInitData(callback: CommonCallback<String>) {
        if (serverHost.isNotEmpty()) {
            callback.onCall(serverHost)
            return
        }
        val api: MirozApi =
            DataApi.createRetrofit("http://www.metatribox.work").create(MirozApi::class.java)
        val dis = api.findInitParams(MirozApiParams.getInitParams())
            .map { findServerHost(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ s: String ->
                serverHost = s
                callback.onCall(s)
            }) { throwable: Throwable ->
                callback.onFailed(throwable.message ?: "")
            }
    }

    fun requestVideoLink(vid: String, callback: CommonCallback<String>) {
        requestInitData(object : CommonCallback<String> {
            override fun onCall(value: String) {
                requestVideoDetails(vid, callback)
            }

            override fun onFailed(msg: String) {
                callback.onFailed(msg)
            }
        })
    }

    private fun requestVideoDetails(vid: String, callback: CommonCallback<String>) {
        val params = MirozApiParams.map2MultiMapBody(MirozApiParams.getMediaDetailsParams(vid))
        val api = DataApi.createRetrofit(serverHost).create(MirozApi::class.java)
        val dis = api.findVideoDetails(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s: String ->
                val link = parseVideoUrl(s)
                if (link.isEmpty()) {
                    callback.onFailed("json error")
                } else {
                    callback.onCall(link)
                }
            }) { throwable: Throwable ->
                callback.onFailed(
                    throwable.message!!
                )
            }
    }

    private fun requestTvLink(ssnId:String, callback: CommonCallback<String>){
        val params = MirozApiParams.map2MultiMapBody(MirozApiParams.getTTLinkParams(ssnId))
        val api = DataApi.createRetrofit(serverHost).create(MirozApi::class.java)
        val dis = api.findTTLink(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s: String ->
                val link = parseVideoUrl(s)
                if (link.isEmpty()) {
                    callback.onFailed("json error")
                } else {
                    callback.onCall(link)
                }
            }) { throwable: Throwable ->
                callback.onFailed(
                    throwable.message ?: ""
                )
            }
    }

    private fun parseVideoUrl(body: String): String {
        try {
            val dataJson = JSONObject(body).optJSONObject("data")
            var cfLink = dataJson?.optString("cflink") ?: ""
            if (cfLink.isEmpty()) {
                val playLink = dataJson?.optJSONObject("hd")?.optString("link") ?: ""
                cfLink = playLink
            }
            return cfLink
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun findServerHost(content: String): String {
        try {
            return JSONObject(content).getJSONObject("data").getJSONObject("tab1")
                .getJSONArray("data").getJSONObject(0).getString("api1")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "https://server.tunever.top"
    }
}