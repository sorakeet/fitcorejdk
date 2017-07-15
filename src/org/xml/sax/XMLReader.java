/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// XMLReader.java - read an XML document.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: XMLReader.java,v 1.3 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

import java.io.IOException;

public interface XMLReader{
    ////////////////////////////////////////////////////////////////////
    // Configuration.
    ////////////////////////////////////////////////////////////////////

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    public void setProperty(String name,Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException;
    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////

    public EntityResolver getEntityResolver();

    public void setEntityResolver(EntityResolver resolver);

    public DTDHandler getDTDHandler();

    public void setDTDHandler(DTDHandler handler);

    public ContentHandler getContentHandler();

    public void setContentHandler(ContentHandler handler);

    public ErrorHandler getErrorHandler();

    public void setErrorHandler(ErrorHandler handler);
    ////////////////////////////////////////////////////////////////////
    // Parsing.
    ////////////////////////////////////////////////////////////////////

    public void parse(InputSource input)
            throws IOException, SAXException;

    public void parse(String systemId)
            throws IOException, SAXException;
}
