package com.imagecompressor.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imagecompressor.app.auth.FirebaseAuthManager
import com.imagecompressor.app.data.CompressionEngine
import com.imagecompressor.app.data.CompressionOptions
import com.imagecompressor.app.data.CompressionResult
import com.imagecompressor.app.data.ImageFileUtils
import com.imagecompressor.app.data.repository.CompressionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One image queued for compression, alongside its result once processed. */
data class QueuedImage(
    val uri: Uri,
    val displayName: String,
    val isProcessing: Boolean = false,
    val result: CompressionResult? = null,
    val error: String? = null
)

data class CompressionScreenState(
    val queue: List<QueuedImage> = emptyList(),
    val quality: Int = 80,
    val keepOriginalDimensions: Boolean = true,
    val maxDimension: Int = 1920,
    val isBatchRunning: Boolean = false
)

class CompressionViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CompressionEngine(application)
    private val repository = CompressionRepository(application)
    private val authManager = FirebaseAuthManager()

    private val _uiState = MutableStateFlow(CompressionScreenState())
    val uiState: StateFlow<CompressionScreenState> = _uiState.asStateFlow()

    val history = repository.observeHistory()

    fun setQuality(value: Int) {
        _uiState.value = _uiState.value.copy(quality = value)
    }

    fun setKeepOriginalDimensions(value: Boolean) {
        _uiState.value = _uiState.value.copy(keepOriginalDimensions = value)
    }

    fun setMaxDimension(value: Int) {
        _uiState.value = _uiState.value.copy(maxDimension = value)
    }

    fun addImages(uris: List<Uri>) {
        val app = getApplication<Application>()
        val newItems = uris.map { uri ->
            QueuedImage(uri = uri, displayName = queryDisplayName(uri) ?: "image_${System.currentTimeMillis()}.jpg")
        }
        _uiState.value = _uiState.value.copy(queue = _uiState.value.queue + newItems)
    }

    fun clearQueue() {
        _uiState.value = _uiState.value.copy(queue = emptyList())
    }

    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(queue = _uiState.value.queue.filterNot { it.uri == uri })
    }

    /** Compresses every image currently in the queue sequentially (batch compression). */
    fun compressAll() {
        val state = _uiState.value
        if (state.queue.isEmpty() || state.isBatchRunning) return
        _uiState.value = state.copy(isBatchRunning = true)

        viewModelScope.launch {
            val options = CompressionOptions(
                quality = state.quality,
                keepOriginalDimensions = state.keepOriginalDimensions,
                maxDimension = state.maxDimension,
                outputFormat = Bitmap.CompressFormat.JPEG
            )
            val outputDir = ImageFileUtils.compressionCacheDir(getApplication())

            for (item in _uiState.value.queue) {
                if (item.result != null) continue
                markProcessing(item.uri, true)
                val result = engine.compress(item.uri, item.displayName, options, outputDir)
                result.onSuccess { r ->
                    updateItemResult(item.uri, r, null)
                    repository.recordResult(r, options.quality, authManager.currentUser?.uid)
                }.onFailure { e ->
                    updateItemResult(item.uri, null, e.message ?: "Compression failed")
                }
            }
            _uiState.value = _uiState.value.copy(isBatchRunning = false)
        }
    }

    private fun markProcessing(uri: Uri, isProcessing: Boolean) {
        _uiState.value = _uiState.value.copy(
            queue = _uiState.value.queue.map {
                if (it.uri == uri) it.copy(isProcessing = isProcessing) else it
            }
        )
    }

    private fun updateItemResult(uri: Uri, result: CompressionResult?, error: String?) {
        _uiState.value = _uiState.value.copy(
            queue = _uiState.value.queue.map {
                if (it.uri == uri) it.copy(isProcessing = false, result = result, error = error) else it
            }
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        val resolver = getApplication<Application>().contentResolver
        var name: String? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) name = cursor.getString(index)
        }
        return name
    }
}
