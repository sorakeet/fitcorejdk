/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

public class WebServiceException extends RuntimeException{
    public WebServiceException(){
        super();
    }

    public WebServiceException(String message){
        super(message);
    }

    public WebServiceException(String message,Throwable cause){
        super(message,cause);
    }

    public WebServiceException(Throwable cause){
        super(cause);
    }
}
