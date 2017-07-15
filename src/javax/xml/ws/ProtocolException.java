/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

public class ProtocolException extends WebServiceException{
    public ProtocolException(){
        super();
    }

    public ProtocolException(String message){
        super(message);
    }

    public ProtocolException(String message,Throwable cause){
        super(message,cause);
    }

    public ProtocolException(Throwable cause){
        super(cause);
    }
}
