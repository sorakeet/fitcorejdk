/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Locator2.java - extended Locator
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: Locator2.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.Locator;

public interface Locator2 extends Locator{
    public String getXMLVersion();

    public String getEncoding();
}
