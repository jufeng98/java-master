package org.javamaster.invocationlab.admin.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.pdf.BaseFont;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MyFontProvider implements FontProvider {
    private BaseColor bc;
    private String fontname;
    private String encoding;
    private boolean embedded;
    private boolean cached;
    private float size;
    private int style;
    private BaseFont baseFont;

    public MyFontProvider(BaseColor bc, String fontname, String encoding, boolean embedded, boolean cached, float size,
                          int style, BaseFont baseFont) {
        super();
        this.bc = bc;
        this.fontname = fontname;
        this.encoding = encoding;
        this.embedded = embedded;
        this.cached = cached;
        this.size = size;
        this.style = style;
        this.baseFont = baseFont;
    }

    @Override
    public Font getFont(String arg0, String arg1, boolean arg2, float arg3, int arg4, BaseColor arg5) {
        Font font;
        if (baseFont == null) {
            font = new Font();
        } else {
            font = new Font(baseFont);
        }
        font.setColor(arg5);
        font.setFamily(fontname);
        font.setSize(size);
        font.setStyle(arg4);
        return font;
    }

    @Override
    public boolean isRegistered(String arg0) {
        return true;
    }

}