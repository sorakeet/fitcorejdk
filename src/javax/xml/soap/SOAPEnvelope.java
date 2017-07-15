/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public interface SOAPEnvelope extends SOAPElement{
    public abstract Name createName(String localName,String prefix,
                                    String uri)
            throws SOAPException;

    public abstract Name createName(String localName)
            throws SOAPException;

    public SOAPHeader getHeader() throws SOAPException;

    public SOAPBody getBody() throws SOAPException;

    public SOAPHeader addHeader() throws SOAPException;

    public SOAPBody addBody() throws SOAPException;
}
