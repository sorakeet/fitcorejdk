/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

public class CertificateParsingException extends CertificateException{
    private static final long serialVersionUID=-8449352422951136229L;

    public CertificateParsingException(){
        super();
    }

    public CertificateParsingException(String message){
        super(message);
    }
}
