/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

public class ExportException extends java.rmi.RemoteException{
    private static final long serialVersionUID=-9155485338494060170L;

    public ExportException(String s){
        super(s);
    }

    public ExportException(String s,Exception ex){
        super(s,ex);
    }
}
