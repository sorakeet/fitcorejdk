/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

public class CertificateEncodingException extends CertificateException{
    private static final long serialVersionUID=-8187642723048403470L;

    public CertificateEncodingException(){
        super();
    }

    public CertificateEncodingException(String message){
        super(message);
    }
}
