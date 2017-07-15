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

import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

public interface XMLEventReader extends Iterator{
    public XMLEvent nextEvent() throws XMLStreamException;

    public boolean hasNext();

    public XMLEvent peek() throws XMLStreamException;

    public String getElementText() throws XMLStreamException;

    public XMLEvent nextTag() throws XMLStreamException;

    public Object getProperty(String name) throws IllegalArgumentException;

    public void close() throws XMLStreamException;
}
