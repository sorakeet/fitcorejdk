/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// DefaultHandler2.java - extended DefaultHandler
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: DefaultHandler2.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class DefaultHandler2 extends DefaultHandler
        implements LexicalHandler, DeclHandler, EntityResolver2{
    public DefaultHandler2(){
    }
    // SAX2 ext-1.0 LexicalHandler

    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
    }

    public void endDTD()
            throws SAXException{
    }

    public void startEntity(String name)
            throws SAXException{
    }

    public void endEntity(String name)
            throws SAXException{
    }

    public void startCDATA()
            throws SAXException{
    }

    public void endCDATA()
            throws SAXException{
    }

    public void comment(char ch[],int start,int length)
            throws SAXException{
    }
    // SAX2 ext-1.0 DeclHandler

    public void elementDecl(String name,String model)
            throws SAXException{
    }

    public void attributeDecl(String eName,String aName,
                              String type,String mode,String value)
            throws SAXException{
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
    }

    public void externalEntityDecl(String name,
                                   String publicId,String systemId)
            throws SAXException{
    }
    // SAX2 ext-1.1 EntityResolver2

    public InputSource getExternalSubset(String name,String baseURI)
            throws SAXException, IOException{
        return null;
    }

    public InputSource resolveEntity(String name,String publicId,
                                     String baseURI,String systemId)
            throws SAXException, IOException{
        return null;
    }
    // SAX1 EntityResolver

    public InputSource resolveEntity(String publicId,String systemId)
            throws SAXException, IOException{
        return resolveEntity(null,publicId,null,systemId);
    }
}
