package com.tb.moviestools.fio

import com.tb.moviestools.TheApp
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
 * date        : 2023/8/25 17:41
 * description :
 */
object CacheFileTool {
    //link:xxx,status:0,
    private const val descName = "desc.txt"
    private const val m3u8Name = "index.m3u8"
    fun getMovieCacheDir(): String {
        val dir = File(TheApp.instance!!.cacheDir, "movie")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

    fun findMoviePath(vid: String): String {
        return getMovieCacheDir() + File.separator + vid
    }
}