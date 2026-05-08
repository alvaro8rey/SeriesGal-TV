package com.seriegel.tv.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seriegel.tv.data.local.db.entity.DownloadItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_items ORDER BY title")
    fun observeAll(): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DownloadItemEntity)

    @Query("DELETE FROM download_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM download_items")
    suspend fun deleteAll()
}
