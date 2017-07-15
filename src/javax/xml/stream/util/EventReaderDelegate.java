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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class EventReaderDelegate implements XMLEventReader{
    private XMLEventReader reader;

    public EventReaderDelegate(){
    }

    public EventReaderDelegate(XMLEventReader reader){
        this.reader=reader;
    }

    public XMLEventReader getParent(){
        return reader;
    }

    public void setParent(XMLEventReader reader){
        this.reader=reader;
    }

    public XMLEvent nextEvent()
            throws XMLStreamException{
        return reader.nextEvent();
    }

    public boolean hasNext(){
        return reader.hasNext();
    }

    public XMLEvent peek()
            throws XMLStreamException{
        return reader.peek();
    }

    public String getElementText()
            throws XMLStreamException{
        return reader.getElementText();
    }

    public XMLEvent nextTag()
            throws XMLStreamException{
        return reader.nextTag();
    }

    public Object getProperty(String name)
            throws IllegalArgumentException{
        return reader.getProperty(name);
    }

    public void close()
            throws XMLStreamException{
        reader.close();
    }

    public Object next(){
        return reader.next();
    }

    public void remove(){
        reader.remove();
    }
}
