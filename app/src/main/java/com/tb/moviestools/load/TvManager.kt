package com.tb.moviestools.load

import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.EpsItem
import com.tb.moviestools.VideoInfo
import com.tb.moviestools.api.AllDataHelper
import com.tb.moviestools.api.DataApi
import java.util.*

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
 * date        : 2023/8/26 15:00
 * description :
 */
object TvManager {
    //tv 一级目录加载
    private val mMovieLoader = AllDataHelper(1, 1000, 2)

    private var step = 0

    private var mTvMTask: TvSeasonMutliTask? = null

    private val mFirstVideoList = ArrayList<VideoInfo>()

    private var mAttachCallback: CommonCallback<List<EpsItem>>? = null

    private var mCommonCallback = object : CommonCallback<List<EpsItem>>{
        override fun onCall(value: List<EpsItem>) {
            mAttachCallback?.onCall(value)
        }

        override fun onFailed(msg: String) {
        }

    }

    fun setAttachCallback(callback: CommonCallback<List<EpsItem>>?) {
        this.mAttachCallback = callback
    }

    fun getFailedCount():Int{
        return mTvMTask?.getAllFailedCount() ?: 0
    }

    fun isLoading(): Boolean {
        if (step == 0) {
            return mMovieLoader.isLoading()
        }
        return mTvMTask?.isLoading() ?: false
    }

    fun getLoadDataSize(): Int {
        return mTvMTask?.getAllLoadCompleteSize() ?: 0
    }

    fun getCurrentTvCount():Int{
        return mTvMTask?.getCurrentTvCount() ?: 0
    }

    fun getTvTotalCount():Int{
        return mFirstVideoList.size
    }

    fun getLoadData(): List<EpsItem> {
        return mTvMTask?.getAllLoadData() ?: emptyList()
    }

    fun loadTv() {
        if (isLoading()) {
            AppLog.logE("正在加载中")
            return
        }
        if (mFirstVideoList.isEmpty()) {
            loadTvFirst()
        } else {
            loadSeason()
        }
    }

    private fun loadTvFirst() {
        step = 0
        mFirstVideoList.clear()
        mMovieLoader.setCallback(object : CommonCallback<List<VideoInfo>> {
            override fun onCall(value: List<VideoInfo>) {
                mFirstVideoList.addAll(value)
                //继续下一步
                loadSeason()
            }

            override fun onFailed(msg: String) {
                AppLog.logE("tv load failed $msg")
                mAttachCallback?.onFailed(msg)
            }
        })
        mMovieLoader.findAllVideo()
    }

    private fun loadSeason() {
        step++
        val newData = ArrayList(mFirstVideoList)
        val task = TvSeasonMutliTask(
            Collections.synchronizedList(newData),
            DataApi.createCommonOkHttp(),
            20
        )
        mTvMTask = task
        task.setAttachCallback(mCommonCallback)
        task.start()
    }


    fun stop() {
        setAttachCallback(null)
        mTvMTask?.stop()
        mTvMTask = null
    }
}