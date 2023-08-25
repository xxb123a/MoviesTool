package com.tb.moviestools.act

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tb.moviestools.*
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
        }

        @SuppressLint("SetTextI18n")
        override fun onFailed(msg: String) {
            closeDispose(mDispose)
            mShowInfoTv.text = "加载出错 $msg"
            mBtn.isEnabled = true
        }
    }
    private var mDispose: Disposable? = null
    private var mStartDispose: Disposable? = null
    private val mShowInfoTv by lazy { findViewById<TextView>(R.id.tv_info) }
    private val mBtn by lazy { findViewById<View>(R.id.tv_button) }

    private val mBtnCreateLink by lazy { findViewById<View>(R.id.tv_crate_link) }

    private val mBtnUploadLink by lazy { findViewById<View>(R.id.tv_upload_link) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_load)
        MovieManager.setMovieCallback(movieCallback)
        mBtn.setOnClickListener {
            it.isEnabled = false
            MovieManager.loadMovies()
            startDispose()
        }
        mBtn.isEnabled = false
        mBtnCreateLink.isEnabled = false
        mBtnUploadLink.isEnabled = false
        if(MovieManager.isLoadingMovie()){
            //正在加载中
            startDispose()
        }else{
            loadLocalData()
        }
    }

    private fun loadLocalData() {
        mShowInfoTv.text = "请稍等正在加载数据库中的数据"
        closeDispose(mStartDispose)
        mStartDispose = Observable.just(TheApp.dao)
            .map {
                it.getAll(0)
            }
            .map {
                it.size
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mBtn.isEnabled = true
                mShowInfoTv.text = "数据库中已经有 $it 条数据"
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

    override fun onDestroy() {
        MovieManager.setMovieCallback(null)
        closeDispose(mStartDispose)
        closeDispose(mDispose)
        super.onDestroy()
    }
}