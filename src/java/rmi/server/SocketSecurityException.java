/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

@Deprecated
public class SocketSecurityException extends ExportException{
    private static final long serialVersionUID=-7622072999407781979L;

    public SocketSecurityException(String s){
        super(s);
    }

    public SocketSecurityException(String s,Exception ex){
        super(s,ex);
    }
}
