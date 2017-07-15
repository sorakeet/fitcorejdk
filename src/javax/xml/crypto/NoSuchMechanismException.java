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
 * $Id: NoSuchMechanismException.java,v 1.4 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * $Id: NoSuchMechanismException.java,v 1.4 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;

public class NoSuchMechanismException extends RuntimeException{
    private static final long serialVersionUID=4189669069570660166L;
    private Throwable cause;

    public NoSuchMechanismException(){
        super();
    }

    public NoSuchMechanismException(String message){
        super(message);
    }

    public NoSuchMechanismException(String message,Throwable cause){
        super(message);
        this.cause=cause;
    }

    public NoSuchMechanismException(Throwable cause){
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
