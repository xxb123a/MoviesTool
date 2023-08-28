package com.tb.moviestools.act

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tb.moviestools.*
import com.tb.moviestools.db.entity.VideoEntity
import com.tb.moviestools.load.TvManager
import com.tb.moviestools.task.MutilLinkRoadSaveTask
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
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
 * date        : 2023/8/26 14:48
 * description :
 */
class TvBaseActivity : AppCompatActivity() {
    companion object {
        fun launch(activity: Activity) {
            activity.startActivity(Intent(activity, TvBaseActivity::class.java))
        }
    }
    private var linkCreateTask: MutilLinkRoadSaveTask? = null
    private val mShowInfoTv by lazy { findViewById<TextView>(R.id.tv_info) }
    private val mBtn by lazy { findViewById<View>(R.id.tv_button) }
    private val mBtnCreateLink by lazy { findViewById<View>(R.id.tv_crate_link) }
    private val mBtnUploadLink by lazy { findViewById<View>(R.id.tv_upload_link) }
    private val mAllVideos = ArrayList<VideoEntity>()
    private var mDispose: Disposable? = null
    private var mStartDispose: Disposable? = null

    private val tvCallback = object : CommonCallback<List<EpsItem>> {
        override fun onCall(value: List<EpsItem>) {
            runOnUiThread {
                closeDispose(mDispose)
                val txt =
                    "保存成功 count ${value.size} failedCount ${TvManager.getFailedCount()}\n 如果有失败数据 请再次点击获取按钮"
                mShowInfoTv.text = txt
                setAllBtn(true)
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onFailed(msg: String) {
            runOnUiThread {
                closeDispose(mDispose)
                mShowInfoTv.text = "加载出错 $msg"
                mBtn.isEnabled = true
            }
        }
    }

    private val updateLinkProgressTask = object : MutilLinkRoadSaveTask.TaskCallback{
        override fun update(failedCount: Int, successCount: Int, totalCount: Int) {
            mShowInfoTv.text = "link create failed $failedCount suc $successCount total $totalCount"
            if(failedCount + successCount >= totalCount){
                mShowInfoTv.text = "链接创建完成 失败个数 $failedCount 成功个数 $successCount"
                setAllBtn(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_load)
        mBtn.setOnClickListener {
            loadBaseTvs()
        }
        mBtnCreateLink.setOnClickListener {
            if (mAllVideos.isEmpty()) return@setOnClickListener
            setAllBtn(false)
            startCreateLink()
        }
        TvManager.setAttachCallback(tvCallback)
        setAllBtn(false)
        if (TvManager.isLoading()) {
            startDispose()
        } else {
            loadLocalData()
        }
    }


    private fun startCreateLink(){
        linkCreateTask?.stop()
        val task = MutilLinkRoadSaveTask(mAllVideos,5)
        linkCreateTask = task
        task.setCallback(updateLinkProgressTask)
        task.start()
    }


    private fun loadBaseTvs() {
        if (mAllVideos.isEmpty()) {
            setAllBtn(false)
            TvManager.loadTv()
            startDispose()
        } else {
            showReGetDialog {
                setAllBtn(false)
                TvManager.loadTv()
                startDispose()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startDispose() {
        closeDispose(mDispose)
        mDispose = Observable.interval(3, 1, TimeUnit.SECONDS)
            .map {
                arrayOf(
                    TvManager.getLoadDataSize(),
                    TvManager.getTvTotalCount() - TvManager.getCurrentTvCount(),
                    TvManager.getTvTotalCount(),
                    TvManager.getFailedCount()
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it[2] == 0) {
                    mShowInfoTv.text = "正在加载中，请稍等"
                } else {
                    val progress = ((it[1] * 1f / it[2]) * 100).toInt()
                    mShowInfoTv.text =
                        "加载进度 ${progress}% ${it[1]}/${it[2]} -> ${it[0]} \n failedCount : ${it[3]}"
                }
            }
    }

    private fun loadLocalData() {
        mShowInfoTv.text = "请稍等正在加载数据库中的数据"
        closeDispose(mStartDispose)
        mStartDispose = Observable.just(TheApp.dao)
            .map {
                mAllVideos.addAll(it.getAll(1))
                mAllVideos.size
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setAllBtn(true)
                mShowInfoTv.text = "数据库中已经有 $it 条数据"
            }

    }

    private fun setAllBtn(enable: Boolean) {
        mBtn.isEnabled = enable
        mBtnCreateLink.isEnabled = enable
        mBtnUploadLink.isEnabled = enable
    }

    private fun closeDispose(dis: Disposable?) {
        dis?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    override fun onDestroy() {
        closeDispose(mStartDispose)
        closeDispose(mDispose)
        linkCreateTask?.stop()
        TvManager.stop()
        super.onDestroy()
    }

    private fun showReGetDialog(call: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("你需要重新获取基本数据吗")
            .setNegativeButton("取消") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("获取") { dialog, _ ->
                dialog.cancel()
                call.invoke()
            }
            .show()
    }

    private fun showReSaveDialog(call: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("你确定需要重新保存基本信息吗")
            .setNegativeButton("取消") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("保存") { dialog, _ ->
                dialog.cancel()
                call.invoke()
            }
            .show()
    }
}