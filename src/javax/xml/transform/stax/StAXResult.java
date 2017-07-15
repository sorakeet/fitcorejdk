/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.stax;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

public class StAXResult implements Result{
    public static final String FEATURE=
            "http://javax.xml.transform.stax.StAXResult/feature";
    private XMLEventWriter xmlEventWriter=null;
    private XMLStreamWriter xmlStreamWriter=null;
    private String systemId=null;

    public StAXResult(final XMLEventWriter xmlEventWriter){
        if(xmlEventWriter==null){
            throw new IllegalArgumentException(
                    "StAXResult(XMLEventWriter) with XMLEventWriter == null");
        }
        this.xmlEventWriter=xmlEventWriter;
    }

    public StAXResult(final XMLStreamWriter xmlStreamWriter){
        if(xmlStreamWriter==null){
            throw new IllegalArgumentException(
                    "StAXResult(XMLStreamWriter) with XMLStreamWriter == null");
        }
        this.xmlStreamWriter=xmlStreamWriter;
    }

    public XMLEventWriter getXMLEventWriter(){
        return xmlEventWriter;
    }

    public XMLStreamWriter getXMLStreamWriter(){
        return xmlStreamWriter;
    }

    public void setSystemId(final String systemId){
        throw new UnsupportedOperationException(
                "StAXResult#setSystemId(systemId) cannot set the "
                        +"system identifier for a StAXResult");
    }

    public String getSystemId(){
        return null;
    }
}
