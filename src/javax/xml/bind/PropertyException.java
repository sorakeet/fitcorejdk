/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public class PropertyException extends JAXBException{
    public PropertyException(String message){
        super(message);
    }

    public PropertyException(String message,String errorCode){
        super(message,errorCode);
    }

    public PropertyException(Throwable exception){
        super(exception);
    }

    public PropertyException(String message,Throwable exception){
        super(message,exception);
    }

    public PropertyException(
            String message,
            String errorCode,
            Throwable exception){
        super(message,errorCode,exception);
    }

    public PropertyException(String name,Object value){
        super(Messages.format(Messages.NAME_VALUE,
                name,
                value.toString()));
    }
}
