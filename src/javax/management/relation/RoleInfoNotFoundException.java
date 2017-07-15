/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class RoleInfoNotFoundException extends RelationException{
    private static final long serialVersionUID=4394092234999959939L;

    public RoleInfoNotFoundException(){
        super();
    }

    public RoleInfoNotFoundException(String message){
        super(message);
    }
}
