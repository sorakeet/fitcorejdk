/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

public class ServerNotActiveException extends Exception{
    private static final long serialVersionUID=4687940720827538231L;

    public ServerNotActiveException(){
    }

    public ServerNotActiveException(String s){
        super(s);
    }
}
