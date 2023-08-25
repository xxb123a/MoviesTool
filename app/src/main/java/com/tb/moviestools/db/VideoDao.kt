package com.tb.moviestools.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tb.moviestools.db.entity.VideoEntity

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
 * date        : 2023/8/25 13:51
 * description :
 */
@Dao
interface VideoDao {
    @Query("select * from video_entity")
    fun getAll():List<VideoEntity>
    @Query("select * from video_entity where _type=:type")
    fun getAll(type:Int):List<VideoEntity>
    @Insert
    fun addAll(data:List<VideoEntity>)

    @Query("delete from video_entity where _type=:type")
    fun deleteByType(type:Int)
}