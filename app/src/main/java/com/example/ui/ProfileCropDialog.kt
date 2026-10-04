package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileOutputStream

/**
 * Loads a full-resolution [Bitmap] from a given [Uri] without quality downsampling.
 */
fun loadFullResolutionBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, BitmapFactory.Options().apply {
                    inScaled = false
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                })
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Saves a bitmap to app internal storage at 100% full JPEG quality.
 */
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "pfp"): String? {
    return try {
        val dir = context.filesDir
        if (prefix == "pfp") {
            dir.listFiles()?.filter { it.name.startsWith("pfp_") }?.forEach { it.delete() }
        }
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Interactive Profile Picture Crop & Photo Editor Dialog.
 * Allows zoom, pan, rotation, frame shape selection, and 100% high quality export.
 */
@Composable
fun ProfilePictureCropDialog(
    initialBitmap: Bitmap,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isCircleCrop by remember { mutableStateOf(true) }

    val cropBoxSizeDp = 270.dp
    val cropBoxSizePx = with(density) { cropBoxSizeDp.toPx() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1014)),
            color = Color(0xFF0F1014)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                    Text(
                        text = "Crop Profile Photo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            try {
                                val cropped = cropBitmap(
                                    sourceBitmap = initialBitmap,
                                    rotationAngle = rotationAngle,
                                    scale = scale,
                                    offset = offset,
                                    cropBoxSizePx = cropBoxSizePx
                                )
                                val savedPath = saveBitmapToInternalStorage(context, cropped, "pfp")
                                if (savedPath != null) {
                                    onSave(savedPath)
                                } else {
                                    Toast.makeText(context, "Error saving profile photo", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Crop error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Photo",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Crop Workspace
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Image Canvas with Transform Matrix
                    Image(
                        bitmap = initialBitmap.asImageBitmap(),
                        contentDescription = "Profile Photo Workspace",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y,
                                rotationZ = rotationAngle
                            )
                    )

                    // Crop Overlay Mask with Grid Guides
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val cropRect = Rect(
                            left = (canvasWidth - cropBoxSizePx) / 2f,
                            top = (canvasHeight - cropBoxSizePx) / 2f,
                            right = (canvasWidth + cropBoxSizePx) / 2f,
                            bottom = (canvasHeight + cropBoxSizePx) / 2f
                        )

                        // Outer Dimmed Mask Path
                        val maskPath = Path().apply {
                            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                            if (isCircleCrop) {
                                addOval(cropRect)
                            } else {
                                addRoundRect(RoundRect(cropRect, CornerRadius(24f, 24f)))
                            }
                            fillType = PathFillType.EvenOdd
                        }
                        drawPath(maskPath, color = Color.Black.copy(alpha = 0.65f))

                        // Frame Border
                        if (isCircleCrop) {
                            drawOval(
                                color = Color.White,
                                topLeft = cropRect.topLeft,
                                size = cropRect.size,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        } else {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = cropRect.topLeft,
                                size = cropRect.size,
                                cornerRadius = CornerRadius(24f, 24f),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Rule of Thirds Grid Lines inside Crop Area
                        val thirdW = cropBoxSizePx / 3f
                        val thirdH = cropBoxSizePx / 3f

                        val lineAlpha = 0.35f
                        val gridColor = Color.White.copy(alpha = lineAlpha)

                        // Vertical grid lines
                        drawLine(gridColor, Offset(cropRect.left + thirdW, cropRect.top), Offset(cropRect.left + thirdW, cropRect.bottom), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, Offset(cropRect.left + thirdW * 2, cropRect.top), Offset(cropRect.left + thirdW * 2, cropRect.bottom), strokeWidth = 1.dp.toPx())

                        // Horizontal grid lines
                        drawLine(gridColor, Offset(cropRect.left, cropRect.top + thirdH), Offset(cropRect.right, cropRect.top + thirdH), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, Offset(cropRect.left, cropRect.top + thirdH * 2), Offset(cropRect.right, cropRect.top + thirdH * 2), strokeWidth = 1.dp.toPx())
                    }

                    // Floating Instruction
                    Text(
                        text = "Pinch to zoom • Drag to align face",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Adjustment Toolbar Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C22)),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E303A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Zoom Slider Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Zoom", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                            Slider(
                                value = scale,
                                onValueChange = { scale = it },
                                valueRange = 0.8f..4.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color(0xFF10B981),
                                    inactiveTrackColor = Color(0xFF2E303A)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text("%.1fx".format(scale), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFF2E303A))

                        // Controls Row: Rotate Left, Rotate Right, Shape Toggle, Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rotate 90° Left
                            OutlinedButton(
                                onClick = { rotationAngle = (rotationAngle - 90f) % 360f },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E303A)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.RotateLeft, contentDescription = "Rotate Left", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("90° Left", fontSize = 11.sp, color = Color.White)
                            }

                            // Rotate 90° Right
                            OutlinedButton(
                                onClick = { rotationAngle = (rotationAngle + 90f) % 360f },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E303A)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.RotateRight, contentDescription = "Rotate Right", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("90° Right", fontSize = 11.sp, color = Color.White)
                            }

                            // Shape Toggle
                            OutlinedButton(
                                onClick = { isCircleCrop = !isCircleCrop },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E303A)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.CropFree, contentDescription = "Toggle Shape", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isCircleCrop) "Circle" else "Square", fontSize = 11.sp, color = Color.White)
                            }

                            // Reset
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                    rotationAngle = 0f
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Transforms", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculates high-resolution cropped bitmap matching user transforms.
 */
private fun cropBitmap(
    sourceBitmap: Bitmap,
    rotationAngle: Float,
    scale: Float,
    offset: Offset,
    cropBoxSizePx: Float
): Bitmap {
    // 1. Apply rotation if needed
    val rotated = if (rotationAngle % 360f != 0f) {
        val matrix = Matrix().apply { postRotate(rotationAngle) }
        Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
    } else {
        sourceBitmap
    }

    // 2. Compute display ratio
    val minSide = minOf(rotated.width, rotated.height).toFloat()
    val bitmapPxPerScreenPx = (minSide / cropBoxSizePx) / scale

    val cropDimension = (cropBoxSizePx * bitmapPxPerScreenPx).toInt()
        .coerceIn(10, minOf(rotated.width, rotated.height))

    // 3. Center point of rotated bitmap shifted by offset
    val centerX = rotated.width / 2f - (offset.x * bitmapPxPerScreenPx)
    val centerY = rotated.height / 2f - (offset.y * bitmapPxPerScreenPx)

    val left = (centerX - cropDimension / 2f).toInt()
        .coerceIn(0, (rotated.width - cropDimension).coerceAtLeast(0))
    val top = (centerY - cropDimension / 2f).toInt()
        .coerceIn(0, (rotated.height - cropDimension).coerceAtLeast(0))

    val width = cropDimension.coerceAtMost(rotated.width - left)
    val height = cropDimension.coerceAtMost(rotated.height - top)

    return Bitmap.createBitmap(rotated, left, top, width, height)
}
