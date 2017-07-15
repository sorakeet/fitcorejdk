/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

public class NoSuchAlgorithmException extends GeneralSecurityException{
    private static final long serialVersionUID=-7443947487218346562L;

    public NoSuchAlgorithmException(){
        super();
    }

    public NoSuchAlgorithmException(String msg){
        super(msg);
    }

    public NoSuchAlgorithmException(String message,Throwable cause){
        super(message,cause);
    }

    public NoSuchAlgorithmException(Throwable cause){
        super(cause);
    }
}
