/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public abstract class NamingSecurityException extends NamingException{
    private static final long serialVersionUID=5855287647294685775L;

    public NamingSecurityException(String explanation){
        super(explanation);
    }

    public NamingSecurityException(){
        super();
    }
};
