package com.nbow.texteditorpro

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.*
import android.text.Html.ImageGetter
import android.text.Html.TagHandler
import android.text.style.*
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.StringReader
import java.lang.RuntimeException
import java.util.*
import java.util.regex.Pattern
import org.ccil.cowan.tagsoup.HTMLSchema
import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.*
import java.lang.NumberFormatException
import kotlin.collections.HashMap


class CustomHtmlCompact {

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
                val style: Array<ParagraphStyle> = text.getSpans<ParagraphStyle>(i, next, ParagraphStyle::class.java)
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



                withinDiv(result, text, i, next,needDiv)

                if (needDiv) {
                    result.append("</div>")
                }


                i = next
            }



            return result.toString()

        }




        private fun withinDiv(
            out: StringBuilder, text: Spanned, start: Int, end: Int,needDiv:Boolean = false
        ) {
            Log.e(TAG, "withinDiv: $start $end")
            var next: Int
            var i = start
            while (i < end) {
                next = text.nextSpanTransition(i, end, QuoteSpan::class.java)
                val quotes = text.getSpans(i, next, QuoteSpan::class.java)
                for (quote in quotes) {
                    out.append("<blockquote>")
                }
                withinBlockquoteConsecutive(out, text, i, next,needDiv)
                for (quote in quotes) {
                    out.append("</blockquote>\n")
                }
                i = next
            }
        }


        private fun withinBlockquoteConsecutive(
            out: StringBuilder, text: Spanned, start: Int,
            end: Int,needDiv:Boolean = false
        ) {
            Log.e("", "withinBlockquoteConsecutive: $start $end ")
//            out.append("<p>")
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
                Log.e(TAG, "withinBlockquoteConsecutive: new lines : $nl")
                val spans = text.getSpans(i,next-nl,RelativeSizeSpan::class.java)
                var isHeading = false
                for(span in spans){
                    if(span is RelativeSizeSpan){
                        if(span.sizeChange == Utils.heading[0]){
                            out.append("<h1>")
                            isHeading = true
                        }else if(span.sizeChange == Utils.heading[1]){
                            out.append("<h2>")
                            isHeading = true
                        }else if(span.sizeChange == Utils.heading[2]){
                            out.append("<h3>")
                            isHeading = true
                        }else if(span.sizeChange == Utils.heading[3]){
                            out.append("<h4>")
                            isHeading = true
                        }else if(span.sizeChange == Utils.heading[4]){
                            out.append("<h5>")
                            isHeading = true
                        }else if(span.sizeChange == Utils.heading[5]){
                            out.append("<h6>")
                            isHeading = true
                        }
                    }
                }

                val spans2 = text.getSpans(i,next-nl,BulletSpan::class.java)
                var isBullets =false

                    for (span in spans2) {
                        if (span is BulletSpan) {
                            if(i != next-nl) {
                                out.append("<ul><li>")
                            }
                            isBullets = true
                            break
                        }
                    }

                withinParagraph(out, text, i, next - nl,isHeading)


                for(span in spans){
                    if(span is RelativeSizeSpan){
                        if(span.sizeChange == Utils.heading[0]){
                            out.append("</h1>")
                        }else if(span.sizeChange == Utils.heading[1]){
                            out.append("</h2>")
                        }else if(span.sizeChange == Utils.heading[2]){
                            out.append("</h3>")
                        }else if(span.sizeChange == Utils.heading[3]){
                            out.append("</h4>")
                        }else if(span.sizeChange == Utils.heading[4]){
                            out.append("</h5>")
                        }else if(span.sizeChange == Utils.heading[5]){
                            out.append("</h6>")
                        }
                    }
                }
                if(i!=next-nl) {
                    for (span in spans2) {
                        if (span is BulletSpan) {
                            out.append("</li></ul>")
                        }
                        break
                    }
                }
                if(isHeading || needDiv || isBullets){
                    nl--
                }

                if (nl == 1) {
                    out.append("<br>\n")
                } else {
                    for (j in 1..nl) {
                        out.append("<br>")
                    }
//                    if (next != end) {
//                        /* Paragraph should be closed and reopened */
////                        out.append("</p>\n");
////                        out.append("<p>")
//                    }
                }
                i = next
            }
//
//            out.append("</p>\n");
        }


        private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int,isHeading : Boolean = false) {

            Log.e(TAG, "withinParagraph: $start $end")
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

                    if (style[j] is TypefaceSpan) {
                        val s = (style[j] as TypefaceSpan).family
                        if ("monospace" == s) {
                            out.append("<tt>")
                        }


                    }
                    if(style[j] is  CustomTypefaceSpan){
                        val s1=(style[j] as CustomTypefaceSpan).name
                        out.append("<span style=\"font-family:${s1}\">")
                    }

                    if (style[j] is StyleSpan) {
                        val s = (style[j] as StyleSpan).style
                        if (s and Typeface.BOLD != 0) {
                            if(!isHeading)
                                out.append("<b>")
                        }
                        if (s and Typeface.ITALIC != 0) {
                            out.append("<i>")
                        }
                    }
                    if (style[j] is SuperscriptSpan) {
                        out.append("<sup>")
                    }
                    if (style[j] is SubscriptSpan) {
                        out.append("<sub>")
                    }
                    if (style[j] is CustomUnderlineSpan) {
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

                        if(sizeEm==0.8f)
                        {
                            out.append("<small>")
                        }
                        else if(sizeEm==1.25f)
                        {
                            out.append("<big>")
                        }

                        else if(!(sizeEm==Utils.heading[0] || sizeEm==Utils.heading[1] || sizeEm==Utils.heading[2] || sizeEm==Utils.heading[3] || sizeEm==Utils.heading[4] || sizeEm==Utils.heading[5])) {
                            out.append(String.format("<span style=\"font-size:%.2fem;\">", sizeEm))
                            hCount = 0
                        }




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

                        val sizeEm = (style[j] as RelativeSizeSpan).sizeChange
                        if(sizeEm==0.8f)
                        {
                            out.append("</small>")
                        }
                        else if(sizeEm==1.25f)
                        {
                            out.append("</big>")
                        }
                        else if(hCount==0) {
                            out.append("</span>")
                            hCount = -1
                        }

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
                    if (style[j] is CustomUnderlineSpan) {
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
//

                    }
                    if(style[j] is  CustomTypefaceSpan){
                        out.append("</span>")
                        //out.append("</p>")
                    }
                    if (style[j] is StyleSpan) {
                        val s = (style[j] as StyleSpan).style
                        if (s and Typeface.BOLD != 0) {
                            if(!isHeading)
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
            Log.e(TAG, "withinStyle: $start $end")
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

        // Html to spannable
        // html to spannable starts from here

        fun fromHtml(
            context: Context,
            source: String, flags: Int, imageGetter: ImageGetter?,
            tagHandler: TagHandler?
        ): Spanned {
            val parser = Parser()
            try {
                parser.setProperty(Parser.schemaProperty, HTMLSchema())
            } catch (e: SAXNotRecognizedException) {
                // Should not happen.
                throw RuntimeException(e)
            } catch (e: SAXNotSupportedException) {
                // Should not happen.
                throw RuntimeException(e)
            }
            val converter: HtmlToSpannedConverter =
                HtmlToSpannedConverter(context,source, imageGetter, tagHandler, parser, flags)
            return converter.convert()
        }

    }

}

internal class HtmlToSpannedConverter(

    private val context: Context, private val mSource: String,
    imageGetter: ImageGetter?, tagHandler: TagHandler?, parser: Parser, flags: Int
) :
    ContentHandler {
    private val mReader: XMLReader
    private val mSpannableStringBuilder: SpannableStringBuilder
    private val mImageGetter: ImageGetter?
    private val mTagHandler: TagHandler?
    private val mFlags: Int
    private var sColorNameMap: HashMap<String, Int> = HashMap()

    @ColorInt
    val BLACK = -0x1000000
    @ColorInt
    val DKGRAY = -0xbbbbbc
    @ColorInt
    val GRAY = -0x777778

    @ColorInt
    val LTGRAY = -0x333334

    @ColorInt
    val WHITE = -0x1

    @ColorInt
    val RED = -0x10000

    @ColorInt
    val GREEN = -0xff0100

    @ColorInt
    val BLUE = -0xffff01

    @ColorInt
    val YELLOW = -0x100

    @ColorInt
    val CYAN = -0xff0001

    @ColorInt
    val MAGENTA = -0xff01

//    val AQUA =

    @ColorInt
    val TRANSPARENT = 0
    init
    {
//        sColorNameMap = new HashMap < > ()
        sColorNameMap["black"] = BLACK
        sColorNameMap["darkgray"] = DKGRAY
        sColorNameMap["gray"] = GRAY
        sColorNameMap["lightgray"] = LTGRAY
        sColorNameMap["white"] = WHITE
        sColorNameMap["red"] = RED
        sColorNameMap["green"] = GREEN
        sColorNameMap["blue"] = BLUE
        sColorNameMap["yellow"] = YELLOW
        sColorNameMap["cyan"] = CYAN
        sColorNameMap["magenta"] = MAGENTA
        sColorNameMap.put("aqua", 0xFF00FFFF.toInt())
        sColorNameMap["fuchsia"] = 0xFFFF00FF.toInt()
        sColorNameMap["darkgrey"] = DKGRAY
        sColorNameMap["grey"] = GRAY
        sColorNameMap["lightgrey"] = LTGRAY
        sColorNameMap["lime"] = 0xFF00FF00.toInt()
        sColorNameMap["maroon"] = 0xFF800000.toInt()
        sColorNameMap["navy"] = 0xFF000080.toInt()
        sColorNameMap["olive"] = 0xFF808000.toInt()
        sColorNameMap["purple"] = 0xFF800080.toInt()
        sColorNameMap["silver"] = 0xFFC0C0C0.toInt()
        sColorNameMap["teal"] = 0xFF008080.toInt()

    }

    companion object {
        private val HEADING_SIZES = floatArrayOf(
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f
        )
        private var sTextAlignPattern: Pattern? = null
        private var sForegroundColorPattern: Pattern? = null
        private var sBackgroundColorPattern: Pattern? = null
        private var sTextDecorationPattern: Pattern? = null
        private var sFontFamilyPattern: Pattern? = null

        /**
         * Name-value mapping of HTML/CSS colors which have different values in [Color].
         */
        private val sColorMap: MutableMap<String, Int> = HashMap()
        private val textAlignPattern: Pattern?
            private get() {
                if (sTextAlignPattern == null) {
                    sTextAlignPattern = Pattern.compile("(?:\\s+|\\A)text-align\\s*:\\s*(\\S*)\\b")
                }
                return sTextAlignPattern
            }
        private val foregroundColorPattern: Pattern?
            private get() {
                if (sForegroundColorPattern == null) {
                    sForegroundColorPattern = Pattern.compile(
                        "(?:\\s+|\\A)color\\s*:\\s*(\\S*)\\b"
                    )
                }
                return sForegroundColorPattern
            }
        private val backgroundColorPattern: Pattern?
            private get() {
                if (sBackgroundColorPattern == null) {
                    sBackgroundColorPattern = Pattern.compile(
                        "(?:\\s+|\\A)background(?:-color)?\\s*:\\s*(\\S*)\\b"
                    )
                }
                return sBackgroundColorPattern
            }
        private val textDecorationPattern: Pattern?
            private get() {
                if (sTextDecorationPattern == null) {
                    sTextDecorationPattern = Pattern.compile(
                        "(?:\\s+|\\A)text-decoration\\s*:\\s*(\\S*)\\b"
                    )
                }
                return sTextDecorationPattern
            }
        private val fontFamilyPattern: Pattern?
            private get() {
                if (sFontFamilyPattern == null) {
                    sFontFamilyPattern = Pattern.compile(
                        "(?:\\s+|\\A)font-family\\s*:\\s*(\\S*)\\b"
                    )
                }
                return sFontFamilyPattern
            }

        private fun appendNewlines(text: Editable, minNewline: Int) {
            val len = text.length
            if (len == 0) {
                return
            }
            var existingNewlines = 0
            var i = len - 1
            while (i >= 0 && text[i] == '\n') {
                existingNewlines++
                i--
            }
            for (j in existingNewlines until minNewline) {
                text.append("\n")
            }
        }

        private fun startBlockElement(text: Editable, attributes: Attributes, margin: Int) {
            val len = text.length
            if (margin > 0) {
                appendNewlines(text, margin)
                start(text, Newline(margin))
            }
            val style = attributes.getValue("", "style")
            if (style != null) {
                val m = textAlignPattern!!.matcher(style)
                if (m.find()) {
                    val alignment = m.group(1)
                    if (alignment.equals("start", ignoreCase = true)) {
                        start(text, Alignment(Layout.Alignment.ALIGN_NORMAL))
                    } else if (alignment.equals("center", ignoreCase = true)) {
                        start(text, Alignment(Layout.Alignment.ALIGN_CENTER))
                    } else if (alignment.equals("end", ignoreCase = true)) {
                        start(text, Alignment(Layout.Alignment.ALIGN_OPPOSITE))
                    }
                }
            }
        }

        private fun endBlockElement(text: Editable) {
            val n = getLast(
                text,
                Newline::class.java
            )
            if (n != null) {
                appendNewlines(text, n.mNumNewlines)
                text.removeSpan(n)
            }
            val a = getLast(
                text,
                Alignment::class.java
            )
            if (a != null) {
                setSpanFromMark(text, a, AlignmentSpan.Standard(a.mAlignment))
            }
        }

        private fun handleBr(text: Editable) {
            text.append('\n')
        }

        private fun endLi(text: Editable) {
            endCssStyle(text)
            endBlockElement(text)
            end(
                text,
                Bullet::class.java, BulletSpan()
            )
        }

        private fun endBlockquote(text: Editable) {
            endBlockElement(text)
            end(
                text,
                Blockquote::class.java, QuoteSpan()
            )
        }

        private fun endHeading(text: Editable) {
            // RelativeSizeSpan and StyleSpan are CharacterStyles
            // Their ranges should not include the newlines at the end
            val h = getLast(
                text,
                Heading::class.java
            )
            if (h != null) {
                setSpanFromMark(
                    text, h, RelativeSizeSpan(
                        HEADING_SIZES[h.mLevel]
                    ),
                    StyleSpan(Typeface.BOLD)
                )
            }
            endBlockElement(text)
        }

        private fun <T> getLast(text: Spanned, kind: Class<T>): T? {
            /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
            val objs = text.getSpans(0, text.length, kind)
            return if (objs.size == 0) {
                null
            } else {
                objs[objs.size - 1]
            }
        }

        private fun setSpanFromMark(text: Spannable, mark: Any, vararg spans: Any) {
            val where = text.getSpanStart(mark)
            text.removeSpan(mark)
            val len = text.length
            Log.e("set span from mark TAG", "setSpanFromMark: where : $where len : $len text : $text" )
            if (where != len) {
                var isCustomTypeface = false
                for (span in spans) {
                    text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    if(span is CustomTypefaceSpan){
                        isCustomTypeface = true
                    }
                }
                if(isCustomTypeface){
                    val styleSpans = text.getSpans(where, len, StyleSpan::class.java)
                    var isB = false
                    var isI = false
                    for (ss in styleSpans) {
                        val style = (ss as StyleSpan).style
                        if (style == Typeface.BOLD) {
                            text.removeSpan(ss)
                            isB = true
                            Log.e("TAG", "setSpanFromMark: custome span bold", )
                        }
                        if(style == Typeface.ITALIC){
                            text.removeSpan(ss)
                            isI = true
                        }
                    }
                    if (isB) text.setSpan(StyleSpan(Typeface.BOLD), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    if(isI) text.setSpan(StyleSpan(Typeface.ITALIC), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                }
            }

        }

        private fun start(text: Editable, mark: Any) {
            val len = text.length
            text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        private fun end(text: Editable, kind: Class<*>, repl: Any) {
            val len = text.length
            val obj = getLast(text, kind)
            if (obj != null) {
                setSpanFromMark(text, obj, repl)
            }
        }

        private fun endCssStyle(text: Editable) {
            Log.e("end css style", "endCssStyle:", )
            val ff = getLast(
                text,
                CustomTypefaceSpan::class.java
            )
            if(ff!=null){
                setSpanFromMark(text, ff, CustomTypefaceSpan(ff.typeface,ff.name))

//                val start = (text as Spannable).getSpanStart(ff as Any)
//                val end = text.length
//                Log.e("endCssStyle", "endCssStyle: start : $start end : $end")
//                if(start!=end && start != -1) {
//
//                }
            }

            val s = getLast(
                text,
                Strikethrough::class.java
            )
            if (s != null) {
                setSpanFromMark(text, s, StrikethroughSpan())
            }
            val b = getLast(
                text,
                Background::class.java
            )
            if (b != null) {
                setSpanFromMark(text, b, BackgroundColorSpan(b.mBackgroundColor))
            }
            val f = getLast(
                text,
                Foreground::class.java
            )
            if (f != null) {
                setSpanFromMark(text, f, ForegroundColorSpan(f.mForegroundColor))
            }

        }

        private fun startImg(context: Context,text: Editable, attributes: Attributes, img: ImageGetter?) {
            val src = attributes.getValue("", "src")
            var d: Drawable? = null
            if (img != null) {
                d = img.getDrawable(src)
            }
            if (d == null) {
                d = ContextCompat.getDrawable(context,R.drawable.ic_color_text)
                d!!.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            }
            val len = text.length
            text.append("\uFFFC")
            text.setSpan(
                ImageSpan(d, src), len, text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        private fun endFont(text: Editable) {
            val font = getLast(
                text,
                Font::class.java
            )
            if (font != null) {
                setSpanFromMark(text, font, TypefaceSpan(font.mFace))
            }
            val foreground = getLast(
                text,
                Foreground::class.java
            )
            if (foreground != null) {
                setSpanFromMark(
                    text, foreground,
                    ForegroundColorSpan(foreground.mForegroundColor)
                )
            }
        }

        private fun startA(text: Editable, attributes: Attributes) {
            val href = attributes.getValue("", "href")
            start(text, Href(href))
        }

        private fun endA(text: Editable) {
            val h = getLast(
                text,
                Href::class.java
            )
            if (h != null) {
                if (h.mHref != null) {
                    setSpanFromMark(text, h, URLSpan(h.mHref))
                }
            }
        }

        init {
//            sColorMap = HashMap()
            sColorMap["darkgray"] = -0x565657
            sColorMap["gray"] = -0x7f7f80
            sColorMap["lightgray"] = -0x2c2c2d
            sColorMap["darkgrey"] = -0x565657
            sColorMap["grey"] = -0x7f7f80
            sColorMap["lightgrey"] = -0x2c2c2d
            sColorMap["green"] = -0xff8000
        }
    }

    fun convert(): Spanned {
        mReader.contentHandler = this
        try {
            mReader.parse(InputSource(StringReader(mSource)))
        } catch (e: IOException) {
            // We are reading from a string. There should not be IO problems.
            throw RuntimeException(e)
        } catch (e: SAXException) {
            // TagSoup doesn't throw parse exceptions.
            throw RuntimeException(e)
        }

        // Fix flags and range for paragraph-type markup.
        val obj = mSpannableStringBuilder.getSpans(
            0, mSpannableStringBuilder.length,
            ParagraphStyle::class.java
        )
        for (i in obj.indices) {
            val start = mSpannableStringBuilder.getSpanStart(obj[i])
            var end = mSpannableStringBuilder.getSpanEnd(obj[i])

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (mSpannableStringBuilder[end - 1] == '\n' &&
                    mSpannableStringBuilder[end - 2] == '\n'
                ) {
                    end--
                }
            }
            if (end == start) {
                mSpannableStringBuilder.removeSpan(obj[i])
            } else {
                mSpannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH)
            }
        }
        return mSpannableStringBuilder
    }

    private fun handleStartTag(tag: String, attributes: Attributes) {
        if (tag.equals("br", ignoreCase = true)) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equals("p", ignoreCase = true)) {
            startBlockElement(
                mSpannableStringBuilder, attributes,
                marginParagraph
            )
            startCssStyle(mSpannableStringBuilder, attributes)
        } else if (tag.equals("ul", ignoreCase = true)) {
            startBlockElement(
                mSpannableStringBuilder, attributes,
                1
            )
        } else if (tag.equals("li", ignoreCase = true)) {
            startLi(mSpannableStringBuilder, attributes)
        } else if (tag.equals("div", ignoreCase = true)) {
            startBlockElement(
                mSpannableStringBuilder, attributes,
                1 // marginDiv
            )
        } else if (tag.equals("span", ignoreCase = true)) {
            startCssStyle(mSpannableStringBuilder, attributes)
        } else if (tag.equals("strong", ignoreCase = true)) {
            start(mSpannableStringBuilder, Bold())
        } else if (tag.equals("b", ignoreCase = true)) {
            start(mSpannableStringBuilder, Bold())
        } else if (tag.equals("em", ignoreCase = true)) {
            start(mSpannableStringBuilder, Italic())
        } else if (tag.equals("cite", ignoreCase = true)) {
            start(mSpannableStringBuilder, Italic())
        } else if (tag.equals("dfn", ignoreCase = true)) {
            start(mSpannableStringBuilder, Italic())
        } else if (tag.equals("i", ignoreCase = true)) {
            start(mSpannableStringBuilder, Italic())
        } else if (tag.equals("big", ignoreCase = true)) {
            start(mSpannableStringBuilder, Big())
        } else if (tag.equals("small", ignoreCase = true)) {
            start(mSpannableStringBuilder, Small())
        } else if (tag.equals("font", ignoreCase = true)) {
            startFont(mSpannableStringBuilder, attributes)
        } else if (tag.equals("blockquote", ignoreCase = true)) {
            startBlockquote(mSpannableStringBuilder, attributes)
        } else if (tag.equals("tt", ignoreCase = true)) {
            start(mSpannableStringBuilder, Monospace())
        } else if (tag.equals("a", ignoreCase = true)) {
            startA(mSpannableStringBuilder, attributes)
        } else if (tag.equals("u", ignoreCase = true)) {
            start(mSpannableStringBuilder, Underline())
        } else if (tag.equals("del", ignoreCase = true)) {
            start(mSpannableStringBuilder, Strikethrough())
        } else if (tag.equals("s", ignoreCase = true)) {
            start(mSpannableStringBuilder, Strikethrough())
        } else if (tag.equals("strike", ignoreCase = true)) {
            start(mSpannableStringBuilder, Strikethrough())
        } else if (tag.equals("sup", ignoreCase = true)) {
            start(mSpannableStringBuilder, Super())
        } else if (tag.equals("sub", ignoreCase = true)) {
            start(mSpannableStringBuilder, Sub())

        } else if (tag.length == 2 && Character.toLowerCase(tag[0]) == 'h' && tag[1] >= '1' && tag[1] <= '6') {
            startHeading(mSpannableStringBuilder, attributes, tag[1] - '1')
        } else if (tag.equals("img", ignoreCase = true)) {
            startImg(context,mSpannableStringBuilder, attributes, mImageGetter)
        } else mTagHandler?.handleTag(true, tag, mSpannableStringBuilder, mReader)
    }

    private fun handleEndTag(tag: String) {
        if (tag.equals("br", ignoreCase = true)) {
            handleBr(mSpannableStringBuilder)
        } else if (tag.equals("p", ignoreCase = true)) {
            Log.e("handleEndTag TAG", "handleEndTag: from p end css style", )
            endCssStyle(mSpannableStringBuilder)
            endBlockElement(mSpannableStringBuilder)
        } else if (tag.equals("ul", ignoreCase = true)) {
            endBlockElement(mSpannableStringBuilder)
        } else if (tag.equals("li", ignoreCase = true)) {
            endLi(mSpannableStringBuilder)
        } else if (tag.equals("div", ignoreCase = true)) {
            endBlockElement(mSpannableStringBuilder)
        } else if (tag.equals("span", ignoreCase = true)) {
            Log.e("handleEndTag TAG", "handleEndTag: from span endCssStyle", )
            endCssStyle(mSpannableStringBuilder)
        } else if (tag.equals("strong", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Bold::class.java, StyleSpan(Typeface.BOLD)
            )
        } else if (tag.equals("b", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Bold::class.java, StyleSpan(Typeface.BOLD)
            )
        } else if (tag.equals("em", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Italic::class.java, StyleSpan(Typeface.ITALIC)
            )
        } else if (tag.equals("cite", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Italic::class.java, StyleSpan(Typeface.ITALIC)
            )
        } else if (tag.equals("dfn", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Italic::class.java, StyleSpan(Typeface.ITALIC)
            )
        } else if (tag.equals("i", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Italic::class.java, StyleSpan(Typeface.ITALIC)
            )
        } else if (tag.equals("big", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Big::class.java, RelativeSizeSpan(1.25f)
            )
        } else if (tag.equals("small", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Small::class.java, RelativeSizeSpan(0.8f)
            )
        } else if (tag.equals("font", ignoreCase = true)) {
            endFont(mSpannableStringBuilder)
        } else if (tag.equals("blockquote", ignoreCase = true)) {
            endBlockquote(mSpannableStringBuilder)
        } else if (tag.equals("tt", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Monospace::class.java, TypefaceSpan("monospace")
            )
        } else if (tag.equals("a", ignoreCase = true)) {
            endA(mSpannableStringBuilder)
        } else if (tag.equals("u", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Underline::class.java, CustomUnderlineSpan()
            )
        } else if (tag.equals("del", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Strikethrough::class.java, StrikethroughSpan()
            )
        } else if (tag.equals("s", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Strikethrough::class.java, StrikethroughSpan()
            )
        } else if (tag.equals("strike", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Strikethrough::class.java, StrikethroughSpan()
            )
        } else if (tag.equals("sup", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Super::class.java, SuperscriptSpan()
            )
        } else if (tag.equals("sub", ignoreCase = true)) {
            end(
                mSpannableStringBuilder,
                Sub::class.java, SubscriptSpan()
            )
        } else if (tag.length == 2 && Character.toLowerCase(tag[0]) == 'h' && tag[1] >= '1' && tag[1] <= '6') {
            endHeading(mSpannableStringBuilder)
        } else mTagHandler?.handleTag(false, tag, mSpannableStringBuilder, mReader)
    }

    private val marginParagraph: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
    private val marginHeading: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING)
    private val marginListItem: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM)
    private val marginList: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST)
    private val marginDiv: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV)
    private val marginBlockquote: Int
        private get() = getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE)

    /**
     * Returns the minimum number of newline characters needed before and after a given block-level
     * element.
     *
     * @param flag the corresponding option flag defined in [Html] of a block-level element
     */
    private fun getMargin(flag: Int): Int {
        return if (flag and mFlags != 0) {
            1
        } else 2
    }

    private fun startLi(text: Editable, attributes: Attributes) {
        startBlockElement(text, attributes, 1)  // marginListItem)
        start(text, Bullet())
        startCssStyle(text, attributes)
    }

    private fun startBlockquote(text: Editable, attributes: Attributes) {
        startBlockElement(text, attributes, marginBlockquote)
        start(text, Blockquote())
    }

    private fun startHeading(text: Editable, attributes: Attributes, level: Int) {
        startBlockElement(text, attributes, 1) // marginHeading)
        start(text, Heading(level))
    }

    private fun startCssStyle(text: Editable, attributes: Attributes) {
        val style = attributes.getValue("", "style")
        if (style != null) {
            var m = foregroundColorPattern!!.matcher(style)
            if (m.find()) {
                val c = getHtmlColor(m.group(1))
                if (c != -1) {
                    start(text, Foreground(c or -0x1000000))
                }
            }
            m = backgroundColorPattern!!.matcher(style)
            if (m.find()) {
                val c = getHtmlColor(m.group(1))
                if (c != -1) {
                    start(text, Background(c or -0x1000000))
                }
            }
            m = textDecorationPattern!!.matcher(style)
            if (m.find()) {
                val textDecoration = m.group(1)
                if (textDecoration.equals("line-through", ignoreCase = true)) {
                    start(text, Strikethrough())
                }
            }
            m = fontFamilyPattern!!.matcher(style)
            if (m.find()) {
                //Log.e("fontfamily-pattern", "startCssStyle: fontfamilypatter matcher inside find and text = $text", )
                val textDecoration = m.group(1)
                Log.e("fontfamily-pattern", "startCssStyle:new font found ${textDecoration}", )
                if (textDecoration.equals(Utils.arial, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.arial)
                    start(text,CustomTypefaceSpan(myTypeface,Utils.arial) )
                }
                else if (textDecoration.equals(Utils.georgia, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.georgia)
                    start(text,CustomTypefaceSpan(myTypeface,Utils.georgia) )
                }
                else if (textDecoration.equals(Utils.verdana, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.verdana)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.verdana) )
                }
                else if (textDecoration.equals(Utils.helvetica, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.helvetica)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.helvetica) )
                }
                else if (Utils.courier.startsWith(textDecoration, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.courier)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.courier) )
                }
                else if (Utils.timesnew.startsWith(textDecoration, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.timesnew)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.timesnew) )
                }
                else if (Utils.trebuchet.startsWith(textDecoration, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.trebuchet)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.trebuchet) )
                }
                else if (Utils.brushscript.startsWith(textDecoration, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.brushscript)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.brushscript) )
                }
                else if (textDecoration.equals(Utils.tahoma, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.tahoma)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.tahoma) )
                }
                else if (textDecoration.equals(Utils.garamond, ignoreCase = true)) {
                    val myTypeface = Utils.getTypefaceFromName(context,Utils.garamond)

                    start(text,CustomTypefaceSpan(myTypeface,Utils.garamond) )
                }

                else if (textDecoration.equals("default", ignoreCase = true)) {

                    start(text,CustomTypefaceSpan(Typeface.DEFAULT,"default") )
                }

            }

        }
    }

    private fun startFont(text: Editable, attributes: Attributes) {
        val color = attributes.getValue("", "color")
        val face = attributes.getValue("", "face")
        if (!TextUtils.isEmpty(color)) {
            val c = getHtmlColor(color)
            if (c != -1) {
                start(text, Foreground(c or -0x1000000))
            }
        }
        if (!TextUtils.isEmpty(face)) {
            start(text, Font(face))
        }
    }

    private fun getHtmlColor(color: String): Int {
        if (mFlags and Html.FROM_HTML_OPTION_USE_CSS_COLORS
            == Html.FROM_HTML_OPTION_USE_CSS_COLORS
        ) {
            val i = sColorMap!![color.toLowerCase(Locale.US)]
            if (i != null) {
                return i
            }
        }
        val i: Int?= sColorNameMap.get(color.toLowerCase(Locale.ROOT))
        return i
            ?: try {
                Utils.convertValueToInt(color, -1)
            } catch (nfe: NumberFormatException) {
                -1
            }
    }

    override fun setDocumentLocator(locator: Locator) {}

    @Throws(SAXException::class)
    override fun startDocument() {
    }

    @Throws(SAXException::class)
    override fun endDocument() {
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String, uri: String) {
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String) {
    }

    @Throws(SAXException::class)
    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes
    ) {
        handleStartTag(localName, attributes)
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        handleEndTag(localName)
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        val sb = java.lang.StringBuilder()

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */for (i in 0 until length) {
            val c = ch[i + start]
            if (c == ' ' || c == '\n') {
                var pred: Char
                var len = sb.length
                if (len == 0) {
                    len = mSpannableStringBuilder.length
                    pred = if (len == 0) {
                        '\n'
                    } else {
                        mSpannableStringBuilder[len - 1]
                    }
                } else {
                    pred = sb[len - 1]
                }
                if (pred != ' ' && pred != '\n') {
                    sb.append(' ')
                }
            } else {
                sb.append(c)
            }
        }
        mSpannableStringBuilder.append(sb)
    }

    @Throws(SAXException::class)
    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String, data: String) {
    }

    @Throws(SAXException::class)
    override fun skippedEntity(name: String) {
    }

    private class Bold
    private class Italic
    private class Underline
    private class Strikethrough
    private class Big
    private class Small
    private class Monospace
    private class Blockquote
    private class Super
    private class Sub
    private class Bullet
    private class Font(var mFace: String)
    private class Href(var mHref: String?)
    private class Foreground(val mForegroundColor: Int)
    private class Background(val mBackgroundColor: Int)
    private class Heading(val mLevel: Int)
    private class Newline(val mNumNewlines: Int)
    private class Alignment(val mAlignment: Layout.Alignment)

    init {
        mSpannableStringBuilder = SpannableStringBuilder()
        mImageGetter = imageGetter
        mTagHandler = tagHandler
        mReader = parser
        mFlags = flags
    }
}