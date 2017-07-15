/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import java.io.IOException;

public class JMXProviderException extends IOException{
    private static final long serialVersionUID=-3166703627550447198L;
    private Throwable cause=null;

    public JMXProviderException(){
    }

    public JMXProviderException(String message){
        super(message);
    }

    public JMXProviderException(String message,Throwable cause){
        super(message);
        this.cause=cause;
    }

    public Throwable getCause(){
        return cause;
    }
}
