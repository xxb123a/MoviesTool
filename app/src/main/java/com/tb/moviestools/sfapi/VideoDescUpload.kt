package com.tb.moviestools.sfapi

import com.libsign.libsign.MirozDataSource
import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Request

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
 * date        : 2023/10/30 14:49
 * description :
 */

object VideoUploadManager {
    var isRunning = false

    private var mTotal = 0
    private var mDataList = ArrayList<VideoDescInfo>()

    private var mCallback: VideoUploadCallback? = null
    private val mItemTaskCallback = object : CommonCallback<String> {
        override fun onCall(value: String) {
            onNextTaskStart()
        }

        override fun onFailed(msg: String) {
            onNextTaskStart()
        }

    }

    fun setCallback(callback: VideoUploadCallback) {
        this.mCallback = callback
    }

    fun attachData(dataList: List<VideoDescInfo>) {
        if (isRunning) return
        isRunning = true
        mDataList.addAll(dataList)
        mTotal = dataList.size
        onNextTaskStart()
    }

    private fun onNextTaskStart() {
        if (mDataList.size > 0) {
            val data = mDataList.removeFirst()
            VideoUploadTask(data, mItemTaskCallback).start()
            mCallback?.onItemTaskUpdate(mDataList.size, mTotal)
        } else {
            isRunning = false
            //完成
            mCallback?.onAllTaskComplete(mTotal)
        }
    }
}

interface VideoUploadCallback {
    fun onItemTaskUpdate(listSize: Int, total: Int)
    fun onAllTaskComplete(total: Int)

}

class VideoUploadTask(
    private val data: VideoDescInfo,
    private val callback: CommonCallback<String>
) {
    //url 获取回调
    private val urlCallback = object : CommonCallback<String> {
        override fun onCall(value: String) {
            uploadM3u8(value)
        }

        override fun onFailed(msg: String) {
            onItemFailed(msg)
        }
    }

    fun start() {
        //0 tv 1 movies
        if (data.type == 0) {
            MirozDataSource.requestTvLink(data.id, urlCallback)
        } else {
            MirozDataSource.requestVideoLink(data.id, urlCallback)
        }
    }

    private fun onItemFailed(msg: String) {
        AppLog.logE("link fetch failed ${data.id} type: ${data.type}")
        callback.onFailed(msg)
    }

    private fun onItemSuccess(url: String) {
        callback.onCall(url)
    }

    private fun uploadM3u8(url: String) {
        val dis = Single.fromCallable { createM3u8(url) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onItemSuccess("")
            }, {
                onItemFailed(it.message ?: "")
            })
    }

    private fun createM3u8(url: String): Boolean {
        val content = getHttpGetContent(url)
        if (content.trim().startsWith("#")) {
            //1 movie 0 tv
            val flag = TransferVideoApi.crateM3u8(url, data.id, data.type == 1, content)
            AppLog.logE("m3u8 upload flag $flag id : ${data.id} type:${data.type}")
            return flag
        } else {
            AppLog.logE("m3u8 获取内容错误 id : ${data.id} type:${data.type}")
            AppLog.logE("m3u8 url : $url")
            AppLog.logE("m3u8 content pre 200 ${ if(content.length > 200) content.subSequence(0, 200) else content}")
        }
        return false
    }

    private fun getHttpGetContent(url: String): String {
        val okhttp = TransferVideoApi.createCommonOkhttp()
        val request = Request.Builder().url(url).build()
        try {
            val response = okhttp.newCall(request).execute()
            if (response.isSuccessful) {
                return response.body()?.string() ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
