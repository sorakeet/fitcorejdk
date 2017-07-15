/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class AccessException extends RemoteException{
    private static final long serialVersionUID=6314925228044966088L;

    public AccessException(String s){
        super(s);
    }

    public AccessException(String s,Exception ex){
        super(s,ex);
    }
}
