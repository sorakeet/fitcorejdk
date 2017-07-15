/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public abstract class SOAPFactory{
    static final String DEFAULT_SOAP_FACTORY
            ="com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl";
    static private final String SOAP_FACTORY_PROPERTY=
            "javax.xml.soap.SOAPFactory";

    public static SOAPFactory newInstance()
            throws SOAPException{
        try{
            SOAPFactory factory=(SOAPFactory)FactoryFinder.find(
                    SOAP_FACTORY_PROPERTY,DEFAULT_SOAP_FACTORY,false);
            if(factory!=null)
                return factory;
            return newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        }catch(Exception ex){
            throw new SOAPException(
                    "Unable to create SOAP Factory: "+ex.getMessage());
        }
    }

    public static SOAPFactory newInstance(String protocol)
            throws SOAPException{
        return SAAJMetaFactory.getInstance().newSOAPFactory(protocol);
    }

    public SOAPElement createElement(Element domElement) throws SOAPException{
        throw new UnsupportedOperationException("createElement(org.w3c.dom.Element) must be overridden by all subclasses of SOAPFactory.");
    }

    public abstract SOAPElement createElement(Name name) throws SOAPException;

    public SOAPElement createElement(QName qname) throws SOAPException{
        throw new UnsupportedOperationException("createElement(QName) must be overridden by all subclasses of SOAPFactory.");
    }

    public abstract SOAPElement createElement(String localName)
            throws SOAPException;

    public abstract SOAPElement createElement(
            String localName,
            String prefix,
            String uri)
            throws SOAPException;

    public abstract Detail createDetail() throws SOAPException;

    public abstract SOAPFault createFault(String reasonText,QName faultCode) throws SOAPException;

    public abstract SOAPFault createFault() throws SOAPException;

    public abstract Name createName(
            String localName,
            String prefix,
            String uri)
            throws SOAPException;

    public abstract Name createName(String localName) throws SOAPException;
}
