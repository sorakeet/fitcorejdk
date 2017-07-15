/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class InvalidRelationServiceException extends RelationException{
    private static final long serialVersionUID=3400722103759507559L;

    public InvalidRelationServiceException(){
        super();
    }

    public InvalidRelationServiceException(String message){
        super(message);
    }
}
