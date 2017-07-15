/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX parser interface.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: Parser.java,v 1.2 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

import java.io.IOException;
import java.util.Locale;

public interface Parser{
    public abstract void setLocale(Locale locale)
            throws SAXException;

    public abstract void setEntityResolver(EntityResolver resolver);

    public abstract void setDTDHandler(DTDHandler handler);

    public abstract void setDocumentHandler(DocumentHandler handler);

    public abstract void setErrorHandler(ErrorHandler handler);

    public abstract void parse(InputSource source)
            throws SAXException, IOException;

    public abstract void parse(String systemId)
            throws SAXException, IOException;
}
// end of Parser.java
