/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class InvalidRelationTypeException extends RelationException{
    private static final long serialVersionUID=3007446608299169961L;

    public InvalidRelationTypeException(){
        super();
    }

    public InvalidRelationTypeException(String message){
        super(message);
    }
}
