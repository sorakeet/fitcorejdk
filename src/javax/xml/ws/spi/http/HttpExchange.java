/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.spi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class HttpExchange{
    public static final String REQUEST_CIPHER_SUITE=
            "javax.xml.ws.spi.http.request.cipher.suite";
    public static final String REQUEST_KEY_SIZE=
            "javax.xml.ws.spi.http.request.key.size";
    public static final String REQUEST_X509CERTIFICATE=
            "javax.xml.ws.spi.http.request.cert.X509Certificate";

    public abstract Map<String,List<String>> getRequestHeaders();

    public abstract String getRequestHeader(String name);

    public abstract Map<String,List<String>> getResponseHeaders();

    public abstract void addResponseHeader(String name,String value);

    public abstract String getRequestURI();

    public abstract String getContextPath();

    public abstract String getRequestMethod();

    public abstract HttpContext getHttpContext();

    public abstract void close() throws IOException;

    public abstract InputStream getRequestBody() throws IOException;

    public abstract OutputStream getResponseBody() throws IOException;

    public abstract void setStatus(int status);

    public abstract InetSocketAddress getRemoteAddress();

    public abstract InetSocketAddress getLocalAddress();

    public abstract String getProtocol();

    public abstract String getScheme();

    public abstract String getPathInfo();

    public abstract String getQueryString();

    public abstract Object getAttribute(String name);

    public abstract Set<String> getAttributeNames();

    public abstract Principal getUserPrincipal();

    public abstract boolean isUserInRole(String role);
}
