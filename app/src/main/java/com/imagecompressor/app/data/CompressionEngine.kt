package com.imagecompressor.app.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Options controlling a single compression operation.
 *
 * @param quality JPEG/WEBP quality 1-100 (ignored for PNG, which is lossless).
 * @param keepOriginalDimensions when true (the default) the image is compressed
 *        without resizing — only encoding quality is reduced.
 * @param maxDimension used only when [keepOriginalDimensions] is false; the longest
 *        side of the image is scaled down to this value, preserving aspect ratio.
 * @param outputFormat target format for the compressed file.
 */
data class CompressionOptions(
    val quality: Int = 80,
    val keepOriginalDimensions: Boolean = true,
    val maxDimension: Int = 1920,
    val outputFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
)

data class CompressionResult(
    val sourceUri: Uri,
    val displayName: String,
    val originalBytes: Long,
    val compressedBytes: Long,
    val outputFile: File,
    val originalWidth: Int,
    val originalHeight: Int,
    val finalWidth: Int,
    val finalHeight: Int
) {
    val savedPercent: Int
        get() = if (originalBytes <= 0L) 0
        else (100 - (compressedBytes * 100 / originalBytes)).toInt().coerceIn(0, 100)
}

/**
 * Native Android image compression engine.
 *
 * Uses [BitmapFactory] to decode (with inSampleSize down-sampling to avoid OOM on large
 * images), [ExifInterface] to preserve correct orientation, and [Bitmap.compress] to
 * re-encode at the desired quality. No third-party native compression library is used —
 * everything here relies purely on the Android SDK.
 */
class CompressionEngine(private val context: Context) {

    private val resolver: ContentResolver get() = context.contentResolver

    suspend fun compress(
        uri: Uri,
        displayName: String,
        options: CompressionOptions,
        outputDir: File
    ): Result<CompressionResult> = withContext(Dispatchers.IO) {
        runCatching {
            val originalBytes = queryFileSize(uri)

            // 1) Read bounds only, to compute an efficient inSampleSize.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val originalWidth = bounds.outWidth
            val originalHeight = bounds.outHeight

            val targetLongSide = if (options.keepOriginalDimensions) {
                maxOf(originalWidth, originalHeight)
            } else {
                options.maxDimension
            }

            val sampleSize = calculateInSampleSize(originalWidth, originalHeight, targetLongSide)

            // 2) Decode the actual (possibly down-sampled) bitmap.
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            var bitmap = openStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
                ?: error("Unable to decode image")

            // 3) Correct orientation using EXIF metadata.
            val rotationDegrees = readExifRotation(uri)
            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) bitmap.recycle()
                bitmap = rotated
            }

            // 4) If a custom max dimension was requested, scale precisely (inSampleSize is coarse).
            if (!options.keepOriginalDimensions) {
                val longSide = maxOf(bitmap.width, bitmap.height)
                if (longSide > options.maxDimension) {
                    val scale = options.maxDimension.toFloat() / longSide
                    val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
                    val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                    if (scaled != bitmap) bitmap.recycle()
                    bitmap = scaled
                }
            }

            // 5) Encode at the requested quality.
            val baos = ByteArrayOutputStream()
            bitmap.compress(options.outputFormat, options.quality.coerceIn(1, 100), baos)

            val extension = when (options.outputFormat) {
                Bitmap.CompressFormat.PNG -> "png"
                Bitmap.CompressFormat.WEBP_LOSSY, Bitmap.CompressFormat.WEBP_LOSSLESS -> "webp"
                else -> "jpg"
            }
            if (!outputDir.exists()) outputDir.mkdirs()
            val outFile = File(outputDir, "compressed_${System.currentTimeMillis()}_${sanitize(displayName)}.$extension")
            FileOutputStream(outFile).use { it.write(baos.toByteArray()) }

            val finalWidth = bitmap.width
            val finalHeight = bitmap.height
            bitmap.recycle()

            CompressionResult(
                sourceUri = uri,
                displayName = displayName,
                originalBytes = originalBytes,
                compressedBytes = outFile.length(),
                outputFile = outFile,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                finalWidth = finalWidth,
                finalHeight = finalHeight
            )
        }
    }

    private fun sanitize(name: String): String = name.replace(Regex("[^A-Za-z0-9._-]"), "_")

    private fun openStream(uri: Uri): InputStream? = resolver.openInputStream(uri)

    private fun queryFileSize(uri: Uri): Long {
        resolver.openAssetFileDescriptor(uri, "r")?.use { return it.length }
        return 0L
    }

    private fun readExifRotation(uri: Uri): Int {
        return try {
            openStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, targetLongSide: Int): Int {
        var inSampleSize = 1
        val longSide = maxOf(width, height)
        if (longSide > targetLongSide) {
            val halfLongSide = longSide / 2
            while (halfLongSide / inSampleSize >= targetLongSide) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
