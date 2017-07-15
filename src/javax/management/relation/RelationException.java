/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import javax.management.JMException;

public class RelationException extends JMException{
    private static final long serialVersionUID=5434016005679159613L;

    public RelationException(){
        super();
    }

    public RelationException(String message){
        super(message);
    }
}
