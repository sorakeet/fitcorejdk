/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.parsers;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.validation.Schema;

public abstract class SAXParserFactory{
    private boolean validating=false;
    private boolean namespaceAware=false;

    protected SAXParserFactory(){
    }

    public static SAXParserFactory newInstance(){
        return FactoryFinder.find(
                /** The default property name according to the JAXP spec */
                SAXParserFactory.class,
                /** The fallback implementation class name */
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
    }

    public static SAXParserFactory newInstance(String factoryClassName,ClassLoader classLoader){
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.newInstance(SAXParserFactory.class,
                factoryClassName,classLoader,false);
    }

    public abstract SAXParser newSAXParser()
            throws ParserConfigurationException, SAXException;

    public boolean isNamespaceAware(){
        return namespaceAware;
    }

    public void setNamespaceAware(boolean awareness){
        this.namespaceAware=awareness;
    }

    public boolean isValidating(){
        return validating;
    }

    public void setValidating(boolean validating){
        this.validating=validating;
    }

    public abstract void setFeature(String name,boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

    public abstract boolean getFeature(String name)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

    public Schema getSchema(){
        throw new UnsupportedOperationException(
                "This parser does not support specification \""
                        +this.getClass().getPackage().getSpecificationTitle()
                        +"\" version \""
                        +this.getClass().getPackage().getSpecificationVersion()
                        +"\""
        );
    }

    public void setSchema(Schema schema){
        throw new UnsupportedOperationException(
                "This parser does not support specification \""
                        +this.getClass().getPackage().getSpecificationTitle()
                        +"\" version \""
                        +this.getClass().getPackage().getSpecificationVersion()
                        +"\""
        );
    }

    public boolean isXIncludeAware(){
        throw new UnsupportedOperationException(
                "This parser does not support specification \""
                        +this.getClass().getPackage().getSpecificationTitle()
                        +"\" version \""
                        +this.getClass().getPackage().getSpecificationVersion()
                        +"\""
        );
    }

    public void setXIncludeAware(final boolean state){
        if(state){
            throw new UnsupportedOperationException(" setXIncludeAware "+
                    "is not supported on this JAXP"+
                    " implementation or earlier: "+this.getClass());
        }
    }
}
