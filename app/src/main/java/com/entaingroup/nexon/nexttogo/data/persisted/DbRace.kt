package com.entaingroup.nexon.nexttogo.data.persisted

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.entaingroup.nexon.nexttogo.domain.Race
import java.time.Instant

@Entity(
    tableName = "race",
    // Might only be worth doing if we expect the table to have thousands of rows:
    // indices = [Index("start_time"), Index("category_id")],
)
internal data class DbRace(
    @PrimaryKey val id: String,
    val name: String,
    val number: Int,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
)

internal fun DbRace.toRace(): Race = Race(
    id = id,
    name = name,
    number = number,
    startTime = Instant.ofEpochSecond(startTime),
)
