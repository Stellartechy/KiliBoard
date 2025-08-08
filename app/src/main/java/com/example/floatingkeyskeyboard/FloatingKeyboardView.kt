package com.example.floatingkeyskeyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import kotlin.random.Random

class FloatingKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Key(
        val label: String,
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val size: Float
    )

    private val keys = mutableListOf<Key>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        color = Color.BLACK
    }
    private val handler = Handler()
    private val random = Random(System.currentTimeMillis())

    private var viewWidth = 0
    private var viewHeight = 0

    private val moveRunnable = object : Runnable {
        override fun run() {
            moveKeys()
            invalidate()
            handler.postDelayed(this, 50)  // roughly 20 frames per second
        }
    }

    init {
        val letters = "QWERTYUIOPASDFGHJKLZXCVBNM"
        for (char in letters) {
            val size = 80f
            val startX = random.nextInt(600) + 100f
            val startY = random.nextInt(300) + 100f
            val vx = (random.nextFloat() - 0.5f) * 20f
            val vy = (random.nextFloat() - 0.5f) * 20f
            keys.add(Key(char.toString(), startX, startY, vx, vy, size))
        }
        handler.post(moveRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = width
        viewHeight = height

        // Draw background
        paint.color = Color.LTGRAY
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        // Draw keys
        paint.color = Color.DKGRAY
        for (key in keys) {
            canvas.drawCircle(key.x, key.y, key.size / 2, paint)

            paint.color = Color.WHITE
            val textWidth = paint.measureText(key.label)
            val textHeight = paint.textSize
            canvas.drawText(key.label, key.x - textWidth / 2, key.y + textHeight / 3, paint)

            paint.color = Color.DKGRAY
        }
    }

    private fun moveKeys() {
        if (viewWidth == 0 || viewHeight == 0) return

        for (key in keys) {
            key.x += key.vx
            key.y += key.vy

            // Bounce off edges
            if (key.x < key.size / 2) {
                key.x = key.size / 2
                key.vx = -key.vx
            }
            if (key.x > viewWidth - key.size / 2) {
                key.x = viewWidth - key.size / 2
                key.vx = -key.vx
            }
            if (key.y < key.size / 2) {
                key.y = key.size / 2
                key.vy = -key.vy
            }
            if (key.y > viewHeight - key.size / 2) {
                key.y = viewHeight - key.size / 2
                key.vy = -key.vy
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            for (key in keys) {
                val dx = touchX - key.x
                val dy = touchY - key.y
                if (dx * dx + dy * dy <= (key.size / 2) * (key.size / 2)) {
                    val ic: InputConnection? = (context as FloatingKeyboardService).currentInputConnection
                    ic?.commitText(key.label.lowercase(), 1)
                    break
                }
            }
        }
        return true
    }
}