/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAXNotRecognizedException.java - unrecognized feature or value.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the Public Domain.
// $Id: SAXNotRecognizedException.java,v 1.3 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public class SAXNotRecognizedException extends SAXException{
    // Added serialVersionUID to preserve binary compatibility
    static final long serialVersionUID=5440506620509557213L;

    public SAXNotRecognizedException(){
        super();
    }

    public SAXNotRecognizedException(String message){
        super(message);
    }
}
// end of SAXNotRecognizedException.java
