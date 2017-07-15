/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.datatype;

public class DatatypeConfigurationException extends Exception{
    public DatatypeConfigurationException(){
        super();
    }

    public DatatypeConfigurationException(String message){
        super(message);
    }

    public DatatypeConfigurationException(String message,Throwable cause){
        super(message,cause);
    }

    public DatatypeConfigurationException(Throwable cause){
        super(cause);
    }
}
