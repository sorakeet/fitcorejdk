/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.parsers;

public class FactoryConfigurationError extends Error{
    private static final long serialVersionUID=-827108682472263355L;
    private Exception exception;

    public FactoryConfigurationError(){
        super();
        this.exception=null;
    }

    public FactoryConfigurationError(String msg){
        super(msg);
        this.exception=null;
    }

    public FactoryConfigurationError(Exception e){
        super(e.toString());
        this.exception=e;
    }

    public FactoryConfigurationError(Exception e,String msg){
        super(msg);
        this.exception=e;
    }

    public String getMessage(){
        String message=super.getMessage();
        if(message==null&&exception!=null){
            return exception.getMessage();
        }
        return message;
    }

    @Override
    public Throwable getCause(){
        return exception;
    }

    public Exception getException(){
        return exception;
    }
}
