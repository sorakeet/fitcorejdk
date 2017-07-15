/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Attributes.java - attribute list with Namespace support
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the public domain.
// $Id: Attributes.java,v 1.2 2004/11/03 22:44:51 jsuttor Exp $
package org.xml.sax;

public interface Attributes{
    ////////////////////////////////////////////////////////////////////
    // Indexed access.
    ////////////////////////////////////////////////////////////////////

    public abstract int getLength();

    public abstract String getURI(int index);

    public abstract String getLocalName(int index);

    public abstract String getQName(int index);

    public abstract String getType(int index);

    public abstract String getValue(int index);
    ////////////////////////////////////////////////////////////////////
    // Name-based query.
    ////////////////////////////////////////////////////////////////////

    public int getIndex(String uri,String localName);

    public int getIndex(String qName);

    public abstract String getType(String uri,String localName);

    public abstract String getType(String qName);

    public abstract String getValue(String uri,String localName);

    public abstract String getValue(String qName);
}
// end of Attributes.java
