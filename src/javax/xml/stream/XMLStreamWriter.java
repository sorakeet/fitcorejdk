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
 * Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream;

import javax.xml.namespace.NamespaceContext;

public interface XMLStreamWriter{
    public void writeStartElement(String localName)
            throws XMLStreamException;

    public void writeStartElement(String namespaceURI,String localName)
            throws XMLStreamException;

    public void writeStartElement(String prefix,
                                  String localName,
                                  String namespaceURI)
            throws XMLStreamException;

    public void writeEmptyElement(String namespaceURI,String localName)
            throws XMLStreamException;

    public void writeEmptyElement(String prefix,String localName,String namespaceURI)
            throws XMLStreamException;

    public void writeEmptyElement(String localName)
            throws XMLStreamException;
    //  public void writeRaw(String data) throws XMLStreamException;

    public void writeEndElement()
            throws XMLStreamException;

    public void writeEndDocument()
            throws XMLStreamException;

    public void close()
            throws XMLStreamException;

    public void flush()
            throws XMLStreamException;

    public void writeAttribute(String localName,String value)
            throws XMLStreamException;

    public void writeAttribute(String prefix,
                               String namespaceURI,
                               String localName,
                               String value)
            throws XMLStreamException;

    public void writeAttribute(String namespaceURI,
                               String localName,
                               String value)
            throws XMLStreamException;

    public void writeNamespace(String prefix,String namespaceURI)
            throws XMLStreamException;

    public void writeDefaultNamespace(String namespaceURI)
            throws XMLStreamException;

    public void writeComment(String data)
            throws XMLStreamException;

    public void writeProcessingInstruction(String target)
            throws XMLStreamException;

    public void writeProcessingInstruction(String target,
                                           String data)
            throws XMLStreamException;

    public void writeCData(String data)
            throws XMLStreamException;

    public void writeDTD(String dtd)
            throws XMLStreamException;

    public void writeEntityRef(String name)
            throws XMLStreamException;

    public void writeStartDocument()
            throws XMLStreamException;

    public void writeStartDocument(String version)
            throws XMLStreamException;

    public void writeStartDocument(String encoding,
                                   String version)
            throws XMLStreamException;

    public void writeCharacters(String text)
            throws XMLStreamException;

    public void writeCharacters(char[] text,int start,int len)
            throws XMLStreamException;

    public String getPrefix(String uri)
            throws XMLStreamException;

    public void setPrefix(String prefix,String uri)
            throws XMLStreamException;

    public void setDefaultNamespace(String uri)
            throws XMLStreamException;

    public NamespaceContext getNamespaceContext();

    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException;

    public Object getProperty(String name) throws IllegalArgumentException;
}
