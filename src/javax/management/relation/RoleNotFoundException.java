/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class RoleNotFoundException extends RelationException{
    private static final long serialVersionUID=-2986406101364031481L;

    public RoleNotFoundException(){
        super();
    }

    public RoleNotFoundException(String message){
        super(message);
    }
}
