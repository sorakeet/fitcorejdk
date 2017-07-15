/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class ReflectionException extends JMException{
    private static final long serialVersionUID=9170809325636915553L;
    private Exception exception;

    public ReflectionException(Exception e){
        super();
        exception=e;
    }

    public ReflectionException(Exception e,String message){
        super(message);
        exception=e;
    }

    public Exception getTargetException(){
        return exception;
    }

    public Throwable getCause(){
        return exception;
    }
}
