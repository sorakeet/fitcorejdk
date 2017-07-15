/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public class FailedLoginException extends LoginException{
    private static final long serialVersionUID=802556922354616286L;

    public FailedLoginException(){
        super();
    }

    public FailedLoginException(String msg){
        super(msg);
    }
}
