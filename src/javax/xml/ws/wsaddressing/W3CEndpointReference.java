/**
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.wsaddressing;

import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

// XmlRootElement allows this class to be marshalled on its own
@XmlRootElement(name="EndpointReference", namespace=W3CEndpointReference.NS)
@XmlType(name="EndpointReferenceType", namespace=W3CEndpointReference.NS)
public final class W3CEndpointReference extends EndpointReference{
    // should be changed to package private, keeping original modifier to keep backwards compatibility
    protected static final String NS="http://www.w3.org/2005/08/addressing";
    private final JAXBContext w3cjc=getW3CJaxbContext();
    // attributes and elements are not private for performance reasons
    // (JAXB can bypass reflection)
    @XmlAnyAttribute
    Map<QName,String> attributes;
    @XmlAnyElement
    List<Element> elements;
    // private but necessary properties for databinding
    @XmlElement(name="Address", namespace=NS)
    private Address address;
    @XmlElement(name="ReferenceParameters", namespace=NS)
    private Elements referenceParameters;
    @XmlElement(name="Metadata", namespace=NS)
    private Elements metadata;
    // default constructor forbidden ...
    // should be private, keeping original modifier to keep backwards compatibility
    protected W3CEndpointReference(){
    }
    public W3CEndpointReference(Source source){
        try{
            W3CEndpointReference epr=w3cjc.createUnmarshaller().unmarshal(source,W3CEndpointReference.class).getValue();
            this.address=epr.address;
            this.metadata=epr.metadata;
            this.referenceParameters=epr.referenceParameters;
            this.elements=epr.elements;
            this.attributes=epr.attributes;
        }catch(JAXBException e){
            throw new WebServiceException("Error unmarshalling W3CEndpointReference ",e);
        }catch(ClassCastException e){
            throw new WebServiceException("Source did not contain W3CEndpointReference",e);
        }
    }

    private static JAXBContext getW3CJaxbContext(){
        try{
            return JAXBContext.newInstance(W3CEndpointReference.class);
        }catch(JAXBException e){
            throw new WebServiceException("Error creating JAXBContext for W3CEndpointReference. ",e);
        }
    }

    public void writeTo(Result result){
        try{
            Marshaller marshaller=w3cjc.createMarshaller();
            marshaller.marshal(this,result);
        }catch(JAXBException e){
            throw new WebServiceException("Error marshalling W3CEndpointReference. ",e);
        }
    }

    @XmlType(name="address", namespace=W3CEndpointReference.NS)
    private static class Address{
        @XmlValue
        String uri;
        @XmlAnyAttribute
        Map<QName,String> attributes;
        protected Address(){
        }
    }

    @XmlType(name="elements", namespace=W3CEndpointReference.NS)
    private static class Elements{
        @XmlAnyElement
        List<Element> elements;
        @XmlAnyAttribute
        Map<QName,String> attributes;
        protected Elements(){
        }
    }
}
