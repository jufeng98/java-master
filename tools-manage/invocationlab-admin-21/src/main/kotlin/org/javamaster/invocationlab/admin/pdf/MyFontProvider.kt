package org.javamaster.invocationlab.admin.pdf

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import com.itextpdf.text.FontProvider
import com.itextpdf.text.pdf.BaseFont

class MyFontProvider(
    val bc: BaseColor,
    private val fontname: String,
    val encoding: String,
    val embedded: Boolean,
    val size: Float,
    val style: Int,
    val baseFont: BaseFont?
) : FontProvider {
    override fun getFont(
        arg0: String?,
        arg1: String?,
        embedded: Boolean,
        size: Float,
        arg4: Int,
        arg5: BaseColor?
    ): Font {
        val font = if (baseFont == null) {
            Font()
        } else {
            Font(baseFont)
        }
        font.color = arg5
        font.setFamily(fontname)
        font.size = size
        font.style = arg4
        return font
    }

    override fun isRegistered(arg0: String): Boolean {
        return true
    }
}