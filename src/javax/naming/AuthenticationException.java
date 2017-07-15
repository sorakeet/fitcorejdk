/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class AuthenticationException extends NamingSecurityException{
    private static final long serialVersionUID=3678497619904568096L;

    public AuthenticationException(String explanation){
        super(explanation);
    }

    public AuthenticationException(){
        super();
    }
}
