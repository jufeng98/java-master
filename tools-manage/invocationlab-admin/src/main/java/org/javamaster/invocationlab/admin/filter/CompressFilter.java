package org.javamaster.invocationlab.admin.filter;

import com.google.common.collect.Sets;
import lombok.Cleanup;
import org.springframework.http.HttpHeaders;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * @author yudong
 * @date 2023/8/5
 */
@WebFilter(urlPatterns = "/*")
public class CompressFilter implements Filter {
    private Set<String> excludePaths;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        res.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        String requestURI = req.getRequestURI();
        if (excludePaths.contains(requestURI)) {
            filterChain.doFilter(req, res);
            return;
        }
        if (!isGipSupported(req)) {
            filterChain.doFilter(req, res);
            return;
        }
        res.setHeader("Content-Encoding", "gzip");
        ResponseWrapper responseWrapper = new ResponseWrapper(res);
        filterChain.doFilter(req, responseWrapper);
        @Cleanup
        ServletByteArrayOutputStream originalOutputStream = (ServletByteArrayOutputStream) responseWrapper.getOutputStream();
        @Cleanup
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        @Cleanup
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(originalOutputStream.toByteArray());
        gzipOutputStream.finish();
        ServletOutputStream realOutPutStream = res.getOutputStream();
        byteArrayOutputStream.writeTo(realOutPutStream);
        realOutPutStream.flush();
    }


    private boolean isGipSupported(HttpServletRequest req) {
        String encoding = req.getHeader("Accept-Encoding");
        return encoding != null && encoding.contains("gzip");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        excludePaths = Sets.newHashSet(
                "/ncnb/project/exportErd",
                "/ncnb/queryInfo/exportSql",
                "/actuator/health"
        );
    }

}
