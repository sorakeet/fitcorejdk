/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class AccountLockedException extends AccountException{
    private static final long serialVersionUID=8280345554014066334L;

    public AccountLockedException(){
        super();
    }

    public AccountLockedException(String msg){
        super(msg);
    }
}
