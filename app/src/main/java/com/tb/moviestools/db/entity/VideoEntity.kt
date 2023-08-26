package com.tb.moviestools.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

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
 * date        : 2023/8/25 13:52
 * description :
 */
@Entity(tableName = "video_entity")
class VideoEntity {
    @PrimaryKey(autoGenerate = true)
    var _id: Int = 0

    @ColumnInfo(name = "_name")
    var name: String = ""

    @ColumnInfo(name = "_vid")
    var did: String = ""

    @ColumnInfo(name = "_type")
    var type: Int = 0

    @Ignore
    var linkTime: Long = 0

    @Ignore
    var link = ""

    fun getDLink(): String {
        //一个小时内有效
        if (link.isNotEmpty() && (System.currentTimeMillis() - linkTime) < 1000 * 60 * 60) {
            return link
        }
        return ""
    }

    fun updateLink(link: String) {
        this.link = link
        linkTime = System.currentTimeMillis()
    }
}