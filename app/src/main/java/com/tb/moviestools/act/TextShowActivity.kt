package com.tb.moviestools.act

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tb.moviestools.R
import com.tb.moviestools.api.DataApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
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
 * date        : 2023/8/26 13:51
 * description :
 */
class TextShowActivity : AppCompatActivity() {

    companion object{
        fun launch(activity:Activity,path:String){
            activity.startActivity(Intent(activity,TextShowActivity::class.java).putExtra("path",path))
        }
    }

    private val mText by lazy { findViewById<TextView>(R.id.tv_text) }
    private val path by lazy { intent.getStringExtra("path") ?: "" }
    private var dis:Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_text)
        mText.text = "正在加载中...请稍等"
        dis = Observable.just(path)
            .map { DataApi.readStringByFile(it)  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { mText.text = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        dis?.let { if(!it.isDisposed){
            it.dispose()
        } }
    }
}