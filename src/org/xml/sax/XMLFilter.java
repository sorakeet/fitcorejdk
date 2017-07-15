/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// XMLFilter.java - filter SAX2 events.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: XMLFilter.java,v 1.2 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public interface XMLFilter extends XMLReader{
    public abstract XMLReader getParent();

    public abstract void setParent(XMLReader parent);
}
// end of XMLFilter.java
