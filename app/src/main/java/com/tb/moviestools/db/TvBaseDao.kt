package com.tb.moviestools.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tb.moviestools.db.entity.TvBaseEntity

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
 * date        : 2023/8/28 14:06
 * description :
 */
@Dao
interface TvBaseDao {

    @Query("select * from tv_entity")
    fun getAll(): List<TvBaseEntity>

    @Insert
    fun add(vararg entity: TvBaseEntity)
}