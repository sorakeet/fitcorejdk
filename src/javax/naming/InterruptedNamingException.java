/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class InterruptedNamingException extends NamingException{
    private static final long serialVersionUID=6404516648893194728L;

    public InterruptedNamingException(String explanation){
        super(explanation);
    }

    public InterruptedNamingException(){
        super();
    }
}
