/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import java.util.Locale;

public interface SOAPBody extends SOAPElement{
    public SOAPFault addFault() throws SOAPException;

    public SOAPFault addFault(Name faultCode,String faultString,Locale locale) throws SOAPException;

    public SOAPFault addFault(QName faultCode,String faultString,Locale locale)
            throws SOAPException;

    public SOAPFault addFault(Name faultCode,String faultString)
            throws SOAPException;

    public SOAPFault addFault(QName faultCode,String faultString)
            throws SOAPException;

    public boolean hasFault();

    public SOAPFault getFault();

    public SOAPBodyElement addBodyElement(Name name) throws SOAPException;

    public SOAPBodyElement addBodyElement(QName qname) throws SOAPException;

    public SOAPBodyElement addDocument(Document document)
            throws SOAPException;

    public Document extractContentAsDocument()
            throws SOAPException;
}
