/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2005 The Apache Software Foundation.
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
 * Copyright 2000-2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.LocatorImpl;

import java.io.IOException;

public class DOMParser
        extends AbstractDOMParser{
    //
    // Constants
    //
    // features
    protected static final String USE_ENTITY_RESOLVER2=
            Constants.SAX_FEATURE_PREFIX+Constants.USE_ENTITY_RESOLVER2_FEATURE;
    protected static final String REPORT_WHITESPACE=
            Constants.SUN_SCHEMA_FEATURE_PREFIX+Constants.SUN_REPORT_IGNORED_ELEMENT_CONTENT_WHITESPACE;
    // properties
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String XMLGRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    // recognized features:
    private static final String[] RECOGNIZED_FEATURES={
            REPORT_WHITESPACE
    };
    private static final String[] RECOGNIZED_PROPERTIES={
            SYMBOL_TABLE,
            XMLGRAMMAR_POOL,
    };
    //
    // Data
    //
    // features
    protected boolean fUseEntityResolver2=true;
    //
    // Constructors
    //

    public DOMParser(XMLParserConfiguration config){
        super(config);
    } // <init>(XMLParserConfiguration)

    public DOMParser(){
        this(null,null);
    } // <init>()

    public DOMParser(SymbolTable symbolTable,XMLGrammarPool grammarPool){
        super(new XIncludeAwareParserConfiguration());
        // set properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if(symbolTable!=null){
            fConfiguration.setProperty(SYMBOL_TABLE,symbolTable);
        }
        if(grammarPool!=null){
            fConfiguration.setProperty(XMLGRAMMAR_POOL,grammarPool);
        }
        fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
    } // <init>(SymbolTable,XMLGrammarPool)

    public DOMParser(SymbolTable symbolTable){
        this(symbolTable,null);
    } // <init>(SymbolTable)
    //
    // XMLReader methods
    //

    public void parse(String systemId) throws SAXException, IOException{
        // parse document
        XMLInputSource source=new XMLInputSource(null,systemId,null);
        try{
            parse(source);
        }
        // wrap XNI exceptions as SAX exceptions
        catch(XMLParseException e){
            Exception ex=e.getException();
            if(ex==null){
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                LocatorImpl locatorImpl=new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw new SAXParseException(e.getMessage(),locatorImpl);
            }
            if(ex instanceof SAXException){
                // why did we create an XMLParseException?
                throw (SAXException)ex;
            }
            if(ex instanceof IOException){
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }catch(XNIException e){
            e.printStackTrace();
            Exception ex=e.getException();
            if(ex==null){
                throw new SAXException(e.getMessage());
            }
            if(ex instanceof SAXException){
                throw (SAXException)ex;
            }
            if(ex instanceof IOException){
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
    } // parse(String)

    public void parse(InputSource inputSource)
            throws SAXException, IOException{
        // parse document
        try{
            XMLInputSource xmlInputSource=
                    new XMLInputSource(inputSource.getPublicId(),
                            inputSource.getSystemId(),
                            null);
            xmlInputSource.setByteStream(inputSource.getByteStream());
            xmlInputSource.setCharacterStream(inputSource.getCharacterStream());
            xmlInputSource.setEncoding(inputSource.getEncoding());
            parse(xmlInputSource);
        }
        // wrap XNI exceptions as SAX exceptions
        catch(XMLParseException e){
            Exception ex=e.getException();
            if(ex==null){
                // must be a parser exception; mine it for locator info and throw
                // a SAXParseException
                LocatorImpl locatorImpl=new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw new SAXParseException(e.getMessage(),locatorImpl);
            }
            if(ex instanceof SAXException){
                // why did we create an XMLParseException?
                throw (SAXException)ex;
            }
            if(ex instanceof IOException){
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }catch(XNIException e){
            Exception ex=e.getException();
            if(ex==null){
                throw new SAXException(e.getMessage());
            }
            if(ex instanceof SAXException){
                throw (SAXException)ex;
            }
            if(ex instanceof IOException){
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
    } // parse(InputSource)

    public ErrorHandler getErrorHandler(){
        ErrorHandler errorHandler=null;
        try{
            XMLErrorHandler xmlErrorHandler=
                    (XMLErrorHandler)fConfiguration.getProperty(ERROR_HANDLER);
            if(xmlErrorHandler!=null&&
                    xmlErrorHandler instanceof ErrorHandlerWrapper){
                errorHandler=((ErrorHandlerWrapper)xmlErrorHandler).getErrorHandler();
            }
        }catch(XMLConfigurationException e){
            // do nothing
        }
        return errorHandler;
    } // getErrorHandler():ErrorHandler

    public void setErrorHandler(ErrorHandler errorHandler){
        try{
            XMLErrorHandler xeh=(XMLErrorHandler)fConfiguration.getProperty(ERROR_HANDLER);
            if(xeh instanceof ErrorHandlerWrapper){
                ErrorHandlerWrapper ehw=(ErrorHandlerWrapper)xeh;
                ehw.setErrorHandler(errorHandler);
            }else{
                fConfiguration.setProperty(ERROR_HANDLER,
                        new ErrorHandlerWrapper(errorHandler));
            }
        }catch(XMLConfigurationException e){
            // do nothing
        }
    } // setErrorHandler(ErrorHandler)

    public void setFeature(String featureId,boolean state)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        try{
            // http://xml.org/sax/features/use-entity-resolver2
            //   controls whether the methods of an object implementing
            //   org.xml.sax.ext.EntityResolver2 will be used by the parser.
            //
            if(featureId.equals(USE_ENTITY_RESOLVER2)){
                if(state!=fUseEntityResolver2){
                    fUseEntityResolver2=state;
                    // Refresh EntityResolver wrapper.
                    setEntityResolver(getEntityResolver());
                }
                return;
            }
            //
            // Default handling
            //
            fConfiguration.setFeature(featureId,state);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "feature-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "feature-not-supported",new Object[]{identifier}));
            }
        }
    } // setFeature(String,boolean)

    public EntityResolver getEntityResolver(){
        EntityResolver entityResolver=null;
        try{
            XMLEntityResolver xmlEntityResolver=
                    (XMLEntityResolver)fConfiguration.getProperty(ENTITY_RESOLVER);
            if(xmlEntityResolver!=null){
                if(xmlEntityResolver instanceof EntityResolverWrapper){
                    entityResolver=
                            ((EntityResolverWrapper)xmlEntityResolver).getEntityResolver();
                }else if(xmlEntityResolver instanceof EntityResolver2Wrapper){
                    entityResolver=
                            ((EntityResolver2Wrapper)xmlEntityResolver).getEntityResolver();
                }
            }
        }catch(XMLConfigurationException e){
            // do nothing
        }
        return entityResolver;
    } // getEntityResolver():EntityResolver

    public void setEntityResolver(EntityResolver resolver){
        try{
            XMLEntityResolver xer=(XMLEntityResolver)fConfiguration.getProperty(ENTITY_RESOLVER);
            if(fUseEntityResolver2&&resolver instanceof EntityResolver2){
                if(xer instanceof EntityResolver2Wrapper){
                    EntityResolver2Wrapper er2w=(EntityResolver2Wrapper)xer;
                    er2w.setEntityResolver((EntityResolver2)resolver);
                }else{
                    fConfiguration.setProperty(ENTITY_RESOLVER,
                            new EntityResolver2Wrapper((EntityResolver2)resolver));
                }
            }else{
                if(xer instanceof EntityResolverWrapper){
                    EntityResolverWrapper erw=(EntityResolverWrapper)xer;
                    erw.setEntityResolver(resolver);
                }else{
                    fConfiguration.setProperty(ENTITY_RESOLVER,
                            new EntityResolverWrapper(resolver));
                }
            }
        }catch(XMLConfigurationException e){
            // do nothing
        }
    } // setEntityResolver(EntityResolver)

    public void setProperty(String propertyId,Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        /**
         * It's possible for users to set a security manager through the interface.
         * If it's the old SecurityManager, convert it to the new XMLSecurityManager
         */
        if(propertyId.equals(Constants.SECURITY_MANAGER)){
            securityManager=XMLSecurityManager.convert(value,securityManager);
            setProperty0(Constants.SECURITY_MANAGER,securityManager);
            return;
        }
        if(propertyId.equals(Constants.XML_SECURITY_PROPERTY_MANAGER)){
            if(value==null){
                securityPropertyManager=new XMLSecurityPropertyManager();
            }else{
                securityPropertyManager=(XMLSecurityPropertyManager)value;
            }
            setProperty0(Constants.XML_SECURITY_PROPERTY_MANAGER,securityPropertyManager);
            return;
        }
        if(securityManager==null){
            securityManager=new XMLSecurityManager(true);
            setProperty0(Constants.SECURITY_MANAGER,securityManager);
        }
        if(securityPropertyManager==null){
            securityPropertyManager=new XMLSecurityPropertyManager();
            setProperty0(Constants.XML_SECURITY_PROPERTY_MANAGER,securityPropertyManager);
        }
        int index=securityPropertyManager.getIndex(propertyId);
        if(index>-1){
            /**
             * this is a direct call to this parser, not a subclass since
             * internally the support of this property is done through
             * XMLSecurityPropertyManager
             */
            securityPropertyManager.setValue(index,XMLSecurityPropertyManager.State.APIPROPERTY,(String)value);
        }else{
            //check if the property is managed by security manager
            if(!securityManager.setLimit(propertyId,XMLSecurityManager.State.APIPROPERTY,value)){
                //fall back to the default configuration to handle the property
                setProperty0(propertyId,value);
            }
        }
    }

    public void setProperty0(String propertyId,Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        try{
            fConfiguration.setProperty(propertyId,value);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "property-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "property-not-supported",new Object[]{identifier}));
            }
        }
    } // setProperty(String,Object)

    public Object getProperty(String propertyId)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(propertyId.equals(CURRENT_ELEMENT_NODE)){
            boolean deferred=false;
            try{
                deferred=getFeature(DEFER_NODE_EXPANSION);
            }catch(XMLConfigurationException e){
                // ignore
            }
            if(deferred){
                throw new SAXNotSupportedException("Current element node cannot be queried when node expansion is deferred.");
            }
            return (fCurrentNode!=null&&
                    fCurrentNode.getNodeType()==Node.ELEMENT_NODE)?fCurrentNode:null;
        }
        try{
            XMLSecurityPropertyManager spm=(XMLSecurityPropertyManager)
                    fConfiguration.getProperty(XML_SECURITY_PROPERTY_MANAGER);
            int index=spm.getIndex(propertyId);
            if(index>-1){
                return spm.getValueByIndex(index);
            }
            return fConfiguration.getProperty(propertyId);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "property-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "property-not-supported",new Object[]{identifier}));
            }
        }
    } // getProperty(String):Object

    public boolean getFeature(String featureId)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        try{
            // http://xml.org/sax/features/use-entity-resolver2
            //   controls whether the methods of an object implementing
            //   org.xml.sax.ext.EntityResolver2 will be used by the parser.
            //
            if(featureId.equals(USE_ENTITY_RESOLVER2)){
                return fUseEntityResolver2;
            }
            //
            // Default handling
            //
            return fConfiguration.getFeature(featureId);
        }catch(XMLConfigurationException e){
            String identifier=e.getIdentifier();
            if(e.getType()==Status.NOT_RECOGNIZED){
                throw new SAXNotRecognizedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "feature-not-recognized",new Object[]{identifier}));
            }else{
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fConfiguration.getLocale(),
                                "feature-not-supported",new Object[]{identifier}));
            }
        }
    } // getFeature(String):boolean

    public XMLParserConfiguration getXMLParserConfiguration(){
        return fConfiguration;
    } // getXMLParserConfiguration():XMLParserConfiguration
} // class DOMParser
