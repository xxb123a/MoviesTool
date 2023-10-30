package com.tb.moviestools.db.mig

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
 * date        : 2023/8/28 14:11
 * description :
 */
class Mig_1_2 : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `tv_entity` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `_name` TEXT NOT NULL, `_tid` TEXT NOT NULL)")
    }
}