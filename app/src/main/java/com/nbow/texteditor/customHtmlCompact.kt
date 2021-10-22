package com.nbow.texteditor

import android.app.Application
import android.graphics.Typeface
import android.text.*
import android.text.style.*
import android.util.Log
import androidx.core.text.HtmlCompat

class customHtmlCompact {

    constructor() {

    }

    companion object {

        private val TAG = "customHtmlCompact"
        val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE



        fun spannedtoHtml(text: Spanned): String {
            var result = StringBuilder()
            val len: Int = text.length
            var next: Int
            var i = 0
            while (i < len) {
                next = text.nextSpanTransition(i, len, ParagraphStyle::class.java)
                val style: Array<ParagraphStyle> =
                    text.getSpans<ParagraphStyle>(i, next, ParagraphStyle::class.java)
                var elements = " "
                var needDiv = false
                for (j in style.indices) {
                    if (style[j] is AlignmentSpan) {
                        val align = (style[j] as AlignmentSpan).alignment
                        needDiv = true
                        elements = if (align == Layout.Alignment.ALIGN_CENTER) {
                            "style=\"text-align:center\" $elements"
                        } else if (align == Layout.Alignment.ALIGN_OPPOSITE) {
                            "style=\"text-align:end\" $elements"
                        } else {
                            "style=\"text-align:start\" $elements"
                        }
                    }
                }
                if (needDiv) {
                    result.append("<div ").append(elements).append(">")
                }


                withinDiv(result, text, i, next)


                if (needDiv) {
                    result.append("</div>")
                }
                i = next
            }



            return result.toString()

        }

        private fun withinDiv(
            out: StringBuilder, text: Spanned, start: Int, end: Int,

            ) {
            Log.e(TAG, "withinDiv: $start $end", )
            var next: Int
            var i = start
            while (i < end) {
                next = text.nextSpanTransition(i, end, QuoteSpan::class.java)
                val quotes = text.getSpans(i, next, QuoteSpan::class.java)
                for (quote in quotes) {
                    out.append("<blockquote>")
                }
                withinBlockquoteConsecutive(out, text, i, next)
                for (quote in quotes) {
                    out.append("</blockquote>\n")
                }
                i = next
            }
        }


        private fun withinBlockquoteConsecutive(
            out: StringBuilder, text: Spanned, start: Int,
            end: Int
        ) {
            Log.e("", "withinBlockquoteConsecutive: $start $end ")
                    out.append("<p>")
//
            var next: Int
            var i = start
            while (i < end) {
                next = TextUtils.indexOf(text, '\n', i, end)
                if (next < 0) {
                    next = end
                }
                var nl = 0
                while (next < end && text[next] == '\n') {
                    nl++
                    next++
                }
                withinParagraph(out, text, i, next - nl)
                if (nl == 1) {
                    out.append("<br>\n")
                } else {
                    for (j in 2 until nl) {
                        out.append("<br>")
                    }
                    if (next != end) {
                    /* Paragraph should be closed and reopened */
                        out.append("</p>\n");
                        out.append("<p>")
                    }
                }
                i = next
            }
//
        out.append("</p>\n");
        }


        private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int) {

            Log.e(TAG, "withinParagraph: $start $end", )
            var hCount = -1;

            var next: Int
            var i = start
            while (i < end) {
                next = text.nextSpanTransition(i, end, CharacterStyle::class.java)
                val style = text.getSpans(
                    i, next,
                    CharacterStyle::class.java
                )
                for (j in style.indices) {
                    if (style[j] is StyleSpan) {
                        val s = (style[j] as StyleSpan).style
                        if (s and Typeface.BOLD != 0) {
                            out.append("<b>")
                        }
                        if (s and Typeface.ITALIC != 0) {
                            out.append("<i>")
                        }
                    }
                    if (style[j] is TypefaceSpan) {
                        val s = (style[j] as TypefaceSpan).family
                        if ("monospace" == s) {
                            out.append("<tt>")
                        }
                    }
                    if (style[j] is SuperscriptSpan) {
                        out.append("<sup>")
                    }
                    if (style[j] is SubscriptSpan) {
                        out.append("<sub>")
                    }
                    if (style[j] is UnderlineSpan) {
                        out.append("<u>")
                    }
                    if (style[j] is StrikethroughSpan) {
                        out.append("<span style=\"text-decoration:line-through;\">")
                    }
                    if (style[j] is URLSpan) {
                        out.append("<a href=\"")
                        out.append((style[j] as URLSpan).url)
                        out.append("\">")
                    }
                    if (style[j] is ImageSpan) {
                        out.append("<img src=\"")
                        out.append((style[j] as ImageSpan).source)
                        out.append("\">")

                        // Don't output the dummy character underlying the image.
                        i = next
                    }
                    if (style[j] is AbsoluteSizeSpan) {
                        val s = style[j] as AbsoluteSizeSpan
                        var sizeDip = s.size.toFloat()
                        if (!s.dip) {
                            val application: Application = Application()
                            sizeDip /= application.resources.displayMetrics.density
                        }

                        // px in CSS is the equivalance of dip in Android
                        out.append(String.format("<span style=\"font-size:%.0fpx\";>", sizeDip))
                    }
                    if (style[j] is RelativeSizeSpan) {
                        val sizeEm = (style[j] as RelativeSizeSpan).sizeChange
                        if(sizeEm==Utils.heading[0]) {
                            out.append("<h1>")
                            hCount=1;
                        }
                        else if(sizeEm==Utils.heading[1]) {
                            out.append("<h2>")
                            hCount=2;
                        }
                        else if(sizeEm==Utils.heading[2]){
                            out.append("<h3>")
                            hCount=3;
                        }
                        else if(sizeEm==Utils.heading[3]) {
                            out.append("<h4>")
                            hCount=4;
                        }
                        else if(sizeEm==Utils.heading[4]) {
                            out.append("<h5>")
                            hCount=5;
                        }
                        else if(sizeEm==Utils.heading[5]) {
                            out.append("<h6>")
                            hCount=6;
                        }
                        else
                            out.append(String.format("<span style=\"font-size:%.2fem;\">", sizeEm))
                    }
                    if (style[j] is ForegroundColorSpan) {
                        val color = (style[j] as ForegroundColorSpan).foregroundColor
                        out.append(
                            String.format(
                                "<span style=\"color:#%06X;\">",
                                0xFFFFFF and color
                            )
                        )
                    }
                    if (style[j] is BackgroundColorSpan) {
                        val color = (style[j] as BackgroundColorSpan).backgroundColor
                        out.append(
                            String.format(
                                "<span style=\"background-color:#%06X;\">",
                                0xFFFFFF and color
                            )
                        )
                    }
                }
                withinStyle(out, text, i, next)
                for (j in style.indices.reversed()) {
                    if (style[j] is BackgroundColorSpan) {
                        out.append("</span>")
                    }
                    if (style[j] is ForegroundColorSpan) {
                        out.append("</span>")
                    }
                    if (style[j] is RelativeSizeSpan) {

                        if(hCount>=1 && hCount<=6)
                            out.append("</h$hCount>")
                        else
                            out.append("</span>")
                    }
                    if (style[j] is AbsoluteSizeSpan) {
                        out.append("</span>")
                    }
                    if (style[j] is URLSpan) {
                        out.append("</a>")
                    }
                    if (style[j] is StrikethroughSpan) {
                        out.append("</span>")
                    }
                    if (style[j] is UnderlineSpan) {
                        out.append("</u>")
                    }
                    if (style[j] is SubscriptSpan) {
                        out.append("</sub>")
                    }
                    if (style[j] is SuperscriptSpan) {
                        out.append("</sup>")
                    }
                    if (style[j] is TypefaceSpan) {
                        val s = (style[j] as TypefaceSpan).family
                        if (s == "monospace") {
                            out.append("</tt>")
                        }
                    }
                    if (style[j] is StyleSpan) {
                        val s = (style[j] as StyleSpan).style
                        if (s and Typeface.BOLD != 0) {
                            out.append("</b>")
                        }
                        if (s and Typeface.ITALIC != 0) {
                            out.append("</i>")
                        }
                    }
                }
                i = next
            }
        }


        private fun withinStyle(
            out: StringBuilder, text: CharSequence,
            start: Int, end: Int
        ) {
            Log.e(TAG, "withinStyle: $start $end", )
            var i = start
            while (i < end) {
                val c = text[i]
                if (c == '<') {
                    out.append("&lt;")
                } else if (c == '>') {
                    out.append("&gt;")
                } else if (c == '&') {
                    out.append("&amp;")
                } else if (c.code >= 0xD800 && c.code <= 0xDFFF) {
                    if (c.code < 0xDC00 && i + 1 < end) {
                        val d = text[i + 1]
                        if (d.code >= 0xDC00 && d.code <= 0xDFFF) {
                            i++
                            val codepoint =
                                0x010000 or (c.code - 0xD800 shl 10) or d.code - 0xDC00
                            out.append("&#").append(codepoint).append(";")
                        }
                    }
                } else if (c.code > 0x7E || c < ' ') {
                    out.append("&#").append(c.code).append(";")
                } else if (c == ' ') {
                    while (i + 1 < end && text[i + 1] == ' ') {
                        out.append("&nbsp;")
                        i++
                    }
                    out.append(' ')
                } else {
                    out.append(c)
                }
                i++
            }
        }


    }
}