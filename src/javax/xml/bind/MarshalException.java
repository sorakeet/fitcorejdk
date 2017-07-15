/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public class MarshalException extends JAXBException{
    public MarshalException(String message){
        this(message,null,null);
    }

    public MarshalException(String message,String errorCode,Throwable exception){
        super(message,errorCode,exception);
    }

    public MarshalException(String message,String errorCode){
        this(message,errorCode,null);
    }

    public MarshalException(Throwable exception){
        this(null,null,exception);
    }

    public MarshalException(String message,Throwable exception){
        this(message,null,exception);
    }
}
