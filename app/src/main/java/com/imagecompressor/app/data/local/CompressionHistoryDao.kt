package com.imagecompressor.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressionHistoryDao {

    @Insert
    suspend fun insert(entity: CompressionHistoryEntity): Long

    @Query("SELECT * FROM compression_history ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<CompressionHistoryEntity>>

    @Delete
    suspend fun delete(entity: CompressionHistoryEntity)

    @Query("DELETE FROM compression_history")
    suspend fun clearAll()
}
