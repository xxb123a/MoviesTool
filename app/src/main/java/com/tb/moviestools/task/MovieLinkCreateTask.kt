package com.tb.moviestools.task

import com.libsign.libsign.MirozDataSource
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.api.DataApi
import com.tb.moviestools.db.entity.VideoEntity
import com.tb.moviestools.fio.CacheFileTool
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import java.io.File

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
 * date        : 2023/8/26 09:36
 * description :
 */
class MovieLinkCreateTask(val video: VideoEntity, val okhttp: OkHttpClient) {
    private var callback: CommonCallback<String>? = null
    private val m3u8Path by lazy { CacheFileTool.findM3u8Path(video) }
    private val mDescPath by lazy { CacheFileTool.findDescPath(video) }
    private var mDownloadLink = ""
    private var mDis: Disposable? = null
    private val mLinkCallback = object : CommonCallback<String> {
        override fun onCall(value: String) {
            if(mDownloadLink.isEmpty()){
                video.updateLink(value)
            }
            mDownloadLink = value
            if (value.isNotEmpty()) {
                //进行下一步
                startSaveTask()
            } else {
                callback?.onFailed("link get failed")
            }
        }

        override fun onFailed(msg: String) {
            callback?.onFailed(msg)
        }
    }

    fun setCallback(callback: CommonCallback<String>) {
        this.callback = callback
    }

    fun start() {
        val dis = Observable.just("")
            .map { checkFile(m3u8Path) && checkFile(mDescPath) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    callback?.onCall("")
                } else {
                    val link = video.getDLink()
                    if (link.isNotEmpty()) {
                        mDownloadLink = link
                        mLinkCallback.onCall(link)
                    } else {
                        if (video.type == 0) {
                            MirozDataSource.requestVideoLink(video.did, mLinkCallback)
                        } else {
                            MirozDataSource.requestTvLink(video.did, mLinkCallback)
                        }
                    }
                }
            }


    }

    private fun startSaveTask() {
        closeDispose(mDis)
        mDis = Observable.just("")
            .map { DataApi.getUrlContent(mDownloadLink, okhttp) }
            .map {
                if (it.trim().startsWith("#")) {
                    CacheFileTool.save2File(it, m3u8Path)
                    CacheFileTool.saveDesc(mDownloadLink, 0, mDescPath)
                    true
                } else {
                    false
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    callback?.onCall("")
                } else {
                    callback?.onFailed("saveFile failed")
                }
            }
    }

    private fun checkFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.length() > 0
    }

    private fun closeDispose(dis: Disposable?) {
        dis?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }
}