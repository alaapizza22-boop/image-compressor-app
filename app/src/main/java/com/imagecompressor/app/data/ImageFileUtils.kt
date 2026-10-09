package com.imagecompressor.app.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

/**
 * Helpers for native Android image selection results, saving compressed files back
 * to shared storage (MediaStore), and sharing via a FileProvider-backed content:// Uri.
 */
object ImageFileUtils {

    fun authority(context: Context) = "${context.packageName}.fileprovider"

    /** Returns a sandboxed cache directory used to stage compressed output files. */
    fun compressionCacheDir(context: Context): File =
        File(context.cacheDir, "compressed").apply { mkdirs() }

    /**
     * Saves a compressed image file into the public "Pictures/ImageCompressor" folder
     * using MediaStore (required on Android 10+, works down to API 24 via legacy path).
     */
    fun saveToGallery(context: Context, file: File, displayName: String, mimeType: String): Uri? {
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImageCompressor")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(itemUri)?.use { out ->
                FileInputStream(file).use { input -> input.copyTo(out) }
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)
            return itemUri
        } else {
            val picturesDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ImageCompressor"
            )
            if (!picturesDir.exists()) picturesDir.mkdirs()
            val destFile = File(picturesDir, displayName)
            FileInputStream(file).use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            // Notify the media scanner so the file shows up immediately in the gallery.
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, destFile.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            }
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    /** Builds a share Intent for one or more compressed files using FileProvider Uris. */
    fun buildShareIntent(context: Context, files: List<File>, mimeType: String): Intent {
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, authority(context), file)
        }
        return if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    fun humanReadableBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var value = bytes / 1024.0
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024.0
            unitIndex++
        }
        return "%.1f %s".format(value, units[unitIndex])
    }
}
