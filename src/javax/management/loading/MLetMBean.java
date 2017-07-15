/**
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;

import javax.management.ServiceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public interface MLetMBean{
    public Set<Object> getMBeansFromURL(String url)
            throws ServiceNotFoundException;

    public Set<Object> getMBeansFromURL(URL url)
            throws ServiceNotFoundException;

    public void addURL(URL url);

    public void addURL(String url) throws ServiceNotFoundException;

    public URL[] getURLs();

    public URL getResource(String name);

    public InputStream getResourceAsStream(String name);

    public Enumeration<URL> getResources(String name) throws IOException;

    public String getLibraryDirectory();

    public void setLibraryDirectory(String libdir);
}
