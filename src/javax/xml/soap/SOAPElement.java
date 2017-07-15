/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.xml.namespace.QName;
import java.util.Iterator;

public interface SOAPElement extends Node, org.w3c.dom.Element{
    public SOAPElement addChildElement(Name name) throws SOAPException;

    public SOAPElement addChildElement(QName qname) throws SOAPException;

    public SOAPElement addChildElement(String localName) throws SOAPException;

    public SOAPElement addChildElement(String localName,String prefix)
            throws SOAPException;

    public SOAPElement addChildElement(String localName,String prefix,
                                       String uri)
            throws SOAPException;

    public SOAPElement addChildElement(SOAPElement element)
            throws SOAPException;

    public abstract void removeContents();

    public SOAPElement addTextNode(String text) throws SOAPException;

    public SOAPElement addAttribute(Name name,String value)
            throws SOAPException;

    public SOAPElement addAttribute(QName qname,String value)
            throws SOAPException;

    public SOAPElement addNamespaceDeclaration(String prefix,String uri)
            throws SOAPException;

    public String getAttributeValue(Name name);

    public String getAttributeValue(QName qname);

    public Iterator getAllAttributes();

    public Iterator getAllAttributesAsQNames();

    public String getNamespaceURI(String prefix);

    public Iterator getNamespacePrefixes();

    public Iterator getVisibleNamespacePrefixes();

    public QName createQName(String localName,String prefix)
            throws SOAPException;

    public Name getElementName();

    public QName getElementQName();

    public SOAPElement setElementQName(QName newName) throws SOAPException;

    public boolean removeAttribute(Name name);

    public boolean removeAttribute(QName qname);

    public boolean removeNamespaceDeclaration(String prefix);

    public Iterator getChildElements();

    public Iterator getChildElements(Name name);

    public Iterator getChildElements(QName qname);

    public String getEncodingStyle();

    public void setEncodingStyle(String encodingStyle)
            throws SOAPException;
}
