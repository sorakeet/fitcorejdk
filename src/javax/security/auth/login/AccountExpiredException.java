/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class AccountExpiredException extends AccountException{
    private static final long serialVersionUID=-6064064890162661560L;

    public AccountExpiredException(){
        super();
    }

    public AccountExpiredException(String msg){
        super(msg);
    }
}
