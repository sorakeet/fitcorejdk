/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public abstract class StartTlsResponse implements ExtendedResponse{
    // Constant
    public static final String OID="1.3.6.1.4.1.1466.20037";
    // Called by subclass
    private static final long serialVersionUID=8372842182579276418L;
    // ExtendedResponse methods

    protected StartTlsResponse(){
    }

    public String getID(){
        return OID;
    }
    // StartTls-specific methods

    public byte[] getEncodedValue(){
        return null;
    }

    public abstract void setEnabledCipherSuites(String[] suites);

    public abstract void setHostnameVerifier(HostnameVerifier verifier);

    public abstract SSLSession negotiate() throws IOException;

    public abstract SSLSession negotiate(SSLSocketFactory factory)
            throws IOException;

    public abstract void close() throws IOException;
}
