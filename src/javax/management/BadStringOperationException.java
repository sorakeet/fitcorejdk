/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class BadStringOperationException extends Exception{
    private static final long serialVersionUID=7802201238441662100L;
    private String op;

    public BadStringOperationException(String message){
        this.op=message;
    }

    public String toString(){
        return "BadStringOperationException: "+op;
    }
}
