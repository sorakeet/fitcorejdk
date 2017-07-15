/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import java.io.PrintWriter;

public class JAXBException extends Exception{
    static final long serialVersionUID=-5621384651494307979L;
    private String errorCode;
    private volatile Throwable linkedException;

    public JAXBException(String message){
        this(message,null,null);
    }

    public JAXBException(String message,String errorCode,Throwable exception){
        super(message);
        this.errorCode=errorCode;
        this.linkedException=exception;
    }

    public JAXBException(String message,String errorCode){
        this(message,errorCode,null);
    }

    public JAXBException(Throwable exception){
        this(null,null,exception);
    }

    public JAXBException(String message,Throwable exception){
        this(message,null,exception);
    }

    public String getErrorCode(){
        return this.errorCode;
    }

    public Throwable getLinkedException(){
        return linkedException;
    }

    public void setLinkedException(Throwable exception){
        this.linkedException=exception;
    }

    @Override
    public Throwable getCause(){
        return linkedException;
    }

    public String toString(){
        return linkedException==null?
                super.toString():
                super.toString()+"\n - with linked exception:\n["+
                        linkedException.toString()+"]";
    }

    public void printStackTrace(){
        super.printStackTrace();
    }

    public void printStackTrace(java.io.PrintStream s){
        super.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s){
        super.printStackTrace(s);
    }
}
