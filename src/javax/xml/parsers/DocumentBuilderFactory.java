/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.parsers;

import javax.xml.validation.Schema;

public abstract class DocumentBuilderFactory{
    private boolean validating=false;
    private boolean namespaceAware=false;
    private boolean whitespace=false;
    private boolean expandEntityRef=true;
    private boolean ignoreComments=false;
    private boolean coalescing=false;

    protected DocumentBuilderFactory(){
    }

    public static DocumentBuilderFactory newInstance(){
        return FactoryFinder.find(
                /** The default property name according to the JAXP spec */
                DocumentBuilderFactory.class, // "javax.xml.parsers.DocumentBuilderFactory"
                /** The fallback implementation class name */
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }

    public static DocumentBuilderFactory newInstance(String factoryClassName,ClassLoader classLoader){
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.newInstance(DocumentBuilderFactory.class,
                factoryClassName,classLoader,false);
    }

    public abstract DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException;

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

    public boolean isIgnoringElementContentWhitespace(){
        return whitespace;
    }

    public void setIgnoringElementContentWhitespace(boolean whitespace){
        this.whitespace=whitespace;
    }

    public boolean isExpandEntityReferences(){
        return expandEntityRef;
    }

    public void setExpandEntityReferences(boolean expandEntityRef){
        this.expandEntityRef=expandEntityRef;
    }

    public boolean isIgnoringComments(){
        return ignoreComments;
    }

    public void setIgnoringComments(boolean ignoreComments){
        this.ignoreComments=ignoreComments;
    }

    public boolean isCoalescing(){
        return coalescing;
    }

    public void setCoalescing(boolean coalescing){
        this.coalescing=coalescing;
    }

    public abstract void setAttribute(String name,Object value)
            throws IllegalArgumentException;

    public abstract Object getAttribute(String name)
            throws IllegalArgumentException;

    public abstract void setFeature(String name,boolean value)
            throws ParserConfigurationException;

    public abstract boolean getFeature(String name)
            throws ParserConfigurationException;

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
