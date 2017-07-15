/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class AccountNotFoundException extends AccountException{
    private static final long serialVersionUID=1498349563916294614L;

    public AccountNotFoundException(){
        super();
    }

    public AccountNotFoundException(String msg){
        super(msg);
    }
}
