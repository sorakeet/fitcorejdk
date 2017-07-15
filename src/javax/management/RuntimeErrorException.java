/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class RuntimeErrorException extends JMRuntimeException{
    private static final long serialVersionUID=704338937753949796L;
    private Error error;

    public RuntimeErrorException(Error e){
        super();
        error=e;
    }

    public RuntimeErrorException(Error e,String message){
        super(message);
        error=e;
    }

    public Error getTargetError(){
        return error;
    }

    public Throwable getCause(){
        return error;
    }
}
