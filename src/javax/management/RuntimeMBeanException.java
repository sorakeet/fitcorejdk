/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class RuntimeMBeanException extends JMRuntimeException{
    private static final long serialVersionUID=5274912751982730171L;
    private RuntimeException runtimeException;

    public RuntimeMBeanException(RuntimeException e){
        super();
        runtimeException=e;
    }

    public RuntimeMBeanException(RuntimeException e,String message){
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
