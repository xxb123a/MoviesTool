package com.tb.moviestools

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tb.moviestools.sfapi.TransferVideoApi
import com.tb.moviestools.sfapi.VideoUploadCallback
import com.tb.moviestools.sfapi.VideoUploadManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Locale
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
 *_     (((__) (__)))
 * author      : xue
 * date        : 2023/10/30 14:03
 * description :
 */
class NeedUploadActivity : AppCompatActivity() {
    private val retryBtn by lazy { findViewById<Button>(R.id.btn_retry) }
    private val tvContent by lazy { findViewById<TextView>(R.id.tv_content) }
    private var isListRequesting = false
    private var mDis : Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_need_upload)
        VideoUploadManager.setCallback(object : VideoUploadCallback {
            override fun onItemTaskUpdate(listSize: Int, total: Int) {
                setShowText("任务执行情况: $listSize/$total")
            }

            override fun onAllTaskComplete(total:Int) {
                setShowText("任务全部完成了 数量： $total")
            }

        })
        retryBtn.setOnClickListener {
            if (VideoUploadManager.isRunning || isListRequesting) {
                Toast.makeText(this, "请等上一个任务执行完成之后再点击", Toast.LENGTH_LONG).show()
            }
            start()
        }
        start()
        startLoopCheck()
    }


    private fun start() {
        //如果在运行中就跳出
        if (VideoUploadManager.isRunning || isListRequesting) {
            return
        }
        isListRequesting = true
        setShowText("正在初始化请求 请稍等")
        TransferVideoApi.getList(100) {
            isListRequesting = false
            if(it.isNotEmpty()){
                setShowText("初始化成功\n 需要上传的数量:${it.size}")
                VideoUploadManager.attachData(it)
            }else{
                setShowText("暂时没发现需要上传播放链接的视频")
            }
        }
    }

    private fun setShowText(msg: String) {
        tvContent.text = getAppendText(msg)
    }

    private fun getAppendText(msg: String): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE)
        return "运行中：${sdf.format(System.currentTimeMillis())} \n content: $msg"
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }

    private fun startLoopCheck(){
       mDis = Observable.interval(30,60,TimeUnit.MINUTES)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                AppLog.logE("loop run ----> ${System.currentTimeMillis()}")
                start()
            }
    }

    override fun onDestroy() {
        mDis?.let {
            if(!it.isDisposed){
                it.dispose()
            }
        }
        super.onDestroy()
    }
}