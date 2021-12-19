package com.nbow.texteditor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import java.util.*

class CustomEditText : AppCompatEditText {



    private var lineNumberRect: Rect
    private var lineNumberPaint: Paint
    private var enableLineNumber = false


    init {
        lineNumberRect = Rect()
        lineNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        lineNumberPaint.setStyle(Paint.Style.FILL)
        lineNumberPaint.textSize=this.textSize
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        enableLineNumber =  preferences.getBoolean("line_number",false)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){

    }

    constructor(context: Context) : super(context)


    fun setLineNumberTextSize(size:Float)
    {
        this.lineNumberPaint.textSize=size
    }

    override fun onDraw(canvas: Canvas) {
        if (enableLineNumber) {
            var baseline: Int
            val lineCount = lineCount
            var lineNumber: Int = 1
            for (i in 0 until lineCount) {
                baseline = getLineBounds(i, null)
                if (i == 0 || text!![layout.getLineStart(i) - 1] == '\n') {
                    canvas.drawText(
                        String.format(Locale.ENGLISH, " %d", lineNumber),
                        lineNumberRect.left.toFloat(),
                        baseline.toFloat(),
                        lineNumberPaint
                    )
                    lineNumber++
                }
            }
            val paddingLeft = (Math.log10(lineCount.toDouble()) + 1).toInt() * getLineNumberTextSize().toInt()
            setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }
        super.onDraw(canvas)
    }

    fun getLineNumberTextSize(): Float {
        return lineNumberPaint.textSize
    }

}