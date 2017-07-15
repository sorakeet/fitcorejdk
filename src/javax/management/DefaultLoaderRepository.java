/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

@Deprecated
public class DefaultLoaderRepository{
    public static Class<?> loadClass(String className)
            throws ClassNotFoundException{
        return javax.management.loading.DefaultLoaderRepository.loadClass(className);
    }

    public static Class<?> loadClassWithout(ClassLoader loader,String className)
            throws ClassNotFoundException{
        return javax.management.loading.DefaultLoaderRepository.loadClassWithout(loader,className);
    }
}
