package com.tb.moviestools

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.leo618.zip.IZipCallback
import com.leo618.zip.ZipManager
import com.tb.moviestools.act.FileDirBrowserActivity
import com.tb.moviestools.act.MovieBaseActivity
import com.tb.moviestools.act.TvBaseActivity
import com.tb.moviestools.fio.CacheFileTool
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class MainActivity : AppCompatActivity() {
    private val mTvInfo by lazy { findViewById<TextView>(R.id.tv_info) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_movie).setOnClickListener {
            MovieBaseActivity.launch(this)
        }
        findViewById<View>(R.id.btn_tv).setOnClickListener {
            TvBaseActivity.launch(this)
        }
        findViewById<View>(R.id.btn_browser).setOnClickListener {
            FileDirBrowserActivity.launch(this, cacheDir.absolutePath)
        }
        findViewById<View>(R.id.btn_zip_movie).setOnClickListener {
           zipMovie()
        }
        findViewById<View>(R.id.btn_zip_tv1).setOnClickListener {
            testZip()
        }
        findViewById<View>(R.id.btn_delete_tv).setOnClickListener {
            testMerge()
        }
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),11)



    }
    private var lastUpdate = 0L
    private fun updateTvInfo(info:String,isForce:Boolean = false){
        val time = System.currentTimeMillis()
        if(!isForce && time - lastUpdate < 1000){
            return
        }
        lastUpdate = time
        runOnUiThread {
            mTvInfo.text = info
        }
    }

    private fun copy(){
        //
        Thread{
            val targetFile = File(CacheFileTool.getCacheDir("tv"))
            val destFile1 = File(Environment.getExternalStorageDirectory(),"tv_1")
            val destFile2 = File(Environment.getExternalStorageDirectory(),"tv_2")

            val files = targetFile.listFiles() ?: return@Thread
            var count = 0
            //一个文件夹最多10万
            val maxCount = 100000
            updateTvInfo("正在复制请稍等...")
            val len = files.size
            for (file in files) {
                count ++
                val destFileRoot = if(count > maxCount) destFile2 else destFile1
                CacheFileTool.copyFile(file, File(destFileRoot,file.name),null)
                updateTvInfo("复制进度... ${count * 100 / len}%")
            }
            updateTvInfo("恭喜你复制完成了哟...",true)
        }.start()
    }

    private fun zipMovie(){
        Thread{
            val zipFile = File(Environment.getExternalStorageDirectory(),"movie_${System.currentTimeMillis()}.zip")
            zipPath(CacheFileTool.getCacheDir("movie"),zipFile.absolutePath)
        }.start()
    }

    private fun zipTv(name:String){
        Thread{
            val destFile = File(Environment.getExternalStorageDirectory(),name)
            val zipFile = File(Environment.getExternalStorageDirectory(),"${name}_${System.currentTimeMillis()}.zip")
            zipPath(destFile.absolutePath,zipFile.absolutePath)
        }.start()
    }
    private fun zipPath(targetPath:String,destPath:String){
        updateTvInfo("请稍等，正在准备中")
        ZipManager.zip(targetPath,destPath,object : IZipCallback{
            override fun onStart() {
                updateTvInfo("开始压缩",true)
            }

            override fun onProgress(percentDone: Int) {
                updateTvInfo("压缩进度 $percentDone %")
            }

            override fun onFinish(success: Boolean) {
                updateTvInfo("压缩结果 -> $success",true)
            }

        })
    }

    private fun testZip(){
        val dis = Observable.zip(createObs("1"),createObs("2")){v1,v2-> "$v1$v2"}
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("zip result  $it")
            },{
                println("error ${it.message}")
            })
    }

    private fun testMerge(){
        val dis = Observable.merge(createObs("1"),createObs("2"))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("merge result  $it")
            },{
                println("error ${it.message}")
            })
    }

    private fun createObs(str:String) = Observable.create {
        println("$str -> obs run")
        for (idx in 0 until 3){
            it.onNext("$str idx : $idx")
            println("$str idx : $idx")
            SystemClock.sleep(1000)
        }
        it.onComplete()
    }.subscribeOn(Schedulers.newThread())
}