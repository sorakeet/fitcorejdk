/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class UnexpectedException extends RemoteException{
    private static final long serialVersionUID=1800467484195073863L;

    public UnexpectedException(String s){
        super(s);
    }

    public UnexpectedException(String s,Exception ex){
        super(s,ex);
    }
}
