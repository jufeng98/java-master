package org.javamaster.invocationlab.admin.filter;


import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private final ServletByteArrayOutputStream outputStream = new ServletByteArrayOutputStream();

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(outputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String toString() {
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

}

class ServletByteArrayOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener listener) {

    }

    @Override
    public void write(int b) {
        buf.write(b);
    }

    public byte[] toByteArray() {
        return buf.toByteArray();
    }
}
