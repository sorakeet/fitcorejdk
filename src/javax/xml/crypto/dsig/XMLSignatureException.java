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
 * $Id: XMLSignatureException.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
/**
 * $Id: XMLSignatureException.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
package javax.xml.crypto.dsig;

import java.io.PrintStream;
import java.io.PrintWriter;

public class XMLSignatureException extends Exception{
    private static final long serialVersionUID=-3438102491013869995L;
    private Throwable cause;

    public XMLSignatureException(){
        super();
    }

    public XMLSignatureException(String message){
        super(message);
    }

    public XMLSignatureException(String message,Throwable cause){
        super(message);
        this.cause=cause;
    }

    public XMLSignatureException(Throwable cause){
        super(cause==null?null:cause.toString());
        this.cause=cause;
    }

    public Throwable getCause(){
        return cause;
    }

    public void printStackTrace(){
        super.printStackTrace();
        if(cause!=null){
            cause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream s){
        super.printStackTrace(s);
        if(cause!=null){
            cause.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s){
        super.printStackTrace(s);
        if(cause!=null){
            cause.printStackTrace(s);
        }
    }
}
