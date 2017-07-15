/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.util;

import javax.xml.namespace.NamespaceContext;
import java.util.Vector;

public class NamespaceContextWrapper implements NamespaceContext{
    private com.sun.org.apache.xerces.internal.xni.NamespaceContext fNamespaceContext;

    public NamespaceContextWrapper(NamespaceSupport namespaceContext){
        fNamespaceContext=namespaceContext;
    }

    public String getNamespaceURI(String prefix){
        if(prefix==null){
            throw new IllegalArgumentException("Prefix can't be null");
        }
        return fNamespaceContext.getURI(prefix.intern());
    }

    public String getPrefix(String namespaceURI){
        if(namespaceURI==null){
            throw new IllegalArgumentException("URI can't be null.");
        }
        return fNamespaceContext.getPrefix(namespaceURI.intern());
    }

    public java.util.Iterator getPrefixes(String namespaceURI){
        if(namespaceURI==null){
            throw new IllegalArgumentException("URI can't be null.");
        }else{
            Vector vector=
                    ((NamespaceSupport)fNamespaceContext).getPrefixes(namespaceURI.intern());
            return vector.iterator();
        }
    }

    public com.sun.org.apache.xerces.internal.xni.NamespaceContext getNamespaceContext(){
        return fNamespaceContext;
    }
}
