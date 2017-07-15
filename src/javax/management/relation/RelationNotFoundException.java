/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class RelationNotFoundException extends RelationException{
    private static final long serialVersionUID=-3793951411158559116L;

    public RelationNotFoundException(){
        super();
    }

    public RelationNotFoundException(String message){
        super(message);
    }
}
