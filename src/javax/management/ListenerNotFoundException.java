/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class ListenerNotFoundException extends OperationsException{
    private static final long serialVersionUID=-7242605822448519061L;

    public ListenerNotFoundException(){
        super();
    }

    public ListenerNotFoundException(String message){
        super(message);
    }
}
