package com.nbow.texteditor;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan {

    public final Typeface typeface;
    public final String name;

    public CustomTypefaceSpan(Typeface typeface,String name) {
        this.typeface = typeface;
        this.name=name;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, typeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, typeface);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        paint.setTypeface(tf);
    }


}
