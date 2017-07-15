/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public abstract class SAAJMetaFactory{
    static final String DEFAULT_META_FACTORY_CLASS=
            "com.sun.xml.internal.messaging.saaj.soap.SAAJMetaFactoryImpl";
    static private final String META_FACTORY_CLASS_PROPERTY=
            "javax.xml.soap.MetaFactory";

    protected SAAJMetaFactory(){
    }

    static SAAJMetaFactory getInstance() throws SOAPException{
        try{
            SAAJMetaFactory instance=
                    (SAAJMetaFactory)FactoryFinder.find(
                            META_FACTORY_CLASS_PROPERTY,
                            DEFAULT_META_FACTORY_CLASS);
            return instance;
        }catch(Exception e){
            throw new SOAPException(
                    "Unable to create SAAJ meta-factory"+e.getMessage());
        }
    }

    protected abstract MessageFactory newMessageFactory(String protocol)
            throws SOAPException;

    protected abstract SOAPFactory newSOAPFactory(String protocol)
            throws SOAPException;
}
