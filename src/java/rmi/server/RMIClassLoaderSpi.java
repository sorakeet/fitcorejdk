/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.net.MalformedURLException;

public abstract class RMIClassLoaderSpi{
    public abstract Class<?> loadClass(String codebase,String name,
                                       ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException;

    public abstract Class<?> loadProxyClass(String codebase,
                                            String[] interfaces,
                                            ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException;

    public abstract ClassLoader getClassLoader(String codebase)
            throws MalformedURLException; // SecurityException

    public abstract String getClassAnnotation(Class<?> cl);
}
