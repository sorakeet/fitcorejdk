/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class AlreadyBoundException extends Exception{
    private static final long serialVersionUID=9218657361741657110L;

    public AlreadyBoundException(){
        super();
    }

    public AlreadyBoundException(String s){
        super(s);
    }
}
