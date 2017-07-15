/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: MarshalException.java,v 1.5 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: MarshalException.java,v 1.5 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MarshalException extends Exception{
    private static final long serialVersionUID=-863185580332643547L;
    private Throwable cause;

    public MarshalException(){
        super();
    }

    public MarshalException(String message){
        super(message);
    }

    public MarshalException(String message,Throwable cause){
        super(message);
        this.cause=cause;
    }

    public MarshalException(Throwable cause){
        super(cause==null?null:cause.toString());
        this.cause=cause;
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
