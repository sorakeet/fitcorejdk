/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class MBeanException extends JMException{
    private static final long serialVersionUID=4066342430588744142L;
    private Exception exception;

    public MBeanException(Exception e){
        super();
        exception=e;
    }

    public MBeanException(Exception e,String message){
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
