/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class InstanceNotFoundException extends OperationsException{
    private static final long serialVersionUID=-882579438394773049L;

    public InstanceNotFoundException(){
        super();
    }

    public InstanceNotFoundException(String message){
        super(message);
    }
}
