package com.example.propertymanagementsystem.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.ReceiptData
import java.io.File
import java.io.FileOutputStream

/**
 * Draws a receipt as an image and hands it to the Android share sheet.
 *
 * The receipt is deliberately a real artefact, not a screenshot: it has the
 * agent's figures, the receipt number and the balance after the payment.
 */
object ReceiptGenerator {

    private const val WIDTH = 1080
    private const val HEIGHT = 1600

    fun share(context: Context, data: ReceiptData) {
        val bitmap = render(context, data)
        val directory = File(context.cacheDir, "receipts").apply { mkdirs() }
        val number = data.payment.receiptNumber.ifBlank { "receipt-${data.payment.id}" }
        val file = File(directory, "$number.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "Sahihi receipt $number for ${data.tenant.name} — " +
                    Money.format(data.payment.amount)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share receipt"))
    }

    fun render(context: Context, data: ReceiptData): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#F7F1E3"))

        val serif = ResourcesCompat.getFont(context, R.font.dm_serif_display)
            ?: Typeface.SERIF
        val sans = ResourcesCompat.getFont(context, R.font.dm_sans) ?: Typeface.SANS_SERIF
        val mono = ResourcesCompat.getFont(context, R.font.dm_mono_regular) ?: Typeface.MONOSPACE

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val forest = Color.parseColor("#0F3D2E")
        val gold = Color.parseColor("#C9A227")
        val cream = Color.parseColor("#FFFDF7")
        val ink = Color.parseColor("#1C1B18")
        val muted = Color.parseColor("#7C7568")
        val line = Color.parseColor("#D8CBB7")

        // The forest band at the top.
        paint.color = forest
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), 190f, paint)

        paint.typeface = serif
        paint.color = cream
        paint.textSize = 64f
        canvas.drawText("SAHIHI", 72f, 112f, paint)

        paint.typeface = mono
        paint.textSize = 22f
        paint.color = gold
        canvas.drawText("RENT RECEIPT", 72f, 154f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = cream
        paint.textSize = 24f
        val receiptNumber = data.payment.receiptNumber.ifBlank {
            "SH-" + data.payment.date.take(4) + "-" + data.payment.id.toString().padStart(4, '0')
        }
        canvas.drawText(receiptNumber, WIDTH - 72f, 154f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Who paid.
        paint.typeface = sans
        paint.color = muted
        paint.textSize = 24f
        canvas.drawText("RECEIVED FROM", 72f, 270f, paint)

        paint.typeface = serif
        paint.color = ink
        paint.textSize = 46f
        canvas.drawText(data.tenant.name, 72f, 330f, paint)

        paint.typeface = sans
        paint.color = muted
        paint.textSize = 26f
        val unit = data.unit?.name.orEmpty()
        val building = data.building?.name.orEmpty()
        canvas.drawText(listOf(building, unit).filter { it.isNotBlank() }.joinToString(" · "), 72f, 370f, paint)

        dashedLine(canvas, paint, 430f, line)

        // The body rows.
        var y = 500f
        row(canvas, paint, sans, mono, "Period", Dates.monthLabel(data.payment.month), y, ink, muted)
        y += 70f
        row(canvas, paint, sans, mono, "Date", data.payment.date, y, ink, muted)
        y += 70f
        row(canvas, paint, sans, mono, "Method", data.payment.method, y, ink, muted)
        y += 70f
        row(
            canvas, paint, sans, mono, "Reference",
            data.payment.reference?.ifBlank { "—" } ?: "—", y, ink, muted
        )
        y += 70f
        row(
            canvas, paint, sans, mono, "Collected by",
            data.payment.recordedBy, y, ink, muted
        )

        dashedLine(canvas, paint, 910f, line)

        paint.typeface = sans
        paint.color = muted
        paint.textSize = 24f
        canvas.drawText("AMOUNT PAID", 72f, 990f, paint)

        paint.typeface = serif
        paint.color = forest
        paint.textSize = 76f
        canvas.drawText(Money.format(data.payment.amount), 72f, 1075f, paint)

        paint.typeface = mono
        paint.textSize = 28f
        paint.color = muted
        val balance = data.payment.balanceAfter
        val balanceText = when {
            balance == null -> "Balance: —"
            balance > 0.005 -> "Balance after: " + Money.format(balance)
            balance < -0.005 -> "Credit after: " + Money.format(-balance)
            else -> "Balance after: Ksh 0"
        }
        canvas.drawText(balanceText, 72f, 1135f, paint)

        // The stamp.
        canvas.save()
        canvas.rotate(-12f, 820f, 1215f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = gold
        canvas.drawCircle(820f, 1215f, 112f, paint)
        paint.strokeWidth = 2f
        canvas.drawCircle(820f, 1215f, 96f, paint)
        paint.style = Paint.Style.FILL
        paint.typeface = serif
        paint.textAlign = Paint.Align.CENTER
        paint.color = gold
        paint.textSize = 40f
        canvas.drawText("SAHIHI", 820f, 1205f, paint)
        paint.typeface = mono
        paint.textSize = 18f
        canvas.drawText("VERIFIED", 820f, 1240f, paint)
        canvas.restore()
        paint.textAlign = Paint.Align.LEFT

        // Footer.
        paint.typeface = mono
        paint.color = muted
        paint.textSize = 20f
        canvas.drawText("Keep this receipt. Every shilling, accounted for.", 72f, 1470f, paint)
        paint.textSize = 18f
        canvas.drawText("Generated by Sahihi · " + Dates.today(), 72f, 1510f, paint)

        return bitmap
    }

    private fun row(
        canvas: Canvas,
        paint: Paint,
        sans: Typeface,
        mono: Typeface,
        label: String,
        value: String,
        y: Float,
        ink: Int,
        muted: Int
    ) {
        paint.typeface = sans
        paint.color = muted
        paint.textSize = 24f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, 72f, y, paint)

        paint.typeface = mono
        paint.color = ink
        paint.textSize = 26f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, WIDTH - 72f, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun dashedLine(canvas: Canvas, paint: Paint, y: Float, color: Int) {
        paint.color = color
        paint.strokeWidth = 2f
        paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
        canvas.drawLine(72f, y, WIDTH - 72f, y, paint)
        paint.pathEffect = null
    }
}
