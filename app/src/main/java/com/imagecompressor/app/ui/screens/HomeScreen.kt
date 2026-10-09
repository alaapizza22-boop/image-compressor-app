package com.imagecompressor.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imagecompressor.app.R
import com.imagecompressor.app.data.ImageFileUtils
import com.imagecompressor.app.ui.components.BannerAdView
import com.imagecompressor.app.viewmodel.CompressionViewModel
import com.imagecompressor.app.viewmodel.QueuedImage

@Composable
fun HomeScreen(paddingValues: PaddingValues, viewModel: CompressionViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> if (uris.isNotEmpty()) viewModel.addImages(uris) }

    val pickSingleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.addImages(listOf(it)) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(stringResourceCompat(R.string.home_title), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text(stringResourceCompat(R.string.home_subtitle), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickSingleLauncher.launch(arrayOf("image/*")) }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(stringResourceCompat(R.string.select_single_image))
                    }
                    Button(onClick = { pickMultipleLauncher.launch(arrayOf("image/*")) }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(stringResourceCompat(R.string.select_multiple_images))
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResourceCompat(R.string.quality_label, state.quality))
                        Slider(
                            value = state.quality.toFloat(),
                            onValueChange = { viewModel.setQuality(it.toInt()) },
                            valueRange = 1f..100f
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResourceCompat(R.string.keep_original_dimensions))
                            Switch(
                                checked = state.keepOriginalDimensions,
                                onCheckedChange = { viewModel.setKeepOriginalDimensions(it) }
                            )
                        }
                        if (!state.keepOriginalDimensions) {
                            Spacer(Modifier.size(8.dp))
                            Text(stringResourceCompat(R.string.custom_max_dimension) + ": ${state.maxDimension}px")
                            Slider(
                                value = state.maxDimension.toFloat(),
                                onValueChange = { viewModel.setMaxDimension(it.toInt()) },
                                valueRange = 320f..4096f
                            )
                        }
                    }
                }
            }

            if (state.queue.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text(stringResourceCompat(R.string.no_images_selected))
                    }
                }
            } else {
                items(state.queue, key = { it.uri }) { item ->
                    QueuedImageCard(item, onRemove = { viewModel.removeImage(item.uri) })
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.compressAll() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResourceCompat(R.string.compress_all_button, state.queue.size))
                        }
                        OutlinedButton(onClick = { viewModel.clearQueue() }) {
                            Text(stringResourceCompat(R.string.clear_button))
                        }
                    }
                    if (state.isBatchRunning) {
                        Spacer(Modifier.size(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // AdMob banner placement (uses Google test ad unit by default).
        BannerAdView()
    }
}

@Composable
private fun QueuedImageCard(item: QueuedImage, onRemove: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    modifier = Modifier.size(64.dp).padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.displayName, maxLines = 1)
                    item.result?.let { result ->
                        Text(stringResourceCompat(R.string.original_size, ImageFileUtils.humanReadableBytes(result.originalBytes)))
                        Text(stringResourceCompat(R.string.compressed_size, ImageFileUtils.humanReadableBytes(result.compressedBytes)))
                        Text(stringResourceCompat(R.string.saved_percent, result.savedPercent))
                    }
                    item.error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                    if (item.isProcessing) {
                        Text(stringResourceCompat(R.string.compressing))
                    }
                }
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = null) }
            }

            if (item.result != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedButton(onClick = {
                        val uri = ImageFileUtils.saveToGallery(
                            context, item.result.outputFile, item.result.outputFile.name, "image/jpeg"
                        )
                        Toast.makeText(
                            context,
                            if (uri != null) "Saved" else "Failed to save",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.size(4.dp))
                        Text(stringResourceCompat(R.string.save_button))
                    }
                    OutlinedButton(onClick = {
                        val intent = ImageFileUtils.buildShareIntent(context, listOf(item.result.outputFile), "image/jpeg")
                        context.startActivity(android.content.Intent.createChooser(intent, "Share compressed image"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.size(4.dp))
                        Text(stringResourceCompat(R.string.share_button))
                    }
                }
            }
        }
    }
}

/** Small helper so this file can use string resources with simple formatting. */
@Composable
private fun stringResourceCompat(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id, *args)
