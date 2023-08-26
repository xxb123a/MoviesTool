package com.tb.moviestools.load

import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.TheApp
import com.tb.moviestools.VideoInfo
import com.tb.moviestools.api.AllDataHelper
import com.tb.moviestools.db.entity.VideoEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

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
 * date        : 2023/8/25 14:36
 * description :
 */
object MovieManager {

    //电影加载器
    private val mMovieLoader = AllDataHelper(1, 1000, 1)
    private var mMovieAttachCallback : CommonCallback<List<VideoInfo>>? = null
    private val mMovieCallback = object : CommonCallback<List<VideoInfo>> {
        override fun onCall(value: List<VideoInfo>) {
            saveVideo(value)
        }

        override fun onFailed(msg: String) {
            AppLog.logE("movie load failed $msg")
            mMovieAttachCallback?.onFailed(msg)
        }

    }

    fun setMovieCallback(callback: CommonCallback<List<VideoInfo>>?){
        mMovieAttachCallback = callback
    }

    fun getMovieCount():Int{
        return mMovieLoader.getAttachCount()
    }

    fun isLoadingMovie():Boolean{
        return mMovieLoader.isLoading()
    }

    fun loadMovies() {
        mMovieLoader.setCallback(mMovieCallback)
        mMovieLoader.findAllVideo()
    }


    private fun saveVideo(video: List<VideoInfo>) {
        //type = 0 电影 1 tv
        val type = video[0].dataType
        val dis = Observable.just(video)
            .map {
                deleteAllByType(type)
                it
            }
            .map { video2Entity(it) }.map {
                TheApp.dao.addAll(it)
                it.size
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                AppLog.logE("save video success $type count : $it")
                mMovieAttachCallback?.onCall(video)
            }
    }

    private fun deleteAllByType(type: Int) {
        TheApp.dao.deleteByType(type)
    }

    private fun video2Entity(video: List<VideoInfo>): List<VideoEntity> {
        val result = ArrayList<VideoEntity>()
        for (videoInfo in video) {
            val ve = VideoEntity()
            ve.name = videoInfo.name
            ve.did = videoInfo.id
            ve.type = videoInfo.dataType
            result.add(ve)
        }
        return result
    }
}