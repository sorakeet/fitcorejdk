/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.rmi.RemoteException;

@Deprecated
public class SkeletonMismatchException extends RemoteException{
    private static final long serialVersionUID=-7780460454818859281L;

    @Deprecated
    public SkeletonMismatchException(String s){
        super(s);
    }
}
