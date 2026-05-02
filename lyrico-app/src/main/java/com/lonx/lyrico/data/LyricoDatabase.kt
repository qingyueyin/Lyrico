package com.lonx.lyrico.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import com.lonx.lyrico.data.model.dao.FolderDao
import com.lonx.lyrico.data.model.entity.FolderEntity
import com.lonx.lyrico.data.model.entity.SongEntity
import com.lonx.lyrico.data.model.dao.SongDao
import com.lonx.lyrico.data.model.dao.BatchTaskDao
import com.lonx.lyrico.data.model.entity.BatchTaskEntity
import com.lonx.lyrico.data.model.entity.BatchTaskItemEntity
import com.lonx.lyrico.data.migration.DeleteBatchMatchHistorySpec

@Database(
    entities = [
        SongEntity::class, 
        FolderEntity::class,
        BatchTaskEntity::class,
        BatchTaskItemEntity::class
    ],
    version = 12,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11, spec = DeleteBatchMatchHistorySpec::class),
        AutoMigration(from = 11, to = 12)
    ]
)
abstract class LyricoDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun folderDao(): FolderDao
    abstract fun batchTaskDao(): BatchTaskDao
}
