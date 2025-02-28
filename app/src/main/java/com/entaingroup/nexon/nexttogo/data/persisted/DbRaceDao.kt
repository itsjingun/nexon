package com.entaingroup.nexon.nexttogo.data.persisted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface DbRaceDao {
    @Query(
        """
        SELECT * FROM race
        WHERE start_time >= :minStartTime
        ORDER BY start_time, name
        LIMIT :count
        """
    )
    fun getNextRaces(count: Int, minStartTime: Long): Flow<List<DbRace>>

    @Query(
        """
        SELECT * FROM race
        WHERE category_id IN (:categoryIds) AND start_time >= :minStartTime
        ORDER BY start_time, name
        LIMIT :count
        """
    )
    fun getNextRacesByCategoryIds(
        categoryIds: Set<String>,
        count: Int,
        minStartTime: Long,
    ): Flow<List<DbRace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(races: Collection<DbRace>)
}
