/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2009, 2013, by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009, 2013, by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream;

import javax.xml.transform.Result;

public abstract class XMLOutputFactory{
    public static final String IS_REPAIRING_NAMESPACES=
            "javax.xml.stream.isRepairingNamespaces";
    static final String DEFAULIMPL="com.sun.xml.internal.stream.XMLOutputFactoryImpl";

    protected XMLOutputFactory(){
    }

    public static XMLOutputFactory newInstance()
            throws FactoryConfigurationError{
        return FactoryFinder.find(XMLOutputFactory.class,DEFAULIMPL);
    }

    public static XMLOutputFactory newFactory()
            throws FactoryConfigurationError{
        return FactoryFinder.find(XMLOutputFactory.class,DEFAULIMPL);
    }

    public static XMLInputFactory newInstance(String factoryId,
                                              ClassLoader classLoader)
            throws FactoryConfigurationError{
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.find(XMLInputFactory.class,factoryId,classLoader,null);
    }

    public static XMLOutputFactory newFactory(String factoryId,
                                              ClassLoader classLoader)
            throws FactoryConfigurationError{
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.find(XMLOutputFactory.class,factoryId,classLoader,null);
    }

    public abstract XMLStreamWriter createXMLStreamWriter(java.io.Writer stream) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(java.io.OutputStream stream) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(java.io.OutputStream stream,
                                                          String encoding) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(java.io.OutputStream stream) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(java.io.OutputStream stream,
                                                        String encoding) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(java.io.Writer stream) throws XMLStreamException;

    public abstract void setProperty(String name,
                                     Object value)
            throws IllegalArgumentException;

    public abstract Object getProperty(String name)
            throws IllegalArgumentException;

    public abstract boolean isPropertySupported(String name);
}
