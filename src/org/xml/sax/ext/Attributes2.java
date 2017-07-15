/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Attributes2.java - extended Attributes
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: Attributes2.java,v 1.2 2004/11/03 22:49:07 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.Attributes;

public interface Attributes2 extends Attributes{
    public boolean isDeclared(int index);

    public boolean isDeclared(String qName);

    public boolean isDeclared(String uri,String localName);

    public boolean isSpecified(int index);

    public boolean isSpecified(String uri,String localName);

    public boolean isSpecified(String qName);
}
