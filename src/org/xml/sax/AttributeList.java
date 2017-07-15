/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX Attribute List Interface.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: AttributeList.java,v 1.3 2004/11/03 22:44:51 jsuttor Exp $
package org.xml.sax;

public interface AttributeList{
    ////////////////////////////////////////////////////////////////////
    // Iteration methods.
    ////////////////////////////////////////////////////////////////////

    public abstract int getLength();

    public abstract String getName(int i);

    public abstract String getType(int i);

    public abstract String getValue(int i);
    ////////////////////////////////////////////////////////////////////
    // Lookup methods.
    ////////////////////////////////////////////////////////////////////

    public abstract String getType(String name);

    public abstract String getValue(String name);
}
// end of AttributeList.java
