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
import com.tb.moviestools.load.MovieManager
import com.tb.moviestools.task.MutilLinkRoadSaveTask
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Collections
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
 * date        : 2023/8/25 14:15
 * description :
 */
class MovieBaseActivity : AppCompatActivity() {

    companion object {
        fun launch(activity: Activity) {
            activity.startActivity(Intent(activity, MovieBaseActivity::class.java))
        }
    }

    private val movieCallback = object : CommonCallback<List<VideoInfo>> {
        @SuppressLint("SetTextI18n")
        override fun onCall(value: List<VideoInfo>) {
            closeDispose(mDispose)
            mShowInfoTv.text = "加载成功 count ${value.size}"
            loadDataFormDb()

        }

        @SuppressLint("SetTextI18n")
        override fun onFailed(msg: String) {
            closeDispose(mDispose)
            mShowInfoTv.text = "加载出错 $msg"
            mBtn.isEnabled = true
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
    private var linkCreateTask: MutilLinkRoadSaveTask? = null
    private var mDispose: Disposable? = null
    private var mStartDispose: Disposable? = null
    private val mShowInfoTv by lazy { findViewById<TextView>(R.id.tv_info) }
    private val mBtn by lazy { findViewById<View>(R.id.tv_button) }
    private val mAllVideos = ArrayList<VideoEntity>()
    private val mBtnCreateLink by lazy { findViewById<View>(R.id.tv_crate_link) }

    private val mBtnUploadLink by lazy { findViewById<View>(R.id.tv_upload_link) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_movie_load)
        MovieManager.setMovieCallback(movieCallback)
        mBtn.setOnClickListener {
            loadBaseMovies()
        }
        mBtnCreateLink.setOnClickListener {
            if(mAllVideos.isEmpty())return@setOnClickListener
            setAllBtn(false)
            startCreateLink()
        }

        setAllBtn(false)
        if (MovieManager.isLoadingMovie()) {
            //正在加载中
            startDispose()
        } else {
            loadLocalData()
        }
    }


    private fun loadBaseMovies(){
        if(mAllVideos.isEmpty()){
            setAllBtn(false)
            MovieManager.loadMovies()
            startDispose()
        }else{
            showReGetDialog {
                setAllBtn(false)
                MovieManager.loadMovies()
                startDispose()
            }
        }
    }

    private fun setAllBtn(enable:Boolean){
        mBtn.isEnabled = enable
        mBtnCreateLink.isEnabled = enable
        mBtnUploadLink.isEnabled = enable
    }

    private fun startCreateLink(){
        linkCreateTask?.stop()
        val task = MutilLinkRoadSaveTask(mAllVideos,1)
        linkCreateTask = task
        task.setCallback(updateLinkProgressTask)
        task.start()
    }

    private fun loadLocalData() {
        mShowInfoTv.text = "请稍等正在加载数据库中的数据"
        closeDispose(mStartDispose)
        mStartDispose = Observable.just(TheApp.dao)
            .map {
                mAllVideos.addAll(it.getAll(0))
                mAllVideos.size
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setAllBtn(true)
                mShowInfoTv.text = "数据库中已经有 $it 条数据"
            }

    }

    private fun loadDataFormDb(){
        mAllVideos.clear()
        val dis = Observable.just(TheApp.dao)
            .map {
                mAllVideos.addAll(it.getAll(0))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setAllBtn(true)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun startDispose() {
        closeDispose(mDispose)
        mDispose = Observable.interval(3, 1, TimeUnit.SECONDS)
            .map { MovieManager.getMovieCount() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mShowInfoTv.text = "正在加载中。。。 加载个数 ${it * 1000}"
            }
    }


    private fun closeDispose(dis: Disposable?) {
        dis?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    private fun showReGetDialog(call:()->Unit){
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

    override fun onDestroy() {
        MovieManager.setMovieCallback(null)
        linkCreateTask?.stop()
        closeDispose(mStartDispose)
        closeDispose(mDispose)
        super.onDestroy()
    }
}