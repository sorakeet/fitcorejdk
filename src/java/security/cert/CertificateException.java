/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import java.security.GeneralSecurityException;

public class CertificateException extends GeneralSecurityException{
    private static final long serialVersionUID=3192535253797119798L;

    public CertificateException(){
        super();
    }

    public CertificateException(String msg){
        super(msg);
    }

    public CertificateException(String message,Throwable cause){
        super(message,cause);
    }

    public CertificateException(Throwable cause){
        super(cause);
    }
}
