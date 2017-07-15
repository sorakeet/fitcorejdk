/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class DOMConfigurationImpl extends ParserConfigurationSettings
        implements XMLParserConfiguration, DOMConfiguration{
    //
    // Constants
    //
    // feature identifiers
    protected static final String XERCES_VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    protected static final String XERCES_NAMESPACES=
            Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;
    protected static final String SCHEMA=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String SCHEMA_FULL_CHECKING=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_FULL_CHECKING;
    protected static final String DYNAMIC_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.DYNAMIC_VALIDATION_FEATURE;
    protected static final String NORMALIZE_DATA=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SEND_PSVI=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_AUGMENT_PSVI;
    protected final static String DTD_VALIDATOR_FACTORY_PROPERTY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String NAMESPACE_GROWTH=
            Constants.XERCES_FEATURE_PREFIX+Constants.NAMESPACE_GROWTH_FEATURE;
    protected static final String TOLERATE_DUPLICATES=
            Constants.XERCES_FEATURE_PREFIX+Constants.TOLERATE_DUPLICATES_FEATURE;
    // property identifiers
    protected static final String ENTITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String XML_STRING=
            Constants.SAX_PROPERTY_PREFIX+Constants.XML_STRING_PROPERTY;
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String GRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String JAXP_SCHEMA_LANGUAGE=
            Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE;
    protected static final String JAXP_SCHEMA_SOURCE=
            Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_SOURCE;
    protected static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String SCHEMA_DV_FACTORY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_DV_FACTORY_PROPERTY;
    protected final static short NAMESPACES=0x1<<0;
    protected final static short DTNORMALIZATION=0x1<<1;
    protected final static short ENTITIES=0x1<<2;
    protected final static short CDATA=0x1<<3;
    protected final static short SPLITCDATA=0x1<<4;
    protected final static short COMMENTS=0x1<<5;
    protected final static short VALIDATE=0x1<<6;
    protected final static short PSVI=0x1<<7;
    protected final static short WELLFORMED=0x1<<8;
    protected final static short NSDECL=0x1<<9;
    protected final static short INFOSET_TRUE_PARAMS=NAMESPACES|COMMENTS|WELLFORMED|NSDECL;
    protected final static short INFOSET_FALSE_PARAMS=ENTITIES|DTNORMALIZATION|CDATA;
    protected final static short INFOSET_MASK=INFOSET_TRUE_PARAMS|INFOSET_FALSE_PARAMS;
    private static final String SECURITY_MANAGER=Constants.SECURITY_MANAGER;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    protected final DOMErrorHandlerWrapper fErrorHandlerWrapper=
            new DOMErrorHandlerWrapper();
    protected short features=0;
    // components
    protected SymbolTable fSymbolTable;
    protected ArrayList fComponents;
    protected ValidationManager fValidationManager;
    protected Locale fLocale;
    protected XMLErrorReporter fErrorReporter;
    //
    // Data
    //
    XMLDocumentHandler fDocumentHandler;
    // private data
    private DOMStringList fRecognizedParameters;
    //
    // Constructors
    //

    protected DOMConfigurationImpl(){
        this(null,null);
    } // <init>()

    protected DOMConfigurationImpl(SymbolTable symbolTable,
                                   XMLComponentManager parentSettings){
        super(parentSettings);
        // create table for features and properties
        fFeatures=new HashMap();
        fProperties=new HashMap();
        // add default recognized features
        final String[] recognizedFeatures={
                XERCES_VALIDATION,
                XERCES_NAMESPACES,
                SCHEMA,
                SCHEMA_FULL_CHECKING,
                DYNAMIC_VALIDATION,
                NORMALIZE_DATA,
                SEND_PSVI,
                NAMESPACE_GROWTH,
                TOLERATE_DUPLICATES
        };
        addRecognizedFeatures(recognizedFeatures);
        // set state for default features
        setFeature(XERCES_VALIDATION,false);
        setFeature(SCHEMA,false);
        setFeature(SCHEMA_FULL_CHECKING,false);
        setFeature(DYNAMIC_VALIDATION,false);
        setFeature(NORMALIZE_DATA,false);
        setFeature(XERCES_NAMESPACES,true);
        setFeature(SEND_PSVI,true);
        setFeature(NAMESPACE_GROWTH,false);
        // add default recognized properties
        final String[] recognizedProperties={
                XML_STRING,
                SYMBOL_TABLE,
                ERROR_HANDLER,
                ENTITY_RESOLVER,
                ERROR_REPORTER,
                ENTITY_MANAGER,
                VALIDATION_MANAGER,
                GRAMMAR_POOL,
                JAXP_SCHEMA_SOURCE,
                JAXP_SCHEMA_LANGUAGE,
                DTD_VALIDATOR_FACTORY_PROPERTY,
                SCHEMA_DV_FACTORY,
                SECURITY_MANAGER,
                XML_SECURITY_PROPERTY_MANAGER
        };
        addRecognizedProperties(recognizedProperties);
        // set default values for normalization features
        features|=NAMESPACES;
        features|=ENTITIES;
        features|=COMMENTS;
        features|=CDATA;
        features|=SPLITCDATA;
        features|=WELLFORMED;
        features|=NSDECL;
        if(symbolTable==null){
            symbolTable=new SymbolTable();
        }
        fSymbolTable=symbolTable;
        fComponents=new ArrayList();
        setProperty(SYMBOL_TABLE,fSymbolTable);
        fErrorReporter=new XMLErrorReporter();
        setProperty(ERROR_REPORTER,fErrorReporter);
        addComponent(fErrorReporter);
        setProperty(DTD_VALIDATOR_FACTORY_PROPERTY,DTDDVFactory.getInstance());
        XMLEntityManager manager=new XMLEntityManager();
        setProperty(ENTITY_MANAGER,manager);
        addComponent(manager);
        fValidationManager=createValidationManager();
        setProperty(VALIDATION_MANAGER,fValidationManager);
        setProperty(SECURITY_MANAGER,new XMLSecurityManager(true));
        setProperty(Constants.XML_SECURITY_PROPERTY_MANAGER,
                new XMLSecurityPropertyManager());
        // add message formatters
        if(fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN)==null){
            XMLMessageFormatter xmft=new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN,xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN,xmft);
        }
        // REVISIT: try to include XML Schema formatter.
        //          This is a hack to allow DTD configuration to be build.
        //
        if(fErrorReporter.getMessageFormatter("http://www.w3.org/TR/xml-schema-1")==null){
            MessageFormatter xmft=null;
            try{
                xmft=(MessageFormatter)(
                        ObjectFactory.newInstance("com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter",true));
            }catch(Exception exception){
            }
            if(xmft!=null){
                fErrorReporter.putMessageFormatter("http://www.w3.org/TR/xml-schema-1",xmft);
            }
        }
        // set locale
        try{
            setLocale(Locale.getDefault());
        }catch(XNIException e){
            // do nothing
            // REVISIT: What is the right thing to do? -Ac
        }
    } // <init>(SymbolTable)

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        // save state if noone "objects"
        super.setFeature(featureId,state);
    } // setFeature(String,boolean)
    //
    // XMLParserConfiguration methods
    //

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // store value if noone "objects"
        super.setProperty(propertyId,value);
    } // setProperty(String,Object)

    protected PropertyState checkProperty(String propertyId)
            throws XMLConfigurationException{
        // special cases
        if(propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.SAX_PROPERTY_PREFIX.length();
            //
            // http://xml.org/sax/properties/xml-string
            // Value type: String
            // Access: read-only
            //   Get the literal string of characters associated with the
            //   current event.  If the parser recognises and supports this
            //   property but is not currently parsing text, it should return
            //   null (this is a good way to check for availability before the
            //   parse begins).
            //
            if(suffixLength==Constants.XML_STRING_PROPERTY.length()&&
                    propertyId.endsWith(Constants.XML_STRING_PROPERTY)){
                // REVISIT - we should probably ask xml-dev for a precise
                // definition of what this is actually supposed to return, and
                // in exactly which circumstances.
                return PropertyState.NOT_SUPPORTED;
            }
        }
        // check property
        return super.checkProperty(propertyId);
    } // checkProperty(String)    public void setDocumentHandler(XMLDocumentHandler documentHandler){
        fDocumentHandler=documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    protected void addComponent(XMLComponent component){
        // don't add a component more than once
        if(fComponents.contains(component)){
            return;
        }
        fComponents.add(component);
        // register component's recognized features
        String[] recognizedFeatures=component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        // register component's recognized properties
        String[] recognizedProperties=component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
    } // addComponent(XMLComponent)    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    protected ValidationManager createValidationManager(){
        return new ValidationManager();
    }    public void setDTDHandler(XMLDTDHandler dtdHandler){
        //no-op
    } // setDTDHandler(XMLDTDHandler)

    protected DOMConfigurationImpl(SymbolTable symbolTable){
        this(symbolTable,null);
    } // <init>(SymbolTable)    public XMLDTDHandler getDTDHandler(){
        return null;
    } // getDTDHandler():XMLDTDHandler

    public void parse(XMLInputSource inputSource)
            throws XNIException, IOException{
        // no-op
    }    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler){
        //no-op
    } // setDTDContentModelHandler(XMLDTDContentModelHandler)

    public void setParameter(String name,Object value) throws DOMException{
        boolean found=true;
        // REVISIT: Recognizes DOM L3 default features only.
        //          Does not yet recognize Xerces features.
        if(value instanceof Boolean){
            boolean state=((Boolean)value).booleanValue();
            if(name.equalsIgnoreCase(Constants.DOM_COMMENTS)){
                features=(short)(state?features|COMMENTS:features&~COMMENTS);
            }else if(name.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)){
                setFeature(NORMALIZE_DATA,state);
                features=
                        (short)(state?features|DTNORMALIZATION:features&~DTNORMALIZATION);
                if(state){
                    features=(short)(features|VALIDATE);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_NAMESPACES)){
                features=(short)(state?features|NAMESPACES:features&~NAMESPACES);
            }else if(name.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)){
                features=(short)(state?features|CDATA:features&~CDATA);
            }else if(name.equalsIgnoreCase(Constants.DOM_ENTITIES)){
                features=(short)(state?features|ENTITIES:features&~ENTITIES);
            }else if(name.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)){
                features=(short)(state?features|SPLITCDATA:features&~SPLITCDATA);
            }else if(name.equalsIgnoreCase(Constants.DOM_VALIDATE)){
                features=(short)(state?features|VALIDATE:features&~VALIDATE);
            }else if(name.equalsIgnoreCase(Constants.DOM_WELLFORMED)){
                features=(short)(state?features|WELLFORMED:features&~WELLFORMED);
            }else if(name.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)){
                features=(short)(state?features|NSDECL:features&~NSDECL);
            }else if(name.equalsIgnoreCase(Constants.DOM_INFOSET)){
                // Setting to false has no effect.
                if(state){
                    features=(short)(features|INFOSET_TRUE_PARAMS);
                    features=(short)(features&~INFOSET_FALSE_PARAMS);
                    setFeature(NORMALIZE_DATA,false);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)
                    ||name.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM)
                    ||name.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA)
                    ||name.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)
                    ){
                if(state){ // true is not supported
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "FEATURE_NOT_SUPPORTED",
                                    new Object[]{name});
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)){
                if(!state){ // false is not supported
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "FEATURE_NOT_SUPPORTED",
                                    new Object[]{name});
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(SEND_PSVI)){
                // REVISIT: turning augmentation of PSVI is not support,
                // because in this case we won't be able to retrieve element
                // default value.
                if(!state){ // false is not supported
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "FEATURE_NOT_SUPPORTED",
                                    new Object[]{name});
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_PSVI)){
                features=(short)(state?features|PSVI:features&~PSVI);
            }else{
                found=false;
                /**
                 String msg =
                 DOMMessageFormatter.formatMessage(
                 DOMMessageFormatter.DOM_DOMAIN,
                 "FEATURE_NOT_FOUND",
                 new Object[] { name });
                 throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
                 */
            }
        }
        if(!found||!(value instanceof Boolean)){ // set properties
            found=true;
            if(name.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)){
                if(value instanceof DOMErrorHandler||value==null){
                    fErrorHandlerWrapper.setErrorHandler((DOMErrorHandler)value);
                    setErrorHandler(fErrorHandlerWrapper);
                }else{
                    // REVISIT: type mismatch
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)){
                if(value instanceof LSResourceResolver||value==null){
                    try{
                        setEntityResolver(new DOMEntityResolverWrapper((LSResourceResolver)value));
                    }catch(XMLConfigurationException e){
                    }
                }else{
                    // REVISIT: type mismatch
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)){
                if(value instanceof String||value==null){
                    try{
                        // map DOM schema-location to JAXP schemaSource property
                        setProperty(
                                Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_SOURCE,
                                value);
                    }catch(XMLConfigurationException e){
                    }
                }else{
                    // REVISIT: type mismatch
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)){
                if(value instanceof String||value==null){
                    try{
                        if(value==null){
                            setProperty(
                                    Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE,
                                    null);
                        }else if(value.equals(Constants.NS_XMLSCHEMA)){
                            // REVISIT: when add support to DTD validation
                            setProperty(
                                    Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE,
                                    Constants.NS_XMLSCHEMA);
                        }else if(value.equals(Constants.NS_DTD)){
                            // Added support for revalidation against DTDs
                            setProperty(Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE,
                                    Constants.NS_DTD);
                        }
                    }catch(XMLConfigurationException e){
                    }
                }else{
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(SYMBOL_TABLE)){
                // Xerces Symbol Table
                if(value instanceof SymbolTable){
                    setProperty(SYMBOL_TABLE,value);
                }else{
                    // REVISIT: type mismatch
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else if(name.equalsIgnoreCase(GRAMMAR_POOL)){
                if(value instanceof XMLGrammarPool){
                    setProperty(GRAMMAR_POOL,value);
                }else{
                    // REVISIT: type mismatch
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "TYPE_MISMATCH_ERR",
                                    new Object[]{name});
                    throw new DOMException(DOMException.TYPE_MISMATCH_ERR,msg);
                }
            }else{
                // REVISIT: check if this is a boolean parameter -- type mismatch should be thrown.
                //parameter is not recognized
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "FEATURE_NOT_FOUND",
                                new Object[]{name});
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
        }
    }    public XMLDTDContentModelHandler getDTDContentModelHandler(){
        return null;
    } // getDTDContentModelHandler():XMLDTDContentModelHandler

    public Object getParameter(String name) throws DOMException{
        // REVISIT: Recognizes DOM L3 default features only.
        //          Does not yet recognize Xerces features.
        if(name.equalsIgnoreCase(Constants.DOM_COMMENTS)){
            return ((features&COMMENTS)!=0)?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_NAMESPACES)){
            return (features&NAMESPACES)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)){
            // REVISIT: datatype-normalization only takes effect if validation is on
            return (features&DTNORMALIZATION)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)){
            return (features&CDATA)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_ENTITIES)){
            return (features&ENTITIES)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)){
            return (features&SPLITCDATA)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_VALIDATE)){
            return (features&VALIDATE)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_WELLFORMED)){
            return (features&WELLFORMED)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)){
            return (features&NSDECL)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_INFOSET)){
            return (features&INFOSET_MASK)==INFOSET_TRUE_PARAMS?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)
                ||name.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM)
                ||name.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA)
                ||name.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)
                ){
            return Boolean.FALSE;
        }else if(name.equalsIgnoreCase(SEND_PSVI)){
            return Boolean.TRUE;
        }else if(name.equalsIgnoreCase(Constants.DOM_PSVI)){
            return (features&PSVI)!=0?Boolean.TRUE:Boolean.FALSE;
        }else if(name.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)){
            return Boolean.TRUE;
        }else if(name.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)){
            return fErrorHandlerWrapper.getErrorHandler();
        }else if(name.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)){
            XMLEntityResolver entityResolver=getEntityResolver();
            if(entityResolver!=null&&entityResolver instanceof DOMEntityResolverWrapper){
                return ((DOMEntityResolverWrapper)entityResolver).getEntityResolver();
            }
            return null;
        }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)){
            return getProperty(Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE);
        }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)){
            return getProperty(Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_SOURCE);
        }else if(name.equalsIgnoreCase(SYMBOL_TABLE)){
            return getProperty(SYMBOL_TABLE);
        }else if(name.equalsIgnoreCase(GRAMMAR_POOL)){
            return getProperty(GRAMMAR_POOL);
        }else{
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_FOUND",
                            new Object[]{name});
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
    }    public void setEntityResolver(XMLEntityResolver resolver){
        if(resolver!=null){
            fProperties.put(ENTITY_RESOLVER,resolver);
        }
    } // setEntityResolver(XMLEntityResolver)

    public boolean canSetParameter(String name,Object value){
        if(value==null){
            //if null, the returned value is true.
            //REVISIT: I dont like this --- even for unrecognized parameter it would
            //return 'true'. I think it should return false in that case.
            // Application will be surprised to find that setParameter throws not
            //recognized exception when canSetParameter returns 'true' Then what is the use
            //of having canSetParameter ??? - nb.
            return true;
        }
        if(value instanceof Boolean){
            //features whose parameter value can be set either 'true' or 'false'
            // or they accept any boolean value -- so we just need to check that
            // its a boolean value..
            if(name.equalsIgnoreCase(Constants.DOM_COMMENTS)
                    ||name.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)
                    ||name.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)
                    ||name.equalsIgnoreCase(Constants.DOM_ENTITIES)
                    ||name.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)
                    ||name.equalsIgnoreCase(Constants.DOM_NAMESPACES)
                    ||name.equalsIgnoreCase(Constants.DOM_VALIDATE)
                    ||name.equalsIgnoreCase(Constants.DOM_WELLFORMED)
                    ||name.equalsIgnoreCase(Constants.DOM_INFOSET)
                    ||name.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)
                    ){
                return true;
            }//features whose parameter value can not be set to 'true'
            else if(
                    name.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)
                            ||name.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM)
                            ||name.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA)
                            ||name.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)
                    ){
                return (value.equals(Boolean.TRUE))?false:true;
            }//features whose parameter value can not be set to 'false'
            else if(name.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)
                    ||name.equalsIgnoreCase(SEND_PSVI)
                    ){
                return (value.equals(Boolean.TRUE))?true:false;
            }// if name is not among the above listed above -- its not recognized. return false
            else{
                return false;
            }
        }else if(name.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)){
            return (value instanceof DOMErrorHandler)?true:false;
        }else if(name.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)){
            return (value instanceof LSResourceResolver)?true:false;
        }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)){
            return (value instanceof String)?true:false;
        }else if(name.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)){
            // REVISIT: should null value be supported?
            //as of now we are only supporting W3C XML Schema
            return ((value instanceof String)&&value.equals(Constants.NS_XMLSCHEMA))?true:false;
        }else if(name.equalsIgnoreCase(SYMBOL_TABLE)){
            // Xerces Symbol Table
            return (value instanceof SymbolTable)?true:false;
        }else if(name.equalsIgnoreCase(GRAMMAR_POOL)){
            return (value instanceof XMLGrammarPool)?true:false;
        }else{
            //false if the parameter is not recognized or the requested value is not supported.
            return false;
        }
    } //canSetParameter    public XMLEntityResolver getEntityResolver(){
        return (XMLEntityResolver)fProperties.get(ENTITY_RESOLVER);
    } // getEntityResolver():XMLEntityResolver

    public DOMStringList getParameterNames(){
        if(fRecognizedParameters==null){
            Vector parameters=new Vector();
            //Add DOM recognized parameters
            //REVISIT: Would have been nice to have a list of
            //recognized paramters.
            parameters.add(Constants.DOM_COMMENTS);
            parameters.add(Constants.DOM_DATATYPE_NORMALIZATION);
            parameters.add(Constants.DOM_CDATA_SECTIONS);
            parameters.add(Constants.DOM_ENTITIES);
            parameters.add(Constants.DOM_SPLIT_CDATA);
            parameters.add(Constants.DOM_NAMESPACES);
            parameters.add(Constants.DOM_VALIDATE);
            parameters.add(Constants.DOM_INFOSET);
            parameters.add(Constants.DOM_NORMALIZE_CHARACTERS);
            parameters.add(Constants.DOM_CANONICAL_FORM);
            parameters.add(Constants.DOM_VALIDATE_IF_SCHEMA);
            parameters.add(Constants.DOM_CHECK_CHAR_NORMALIZATION);
            parameters.add(Constants.DOM_WELLFORMED);
            parameters.add(Constants.DOM_NAMESPACE_DECLARATIONS);
            parameters.add(Constants.DOM_ELEMENT_CONTENT_WHITESPACE);
            parameters.add(Constants.DOM_ERROR_HANDLER);
            parameters.add(Constants.DOM_SCHEMA_TYPE);
            parameters.add(Constants.DOM_SCHEMA_LOCATION);
            parameters.add(Constants.DOM_RESOURCE_RESOLVER);
            //Add recognized xerces features and properties
            parameters.add(GRAMMAR_POOL);
            parameters.add(SYMBOL_TABLE);
            parameters.add(SEND_PSVI);
            fRecognizedParameters=new DOMStringListImpl(parameters);
        }
        return fRecognizedParameters;
    }//getParameterNames    public void setErrorHandler(XMLErrorHandler errorHandler){
        if(errorHandler!=null){
            fProperties.put(ERROR_HANDLER,errorHandler);
        }
    } // setErrorHandler(XMLErrorHandler)

    protected void reset() throws XNIException{
        if(fValidationManager!=null)
            fValidationManager.reset();
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=(XMLComponent)fComponents.get(i);
            c.reset(this);
        }
    } // reset()    public XMLErrorHandler getErrorHandler(){
        return (XMLErrorHandler)fProperties.get(ERROR_HANDLER);
    } // getErrorHandler():XMLErrorHandler





    public void setLocale(Locale locale) throws XNIException{
        fLocale=locale;
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    public Locale getLocale(){
        return fLocale;
    } // getLocale():Locale








    //
    // Protected methods
    //








} // class XMLParser
