package com.example.floatingkeyskeyboard

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import kotlin.math.pow
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
        val size: Float,
        val color: Int,
        val isSpecial: Boolean = false
    )

    private val letters = "QWERTYUIOPASDFGHJKLZXCVBNM"
    private val lowercase = "qwertyuiopasdfghjklzxcvbnm"
    private val numbers = "1234567890"
    private val symbols = listOf("@", "#", "$", "%", "&", "*", "(", ")", "-", "+")
    private val emojis = listOf("😀", "😂", "😍", "👍", "🙏", "🔥", "💯", "🎉")

    private val keys = mutableListOf<Key>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handler = Handler()
    private val random = Random(System.currentTimeMillis())
    private var viewWidth = 0
    private var viewHeight = 0
    private var currentSet = "LETTERS"
    private var speedFactor = 1.0f

    private val moveRunnable = object : Runnable {
        override fun run() {
            moveKeys()
            invalidate()
            handler.postDelayed(this, 16)
        }
    }

    init {
        loadKeys("LETTERS")
        handler.post(moveRunnable)
    }

    private fun loadKeys(set: String) {
        keys.clear()
        currentSet = set

        val chars = when (set) {
            "NUMBERS" -> numbers.toList().map { it.toString() }
            "SYMBOLS" -> symbols
            "EMOJIS" -> emojis
            "LOWER" -> lowercase.toList().map { it.toString() }
            else -> letters.toList().map { it.toString() }
        }

        for (char in chars) {
            addMovingKey(char)
        }

        // Special circular keys larger than normal
        keys += Key("⌫", 200f, 150f, randomVX(), randomVY(), 140f, Color.RED, isSpecial = true)
        keys += Key("↵", 400f, 150f, randomVX(), randomVY(), 140f, Color.RED, isSpecial = true)
        keys += Key("_", 600f, 150f, randomVX(), randomVY(), 180f, Color.RED, isSpecial = true)
    }

    private fun addMovingKey(label: String) {
        val isNumber = label.firstOrNull()?.isDigit() ?: false
        val isEmoji = emojis.contains(label)
        val size = when {
            isEmoji -> 110f
            isNumber -> 100f
            else -> 90f
        }
        val color = if (label.length > 1) Color.GRAY else Color.BLACK
        val x = random.nextInt(300, 800).toFloat()
        val y = random.nextInt(150, 500).toFloat()
        val vx = randomVX()
        val vy = randomVY()
        keys += Key(label, x, y, vx, vy, size, color)
    }

    private fun randomVX() = ((random.nextFloat() - 0.5f) * 50f) * speedFactor
    private fun randomVY() = ((random.nextFloat() - 0.5f) * 50f) * speedFactor

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = width
        viewHeight = height

        // Background
        canvas.drawColor(Color.WHITE)

        val sidebarWidth = 120f

        // Left Sidebar
        paint.color = Color.DKGRAY
        canvas.drawRect(0f, 0f, sidebarWidth, viewHeight.toFloat(), paint)

        paint.color = Color.WHITE
        paint.textSize = 32f
        val options = listOf("ABC", "abc", "123", "SYM", "EMJ")
        for ((index, label) in options.withIndex()) {
            canvas.drawText(label, 20f, 60f + index * 100f, paint)
        }

        // Speed bar (Right)
        val barX = viewWidth - 60f
        paint.color = Color.LTGRAY
        canvas.drawRect(barX, 100f, barX + 20f, viewHeight - 100f, paint)
        val markerY = 100f + (1.0f - speedFactor) * (viewHeight - 200f)
        paint.color = Color.YELLOW
        canvas.drawCircle(barX + 10f, markerY, 15f, paint)

        // Keys
        for (key in keys) {
            paint.color = key.color
            canvas.drawCircle(key.x, key.y, key.size / 2, paint)

            paint.color = Color.WHITE
            paint.textSize = key.size / 2
            val textWidth = paint.measureText(key.label)
            canvas.drawText(key.label, key.x - textWidth / 2, key.y + paint.textSize / 3, paint)
        }
    }

    private fun moveKeys() {
        if (viewWidth == 0 || viewHeight == 0) return
        val leftBound = 120f
        val rightBound = viewWidth - 60f

        for (key in keys) {
            key.x += key.vx
            key.y += key.vy

            val bounceFactor = -1f

            if (key.x < leftBound + key.size / 2) {
                key.x = leftBound + key.size / 2
                key.vx *= bounceFactor
            }
            if (key.x > rightBound - key.size / 2) {
                key.x = rightBound - key.size / 2
                key.vx *= bounceFactor
            }
            if (key.y < key.size / 2) {
                key.y = key.size / 2
                key.vy *= bounceFactor
            }
            if (key.y > viewHeight - key.size / 2) {
                key.y = viewHeight - key.size / 2
                key.vy *= bounceFactor
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            if (x < 120f) {
                when ((y / 100f).toInt()) {
                    0 -> loadKeys("LETTERS")
                    1 -> loadKeys("LOWER")
                    2 -> loadKeys("NUMBERS")
                    3 -> loadKeys("SYMBOLS")
                    4 -> loadKeys("EMOJIS")
                }
                return true
            }

            if (x > viewWidth - 100f) {
                speedFactor = 1.0f - ((y - 100f) / (viewHeight - 200f)).coerceIn(0f, 1f)
                loadKeys(currentSet)
                return true
            }

            for (key in keys) {
                val dx = x - key.x
                val dy = y - key.y
                val distance = dx * dx + dy * dy
                val radius = (key.size / 2).pow(2)

                if (distance <= radius) {
                    val ic: InputConnection? = (context as FloatingKeyboardService).currentInputConnection
                    when (key.label) {
                        "↵" -> ic?.commitText("\n", 1)
                        "_" -> ic?.commitText(" ", 1)
                        "⌫" -> ic?.deleteSurroundingText(1, 0)
                        else -> ic?.commitText(key.label, 1)
                    }
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                    break
                }
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeightDp = 300
        val density = resources.displayMetrics.density
        val desiredHeightPx = (desiredHeightDp * density).toInt()
        val heightSpec = MeasureSpec.makeMeasureSpec(desiredHeightPx, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    fun cleanUp() {
        handler.removeCallbacks(moveRunnable)
    }
}