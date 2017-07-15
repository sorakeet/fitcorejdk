/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public class DataBindingException extends RuntimeException{
    public DataBindingException(String message,Throwable cause){
        super(message,cause);
    }

    public DataBindingException(Throwable cause){
        super(cause);
    }
}
