/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX default implementation for Locator.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: LocatorImpl.java,v 1.2 2004/11/03 22:53:09 jsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.Locator;

public class LocatorImpl implements Locator{
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    private String publicId;
    private String systemId;
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Locator
    ////////////////////////////////////////////////////////////////////
    private int lineNumber;
    private int columnNumber;

    public LocatorImpl(){
    }

    public LocatorImpl(Locator locator){
        setPublicId(locator.getPublicId());
        setSystemId(locator.getSystemId());
        setLineNumber(locator.getLineNumber());
        setColumnNumber(locator.getColumnNumber());
    }
    ////////////////////////////////////////////////////////////////////
    // Setters for the properties (not in org.xml.sax.Locator)
    ////////////////////////////////////////////////////////////////////

    public String getPublicId(){
        return publicId;
    }

    public String getSystemId(){
        return systemId;
    }

    public int getLineNumber(){
        return lineNumber;
    }

    public int getColumnNumber(){
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber){
        this.columnNumber=columnNumber;
    }

    public void setLineNumber(int lineNumber){
        this.lineNumber=lineNumber;
    }

    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public void setPublicId(String publicId){
        this.publicId=publicId;
    }
}
// end of LocatorImpl.java
