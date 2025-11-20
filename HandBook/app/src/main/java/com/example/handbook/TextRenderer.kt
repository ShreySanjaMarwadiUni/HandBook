package com.example.handbook

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import android.graphics.Typeface
import java.util.regex.Pattern

object TextRenderer {

    fun renderTags(text: String, textView: TextView) {
        val spannable = SpannableStringBuilder(text)
        val tagPattern = Pattern.compile(
            "<(b|i|u|c=\"[^\"]*\"|f=\"[^\"]*\")>(.*?)</\\1>",
            Pattern.DOTALL
        )
        val matcher = tagPattern.matcher(text)
        while (matcher.find()) {
            val fullMatch = matcher.group(0) ?: continue
            val tag = matcher.group(1)
            val innerText = matcher.group(2) ?: continue
            val start = matcher.start()
            val end = matcher.start() + innerText.length

            when {
                tag == "b" -> spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                tag == "i" -> spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                tag == "u" -> spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                tag.startsWith("c=\"") -> {
                    val colorStr = tag.substring(3, tag.length - 1)
                    try { spannable.setSpan(ForegroundColorSpan(Color.parseColor(colorStr)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) } catch(_:Exception){}
                }
                tag.startsWith("f=\"") -> {
                    val fontStr = tag.substring(3, tag.length - 1)
                    spannable.setSpan(TypefaceSpan(fontStr), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        textView.text = spannable
    }
}
