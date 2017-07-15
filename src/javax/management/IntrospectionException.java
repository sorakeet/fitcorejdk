/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class IntrospectionException extends OperationsException{
    private static final long serialVersionUID=1054516935875481725L;

    public IntrospectionException(){
        super();
    }

    public IntrospectionException(String message){
        super(message);
    }
}
