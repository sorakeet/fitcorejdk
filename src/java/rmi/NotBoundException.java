/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

public class NotBoundException extends Exception{
    private static final long serialVersionUID=-1857741824849069317L;

    public NotBoundException(){
        super();
    }

    public NotBoundException(String s){
        super(s);
    }
}
