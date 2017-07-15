/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class RelationServiceNotRegisteredException extends RelationException{
    private static final long serialVersionUID=8454744887157122910L;

    public RelationServiceNotRegisteredException(){
        super();
    }

    public RelationServiceNotRegisteredException(String message){
        super(message);
    }
}
