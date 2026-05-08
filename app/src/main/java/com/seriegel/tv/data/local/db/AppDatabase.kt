package com.seriegel.tv.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.seriegel.tv.data.local.db.dao.DownloadDao
import com.seriegel.tv.data.local.db.entity.DownloadItemEntity

@Database(
    entities = [DownloadItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "seriesgal_tv.db",
            ).build()
        }
    }
}
