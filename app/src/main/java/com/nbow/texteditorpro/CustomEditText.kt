package com.nbow.texteditorpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ScaleGestureDetectorCompat
import androidx.preference.PreferenceManager
import java.util.*

private const val TAG = "CustomEditText"
class CustomEditText : AppCompatEditText  {



    private var lineNumberRect: Rect
    private var lineNumberPaint: Paint
    private var enableLineNumber = false
    lateinit var scaleGestureDetector: ScaleGestureDetector


    init {
        lineNumberRect = Rect()
        lineNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        lineNumberPaint.setStyle(Paint.Style.FILL)
        lineNumberPaint.textSize=this.textSize
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        enableLineNumber =  preferences.getBoolean("line_number",false)


    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        scaleGestureDetector = ScaleGestureDetector(context,ScaleListener())
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


    inner  class ScaleListener : ScaleGestureDetector.OnScaleGestureListener{
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            Log.e(TAG, "onScale: ", )
            var mfactor = 1f
            detector?.apply {
                mfactor *= detector.scaleFactor
            }
            mfactor = Math.max(0.1f,Math.min(mfactor,5.0f))

            val prevSize = this@CustomEditText.textSize
            this@CustomEditText.textSize = prevSize*mfactor
            lineNumberPaint.textSize = prevSize*mfactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            Log.e(TAG, "onScaleBegin: ", )
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            Log.e(TAG, "onScaleEnd: ", )

        }

    }

}