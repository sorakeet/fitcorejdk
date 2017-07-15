/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class LoginException extends java.security.GeneralSecurityException{
    private static final long serialVersionUID=-4679091624035232488L;

    public LoginException(){
        super();
    }

    public LoginException(String msg){
        super(msg);
    }
}
