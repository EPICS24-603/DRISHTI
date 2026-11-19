package com.example.assistantapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun sendFrameToGeminiAI(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = GeminiClient.ask(
            "Briefly describe the scene ahead. Mention an immediate obstacle first, then one useful action. Keep it to two short spoken sentences.",
            bitmap
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        Log.e("DrishtiAI", "Image analysis failed", error)
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not analyze the image. Please try again.") }
    }
}

/** ImageAnalysis frames are YUV_420_888, not JPEG byte arrays. Convert all planes before decoding. */
fun ImageProxy.toBitmap(): Bitmap? {
    return try {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)

        val rowStride = planes[1].rowStride
        val pixelStride = planes[1].pixelStride
        val chromaWidth = (width + 1) / 2
        val chromaHeight = (height + 1) / 2
        var outputIndex = ySize
        for (row in 0 until chromaHeight) {
            for (column in 0 until chromaWidth) {
                val vIndex = row * planes[2].rowStride + column * planes[2].pixelStride
                val uIndex = row * rowStride + column * pixelStride
                nv21[outputIndex++] = vBuffer.get(vIndex)
                nv21[outputIndex++] = uBuffer.get(uIndex)
            }
        }

        val jpeg = ByteArrayOutputStream()
        YuvImage(nv21, ImageFormat.NV21, width, height, null)
            .compressToJpeg(Rect(0, 0, width, height), 88, jpeg)
        val decoded = BitmapFactory.decodeByteArray(jpeg.toByteArray(), 0, jpeg.size()) ?: return null
        val rotation = imageInfo.rotationDegrees
        if (rotation == 0) decoded else {
            val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
                if (it !== decoded) decoded.recycle()
            }
        }
    } catch (error: Exception) {
        Log.e("ImageProxy", "Could not convert camera frame", error)
        null
    }
}
