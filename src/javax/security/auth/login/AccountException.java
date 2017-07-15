/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class AccountException extends LoginException{
    private static final long serialVersionUID=-2112878680072211787L;

    public AccountException(){
        super();
    }

    public AccountException(String msg){
        super(msg);
    }
}
