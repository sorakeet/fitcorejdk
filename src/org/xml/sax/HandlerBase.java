/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX default handler base class.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: HandlerBase.java,v 1.2 2005/06/10 03:50:47 jeffsuttor Exp $
package org.xml.sax;

public class HandlerBase
        implements EntityResolver, DTDHandler, DocumentHandler, ErrorHandler{
    ////////////////////////////////////////////////////////////////////
    // Default implementation of the EntityResolver interface.
    ////////////////////////////////////////////////////////////////////

    public InputSource resolveEntity(String publicId,String systemId)
            throws SAXException{
        return null;
    }
    ////////////////////////////////////////////////////////////////////
    // Default implementation of DTDHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void notationDecl(String name,String publicId,String systemId){
        // no op
    }

    public void unparsedEntityDecl(String name,String publicId,
                                   String systemId,String notationName){
        // no op
    }
    ////////////////////////////////////////////////////////////////////
    // Default implementation of DocumentHandler interface.
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

    public void startElement(String name,AttributeList attributes)
            throws SAXException{
        // no op
    }

    public void endElement(String name)
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
// end of HandlerBase.java
