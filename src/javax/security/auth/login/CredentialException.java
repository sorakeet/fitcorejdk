/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class CredentialException extends LoginException{
    private static final long serialVersionUID=-4772893876810601859L;

    public CredentialException(){
        super();
    }

    public CredentialException(String msg){
        super(msg);
    }
}
