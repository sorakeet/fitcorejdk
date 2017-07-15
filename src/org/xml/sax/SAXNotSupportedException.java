/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAXNotSupportedException.java - unsupported feature or value.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: SAXNotSupportedException.java,v 1.4 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public class SAXNotSupportedException extends SAXException{
    // Added serialVersionUID to preserve binary compatibility
    static final long serialVersionUID=-1422818934641823846L;

    public SAXNotSupportedException(){
        super();
    }

    public SAXNotSupportedException(String message){
        super(message);
    }
}
// end of SAXNotSupportedException.java
