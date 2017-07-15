/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX document handler.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: DocumentHandler.java,v 1.2 2004/11/03 22:44:51 jsuttor Exp $
package org.xml.sax;

public interface DocumentHandler{
    public abstract void setDocumentLocator(Locator locator);

    public abstract void startDocument()
            throws SAXException;

    public abstract void endDocument()
            throws SAXException;

    public abstract void startElement(String name,AttributeList atts)
            throws SAXException;

    public abstract void endElement(String name)
            throws SAXException;

    public abstract void characters(char ch[],int start,int length)
            throws SAXException;

    public abstract void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException;

    public abstract void processingInstruction(String target,String data)
            throws SAXException;
}
// end of DocumentHandler.java
