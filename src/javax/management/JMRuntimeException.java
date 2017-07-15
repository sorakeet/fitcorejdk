/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class JMRuntimeException extends RuntimeException{
    private static final long serialVersionUID=6573344628407841861L;

    public JMRuntimeException(){
        super();
    }

    public JMRuntimeException(String message){
        super(message);
    }

    JMRuntimeException(String message,Throwable cause){
        super(message,cause);
    }
}
