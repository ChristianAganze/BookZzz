package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptGenerator {

    /**
     * Dynamically paints an authentic-looking Mobile Money SMS transaction receipt onto a Bitmap.
     * This allows us to feed dynamic, high-fidelity payment images to the Gemini API!
     */
    fun generateMobileMoneyReceipt(
        operator: String, // "M-Pesa", "Airtel Money", "Orange Money"
        amountUsd: Double,
        hotelName: String,
        phonePaidTo: String
    ): Bitmap {
        val width = 600
        val height = 750
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Base style sheets depending on operator
        val (primaryColor, smsSender) = when (operator) {
            "M-Pesa" -> Color.parseColor("#E11B22") to "MPESA_INFO" // Red
            "Airtel Money" -> Color.parseColor("#FF0000") to "AirtelMoney" // Bright Red/Orange
            "Orange Money" -> Color.parseColor("#FF6600") to "OrangeMoney" // Deep Orange
            else -> Color.parseColor("#1F3A5F") to "TRANS_CORP"
        }

        // Draw general phone SMS screen container background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#EBF5FB") // Soft blue-ish light background
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw simulated phone header bar
        val headerPaint = Paint().apply {
            color = Color.parseColor("#D5F5E3")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 60f, headerPaint)

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("📶 Vodacom RDC | 4G", 30f, 40f, textPaint)
        canvas.drawText("100%🔋 12:45", width - 180f, 40f, textPaint)

        // Draw SMS Bubble Container
        val bubblePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val bubbleRect = RectF(40f, 100f, width - 40f, height - 50f)
        canvas.drawRoundRect(bubbleRect, 24f, 24f, bubblePaint)

        // Draw bubble shadow borders
        val borderPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(bubbleRect, 24f, 24f, borderPaint)

        // Draw SMS Sender Header
        val senderTitlePaint = Paint().apply {
            color = primaryColor
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("✉️ $smsSender", 70f, 160f, senderTitlePaint)

        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }
        canvas.drawLine(70f, 190f, width - 70f, 190f, dividerPaint)

        // Setup transaction values
        val txId = "TX${(10000..99999).random()}BK${(10..99).random()}"
        val currentDate = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault()).format(Date())
        val currentHMS = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val usdFormatted = String.format(Locale.US, "%.2f", amountUsd)
        val cdfAmount = amountUsd * 2800.0 // 1 USD = 2800 CDF
        val cdfFormatted = String.format(Locale.FRANCE, "%,.0f", cdfAmount)

        // SMS transaction content depending on operator
        val smsLines = when (operator) {
            "M-Pesa" -> listOf(
                "M-PESA RDC INFO:",
                "Succès! Vous avez envoyé $usdFormatted USD",
                "à $hotelName ($phonePaidTo).",
                "Frais de transaction: 0.85 USD.",
                "Nouveau solde: 512.40 USD.",
                "ID de transaction: $txId.",
                "Date: $currentDate à $currentHMS.",
                "Merci d'utiliser Vodacom M-Pesa!"
            )
            "Airtel Money" -> listOf(
                "Airtel Money RDC:",
                "Transaction ID: $txId",
                "Transfert de $cdfFormatted CDF",
                "effectué avec succès vers",
                "l'établissement $hotelName ($phonePaidTo).",
                "Date de paiement: $currentDate $currentHMS.",
                "Votre solde Airtel Money disponible",
                "est de 1,240,500 CDF."
            )
            else -> listOf(
                "Orange Money RDC:",
                "Paiement marchand validé.",
                "Montant débité: $usdFormatted USD",
                "Bénéficiaire: BOOKZZZ - $hotelName.",
                "Numéro de référence: $txId.",
                "Fait le $currentDate à $currentHMS.",
                "Orange Money, la vie change avec Orange RDC."
            )
        }

        // Write SMS text onto the canvas inside the card bubble
        val smsTextPaint = Paint().apply {
            color = Color.parseColor("#1F3A5F")
            textSize = 24f
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
        }

        var startY = 240f
        smsLines.forEach { line ->
            canvas.drawText(line, 70f, startY, smsTextPaint)
            startY += 45f
        }

        // Draw a simulated seal / success mark
        val stampPaint = Paint().apply {
            color = Color.parseColor("#2ECC71") // Green success
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        val stampTextPaint = Paint().apply {
            color = Color.parseColor("#2ECC71")
            textSize = 32f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(width - 250f, height - 160f, width - 60f, height - 80f), 12f, 12f, stampPaint)
        canvas.drawText("REÇU OK", width - 215f, height - 110f, stampTextPaint)

        return bitmap
    }
}
