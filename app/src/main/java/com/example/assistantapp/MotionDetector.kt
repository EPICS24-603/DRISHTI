package com.example.assistantapp

import android.graphics.Bitmap
import kotlin.math.abs

/**
 * Ultra-lightweight scene change / motion detector to conserve API tokens and battery.
 * Downsamples frames to 16x16 thumbnails and compares luminance difference.
 * If the user is standing still or scene hasn't changed, skips redundant Gemini calls.
 */
class MotionDetector(
    private val threshold: Float = 0.065f, // 6.5% visual change triggers a new analysis
    private val maxStationaryIntervalMs: Long = 8000L // Always refresh if 8s pass even if static
) {
    private var lastLuminance: IntArray? = null
    private var lastAnalyzedTime: Long = 0L

    /**
     * Checks if the frame is sufficiently different from the last analyzed frame,
     * or if the maximum stationary time has elapsed.
     */
    fun hasSceneChanged(bitmap: Bitmap): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTime >= maxStationaryIntervalMs) {
            recordFrame(bitmap, currentTime)
            return true
        }

        val previous = lastLuminance ?: run {
            recordFrame(bitmap, currentTime)
            return true
        }

        val current = compute16x16Luminance(bitmap)
        var totalDiff = 0
        for (i in 0 until 256) {
            totalDiff += abs(current[i] - previous[i])
        }

        // Maximum possible diff is 256 * 255 = 65,280
        val diffRatio = totalDiff.toFloat() / 65280f
        if (diffRatio >= threshold) {
            lastLuminance = current
            lastAnalyzedTime = currentTime
            return true
        }

        return false
    }

    private fun recordFrame(bitmap: Bitmap, time: Long) {
        lastLuminance = compute16x16Luminance(bitmap)
        lastAnalyzedTime = time
    }

    private fun compute16x16Luminance(bitmap: Bitmap): IntArray {
        val thumb = Bitmap.createScaledBitmap(bitmap, 16, 16, false)
        val pixels = IntArray(256)
        thumb.getPixels(pixels, 0, 16, 0, 0, 16, 16)
        if (thumb !== bitmap) thumb.recycle()

        val luminance = IntArray(256)
        for (i in 0 until 256) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            // Standard perceptual luminance formula: 0.299R + 0.587G + 0.114B
            luminance[i] = ((r * 299) + (g * 587) + (b * 114)) / 1000
        }
        return luminance
    }

    fun reset() {
        lastLuminance = null
        lastAnalyzedTime = 0L
    }
}

