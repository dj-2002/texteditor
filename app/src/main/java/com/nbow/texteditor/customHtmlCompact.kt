package com.nbow.texteditor

import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.Log
import androidx.core.text.HtmlCompat

class customHtmlCompact  {

    constructor(){

    }

    companion object{

        private  val TAG = "customHtmlCompact"
        val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE


        fun spannedtoHtml(spanned: Spanned):String
        {
            var result = SpannableStringBuilder()

            var next: Int

            var i = 0
            while (i < spanned.length) {

                next = spanned.nextSpanTransition(i, spanned.length, ParagraphStyle::class.java)

//                val spans =
//                    spanned.getSpans(i, next, [ParagraphStyle::class.java,CharacterStyle::class.java]
//

                val spans:Array<ParagraphStyle> =
                    spanned.getSpans(i, next, ParagraphStyle::class.java)

                var content = SpannableString(spanned.substring(i,next)).apply {
                    setSpan(spans,0,length,flag)
                }
                Log.e(TAG, "spannedtoHtml: paragraph spans length ${spans.size}")
                for (span in spans) {

                    if (span is AlignmentSpan) {
                        if(span.alignment == Layout.Alignment.ALIGN_CENTER){
                            content.removeSpan(span)
                            result.append("<div style=\"text-align:center\">").append(content).append("</div>")
                        }else if(span.alignment == Layout.Alignment.ALIGN_NORMAL){
                          // i have idea pan to jya jya span hase aej apse etle apne aena index par replace karvu padse s
                        }else if(span.alignment == Layout.Alignment.ALIGN_OPPOSITE){
                            content.removeSpan(span)
                            result.append("<div style=\"text-align:end\">").append(content).append("</div>")
                        }

                    }
                }
                if(spans.size == 0){
                    result.append(content)
                }
                i = next
            }

            var res = Utils.spannableToHtml(result)
            res = res.replace("&lt;","<").replace("&gt;",">")
            Log.e(TAG, "spannedtoHtml: res : $res", )
            return res

        }


    }

}