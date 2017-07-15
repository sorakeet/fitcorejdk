/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

public class CertificateException extends Exception{
    private static final long serialVersionUID=-5757213374030785290L;

    public CertificateException(){
        super();
    }

    public CertificateException(String msg){
        super(msg);
    }
}
