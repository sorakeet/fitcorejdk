/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Locator2Impl.java - extended LocatorImpl
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: Locator2Impl.java,v 1.2 2004/11/03 22:49:08 jsuttor Exp $
package org.xml.sax.ext;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class Locator2Impl extends LocatorImpl implements Locator2{
    private String encoding;
    private String version;

    public Locator2Impl(){
    }

    public Locator2Impl(Locator locator){
        super(locator);
        if(locator instanceof Locator2){
            Locator2 l2=(Locator2)locator;
            version=l2.getXMLVersion();
            encoding=l2.getEncoding();
        }
    }
    ////////////////////////////////////////////////////////////////////
    // Locator2 method implementations
    ////////////////////////////////////////////////////////////////////

    public String getXMLVersion(){
        return version;
    }

    public String getEncoding(){
        return encoding;
    }
    ////////////////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////////////////

    public void setEncoding(String encoding){
        this.encoding=encoding;
    }

    public void setXMLVersion(String version){
        this.version=version;
    }
}
