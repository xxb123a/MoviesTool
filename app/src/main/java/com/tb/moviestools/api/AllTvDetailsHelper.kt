package com.tb.moviestools.api

import com.tb.moviestools.*

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
 * date        : 2023/8/24 16:42
 * description :
 */
class AllTvDetailsHelper(val page:Int,val pageSize:Int,val video: VideoInfo):CommonCallback<List<EpsItem>> {
    private var callback: CommonCallback<List<EpsItem>>? = null
    private var mAttachCount = 0
    private var mSeasonIndex = 0
    private val mSeasonList = ArrayList<SeasonItem>()
    private val mAllEpsItem = ArrayList<EpsItem>()
    private var isLoading = false
    private var mTmpSsnId = ""

    fun setCallback(callback:CommonCallback<List<EpsItem>>){
        this.callback = callback
    }

    private fun findSeason(){
        DataApi.getSeasonInfo(video.id,object : CommonCallback<List<SeasonItem>>{
            override fun onCall(value: List<SeasonItem>) {
                if(value.isEmpty()){
                    callback?.onFailed("SeasonItem get failed")
                    return
                }
                mSeasonList.clear()
                mSeasonList.addAll(value)
                AppLog.logE("season success ${value.size}")
                requestNextSeason()
            }

            override fun onFailed(msg: String) {
                AppLog.logE("season failed $msg")
                callback?.onFailed("season failed $msg")
            }
        })
    }

    private fun requestNextSeason(){
        if(mSeasonList.size == 0){
            video.tvAllEps = mAllEpsItem
            callback?.onCall(mAllEpsItem)
        }else{
            val seasonItem =  mSeasonList.removeFirst()
            mAttachCount = 0
            findEps(seasonItem.id)
            this.mTmpSsnId = seasonItem.id
            mSeasonIndex ++
        }
    }

    private fun requestNextEps(){
        mAttachCount++
        findEps(mTmpSsnId)
    }

    private fun findEps(ssnId:String){
        DataApi.getEpsInfo(page + mAttachCount,pageSize,ssnId,this)
    }

    fun find() {
        if(isLoading){
            AppLog.logE("正在loading .....")
            return
        }
        findSeason()
    }

    override fun onCall(value: List<EpsItem>) {
        AppLog.logE("eps success $mSeasonIndex ${value.size}")
        mAllEpsItem.addAll(value)
        if(value.size == pageSize){
            requestNextEps()
        }else{
            //这一个的Eps加载完了 加载下一个
            requestNextSeason()
        }
    }

    override fun onFailed(msg: String) {
        AppLog.logE("eps failed $mSeasonIndex $msg")
        callback?.onFailed("eps failed $mSeasonIndex $msg")
    }
}
