package com.tb.moviestools.task

import com.tb.moviestools.AppLog
import com.tb.moviestools.CommonCallback
import com.tb.moviestools.api.DataApi
import com.tb.moviestools.db.entity.VideoEntity

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
 * date        : 2023/8/26 10:35
 * description :
 */
class LinkRoadSaveTask(val data: MutableList<VideoEntity>) {
    private var failedCount = 0
    private var successCount = 0
    private var isStop = false
    private val okhttp = DataApi.createCommonOkHttp()
    private val taskCallback = object : CommonCallback<String> {
        override fun onCall(value: String) {
            successCount++
            next()
        }

        override fun onFailed(msg: String) {
            AppLog.logE("failed : $msg")
            failedCount++
            next()
        }
    }

    fun start() {
        next()
    }

    fun getFailedCount() = failedCount

    fun getSuccessCount() = successCount

    private fun next() {
        if (isStop || data.isEmpty()) return
        val ve = data.removeFirst()
        val task = MovieLinkCreateTask(ve, okhttp)
        task.setCallback(taskCallback)
        task.start()
    }

    fun stop(){
        isStop = true
    }
}