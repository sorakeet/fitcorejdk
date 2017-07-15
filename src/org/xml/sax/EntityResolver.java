/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX entity resolver.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: EntityResolver.java,v 1.2 2004/11/03 22:44:52 jsuttor Exp $
package org.xml.sax;

import java.io.IOException;

public interface EntityResolver{
    public abstract InputSource resolveEntity(String publicId,
                                              String systemId)
            throws SAXException, IOException;
}
// end of EntityResolver.java
