/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.stax;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;

public class StAXSource implements Source{
    public static final String FEATURE=
            "http://javax.xml.transform.stax.StAXSource/feature";
    private XMLEventReader xmlEventReader=null;
    private XMLStreamReader xmlStreamReader=null;
    private String systemId=null;

    public StAXSource(final XMLEventReader xmlEventReader)
            throws XMLStreamException{
        if(xmlEventReader==null){
            throw new IllegalArgumentException(
                    "StAXSource(XMLEventReader) with XMLEventReader == null");
        }
        // TODO: This is ugly ...
        // there is no way to know the current position(event) of
        // XMLEventReader.  peek() is the only way to know the next event.
        // The next event on the input stream should be
        // XMLStreamConstants.START_DOCUMENT or
        // XMLStreamConstants.START_ELEMENT.
        XMLEvent event=xmlEventReader.peek();
        int eventType=event.getEventType();
        if(eventType!=XMLStreamConstants.START_DOCUMENT
                &&eventType!=XMLStreamConstants.START_ELEMENT){
            throw new IllegalStateException(
                    "StAXSource(XMLEventReader) with XMLEventReader "
                            +"not in XMLStreamConstants.START_DOCUMENT or "
                            +"XMLStreamConstants.START_ELEMENT state");
        }
        this.xmlEventReader=xmlEventReader;
        systemId=event.getLocation().getSystemId();
    }

    public StAXSource(final XMLStreamReader xmlStreamReader){
        if(xmlStreamReader==null){
            throw new IllegalArgumentException(
                    "StAXSource(XMLStreamReader) with XMLStreamReader == null");
        }
        int eventType=xmlStreamReader.getEventType();
        if(eventType!=XMLStreamConstants.START_DOCUMENT
                &&eventType!=XMLStreamConstants.START_ELEMENT){
            throw new IllegalStateException(
                    "StAXSource(XMLStreamReader) with XMLStreamReader"
                            +"not in XMLStreamConstants.START_DOCUMENT or "
                            +"XMLStreamConstants.START_ELEMENT state");
        }
        this.xmlStreamReader=xmlStreamReader;
        systemId=xmlStreamReader.getLocation().getSystemId();
    }

    public XMLEventReader getXMLEventReader(){
        return xmlEventReader;
    }

    public XMLStreamReader getXMLStreamReader(){
        return xmlStreamReader;
    }

    public void setSystemId(final String systemId){
        throw new UnsupportedOperationException(
                "StAXSource#setSystemId(systemId) cannot set the "
                        +"system identifier for a StAXSource");
    }

    public String getSystemId(){
        return systemId;
    }
}
