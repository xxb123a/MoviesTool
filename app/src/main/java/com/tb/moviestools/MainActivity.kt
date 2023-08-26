package com.tb.moviestools

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tb.moviestools.act.FileDirBrowserActivity
import com.tb.moviestools.act.MovieBaseActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_movie).setOnClickListener {
            MovieBaseActivity.launch(this)
        }
        findViewById<View>(R.id.btn_browser).setOnClickListener {
            FileDirBrowserActivity.launch(this, cacheDir.absolutePath)
        }
    }
}