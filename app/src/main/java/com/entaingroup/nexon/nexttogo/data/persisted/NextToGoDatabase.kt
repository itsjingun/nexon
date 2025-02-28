package com.entaingroup.nexon.nexttogo.data.persisted

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DbRace::class], version = 2 )
internal abstract class NextToGoDatabase : RoomDatabase() {
    abstract fun dbRaceDao(): DbRaceDao
}
