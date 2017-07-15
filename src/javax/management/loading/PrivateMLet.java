/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

public class PrivateMLet extends MLet implements PrivateClassLoader{
    private static final long serialVersionUID=2503458973393711979L;

    public PrivateMLet(URL[] urls,boolean delegateToCLR){
        super(urls,delegateToCLR);
    }

    public PrivateMLet(URL[] urls,ClassLoader parent,boolean delegateToCLR){
        super(urls,parent,delegateToCLR);
    }

    public PrivateMLet(URL[] urls,
                       ClassLoader parent,
                       URLStreamHandlerFactory factory,
                       boolean delegateToCLR){
        super(urls,parent,factory,delegateToCLR);
    }
}
