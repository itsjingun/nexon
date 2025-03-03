package com.entaingroup.nexon.nexttogo.data.persisted

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import java.time.Instant

@Entity(
    tableName = "race",
    // Might only be worth doing if we expect the table to have thousands of rows:
    // indices = [Index("start_time"), Index("category_id")],
)
internal data class DbRace(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "meeting_name") val meetingName: String,
    @ColumnInfo(name = "race_number") val raceNumber: Int,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
)

internal fun DbRace.toRace(): Race = Race(
    id = id,
    meetingName = meetingName,
    raceNumber = raceNumber,
    category = RacingCategory.fromId(categoryId),
    startTime = Instant.ofEpochSecond(startTime),
)

internal fun List<DbRace>.toRaces(): List<Race> = map { it.toRace() }
