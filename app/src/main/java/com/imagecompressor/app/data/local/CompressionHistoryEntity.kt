package com.imagecompressor.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compression_history")
data class CompressionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val filePath: String,
    val originalBytes: Long,
    val compressedBytes: Long,
    val quality: Int,
    val width: Int,
    val height: Int,
    val timestampMillis: Long,
    /** Firebase UID, or null for guest/local-only history. */
    val ownerUid: String? = null
)
