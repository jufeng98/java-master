package org.javamaster.invocationlab.admin.pdf

import com.itextpdf.text.Chunk
import com.itextpdf.text.Element
import com.itextpdf.text.pdf.codec.Base64
import com.itextpdf.tool.xml.NoCustomContextException
import com.itextpdf.tool.xml.Tag
import com.itextpdf.tool.xml.WorkerContext
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException
import com.itextpdf.tool.xml.html.HTML
import com.itextpdf.tool.xml.html.Image

class ImageTagProcessor : Image() {
    override fun end(ctx: WorkerContext, tag: Tag, currentContent: List<Element>): List<Element> {
        val attributes = tag.attributes
        val src = attributes[HTML.Attribute.SRC]
        var elements: MutableList<Element> = ArrayList(1)
        if (src.isNullOrEmpty()) {
            return elements
        }
        var img: com.itextpdf.text.Image? = null
        if (src.startsWith("data:image/")) {
            val base64Data = src.substring(src.indexOf(",") + 1)
            try {
                img = com.itextpdf.text.Image.getInstance(Base64.decode(base64Data))
            } catch (e: Exception) {
                throw RuntimeWorkerException(e)
            }
            try {
                val htmlPipelineContext = getHtmlPipelineContext(ctx)
                val chunk =
                    Chunk(cssAppliers.apply(img, tag, htmlPipelineContext) as com.itextpdf.text.Image, -50f, 0f, true)
                elements.add(cssAppliers.apply(chunk, tag, htmlPipelineContext))
            } catch (e: NoCustomContextException) {
                throw RuntimeWorkerException(e)
            }
        }
        if (img == null) {
            elements = super.end(ctx, tag, currentContent)
        }
        return elements
    }
}