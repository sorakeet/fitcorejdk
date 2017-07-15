/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

public class RelationTypeNotFoundException extends RelationException{
    private static final long serialVersionUID=1274155316284300752L;

    public RelationTypeNotFoundException(){
        super();
    }

    public RelationTypeNotFoundException(String message){
        super(message);
    }
}
