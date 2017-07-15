/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.sasl;

public abstract interface SaslServer{
    public abstract String getMechanismName();

    public abstract byte[] evaluateResponse(byte[] response)
            throws SaslException;

    public abstract boolean isComplete();

    public String getAuthorizationID();

    public abstract byte[] unwrap(byte[] incoming,int offset,int len)
            throws SaslException;

    public abstract byte[] wrap(byte[] outgoing,int offset,int len)
            throws SaslException;

    public abstract Object getNegotiatedProperty(String propName);

    public abstract void dispose() throws SaslException;
}
