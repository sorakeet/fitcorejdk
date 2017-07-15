/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX locator interface for document events.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: Locator.java,v 1.2 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public interface Locator{
    public abstract String getPublicId();

    public abstract String getSystemId();

    public abstract int getLineNumber();

    public abstract int getColumnNumber();
}
// end of Locator.java
