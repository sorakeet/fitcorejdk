/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class InvalidRoleValueException extends RelationException{
    private static final long serialVersionUID=-2066091747301983721L;

    public InvalidRoleValueException(){
        super();
    }

    public InvalidRoleValueException(String message){
        super(message);
    }
}
