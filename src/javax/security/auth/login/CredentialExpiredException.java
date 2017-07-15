/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class CredentialExpiredException extends CredentialException{
    private static final long serialVersionUID=-5344739593859737937L;

    public CredentialExpiredException(){
        super();
    }

    public CredentialExpiredException(String msg){
        super(msg);
    }
}
