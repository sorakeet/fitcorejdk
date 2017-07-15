/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// LexicalHandler.java - optional handler for lexical parse events.
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: LexicalHandler.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.SAXException;

public interface LexicalHandler{
    public abstract void startDTD(String name,String publicId,
                                  String systemId)
            throws SAXException;

    public abstract void endDTD()
            throws SAXException;

    public abstract void startEntity(String name)
            throws SAXException;

    public abstract void endEntity(String name)
            throws SAXException;

    public abstract void startCDATA()
            throws SAXException;

    public abstract void endCDATA()
            throws SAXException;

    public abstract void comment(char ch[],int start,int length)
            throws SAXException;
}
// end of LexicalHandler.java
