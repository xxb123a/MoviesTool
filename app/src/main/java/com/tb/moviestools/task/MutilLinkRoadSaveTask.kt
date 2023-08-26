package com.tb.moviestools.task

import com.tb.moviestools.db.entity.VideoEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

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
 * date        : 2023/8/26 10:45
 * description :
 */
class MutilLinkRoadSaveTask(val data: MutableList<VideoEntity>, val taskCount: Int) {
    private val taskList = ArrayList<LinkRoadSaveTask>()
    private val totalCount = data.size
    private var callback: TaskCallback? = null
    private var mDis: Disposable? = null
    fun setCallback(callback: TaskCallback) {
        this.callback = callback
    }

    fun start() {
        for (idx in 0 until taskCount) {
            val task = LinkRoadSaveTask(data)
            task.start()
            taskList.add(task)
        }

        mDis = Observable.interval(3, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                update()
            }

    }

    private fun update() {
        var failedCount = 0
        var successCount = 0
        for (linkRoadSaveTask in taskList) {
            failedCount += linkRoadSaveTask.getFailedCount()
            successCount += linkRoadSaveTask.getSuccessCount()
        }
        callback?.update(failedCount, successCount, totalCount)
        if (failedCount + successCount >= totalCount) {
            stop()
        }
    }

    fun stop() {
        for (linkRoadSaveTask in taskList) {
            linkRoadSaveTask.stop()
        }
        mDis?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    interface TaskCallback {
        fun update(failedCount: Int, successCount: Int, totalCount: Int)
    }
}