package com.biohazard786.quickupi.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

object QRCodeGenerator {
    @Throws(WriterException::class)
    fun generateQRCode(text: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height
        )
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)

        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = createBitmap(matrixWidth, matrixHeight, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
        return bitmap
    }
}
