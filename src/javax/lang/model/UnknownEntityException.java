/**
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model;

public class UnknownEntityException extends RuntimeException{
    private static final long serialVersionUID=269L;

    protected UnknownEntityException(String message){
        super(message);
    }
}
