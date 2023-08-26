package com.tb.moviestools.act

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tb.moviestools.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

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
 * date        : 2023/8/26 13:59
 * description :
 */
class FileDirBrowserActivity : AppCompatActivity() {
    companion object {
        fun launch(activity: Activity, path: String) {
            activity.startActivity(
                Intent(
                    activity,
                    FileDirBrowserActivity::class.java
                ).putExtra("path", path)
            )
        }
    }

    private val path by lazy { intent.getStringExtra("path") ?: "" }
    private val mTv by lazy { findViewById<TextView>(R.id.tv_center) }
    private val mRv by lazy { findViewById<RecyclerView>(R.id.rv_content) }
    private var dis: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_file_browser)
        mTv.text = "正在加载中请稍等"

        dis = Observable.just(path)
            .map { File(path) }
            .map { it.listFiles() }
            .map { convertEntity(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mTv.isVisible = false
                mRv.layoutManager = LinearLayoutManager(this)
                mRv.adapter = FileAdapter(it)
            }
    }

    private fun convertEntity(files: Array<File>?): List<FileEntity> {
        if (files == null) return emptyList()
        val list = arrayListOf<FileEntity>()
        files.forEach { file ->
            list.add(FileEntity(file.absolutePath, file.isFile, file.name))
        }
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        dis?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    class FileViewHolder(view: View) : ViewHolder(view) {
        fun content(): TextView {
            return itemView as TextView
        }
    }

    inner class FileAdapter(val datas: List<FileEntity>) : RecyclerView.Adapter<FileViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
            return FileViewHolder(view)
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            val data = datas[position]
            if (!data.isFile) {
                holder.content().text = "(文件夹)" + data.name
            } else {
                holder.content().text = data.name
            }
            holder.itemView.setOnClickListener {
                if (data.isFile) {
                    TextShowActivity.launch(this@FileDirBrowserActivity, data.path)
                } else {
                    launch(this@FileDirBrowserActivity, data.path)
                }
            }
        }
    }

    data class FileEntity(val path: String, val isFile: Boolean, val name: String)
}
