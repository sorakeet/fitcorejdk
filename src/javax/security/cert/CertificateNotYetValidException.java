/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

public class CertificateNotYetValidException extends CertificateException{
    private static final long serialVersionUID=-8976172474266822818L;

    public CertificateNotYetValidException(){
        super();
    }

    public CertificateNotYetValidException(String message){
        super(message);
    }
}
