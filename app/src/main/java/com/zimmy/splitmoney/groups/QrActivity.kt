package com.zimmy.splitmoney.groups

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.zimmy.splitmoney.R

class QrActivity : AppCompatActivity() {

    lateinit var qrImage: ImageView

    lateinit var gcode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        gcode = intent.getStringExtra("gcode").toString()

        qrImage = findViewById(R.id.qrImage)
        qrImage.setImageBitmap(getQrCodeBitmap(gcode))

    }

    fun getQrCodeBitmap(encode: String): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = encode
        val hints = hashMapOf<EncodeHintType, Int>().also {
            it[EncodeHintType.MARGIN] = 1
        } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}