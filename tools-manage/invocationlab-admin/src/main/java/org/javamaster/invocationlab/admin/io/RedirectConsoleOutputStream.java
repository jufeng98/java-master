package org.javamaster.invocationlab.admin.io;


import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author yudong
 */
@Slf4j
public class RedirectConsoleOutputStream extends PrintStream {
    private final ByteArrayOutputStream byteArrayOutputStream;

    private final PrintStream out = System.out;

    public RedirectConsoleOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        super(byteArrayOutputStream);
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    @Override
    public void println(String x) {
        out.println(x);
        super.println(x);
    }

    public byte[] getLogByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
}
