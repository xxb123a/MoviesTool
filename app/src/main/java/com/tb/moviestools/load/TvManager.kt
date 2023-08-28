package com.tb.moviestools.load

import com.tb.moviestools.*
import com.tb.moviestools.api.AllDataHelper
import com.tb.moviestools.api.DataApi
import com.tb.moviestools.db.entity.TvBaseEntity
import com.tb.moviestools.db.entity.VideoEntity
import java.util.*
import kotlin.collections.ArrayList

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

    private val mAReadyCacheTv = Collections.synchronizedList(ArrayList<String>())

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

    //初始化已经缓存好的tv基础数据
    fun initLoadCacheTvInfo(){
        Thread{
            val dao = TheApp.db.tvBaseDao()
            for (tvBaseEntity in dao.getAll()) {
                mAReadyCacheTv.add(tvBaseEntity.tid)
            }
            AppLog.logE("ready tv loadComplete")
        }.start()
    }

    fun addCacheTvInfo(info:VideoInfo){
        val allEps = info.tvAllEps ?: return
        if(allEps.isEmpty())return
        mAReadyCacheTv.add(info.id)
        val te = TvBaseEntity()
        te.name = info.name
        te.tid = info.id
        TheApp.db.tvBaseDao().add(te)
        TheApp.dao.addAll(epsList2VideoList(allEps))
        AppLog.logE("eps save success ${te.tid} -> ${allEps.size}")
    }

    fun isCacheTvInfo(tid:String):Boolean{
        return mAReadyCacheTv.contains(tid)
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

    private fun epsList2VideoList(eps:List<EpsItem>):List<VideoEntity>{
        val result = ArrayList<VideoEntity>()
        for (ep in eps) {
            result.add(eps2VideoEntity(ep))
        }
        return result
    }

    private fun eps2VideoEntity(eps:EpsItem): VideoEntity {
        val ve = VideoEntity()
        ve.name = eps.title
        ve.did = eps.id
        //1代表Tv
        ve.type = 1
        return ve
    }

}