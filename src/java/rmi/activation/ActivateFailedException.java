/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

public class ActivateFailedException extends java.rmi.RemoteException{
    private static final long serialVersionUID=4863550261346652506L;

    public ActivateFailedException(String s){
        super(s);
    }

    public ActivateFailedException(String s,Exception ex){
        super(s,ex);
    }
}
