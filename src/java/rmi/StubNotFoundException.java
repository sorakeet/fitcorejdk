/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class StubNotFoundException extends RemoteException{
    private static final long serialVersionUID=-7088199405468872373L;

    public StubNotFoundException(String s){
        super(s);
    }

    public StubNotFoundException(String s,Exception ex){
        super(s,ex);
    }
}
