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
package javax.xml.stream.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public interface XMLEventAllocator{
    public XMLEventAllocator newInstance();

    public XMLEvent allocate(XMLStreamReader reader)
            throws XMLStreamException;

    public void allocate(XMLStreamReader reader,XMLEventConsumer consumer)
            throws XMLStreamException;
}
