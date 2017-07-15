/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.validation.Schema;

public abstract class Binder<XmlNode>{
    public abstract Object unmarshal(XmlNode xmlNode) throws JAXBException;

    public abstract <T> JAXBElement<T>
    unmarshal(XmlNode xmlNode,Class<T> declaredType)
            throws JAXBException;

    public abstract void marshal(Object jaxbObject,XmlNode xmlNode) throws JAXBException;

    public abstract XmlNode getXMLNode(Object jaxbObject);

    public abstract Object getJAXBNode(XmlNode xmlNode);

    public abstract XmlNode updateXML(Object jaxbObject) throws JAXBException;

    public abstract XmlNode updateXML(Object jaxbObject,XmlNode xmlNode) throws JAXBException;

    public abstract Object updateJAXB(XmlNode xmlNode) throws JAXBException;

    public abstract Schema getSchema();

    public abstract void setSchema(Schema schema);

    public abstract ValidationEventHandler getEventHandler() throws JAXBException;

    public abstract void setEventHandler(ValidationEventHandler handler) throws JAXBException;

    abstract public void setProperty(String name,Object value) throws PropertyException;

    abstract public Object getProperty(String name) throws PropertyException;
}
