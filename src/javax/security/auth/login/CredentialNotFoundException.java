/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class CredentialNotFoundException extends CredentialException{
    private static final long serialVersionUID=-7779934467214319475L;

    public CredentialNotFoundException(){
        super();
    }

    public CredentialNotFoundException(String msg){
        super(msg);
    }
}
