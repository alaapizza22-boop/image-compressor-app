package com.imagecompressor.app.data.repository

import android.content.Context
import com.imagecompressor.app.data.CompressionResult
import com.imagecompressor.app.data.local.AppDatabase
import com.imagecompressor.app.data.local.CompressionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Persists compression history locally with Room. When a Firebase user is signed in,
 * entries are tagged with their UID so a future sync-to-cloud backend could filter by
 * owner; by default everything works fully offline for guest users too.
 */
class CompressionRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).compressionHistoryDao()

    fun observeHistory(): Flow<List<CompressionHistoryEntity>> = dao.observeAll()

    suspend fun recordResult(result: CompressionResult, quality: Int, ownerUid: String?) {
        dao.insert(
            CompressionHistoryEntity(
                displayName = result.displayName,
                filePath = result.outputFile.absolutePath,
                originalBytes = result.originalBytes,
                compressedBytes = result.compressedBytes,
                quality = quality,
                width = result.finalWidth,
                height = result.finalHeight,
                timestampMillis = System.currentTimeMillis(),
                ownerUid = ownerUid
            )
        )
    }

    suspend fun delete(entity: CompressionHistoryEntity) = dao.delete(entity)

    suspend fun clearAll() = dao.clearAll()
}
