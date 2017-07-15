/**
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class InvalidApplicationException extends Exception{
    private static final long serialVersionUID=-3048022274675537269L;
    private Object val;

    public InvalidApplicationException(Object val){
        this.val=val;
    }
}
