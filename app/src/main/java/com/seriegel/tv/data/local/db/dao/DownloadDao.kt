package com.seriegel.tv.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seriegel.tv.data.local.db.entity.DownloadItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_items ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM download_items ORDER BY updatedAtEpochMs DESC")
    fun getAll(): List<DownloadItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DownloadItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<DownloadItemEntity>)

    @Query("SELECT * FROM download_items WHERE id = :id LIMIT 1")
    fun getById(id: String): DownloadItemEntity?

    @Query("DELETE FROM download_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM download_items")
    suspend fun deleteAll()
}
