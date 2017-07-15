/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;
// Java import

import sun.reflect.misc.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

class ObjectInputStreamWithLoader extends ObjectInputStream{
    private ClassLoader loader;

    public ObjectInputStreamWithLoader(InputStream in,ClassLoader theLoader)
            throws IOException{
        super(in);
        this.loader=theLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass aClass)
            throws IOException, ClassNotFoundException{
        if(loader==null){
            return super.resolveClass(aClass);
        }else{
            String name=aClass.getName();
            ReflectUtil.checkPackageAccess(name);
            // Query the class loader ...
            return Class.forName(name,false,loader);
        }
    }
}
