package com.tb.moviestools.fio

import com.leo618.zip.IZipCallback
import com.tb.moviestools.AppLog
import com.tb.moviestools.TheApp
import com.tb.moviestools.db.entity.VideoEntity
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStream

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
    fun getCacheDir(name: String): String {
        val dir = File(TheApp.instance!!.cacheDir, name)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

    fun findMoviePath(vid: String): String {
        return getCacheDir("movie") + File.separator + vid
    }

    fun findTvPath(tid: String): String {
        return getCacheDir("tv") + File.separator + tid
    }

    fun findRootPathCompat(video: VideoEntity): String {
        val root = if (video.type == 0) findMoviePath(video.did) else findTvPath(video.did)
        val file = File(root)
        if (!file.exists()) {
            file.mkdirs()
        }
        return root
    }

    fun findM3u8Path(video: VideoEntity): String {
        return findRootPathCompat(video) + File.separator + m3u8Name
    }

    fun findDescPath(video: VideoEntity): String {
        return findRootPathCompat(video) + File.separator + descName
    }


    fun saveDesc(link: String, status: Int, path: String) {
        val json = JSONObject()
        json.put("link", link)
        json.put("status", status)
        save2File(json.toString(), path)
    }

    fun save2File(content: String, path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            val os = BufferedWriter(FileWriter(file))
            os.write(content)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun copyFile(targetFile: File, destFile: File,callback: IZipCallback?) {
        copyFile(targetFile,destFile,true,callback)
    }
    private fun copyFile(targetFile: File, destFile: File,isRootDir:Boolean,callback: IZipCallback?) {
        if(isRootDir){
            callback?.onStart()
        }
        if (targetFile.isFile) {
            if(destFile.exists() && destFile.length() == targetFile.length()){
                //说明复制完了了
                return
            }
            isCopy(FileInputStream(targetFile),FileOutputStream(destFile))
        } else {
            if (!destFile.exists()) {
                destFile.mkdirs()
            }
            val files = targetFile.listFiles()
            val len = files?.size ?: 0
            if (files != null && len > 0) {
                var count = 0
                for (file in files) {
                    copyFile(file, File(destFile, file.name),false,callback)
                    if(isRootDir){
                        count++
                        callback?.onProgress(count * 100 / len)
                    }
                }
            }
        }
        if(isRootDir){
            callback?.onFinish(true)
        }
    }

    private fun isCopy(ins:InputStream,os:OutputStream){
        try {
            var readLen: Int
            val readBytes = ByteArray(4086)
            do{
                readLen = ins.read(readBytes)
                if(readLen > 0){
                    os.write(readBytes,0,readLen)
                }
            }while (readLen > 0)

        }catch (e:Exception){
            e.printStackTrace()
            AppLog.logE("copy error ${e.message}")
        }finally {
           try {
               ins.close()
               os.close()
           }catch (e:Exception){
               e.printStackTrace()
           }
        }
    }


     fun deleteFile(file:File){
         if(!file.exists())return
        if(file.isFile){
            file.delete()
        }else{
            val files = file.listFiles()
            if(files == null || files.isEmpty())return
            for (f1 in files) {
                deleteFile(f1)
            }
            file.delete()
        }
    }
}