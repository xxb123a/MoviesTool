package com.tb.moviestools.api

import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.VideoInfo

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
 * date        : 2023/8/24 14:17
 * description :
 */
class AllDataHelper(val page: Int, val pageSize: Int,val type: Int) : CommonCallback<List<VideoInfo>>{
    private var callback: CommonCallback<List<VideoInfo>>? = null
    private val mCacheList = ArrayList<VideoInfo>();
    private var mAttachCount = 0
    private var isLoading = false
    fun setCallback(callback: CommonCallback<List<VideoInfo>>) {
        this.callback = callback
    }

    fun isLoading():Boolean{
        return isLoading
    }

    fun findAllVideo() {
        if(isLoading){
            AppLog.logE("正在loading ......")
            return
        }
        isLoading = true
        mCacheList.clear()
        realRequest()
    }

    private fun realRequest(){
        DataApi.requestVideoList(page + mAttachCount,pageSize,type,this)
    }

    private fun requestNext(){
        mAttachCount += 1
        realRequest()
    }

    fun getAttachCount() = mAttachCount

    override fun onCall(value: List<VideoInfo>) {
        AppLog.logE("$mAttachCount success ${value.size}")
        isLoading = false
        if(value.isEmpty()){
            callback?.onCall(mCacheList)
        }else{
            mCacheList.addAll(value)
            requestNext()
        }
    }

    override fun onFailed(msg: String) {
        AppLog.logE(" all video page $mAttachCount $msg")
        isLoading = false
    }
}