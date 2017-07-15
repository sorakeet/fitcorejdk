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
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

public interface XMLEventWriter extends XMLEventConsumer{
    public void flush() throws XMLStreamException;

    public void close() throws XMLStreamException;

    public void add(XMLEvent event) throws XMLStreamException;

    public void add(XMLEventReader reader) throws XMLStreamException;

    public String getPrefix(String uri) throws XMLStreamException;

    public void setPrefix(String prefix,String uri) throws XMLStreamException;

    public void setDefaultNamespace(String uri) throws XMLStreamException;

    public NamespaceContext getNamespaceContext();

    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException;
}
