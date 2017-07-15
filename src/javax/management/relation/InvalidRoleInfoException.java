/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class InvalidRoleInfoException extends RelationException{
    private static final long serialVersionUID=7517834705158932074L;

    public InvalidRoleInfoException(){
        super();
    }

    public InvalidRoleInfoException(String message){
        super(message);
    }
}
