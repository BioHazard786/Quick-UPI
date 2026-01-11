package com.biohazard786.quickupi.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareQrCode(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean
    ) {
        val imageFile = generateShareableImage(context, qrBitmap, name, upiId, amount, showUpiId) ?: return
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Code"))
    }

    private fun generateShareableImage(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean
    ): File? {
        try {
            // 1. Create a blank bitmap
            val width = 800
            // estimated height, can be dynamic
            val height = 1000
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)

            // 2. Draw Background
            canvas.drawColor("#f8f9fb".toColorInt())

            // 3. Paint setup
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 50f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            var currentY = 100f

            // 4. Draw Name
            // If name is present, draw it
            if (name.isNotBlank()) {
                textPaint.textSize = 60f
                textPaint.isFakeBoldText = false
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText(name, width / 2f, currentY, textPaint)
                currentY += 60f
            }

            // 5. Draw QR Code
            // Centered
            val qrSize = 600
            val qrLeft = (width - qrSize) / 2
            val qrDestRect =
                Rect(qrLeft, currentY.toInt(), qrLeft + qrSize, (currentY + qrSize).toInt())
            canvas.drawBitmap(qrBitmap, null, qrDestRect, null)
            currentY += qrSize + 120f

            // 6. Draw Amount (if exists)
            if (amount.isNotBlank()) {
                textPaint.textSize = 80f
                textPaint.isFakeBoldText = true
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText("₹$amount", width / 2f, currentY, textPaint)
                currentY += 60f
            } else {
                textPaint.textSize = 60f
                textPaint.isFakeBoldText = true
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText("Scan to Pay", width / 2f, currentY, textPaint)
                currentY += 60f
            }

            // 7. Draw UPI ID
            if (showUpiId) {
                textPaint.textSize = 30f
                textPaint.isFakeBoldText = false
                textPaint.color = "#7b88a1".toColorInt()
                canvas.drawText("UPI ID: $upiId", width / 2f, currentY, textPaint)
            }

            // 8. Save Bitmap to file
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_qr.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            return file

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
