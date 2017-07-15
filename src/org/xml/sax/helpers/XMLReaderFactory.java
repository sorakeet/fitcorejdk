/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// XMLReaderFactory.java - factory for creating a new reader.
// http://www.saxproject.org
// Written by David Megginson
// and by David Brownell
// NO WARRANTY!  This class is in the Public Domain.
// $Id: XMLReaderFactory.java,v 1.2.2.1 2005/07/31 22:48:08 jeffsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

final public class XMLReaderFactory{
    private static final String property="org.xml.sax.driver";
    private static SecuritySupport ss=new SecuritySupport();
    private static String _clsFromJar=null;
    private static boolean _jarread=false;

    private XMLReaderFactory(){
    }

    public static XMLReader createXMLReader()
            throws SAXException{
        String className=null;
        ClassLoader cl=ss.getContextClassLoader();
        // 1. try the JVM-instance-wide system property
        try{
            className=ss.getSystemProperty(property);
        }catch(RuntimeException e){ /** continue searching */}
        // 2. if that fails, try META-INF/services/
        if(className==null){
            if(!_jarread){
                _jarread=true;
                String service="META-INF/services/"+property;
                InputStream in;
                BufferedReader reader;
                try{
                    if(cl!=null){
                        in=ss.getResourceAsStream(cl,service);
                        // If no provider found then try the current ClassLoader
                        if(in==null){
                            cl=null;
                            in=ss.getResourceAsStream(cl,service);
                        }
                    }else{
                        // No Context ClassLoader, try the current ClassLoader
                        in=ss.getResourceAsStream(cl,service);
                    }
                    if(in!=null){
                        reader=new BufferedReader(new InputStreamReader(in,"UTF8"));
                        _clsFromJar=reader.readLine();
                        in.close();
                    }
                }catch(Exception e){
                }
            }
            className=_clsFromJar;
        }
        // 3. Distro-specific fallback
        if(className==null){
// BEGIN DISTRIBUTION-SPECIFIC
            // EXAMPLE:
            // className = "com.example.sax.XmlReader";
            // or a $JAVA_HOME/jre/lib/**properties setting...
            className="com.sun.org.apache.xerces.internal.parsers.SAXParser";
// END DISTRIBUTION-SPECIFIC
        }
        // do we know the XMLReader implementation class yet?
        if(className!=null)
            return loadClass(cl,className);
        // 4. panic -- adapt any SAX1 parser
        try{
            return new ParserAdapter(ParserFactory.makeParser());
        }catch(Exception e){
            throw new SAXException("Can't create default XMLReader; "
                    +"is system property org.xml.sax.driver set?");
        }
    }

    private static XMLReader loadClass(ClassLoader loader,String className)
            throws SAXException{
        try{
            return (XMLReader)NewInstance.newInstance(loader,className);
        }catch(ClassNotFoundException e1){
            throw new SAXException("SAX2 driver class "+className+
                    " not found",e1);
        }catch(IllegalAccessException e2){
            throw new SAXException("SAX2 driver class "+className+
                    " found but cannot be loaded",e2);
        }catch(InstantiationException e3){
            throw new SAXException("SAX2 driver class "+className+
                    " loaded but cannot be instantiated (no empty public constructor?)",
                    e3);
        }catch(ClassCastException e4){
            throw new SAXException("SAX2 driver class "+className+
                    " does not implement XMLReader",e4);
        }
    }

    public static XMLReader createXMLReader(String className)
            throws SAXException{
        return loadClass(ss.getContextClassLoader(),className);
    }
}
