/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class ConnectException extends RemoteException{
    private static final long serialVersionUID=4863550261346652506L;

    public ConnectException(String s){
        super(s);
    }

    public ConnectException(String s,Exception ex){
        super(s,ex);
    }
}
