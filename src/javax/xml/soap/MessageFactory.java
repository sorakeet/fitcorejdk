/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import java.io.IOException;
import java.io.InputStream;

public abstract class MessageFactory{
    static final String DEFAULT_MESSAGE_FACTORY
            ="com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl";
    static private final String MESSAGE_FACTORY_PROPERTY
            ="javax.xml.soap.MessageFactory";

    public static MessageFactory newInstance() throws SOAPException{
        try{
            MessageFactory factory=(MessageFactory)FactoryFinder.find(
                    MESSAGE_FACTORY_PROPERTY,
                    DEFAULT_MESSAGE_FACTORY,
                    false);
            if(factory!=null){
                return factory;
            }
            return newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        }catch(Exception ex){
            throw new SOAPException(
                    "Unable to create message factory for SOAP: "
                            +ex.getMessage());
        }
    }

    public static MessageFactory newInstance(String protocol) throws SOAPException{
        return SAAJMetaFactory.getInstance().newMessageFactory(protocol);
    }

    public abstract SOAPMessage createMessage()
            throws SOAPException;

    public abstract SOAPMessage createMessage(MimeHeaders headers,
                                              InputStream in)
            throws IOException, SOAPException;
}
