/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class RuntimeOperationsException extends JMRuntimeException{
    private static final long serialVersionUID=-8408923047489133588L;
    private RuntimeException runtimeException;

    public RuntimeOperationsException(RuntimeException e){
        super();
        runtimeException=e;
    }

    public RuntimeOperationsException(RuntimeException e,String message){
        super(message);
        runtimeException=e;
    }

    public RuntimeException getTargetException(){
        return runtimeException;
    }

    public Throwable getCause(){
        return runtimeException;
    }
}
