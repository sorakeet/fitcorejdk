/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// DeclHandler.java - Optional handler for DTD declaration events.
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: DeclHandler.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.SAXException;

public interface DeclHandler{
    public abstract void elementDecl(String name,String model)
            throws SAXException;

    public abstract void attributeDecl(String eName,
                                       String aName,
                                       String type,
                                       String mode,
                                       String value)
            throws SAXException;

    public abstract void internalEntityDecl(String name,String value)
            throws SAXException;

    public abstract void externalEntityDecl(String name,String publicId,
                                            String systemId)
            throws SAXException;
}
// end of DeclHandler.java
