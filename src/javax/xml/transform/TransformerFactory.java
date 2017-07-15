/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

public abstract class TransformerFactory{
    protected TransformerFactory(){
    }

    public static TransformerFactory newInstance()
            throws TransformerFactoryConfigurationError{
        return FactoryFinder.find(
                /** The default property name according to the JAXP spec */
                TransformerFactory.class,
                /** The fallback implementation class name, XSLTC */
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    }

    public static TransformerFactory newInstance(String factoryClassName,ClassLoader classLoader)
            throws TransformerFactoryConfigurationError{
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.newInstance(TransformerFactory.class,
                factoryClassName,classLoader,false,false);
    }

    public abstract Transformer newTransformer(Source source)
            throws TransformerConfigurationException;

    public abstract Transformer newTransformer()
            throws TransformerConfigurationException;

    public abstract Templates newTemplates(Source source)
            throws TransformerConfigurationException;

    public abstract Source getAssociatedStylesheet(
            Source source,
            String media,
            String title,
            String charset)
            throws TransformerConfigurationException;

    public abstract URIResolver getURIResolver();

    public abstract void setURIResolver(URIResolver resolver);
    //======= CONFIGURATION METHODS =======

    public abstract void setFeature(String name,boolean value)
            throws TransformerConfigurationException;

    public abstract boolean getFeature(String name);

    public abstract void setAttribute(String name,Object value);

    public abstract Object getAttribute(String name);

    public abstract ErrorListener getErrorListener();

    public abstract void setErrorListener(ErrorListener listener);
}
