package com.tb.moviestools.load

import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.EpsItem
import com.tb.moviestools.VideoInfo
import com.tb.moviestools.api.AllTvDetailsHelper
import okhttp3.OkHttpClient

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
 * date        : 2023/8/26 15:21
 * description :
 */
class TvSeasonMutliTask(
    val data: MutableList<VideoInfo>,
    val okhttp: OkHttpClient,
    val maxCount: Int
) : CommonCallback<List<EpsItem>> {

    private val mCacheTask = ArrayList<SeasonSingleTask>()
    private var mAttachCallback: CommonCallback<List<EpsItem>>? = null

    fun setAttachCallback(callback: CommonCallback<List<EpsItem>>) {
        this.mAttachCallback = callback
    }

    fun getAllLoadCompleteSize(): Int {
        var count = 0
        for (seasonSingleTask in mCacheTask) {
            count += seasonSingleTask.getData().size
        }
        return 0
    }

    fun getAllFailedCount(): Int {
        var count = 0
        for (seasonSingleTask in mCacheTask) {
            count += seasonSingleTask.getFailedCount()
        }
        return count
    }

    fun getAllLoadData(): List<EpsItem> {
        val result = ArrayList<EpsItem>()
        for (seasonSingleTask in mCacheTask) {
            result.addAll(seasonSingleTask.getData())
        }
        return result
    }

    fun isLoading(): Boolean {
        for (seasonSingleTask in mCacheTask) {
            if (seasonSingleTask.isLoading()) {
                return true
            }
        }
        return false
    }

    fun start() {
        if (isLoading()) {
            return
        }
        for (idx in 0 until maxCount) {
            val task = SeasonSingleTask(data, okhttp)
            mCacheTask.add(task)
            task.setAttachCallback(this)
            task.start()
        }
    }

    fun stop() {
        for (seasonSingleTask in mCacheTask) {
            seasonSingleTask.stop()
        }
        mCacheTask.clear()
    }

    override fun onCall(value: List<EpsItem>) {
        if (!isLoading()) {
            //加载完成
            mAttachCallback?.onCall(getAllLoadData())
        }
    }

    override fun onFailed(msg: String) {

    }


    class SeasonSingleTask(val _data: MutableList<VideoInfo>, val okhttp: OkHttpClient) {
        private var isStop = false
        private val mCacheList = ArrayList<EpsItem>()
        private var attachCallback: CommonCallback<List<EpsItem>>? = null
        private var mFailedCount = 0
        private val commonCallback = object : CommonCallback<List<EpsItem>> {
            override fun onCall(value: List<EpsItem>) {
                mCacheList.addAll(value)
                next()
            }

            override fun onFailed(msg: String) {
                AppLog.logE("tv Season failed $msg")
                mFailedCount++
                next()
            }
        }

        fun getFailedCount() = mFailedCount

        fun setAttachCallback(callback: CommonCallback<List<EpsItem>>) {
            this.attachCallback = callback
        }

        fun getData(): List<EpsItem> {
            return mCacheList
        }

        fun isLoading(): Boolean {
            return !isStop
        }

        fun start() {
            mCacheList.clear()
            next()
        }

        private fun next() {
            if (_data.isEmpty()) {
                isStop = true
                attachCallback?.onCall(mCacheList)
                return
            }
            val _data = _data.removeFirst()
            val list = _data.tvAllEps
            if (list != null && list.isNotEmpty()) {
                commonCallback.onCall(list)
                return
            }
            val helper = AllTvDetailsHelper(1, 20, _data)
            helper.setCallback(commonCallback)
            helper.find()
        }

        fun stop() {
            isStop = true
        }
    }
}