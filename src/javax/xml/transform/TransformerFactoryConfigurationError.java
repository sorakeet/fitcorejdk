/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

public class TransformerFactoryConfigurationError extends Error{
    private static final long serialVersionUID=-6527718720676281516L;
    private Exception exception;

    public TransformerFactoryConfigurationError(){
        super();
        this.exception=null;
    }

    public TransformerFactoryConfigurationError(String msg){
        super(msg);
        this.exception=null;
    }

    public TransformerFactoryConfigurationError(Exception e){
        super(e.toString());
        this.exception=e;
    }

    public TransformerFactoryConfigurationError(Exception e,String msg){
        super(msg);
        this.exception=e;
    }

    public String getMessage(){
        String message=super.getMessage();
        if((message==null)&&(exception!=null)){
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
