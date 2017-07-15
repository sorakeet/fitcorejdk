/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;
// JMX import

import javax.management.ObjectName;
import javax.management.loading.ClassLoaderRepository;

public interface ModifiableClassLoaderRepository
        extends ClassLoaderRepository{
    public void addClassLoader(ClassLoader loader);

    public void removeClassLoader(ClassLoader loader);

    public void addClassLoader(ObjectName name,ClassLoader loader);

    public void removeClassLoader(ObjectName name);

    public ClassLoader getClassLoader(ObjectName name);
}
