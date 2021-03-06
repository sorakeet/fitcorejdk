/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * $Id: URIReferenceException.java,v 1.4 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: URIReferenceException.java,v 1.4 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class URIReferenceException extends Exception{
    private static final long serialVersionUID=7173469703932561419L;
    private Throwable cause;
    private URIReference uriReference;

    public URIReferenceException(){
        super();
    }

    public URIReferenceException(String message){
        super(message);
    }

    public URIReferenceException(String message,Throwable cause,
                                 URIReference uriReference){
        this(message,cause);
        if(uriReference==null){
            throw new NullPointerException("uriReference cannot be null");
        }
        this.uriReference=uriReference;
    }

    public URIReferenceException(String message,Throwable cause){
        super(message);
        this.cause=cause;
    }

    public URIReferenceException(Throwable cause){
        super(cause==null?null:cause.toString());
        this.cause=cause;
    }

    public URIReference getURIReference(){
        return uriReference;
    }

    public Throwable getCause(){
        return cause;
    }

    public void printStackTrace(){
        super.printStackTrace();
        //XXX print backtrace of cause
    }

    public void printStackTrace(PrintStream s){
        super.printStackTrace(s);
        //XXX print backtrace of cause
    }

    public void printStackTrace(PrintWriter s){
        super.printStackTrace(s);
        //XXX print backtrace of cause
    }
}
