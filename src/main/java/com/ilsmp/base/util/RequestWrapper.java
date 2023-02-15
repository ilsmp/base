package com.ilsmp.base.util;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Author: zhangjiahao04 Title: RequestWrapper Package: com.data.export.tool.util Description: 自定义wrapper，获取body Date:
 * 2021/10/31 21:15
 */
public class RequestWrapper extends ContentCachingRequestWrapper {
    private byte[] body;

    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        if (!(request instanceof RequestWrapper) && (body == null || body.length == 0)) {
            ServletUtil.remove();
            body = ServletUtil.getRequestBodyString((HttpServletRequest) getRequest()).getBytes(getCharacterEncoding());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (body == null || body.length == 0) {
            return super.getInputStream();
        }
        final ByteArrayInputStream stream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }
}
