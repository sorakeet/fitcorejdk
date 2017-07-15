/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX error handler.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: ErrorHandler.java,v 1.2 2004/11/03 22:44:52 jsuttor Exp $
package org.xml.sax;

public interface ErrorHandler{
    public abstract void warning(SAXParseException exception)
            throws SAXException;

    public abstract void error(SAXParseException exception)
            throws SAXException;

    public abstract void fatalError(SAXParseException exception)
            throws SAXException;
}
// end of ErrorHandler.java
