/**
 * Copyright (c) 2002, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.interceptor;

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;

public interface MBeanServerInterceptor extends MBeanServer{
    public Object instantiate(String className)
            throws ReflectionException, MBeanException;

    public Object instantiate(String className,ObjectName loaderName)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException;

    public Object instantiate(String className,Object[] params,
                              String[] signature) throws ReflectionException, MBeanException;

    public Object instantiate(String className,ObjectName loaderName,
                              Object[] params,String[] signature)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException;

    @Deprecated
    public ObjectInputStream deserialize(ObjectName name,byte[] data)
            throws InstanceNotFoundException, OperationsException;

    @Deprecated
    public ObjectInputStream deserialize(String className,byte[] data)
            throws OperationsException, ReflectionException;

    @Deprecated
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName,byte[] data)
            throws InstanceNotFoundException, OperationsException,
            ReflectionException;

    public ClassLoaderRepository getClassLoaderRepository();
}

