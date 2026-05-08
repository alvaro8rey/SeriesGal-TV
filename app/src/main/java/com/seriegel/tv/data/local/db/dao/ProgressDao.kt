package com.seriegel.tv.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seriegel.tv.data.local.db.entity.ProgressCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProgressCacheEntity)

    @Query(
        """
        SELECT * FROM progress_cache
        WHERE seriesId = :seriesId AND episodeId = :episodeId
        LIMIT 1
        """,
    )
    suspend fun get(seriesId: String, episodeId: String): ProgressCacheEntity?

    @Query(
        """
        SELECT * FROM progress_cache
        WHERE seriesId = :seriesId
        ORDER BY updatedAtEpochMs DESC
        LIMIT 1
        """,
    )
    suspend fun latestForSeries(seriesId: String): ProgressCacheEntity?

    @Query(
        """
        SELECT * FROM progress_cache
        WHERE seriesId = :seriesId
        ORDER BY updatedAtEpochMs DESC
        """,
    )
    fun observeBySeries(seriesId: String): Flow<List<ProgressCacheEntity>>

    @Query("DELETE FROM progress_cache")
    suspend fun clearAll()
}
