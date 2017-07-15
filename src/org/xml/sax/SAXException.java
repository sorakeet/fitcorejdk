/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX exception class.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: SAXException.java,v 1.3 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public class SAXException extends Exception{
    // Added serialVersionUID to preserve binary compatibility
    static final long serialVersionUID=583241635256073760L;
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private Exception exception;

    public SAXException(){
        super();
        this.exception=null;
    }

    public SAXException(String message){
        super(message);
        this.exception=null;
    }

    public SAXException(Exception e){
        super();
        this.exception=e;
    }

    public SAXException(String message,Exception e){
        super(message);
        this.exception=e;
    }

    public String getMessage(){
        String message=super.getMessage();
        if(message==null&&exception!=null){
            return exception.getMessage();
        }else{
            return message;
        }
    }

    public Throwable getCause(){
        return exception;
    }

    public String toString(){
        if(exception!=null){
            return super.toString()+"\n"+exception.toString();
        }else{
            return super.toString();
        }
    }

    public Exception getException(){
        return exception;
    }
}
// end of SAXException.java
