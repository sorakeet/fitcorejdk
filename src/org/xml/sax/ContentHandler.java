/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// ContentHandler.java - handle main document content.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the public domain.
// $Id: ContentHandler.java,v 1.2 2004/11/03 22:44:51 jsuttor Exp $
package org.xml.sax;

public interface ContentHandler{
    public void setDocumentLocator(Locator locator);

    public void startDocument()
            throws SAXException;

    public void endDocument()
            throws SAXException;

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException;

    public void endPrefixMapping(String prefix)
            throws SAXException;

    public void startElement(String uri,String localName,
                             String qName,Attributes atts)
            throws SAXException;

    public void endElement(String uri,String localName,
                           String qName)
            throws SAXException;

    public void characters(char ch[],int start,int length)
            throws SAXException;

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException;

    public void processingInstruction(String target,String data)
            throws SAXException;

    public void skippedEntity(String name)
            throws SAXException;
}
// end of ContentHandler.java
