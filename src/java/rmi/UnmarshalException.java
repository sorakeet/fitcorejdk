/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class UnmarshalException extends RemoteException{
    private static final long serialVersionUID=594380845140740218L;

    public UnmarshalException(String s){
        super(s);
    }

    public UnmarshalException(String s,Exception ex){
        super(s,ex);
    }
}
