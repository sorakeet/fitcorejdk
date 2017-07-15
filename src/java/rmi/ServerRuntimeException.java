/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

@Deprecated
public class ServerRuntimeException extends RemoteException{
    private static final long serialVersionUID=7054464920481467219L;

    @Deprecated
    public ServerRuntimeException(String s,Exception ex){
        super(s,ex);
    }
}
