/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public class SOAPException extends Exception{
    private Throwable cause;

    public SOAPException(){
        super();
        this.cause=null;
    }

    public SOAPException(String reason){
        super(reason);
        this.cause=null;
    }

    public SOAPException(String reason,Throwable cause){
        super(reason);
        initCause(cause);
    }

    public SOAPException(Throwable cause){
        super(cause.toString());
        initCause(cause);
    }

    public String getMessage(){
        String message=super.getMessage();
        if(message==null&&cause!=null){
            return cause.getMessage();
        }else{
            return message;
        }
    }

    public Throwable getCause(){
        return cause;
    }

    public synchronized Throwable initCause(Throwable cause){
        if(this.cause!=null){
            throw new IllegalStateException("Can't override cause");
        }
        if(cause==this){
            throw new IllegalArgumentException("Self-causation not permitted");
        }
        this.cause=cause;
        return this;
    }
}
