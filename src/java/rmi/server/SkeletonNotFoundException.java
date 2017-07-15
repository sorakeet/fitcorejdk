/**
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.rmi.RemoteException;

@Deprecated
public class SkeletonNotFoundException extends RemoteException{
    private static final long serialVersionUID=-7860299673822761231L;

    public SkeletonNotFoundException(String s){
        super(s);
    }

    public SkeletonNotFoundException(String s,Exception ex){
        super(s,ex);
    }
}
