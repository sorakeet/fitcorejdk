/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// DefaultHandler.java - default implementation of the core handlers.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the public domain.
// $Id: DefaultHandler.java,v 1.3 2006/04/13 02:06:32 jeffsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.*;

import java.io.IOException;

public class DefaultHandler
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler{
    ////////////////////////////////////////////////////////////////////
    // Default implementation of the EntityResolver interface.
    ////////////////////////////////////////////////////////////////////

    public InputSource resolveEntity(String publicId,String systemId)
            throws IOException, SAXException{
        return null;
    }
    ////////////////////////////////////////////////////////////////////
    // Default implementation of DTDHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        // no op
    }

    public void unparsedEntityDecl(String name,String publicId,
                                   String systemId,String notationName)
            throws SAXException{
        // no op
    }
    ////////////////////////////////////////////////////////////////////
    // Default implementation of ContentHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void setDocumentLocator(Locator locator){
        // no op
    }

    public void startDocument()
            throws SAXException{
        // no op
    }

    public void endDocument()
            throws SAXException{
        // no op
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        // no op
    }

    public void endPrefixMapping(String prefix)
            throws SAXException{
        // no op
    }

    public void startElement(String uri,String localName,
                             String qName,Attributes attributes)
            throws SAXException{
        // no op
    }

    public void endElement(String uri,String localName,String qName)
            throws SAXException{
        // no op
    }

    public void characters(char ch[],int start,int length)
            throws SAXException{
        // no op
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        // no op
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        // no op
    }

    public void skippedEntity(String name)
            throws SAXException{
        // no op
    }
    ////////////////////////////////////////////////////////////////////
    // Default implementation of the ErrorHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void warning(SAXParseException e)
            throws SAXException{
        // no op
    }

    public void error(SAXParseException e)
            throws SAXException{
        // no op
    }

    public void fatalError(SAXParseException e)
            throws SAXException{
        throw e;
    }
}
// end of DefaultHandler.java
