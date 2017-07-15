/**
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

class NativeLibLoader{
    static void loadLibraries(){
        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>(){
                    public Void run(){
                        System.loadLibrary("awt");
                        return null;
                    }
                });
    }
}
