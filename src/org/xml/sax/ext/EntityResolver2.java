/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// EntityResolver2.java - Extended SAX entity resolver.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: EntityResolver2.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface EntityResolver2 extends EntityResolver{
    public InputSource getExternalSubset(String name,String baseURI)
            throws SAXException, IOException;

    public InputSource resolveEntity(
            String name,
            String publicId,
            String baseURI,
            String systemId
    ) throws SAXException, IOException;
}
