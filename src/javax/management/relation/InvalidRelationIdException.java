/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class InvalidRelationIdException extends RelationException{
    private static final long serialVersionUID=-7115040321202754171L;

    public InvalidRelationIdException(){
        super();
    }

    public InvalidRelationIdException(String message){
        super(message);
    }
}
