/**
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class ServerError extends RemoteException{
    private static final long serialVersionUID=8455284893909696482L;

    public ServerError(String s,Error err){
        super(s,err);
    }
}
