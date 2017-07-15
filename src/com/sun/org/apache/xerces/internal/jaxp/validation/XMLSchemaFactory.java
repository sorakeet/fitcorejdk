/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class XMLSchemaFactory extends SchemaFactory{
    // property identifiers
    private static final String SCHEMA_FULL_CHECKING=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_FULL_CHECKING;
    private static final String XMLGRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String SECURITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SECURITY_MANAGER_PROPERTY;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    //
    // Data
    //
    private final XMLSchemaLoader fXMLSchemaLoader=new XMLSchemaLoader();
    private final DOMEntityResolverWrapper fDOMEntityResolverWrapper;
    private final boolean fUseServicesMechanism;
    private ErrorHandler fErrorHandler;
    private LSResourceResolver fLSResourceResolver;
    private ErrorHandlerWrapper fErrorHandlerWrapper;
    private XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;
    private XMLGrammarPoolWrapper fXMLGrammarPoolWrapper;

    public XMLSchemaFactory(){
        this(true);
    }

    private XMLSchemaFactory(boolean useServicesMechanism){
        fUseServicesMechanism=useServicesMechanism;
        fErrorHandlerWrapper=new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
        fDOMEntityResolverWrapper=new DOMEntityResolverWrapper();
        fXMLGrammarPoolWrapper=new XMLGrammarPoolWrapper();
        fXMLSchemaLoader.setFeature(SCHEMA_FULL_CHECKING,true);
        fXMLSchemaLoader.setProperty(XMLGRAMMAR_POOL,fXMLGrammarPoolWrapper);
        fXMLSchemaLoader.setEntityResolver(fDOMEntityResolverWrapper);
        fXMLSchemaLoader.setErrorHandler(fErrorHandlerWrapper);
        // Enable secure processing feature by default
        fSecurityManager=new XMLSecurityManager(true);
        fXMLSchemaLoader.setProperty(SECURITY_MANAGER,fSecurityManager);
        fSecurityPropertyMgr=new XMLSecurityPropertyManager();
        fXMLSchemaLoader.setProperty(XML_SECURITY_PROPERTY_MANAGER,
                fSecurityPropertyMgr);
    }

    public static XMLSchemaFactory newXMLSchemaFactoryNoServiceLoader(){
        return new XMLSchemaFactory(false);
    }

    public boolean isSchemaLanguageSupported(String schemaLanguage){
        if(schemaLanguage==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "SchemaLanguageNull",null));
        }
        if(schemaLanguage.length()==0){
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "SchemaLanguageLengthZero",null));
        }
        // only W3C XML Schema 1.0 is supported
        return schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "FeatureNameNull",null));
        }
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            return (fSecurityManager!=null&&fSecurityManager.isSecureProcessing());
        }
        try{
            return fXMLSchemaLoader.getFeature(name);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "feature-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "feature-not-supported",new Object[]{identifier}));
            }
        }
    }    public LSResourceResolver getResourceResolver(){
        return fLSResourceResolver;
    }

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "FeatureNameNull",null));
        }
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            if(System.getSecurityManager()!=null&&(!value)){
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(null,
                                "jaxp-secureprocessing-feature",null));
            }
            fSecurityManager.setSecureProcessing(value);
            if(value){
                if(Constants.IS_JDK8_OR_ABOVE){
                    fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD,
                            XMLSecurityPropertyManager.State.FSP,Constants.EXTERNAL_ACCESS_DEFAULT_FSP);
                    fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA,
                            XMLSecurityPropertyManager.State.FSP,Constants.EXTERNAL_ACCESS_DEFAULT_FSP);
                }
            }
            fXMLSchemaLoader.setProperty(SECURITY_MANAGER,fSecurityManager);
            return;
        }else if(name.equals(Constants.ORACLE_FEATURE_SERVICE_MECHANISM)){
            //in secure mode, let _useServicesMechanism be determined by the constructor
            if(System.getSecurityManager()!=null)
                return;
        }
        try{
            fXMLSchemaLoader.setFeature(name,value);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "feature-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "feature-not-supported",new Object[]{identifier}));
            }
        }
    }    public void setResourceResolver(LSResourceResolver resourceResolver){
        fLSResourceResolver=resourceResolver;
        fDOMEntityResolverWrapper.setEntityResolver(resourceResolver);
        fXMLSchemaLoader.setEntityResolver(fDOMEntityResolverWrapper);
    }

    public void setProperty(String name,Object object)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "ProperyNameNull",null));
        }
        if(name.equals(SECURITY_MANAGER)){
            fSecurityManager=XMLSecurityManager.convert(object,fSecurityManager);
            fXMLSchemaLoader.setProperty(SECURITY_MANAGER,fSecurityManager);
            return;
        }else if(name.equals(Constants.XML_SECURITY_PROPERTY_MANAGER)){
            if(object==null){
                fSecurityPropertyMgr=new XMLSecurityPropertyManager();
            }else{
                fSecurityPropertyMgr=(XMLSecurityPropertyManager)object;
            }
            fXMLSchemaLoader.setProperty(Constants.XML_SECURITY_PROPERTY_MANAGER,fSecurityPropertyMgr);
            return;
        }else if(name.equals(XMLGRAMMAR_POOL)){
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                            "property-not-supported",new Object[]{name}));
        }
        try{
            //check if the property is managed by security manager
            if(fSecurityManager==null||
                    !fSecurityManager.setLimit(name,XMLSecurityManager.State.APIPROPERTY,object)){
                //check if the property is managed by security property manager
                if(fSecurityPropertyMgr==null||
                        !fSecurityPropertyMgr.setValue(name,XMLSecurityPropertyManager.State.APIPROPERTY,object)){
                    //fall back to the existing property manager
                    fXMLSchemaLoader.setProperty(name,object);
                }
            }
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "property-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "property-not-supported",new Object[]{identifier}));
            }
        }
    }    public ErrorHandler getErrorHandler(){
        return fErrorHandler;
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                    "ProperyNameNull",null));
        }
        if(name.equals(SECURITY_MANAGER)){
            return fSecurityManager;
        }else if(name.equals(XMLGRAMMAR_POOL)){
            throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                            "property-not-supported",new Object[]{name}));
        }
        try{
            return fXMLSchemaLoader.getProperty(name);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "property-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                                "property-not-supported",new Object[]{identifier}));
            }
        }
    }    public void setErrorHandler(ErrorHandler errorHandler){
        fErrorHandler=errorHandler;
        fErrorHandlerWrapper.setErrorHandler(errorHandler!=null?errorHandler:DraconianErrorHandler.getInstance());
        fXMLSchemaLoader.setErrorHandler(fErrorHandlerWrapper);
    }

    static class XMLGrammarPoolImplExtension extends XMLGrammarPoolImpl{
        public XMLGrammarPoolImplExtension(){
            super();
        }

        public XMLGrammarPoolImplExtension(int initialCapacity){
            super(initialCapacity);
        }

        int getGrammarCount(){
            return fGrammarCount;
        }
    } // XMLSchemaFactory.XMLGrammarPoolImplExtension    public Schema newSchema(Source[] schemas) throws SAXException{
        // this will let the loader store parsed Grammars into the pool.
        XMLGrammarPoolImplExtension pool=new XMLGrammarPoolImplExtension();
        fXMLGrammarPoolWrapper.setGrammarPool(pool);
        XMLInputSource[] xmlInputSources=new XMLInputSource[schemas.length];
        InputStream inputStream;
        Reader reader;
        for(int i=0;i<schemas.length;i++){
            Source source=schemas[i];
            if(source instanceof StreamSource){
                StreamSource streamSource=(StreamSource)source;
                String publicId=streamSource.getPublicId();
                String systemId=streamSource.getSystemId();
                inputStream=streamSource.getInputStream();
                reader=streamSource.getReader();
                xmlInputSources[i]=new XMLInputSource(publicId,systemId,null);
                xmlInputSources[i].setByteStream(inputStream);
                xmlInputSources[i].setCharacterStream(reader);
            }else if(source instanceof SAXSource){
                SAXSource saxSource=(SAXSource)source;
                InputSource inputSource=saxSource.getInputSource();
                if(inputSource==null){
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                            "SAXSourceNullInputSource",null));
                }
                xmlInputSources[i]=new SAXInputSource(saxSource.getXMLReader(),inputSource);
            }else if(source instanceof DOMSource){
                DOMSource domSource=(DOMSource)source;
                Node node=domSource.getNode();
                String systemID=domSource.getSystemId();
                xmlInputSources[i]=new DOMInputSource(node,systemID);
            }else if(source instanceof StAXSource){
                StAXSource staxSource=(StAXSource)source;
                XMLEventReader eventReader=staxSource.getXMLEventReader();
                if(eventReader!=null){
                    xmlInputSources[i]=new StAXInputSource(eventReader);
                }else{
                    xmlInputSources[i]=new StAXInputSource(staxSource.getXMLStreamReader());
                }
            }else if(source==null){
                throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                        "SchemaSourceArrayMemberNull",null));
            }else{
                throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fXMLSchemaLoader.getLocale(),
                        "SchemaFactorySourceUnrecognized",
                        new Object[]{source.getClass().getName()}));
            }
        }
        try{
            fXMLSchemaLoader.loadGrammar(xmlInputSources);
        }catch(XNIException e){
            // this should have been reported to users already.
            throw Util.toSAXException(e);
        }catch(IOException e){
            // this hasn't been reported, so do so now.
            SAXParseException se=new SAXParseException(e.getMessage(),null,e);
            fErrorHandler.error(se);
            throw se; // and we must throw it.
        }
        // Clear reference to grammar pool.
        fXMLGrammarPoolWrapper.setGrammarPool(null);
        // Select Schema implementation based on grammar count.
        final int grammarCount=pool.getGrammarCount();
        AbstractXMLSchema schema=null;
        if(grammarCount>1){
            schema=new XMLSchema(new ReadOnlyGrammarPool(pool));
        }else if(grammarCount==1){
            Grammar[] grammars=pool.retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);
            schema=new SimpleXMLSchema(grammars[0]);
        }else{
            schema=new EmptyXMLSchema();
        }
        propagateFeatures(schema);
        propagateProperties(schema);
        return schema;
    }

    static class XMLGrammarPoolWrapper implements XMLGrammarPool{
        private XMLGrammarPool fGrammarPool;

        public Grammar[] retrieveInitialGrammarSet(String grammarType){
            return fGrammarPool.retrieveInitialGrammarSet(grammarType);
        }

        public void cacheGrammars(String grammarType,Grammar[] grammars){
            fGrammarPool.cacheGrammars(grammarType,grammars);
        }

        public Grammar retrieveGrammar(XMLGrammarDescription desc){
            return fGrammarPool.retrieveGrammar(desc);
        }

        public void lockPool(){
            fGrammarPool.lockPool();
        }

        public void unlockPool(){
            fGrammarPool.unlockPool();
        }

        public void clear(){
            fGrammarPool.clear();
        }

        XMLGrammarPool getGrammarPool(){
            return fGrammarPool;
        }

        void setGrammarPool(XMLGrammarPool grammarPool){
            fGrammarPool=grammarPool;
        }
    } // XMLSchemaFactory.XMLGrammarPoolWrapper    public Schema newSchema() throws SAXException{
        // Use a Schema that uses the system id as the equality source.
        AbstractXMLSchema schema=new WeakReferenceXMLSchema();
        propagateFeatures(schema);
        propagateProperties(schema);
        return schema;
    }









    private void propagateFeatures(AbstractXMLSchema schema){
        schema.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
                (fSecurityManager!=null&&fSecurityManager.isSecureProcessing()));
        schema.setFeature(Constants.ORACLE_FEATURE_SERVICE_MECHANISM,fUseServicesMechanism);
        String[] features=fXMLSchemaLoader.getRecognizedFeatures();
        for(int i=0;i<features.length;++i){
            boolean state=fXMLSchemaLoader.getFeature(features[i]);
            schema.setFeature(features[i],state);
        }
    }

    private void propagateProperties(AbstractXMLSchema schema){
        String[] properties=fXMLSchemaLoader.getRecognizedProperties();
        for(int i=0;i<properties.length;++i){
            Object state=fXMLSchemaLoader.getProperty(properties[i]);
            schema.setProperty(properties[i],state);
        }
    }




} // XMLSchemaFactory
