package com.tb.moviestools

import android.app.Application
import android.content.pm.PackageInfo
import androidx.room.Room
import com.tb.moviestools.db.AppDataBase
import com.tb.moviestools.db.mig.Mig_1_2
import com.tb.moviestools.load.TvManager

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
 * date        : 2023/8/25 09:06
 * description :
 */
class TheApp : Application() {
    companion object{
        val db by lazy { Room.databaseBuilder(instance!!,AppDataBase::class.java,"cache.db")
            .addMigrations(Mig_1_2())
            .allowMainThreadQueries()
            .build() }
        val dao by lazy { db.videoDao() }

        var instance:TheApp? = null

        fun getPackageInfo(): PackageInfo? {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = instance!!.packageManager
                    .getPackageInfo(instance!!.packageName, 0)
            } catch (e: Exception) {
            }
            return packageInfo
        }
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        TvManager.initLoadCacheTvInfo()
    }
}