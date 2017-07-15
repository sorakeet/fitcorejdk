/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public class ValidationException extends JAXBException{
    public ValidationException(String message){
        this(message,null,null);
    }

    public ValidationException(String message,String errorCode,Throwable exception){
        super(message,errorCode,exception);
    }

    public ValidationException(String message,String errorCode){
        this(message,errorCode,null);
    }

    public ValidationException(Throwable exception){
        this(null,null,exception);
    }

    public ValidationException(String message,Throwable exception){
        this(message,null,exception);
    }
}
