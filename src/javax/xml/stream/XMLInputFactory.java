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

import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

public abstract class XMLInputFactory{
    public static final String IS_NAMESPACE_AWARE=
            "javax.xml.stream.isNamespaceAware";
    public static final String IS_VALIDATING=
            "javax.xml.stream.isValidating";
    public static final String IS_COALESCING=
            "javax.xml.stream.isCoalescing";
    public static final String IS_REPLACING_ENTITY_REFERENCES=
            "javax.xml.stream.isReplacingEntityReferences";
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES=
            "javax.xml.stream.isSupportingExternalEntities";
    public static final String SUPPORT_DTD=
            "javax.xml.stream.supportDTD";
    public static final String REPORTER=
            "javax.xml.stream.reporter";
    public static final String RESOLVER=
            "javax.xml.stream.resolver";
    public static final String ALLOCATOR=
            "javax.xml.stream.allocator";
    static final String DEFAULIMPL="com.sun.xml.internal.stream.XMLInputFactoryImpl";

    protected XMLInputFactory(){
    }

    public static XMLInputFactory newInstance()
            throws FactoryConfigurationError{
        return FactoryFinder.find(XMLInputFactory.class,DEFAULIMPL);
    }

    public static XMLInputFactory newFactory()
            throws FactoryConfigurationError{
        return FactoryFinder.find(XMLInputFactory.class,DEFAULIMPL);
    }

    public static XMLInputFactory newInstance(String factoryId,
                                              ClassLoader classLoader)
            throws FactoryConfigurationError{
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.find(XMLInputFactory.class,factoryId,classLoader,null);
    }

    public static XMLInputFactory newFactory(String factoryId,
                                             ClassLoader classLoader)
            throws FactoryConfigurationError{
        //do not fallback if given classloader can't find the class, throw exception
        return FactoryFinder.find(XMLInputFactory.class,factoryId,classLoader,null);
    }

    public abstract XMLStreamReader createXMLStreamReader(java.io.Reader reader)
            throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(Source source)
            throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(java.io.InputStream stream)
            throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(java.io.InputStream stream,String encoding)
            throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String systemId,java.io.InputStream stream)
            throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String systemId,java.io.Reader reader)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(java.io.Reader reader)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String systemId,java.io.Reader reader)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(XMLStreamReader reader)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(Source source)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(java.io.InputStream stream)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(java.io.InputStream stream,String encoding)
            throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String systemId,java.io.InputStream stream)
            throws XMLStreamException;

    public abstract XMLStreamReader createFilteredReader(XMLStreamReader reader,StreamFilter filter)
            throws XMLStreamException;

    public abstract XMLEventReader createFilteredReader(XMLEventReader reader,EventFilter filter)
            throws XMLStreamException;

    public abstract XMLResolver getXMLResolver();

    public abstract void setXMLResolver(XMLResolver resolver);

    public abstract XMLReporter getXMLReporter();

    public abstract void setXMLReporter(XMLReporter reporter);

    public abstract void setProperty(String name,Object value)
            throws IllegalArgumentException;

    public abstract Object getProperty(String name)
            throws IllegalArgumentException;

    public abstract boolean isPropertySupported(String name);

    public abstract XMLEventAllocator getEventAllocator();

    public abstract void setEventAllocator(XMLEventAllocator allocator);
}
