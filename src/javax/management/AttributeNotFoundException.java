/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class AttributeNotFoundException extends OperationsException{
    private static final long serialVersionUID=6511584241791106926L;

    public AttributeNotFoundException(){
        super();
    }

    public AttributeNotFoundException(String message){
        super(message);
    }
}
