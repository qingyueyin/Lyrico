package com.lonx.lyrico.data.migration

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable.Entries(
    DeleteTable(tableName = "batch_match_records"),
    DeleteTable(tableName = "batch_match_history")
)
class DeleteBatchMatchHistorySpec : AutoMigrationSpec