/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class AuthenticationNotSupportedException extends NamingSecurityException{
    private static final long serialVersionUID=-7149033933259492300L;

    public AuthenticationNotSupportedException(String explanation){
        super(explanation);
    }

    public AuthenticationNotSupportedException(){
        super();
    }
}
