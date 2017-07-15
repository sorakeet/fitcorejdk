/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class NoSuchObjectException extends RemoteException{
    private static final long serialVersionUID=6619395951570472985L;

    public NoSuchObjectException(String s){
        super(s);
    }
}
