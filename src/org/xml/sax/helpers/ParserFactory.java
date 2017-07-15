/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX parser factory.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: ParserFactory.java,v 1.2 2004/11/03 22:53:09 jsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.Parser;

public class ParserFactory{
    private static SecuritySupport ss=new SecuritySupport();

    private ParserFactory(){
    }

    public static Parser makeParser()
            throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            NullPointerException,
            ClassCastException{
        String className=ss.getSystemProperty("org.xml.sax.parser");
        if(className==null){
            throw new NullPointerException("No value for sax.parser property");
        }else{
            return makeParser(className);
        }
    }

    public static Parser makeParser(String className)
            throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            ClassCastException{
        return (Parser)NewInstance.newInstance(
                ss.getContextClassLoader(),className);
    }
}
