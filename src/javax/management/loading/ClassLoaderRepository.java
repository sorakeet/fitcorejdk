/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;

public interface ClassLoaderRepository{
    public Class<?> loadClass(String className)
            throws ClassNotFoundException;

    public Class<?> loadClassWithout(ClassLoader exclude,
                                     String className)
            throws ClassNotFoundException;

    public Class<?> loadClassBefore(ClassLoader stop,
                                    String className)
            throws ClassNotFoundException;
}
