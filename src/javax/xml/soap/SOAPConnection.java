/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public abstract class SOAPConnection{
    public abstract SOAPMessage call(SOAPMessage request,
                                     Object to) throws SOAPException;

    public SOAPMessage get(Object to)
            throws SOAPException{
        throw new UnsupportedOperationException("All subclasses of SOAPConnection must override get()");
    }

    public abstract void close()
            throws SOAPException;
}
