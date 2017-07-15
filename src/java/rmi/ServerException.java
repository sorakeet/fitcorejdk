/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class ServerException extends RemoteException{
    private static final long serialVersionUID=-4775845313121906682L;

    public ServerException(String s){
        super(s);
    }

    public ServerException(String s,Exception ex){
        super(s,ex);
    }
}
