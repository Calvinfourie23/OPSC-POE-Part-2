package com.example.opsc7311poepart2

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(private val minValue: Int, private val maxValue: Int) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = (dest?.subSequence(0, dstart).toString() + source + dest?.subSequence(dend, dest.length).toString()).toIntOrNull()
        if (input != null && input in minValue..maxValue) {
            return null
        }
        return ""
    }
}