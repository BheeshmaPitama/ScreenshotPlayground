package com.anubhav.screenshotplayground.screenshot_helper

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.RequiresApi

class ScreenshotHelper(private val window: android.view.Window) {

    fun captureScreenshot(view: View, callback: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use PixelCopy for API 26 and above
            requestPixelCopyScreenshot(view, callback)
        } else {
            // Use DrawingCache for APIs below 26
            callback(takeScreenshotDrawingCache(view))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestPixelCopyScreenshot(view: View, callback: (Bitmap?) -> Unit) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        val location = IntArray(2)
        view.getLocationInWindow(location)

        try {
            PixelCopy.request(
                window,
                Rect(
                    location[0], location[1],
                    location[0] + view.width, location[1] + view.height
                ),
                bitmap,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        callback(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: IllegalArgumentException) {
            callback(null)
        }
    }

    private fun takeScreenshotDrawingCache(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache(true)
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    fun getRoundedBitmap(sourceBitmap: Bitmap, cornerRadius: Float): Bitmap {
        val roundedBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(roundedBitmap)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(sourceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val rect = RectF(0.0f, 0.0f, sourceBitmap.width.toFloat(), sourceBitmap.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        return roundedBitmap
    }
}