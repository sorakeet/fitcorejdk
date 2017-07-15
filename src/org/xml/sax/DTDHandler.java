/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX DTD handler.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: DTDHandler.java,v 1.2 2004/11/03 22:44:51 jsuttor Exp $
package org.xml.sax;

public interface DTDHandler{
    public abstract void notationDecl(String name,
                                      String publicId,
                                      String systemId)
            throws SAXException;

    public abstract void unparsedEntityDecl(String name,
                                            String publicId,
                                            String systemId,
                                            String notationName)
            throws SAXException;
}
// end of DTDHandler.java
