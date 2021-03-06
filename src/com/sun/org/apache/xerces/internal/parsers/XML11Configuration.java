/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * Copyright 2001-2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.*;
import com.sun.org.apache.xerces.internal.impl.dtd.*;
import com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.*;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class XML11Configuration extends ParserConfigurationSettings
        implements XMLPullParserConfiguration, XML11Configurable{
    //
    // Constants
    //
    protected final static String XML11_DATATYPE_VALIDATOR_FACTORY=
            "com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl";
    // feature identifiers
    protected static final String WARN_ON_DUPLICATE_ATTDEF=
            Constants.XERCES_FEATURE_PREFIX+Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF=
            Constants.XERCES_FEATURE_PREFIX+Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    protected static final String WARN_ON_UNDECLARED_ELEMDEF=
            Constants.XERCES_FEATURE_PREFIX+Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String CONTINUE_AFTER_FATAL_ERROR=
            Constants.XERCES_FEATURE_PREFIX+Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String LOAD_EXTERNAL_DTD=
            Constants.XERCES_FEATURE_PREFIX+Constants.LOAD_EXTERNAL_DTD_FEATURE;
    protected static final String NOTIFY_BUILTIN_REFS=
            Constants.XERCES_FEATURE_PREFIX+Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    protected static final String NOTIFY_CHAR_REFS=
            Constants.XERCES_FEATURE_PREFIX+Constants.NOTIFY_CHAR_REFS_FEATURE;
    protected static final String NORMALIZE_DATA=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_ELEMENT_DEFAULT;
    protected static final String SCHEMA_AUGMENT_PSVI=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_AUGMENT_PSVI;
    protected static final String XMLSCHEMA_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String XMLSCHEMA_FULL_CHECKING=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_FULL_CHECKING;
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS=
            Constants.XERCES_FEATURE_PREFIX+Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected static final String VALIDATE_ANNOTATIONS=
            Constants.XERCES_FEATURE_PREFIX+Constants.VALIDATE_ANNOTATIONS_FEATURE;
    protected static final String HONOUR_ALL_SCHEMALOCATIONS=
            Constants.XERCES_FEATURE_PREFIX+Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;
    protected static final String NAMESPACE_GROWTH=
            Constants.XERCES_FEATURE_PREFIX+Constants.NAMESPACE_GROWTH_FEATURE;
    protected static final String TOLERATE_DUPLICATES=
            Constants.XERCES_FEATURE_PREFIX+Constants.TOLERATE_DUPLICATES_FEATURE;
    protected static final String USE_GRAMMAR_POOL_ONLY=
            Constants.XERCES_FEATURE_PREFIX+Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    // feature identifiers
    protected static final String VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    protected static final String NAMESPACES=
            Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;
    protected static final String EXTERNAL_GENERAL_ENTITIES=
            Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
    protected static final String EXTERNAL_PARAMETER_ENTITIES=
            Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
    // property identifiers
    protected static final String XML_STRING=
            Constants.SAX_PROPERTY_PREFIX+Constants.XML_STRING_PROPERTY;
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String SCHEMA_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String SCHEMA_LOCATION=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_LOCATION;
    protected static final String SCHEMA_NONS_LOCATION=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_NONS_LOCATION;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String DOCUMENT_SCANNER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DOCUMENT_SCANNER_PROPERTY;
    protected static final String DTD_SCANNER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DTD_SCANNER_PROPERTY;
    protected static final String XMLGRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String DTD_PROCESSOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DTD_PROCESSOR_PROPERTY;
    protected static final String DTD_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DTD_VALIDATOR_PROPERTY;
    protected static final String NAMESPACE_BINDER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.NAMESPACE_BINDER_PROPERTY;
    protected static final String DATATYPE_VALIDATOR_FACTORY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String JAXP_SCHEMA_LANGUAGE=
            Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_LANGUAGE;
    protected static final String JAXP_SCHEMA_SOURCE=
            Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_SOURCE;
    protected static final String LOCALE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.LOCALE_PROPERTY;
    protected static final String SCHEMA_DV_FACTORY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_DV_FACTORY_PROPERTY;
    // debugging
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE=false;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    private static final String SECURITY_MANAGER=Constants.SECURITY_MANAGER;
    //
    // Data
    //
    protected SymbolTable fSymbolTable;
    protected XMLInputSource fInputSource;
    protected ValidationManager fValidationManager;
    protected XMLVersionDetector fVersionDetector;
    protected XMLLocator fLocator;
    protected Locale fLocale;
    protected ArrayList<XMLComponent> fComponents;
    protected ArrayList<XMLComponent> fXML11Components=null;
    protected ArrayList<XMLComponent> fCommonComponents=null;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDocumentSource fLastComponent;
    protected boolean fParseInProgress=false;
    protected boolean fConfigUpdated=false;
    //
    // XML 1.0 components
    //
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected XMLDTDValidator fDTDValidator;
    protected XMLDTDValidator fNonNSDTDValidator;
    protected XMLDTDScanner fDTDScanner;
    protected XMLDTDProcessor fDTDProcessor;
    //
    // XML 1.1 components
    //
    protected DTDDVFactory fXML11DatatypeFactory=null;
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner=null;
    protected XML11DocumentScannerImpl fXML11DocScanner=null;
    protected XML11NSDTDValidator fXML11NSDTDValidator=null;
    protected XML11DTDValidator fXML11DTDValidator=null;
    protected XML11DTDScannerImpl fXML11DTDScanner=null;
    protected XML11DTDProcessor fXML11DTDProcessor=null;
    //
    // Common components
    //
    protected XMLGrammarPool fGrammarPool;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLSchemaValidator fSchemaValidator;
    protected XMLDocumentScanner fCurrentScanner;
    protected DTDDVFactory fCurrentDVFactory;
    protected XMLDTDScanner fCurrentDTDScanner;
    private boolean f11Initialized=false;
    //
    // Constructors
    //

    public XML11Configuration(){
        this(null,null,null);
    } // <init>()

    public XML11Configuration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool,
            XMLComponentManager parentSettings){
        super(parentSettings);
        // create a vector to hold all the components in use
        // XML 1.0 specialized components
        fComponents=new ArrayList<>();
        // XML 1.1 specialized components
        fXML11Components=new ArrayList<>();
        // Common components for XML 1.1. and XML 1.0
        fCommonComponents=new ArrayList<>();
        // create table for features and properties
        fFeatures=new HashMap<>();
        fProperties=new HashMap<>();
        // add default recognized features
        final String[] recognizedFeatures=
                {
                        CONTINUE_AFTER_FATAL_ERROR,LOAD_EXTERNAL_DTD, // from XMLDTDScannerImpl
                        VALIDATION,
                        NAMESPACES,
                        NORMALIZE_DATA,SCHEMA_ELEMENT_DEFAULT,SCHEMA_AUGMENT_PSVI,
                        GENERATE_SYNTHETIC_ANNOTATIONS,VALIDATE_ANNOTATIONS,
                        HONOUR_ALL_SCHEMALOCATIONS,NAMESPACE_GROWTH,
                        TOLERATE_DUPLICATES,
                        USE_GRAMMAR_POOL_ONLY,
                        // NOTE: These shouldn't really be here but since the XML Schema
                        //       validator is constructed dynamically, its recognized
                        //       features might not have been set and it would cause a
                        //       not-recognized exception to be thrown. -Ac
                        XMLSCHEMA_VALIDATION,XMLSCHEMA_FULL_CHECKING,
                        EXTERNAL_GENERAL_ENTITIES,
                        EXTERNAL_PARAMETER_ENTITIES,
                        PARSER_SETTINGS,
                        XMLConstants.FEATURE_SECURE_PROCESSING
                };
        addRecognizedFeatures(recognizedFeatures);
        // set state for default features
        fFeatures.put(VALIDATION,Boolean.FALSE);
        fFeatures.put(NAMESPACES,Boolean.TRUE);
        fFeatures.put(EXTERNAL_GENERAL_ENTITIES,Boolean.TRUE);
        fFeatures.put(EXTERNAL_PARAMETER_ENTITIES,Boolean.TRUE);
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR,Boolean.FALSE);
        fFeatures.put(LOAD_EXTERNAL_DTD,Boolean.TRUE);
        fFeatures.put(SCHEMA_ELEMENT_DEFAULT,Boolean.TRUE);
        fFeatures.put(NORMALIZE_DATA,Boolean.TRUE);
        fFeatures.put(SCHEMA_AUGMENT_PSVI,Boolean.TRUE);
        fFeatures.put(GENERATE_SYNTHETIC_ANNOTATIONS,Boolean.FALSE);
        fFeatures.put(VALIDATE_ANNOTATIONS,Boolean.FALSE);
        fFeatures.put(HONOUR_ALL_SCHEMALOCATIONS,Boolean.FALSE);
        fFeatures.put(NAMESPACE_GROWTH,Boolean.FALSE);
        fFeatures.put(TOLERATE_DUPLICATES,Boolean.FALSE);
        fFeatures.put(USE_GRAMMAR_POOL_ONLY,Boolean.FALSE);
        fFeatures.put(PARSER_SETTINGS,Boolean.TRUE);
        fFeatures.put(XMLConstants.FEATURE_SECURE_PROCESSING,Boolean.TRUE);
        // add default recognized properties
        final String[] recognizedProperties=
                {
                        SYMBOL_TABLE,
                        ERROR_HANDLER,
                        ENTITY_RESOLVER,
                        ERROR_REPORTER,
                        ENTITY_MANAGER,
                        DOCUMENT_SCANNER,
                        DTD_SCANNER,
                        DTD_PROCESSOR,
                        DTD_VALIDATOR,
                        DATATYPE_VALIDATOR_FACTORY,
                        VALIDATION_MANAGER,
                        SCHEMA_VALIDATOR,
                        XML_STRING,
                        XMLGRAMMAR_POOL,
                        JAXP_SCHEMA_SOURCE,
                        JAXP_SCHEMA_LANGUAGE,
                        // NOTE: These shouldn't really be here but since the XML Schema
                        //       validator is constructed dynamically, its recognized
                        //       properties might not have been set and it would cause a
                        //       not-recognized exception to be thrown. -Ac
                        SCHEMA_LOCATION,
                        SCHEMA_NONS_LOCATION,
                        LOCALE,
                        SCHEMA_DV_FACTORY,
                        SECURITY_MANAGER,
                        XML_SECURITY_PROPERTY_MANAGER
                };
        addRecognizedProperties(recognizedProperties);
        if(symbolTable==null){
            symbolTable=new SymbolTable();
        }
        fSymbolTable=symbolTable;
        fProperties.put(SYMBOL_TABLE,fSymbolTable);
        fGrammarPool=grammarPool;
        if(fGrammarPool!=null){
            fProperties.put(XMLGRAMMAR_POOL,fGrammarPool);
        }
        fEntityManager=new XMLEntityManager();
        fProperties.put(ENTITY_MANAGER,fEntityManager);
        addCommonComponent(fEntityManager);
        fErrorReporter=new XMLErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
        fProperties.put(ERROR_REPORTER,fErrorReporter);
        addCommonComponent(fErrorReporter);
        fNamespaceScanner=new XMLNSDocumentScannerImpl();
        fProperties.put(DOCUMENT_SCANNER,fNamespaceScanner);
        addComponent((XMLComponent)fNamespaceScanner);
        fDTDScanner=new XMLDTDScannerImpl();
        fProperties.put(DTD_SCANNER,fDTDScanner);
        addComponent((XMLComponent)fDTDScanner);
        fDTDProcessor=new XMLDTDProcessor();
        fProperties.put(DTD_PROCESSOR,fDTDProcessor);
        addComponent((XMLComponent)fDTDProcessor);
        fDTDValidator=new XMLNSDTDValidator();
        fProperties.put(DTD_VALIDATOR,fDTDValidator);
        addComponent(fDTDValidator);
        fDatatypeValidatorFactory=DTDDVFactory.getInstance();
        fProperties.put(DATATYPE_VALIDATOR_FACTORY,fDatatypeValidatorFactory);
        fValidationManager=new ValidationManager();
        fProperties.put(VALIDATION_MANAGER,fValidationManager);
        fVersionDetector=new XMLVersionDetector();
        // add message formatters
        if(fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN)==null){
            XMLMessageFormatter xmft=new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN,xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN,xmft);
        }
        // set locale
        try{
            setLocale(Locale.getDefault());
        }catch(XNIException e){
            // do nothing
            // REVISIT: What is the right thing to do? -Ac
        }
        fConfigUpdated=false;
    } // <init>(SymbolTable,XMLGrammarPool)

    protected void addComponent(XMLComponent component){
        // don't add a component more than once
        if(fComponents.contains(component)){
            return;
        }
        fComponents.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } // addComponent(XMLComponent)

    protected void addCommonComponent(XMLComponent component){
        // don't add a component more than once
        if(fCommonComponents.contains(component)){
            return;
        }
        fCommonComponents.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } // addCommonComponent(XMLComponent)

    protected void addRecognizedParamsAndSetDefaults(XMLComponent component){
        // register component's recognized features
        String[] recognizedFeatures=component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        // register component's recognized properties
        String[] recognizedProperties=component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        // set default values
        if(recognizedFeatures!=null){
            for(int i=0;i<recognizedFeatures.length;++i){
                String featureId=recognizedFeatures[i];
                Boolean state=component.getFeatureDefault(featureId);
                if(state!=null){
                    // Do not overwrite values already set on the configuration.
                    if(!fFeatures.containsKey(featureId)){
                        fFeatures.put(featureId,state);
                        // For newly added components who recognize this feature
                        // but did not offer a default value, we need to make
                        // sure these components will get an opportunity to read
                        // the value before parsing begins.
                        fConfigUpdated=true;
                    }
                }
            }
        }
        if(recognizedProperties!=null){
            for(int i=0;i<recognizedProperties.length;++i){
                String propertyId=recognizedProperties[i];
                Object value=component.getPropertyDefault(propertyId);
                if(value!=null){
                    // Do not overwrite values already set on the configuration.
                    if(!fProperties.containsKey(propertyId)){
                        fProperties.put(propertyId,value);
                        // For newly added components who recognize this property
                        // but did not offer a default value, we need to make
                        // sure these components will get an opportunity to read
                        // the value before parsing begins.
                        fConfigUpdated=true;
                    }
                }
            }
        }
    }

    public XML11Configuration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)    public void setLocale(Locale locale) throws XNIException{
        fLocale=locale;
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    public XML11Configuration(SymbolTable symbolTable,XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)    public void setDocumentHandler(XMLDocumentHandler documentHandler){
        fDocumentHandler=documentHandler;
        if(fLastComponent!=null){
            fLastComponent.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fLastComponent);
            }
        }
    } // setDocumentHandler(XMLDocumentHandler)

    //
    // Public methods
    //
    public void setInputSource(XMLInputSource inputSource)
            throws XMLConfigurationException, IOException{
        // REVISIT: this method used to reset all the components and
        //          construct the pipeline. Now reset() is called
        //          in parse (boolean) just before we parse the document
        //          Should this method still throw exceptions..?
        fInputSource=inputSource;
    } // setInputSource(XMLInputSource)    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    public boolean parse(boolean complete) throws XNIException, IOException{
        //
        // reset and configure pipeline and set InputSource.
        if(fInputSource!=null){
            try{
                fValidationManager.reset();
                fVersionDetector.reset(this);
                fConfigUpdated=true;
                resetCommon();
                short version=fVersionDetector.determineDocVersion(fInputSource);
                if(version==Constants.XML_VERSION_1_1){
                    initXML11Components();
                    configureXML11Pipeline();
                    resetXML11();
                }else{
                    configurePipeline();
                    reset();
                }
                // mark configuration as fixed
                fConfigUpdated=false;
                // resets and sets the pipeline.
                fVersionDetector.startDocumentParsing((XMLEntityHandler)fCurrentScanner,version);
                fInputSource=null;
            }catch(XNIException ex){
                if(PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }catch(IOException ex){
                if(PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }catch(RuntimeException ex){
                if(PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }catch(Exception ex){
                if(PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }
        try{
            return fCurrentScanner.scanDocument(complete);
        }catch(XNIException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(IOException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(RuntimeException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(Exception ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
    } // parse(boolean):boolean    public void setDTDHandler(XMLDTDHandler dtdHandler){
        fDTDHandler=dtdHandler;
    } // setDTDHandler(XMLDTDHandler)

    public void cleanup(){
        fEntityManager.closeReaders();
    }    public XMLDTDHandler getDTDHandler(){
        return fDTDHandler;
    } // getDTDHandler():XMLDTDHandler

    public void parse(XMLInputSource source) throws XNIException, IOException{
        if(fParseInProgress){
            // REVISIT - need to add new error message
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress=true;
        try{
            setInputSource(source);
            parse(true);
        }catch(XNIException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(IOException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(RuntimeException ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }catch(Exception ex){
            if(PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }finally{
            fParseInProgress=false;
            // close all streams opened by xerces
            this.cleanup();
        }
    } // parse(InputSource)    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler){
        fDTDContentModelHandler=handler;
    } // setDTDContentModelHandler(XMLDTDContentModelHandler)

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        fConfigUpdated=true;
        // forward to every XML 1.0 component
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fComponents.get(i);
            c.setFeature(featureId,state);
        }
        // forward it to common components
        count=fCommonComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fCommonComponents.get(i);
            c.setFeature(featureId,state);
        }
        // forward to every XML 1.1 component
        count=fXML11Components.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fXML11Components.get(i);
            try{
                c.setFeature(featureId,state);
            }catch(Exception e){
                // no op
            }
        }
        // save state if noone "objects"
        super.setFeature(featureId,state);
    } // setFeature(String,boolean)    public XMLDTDContentModelHandler getDTDContentModelHandler(){
        return fDTDContentModelHandler;
    } // getDTDContentModelHandler():XMLDTDContentModelHandler

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        fConfigUpdated=true;
        if(LOCALE.equals(propertyId)){
            setLocale((Locale)value);
        }
        // forward to every XML 1.0 component
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fComponents.get(i);
            c.setProperty(propertyId,value);
        }
        // forward it to every common Component
        count=fCommonComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fCommonComponents.get(i);
            c.setProperty(propertyId,value);
        }
        // forward it to every XML 1.1 component
        count=fXML11Components.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fXML11Components.get(i);
            try{
                c.setProperty(propertyId,value);
            }catch(Exception e){
                // ignore it
            }
        }
        // store value if noone "objects"
        super.setProperty(propertyId,value);
    } // setProperty(String,Object)    public void setEntityResolver(XMLEntityResolver resolver){
        fProperties.put(ENTITY_RESOLVER,resolver);
    } // setEntityResolver(XMLEntityResolver)

    public FeatureState getFeatureState(String featureId)
            throws XMLConfigurationException{
        // make this feature special
        if(featureId.equals(PARSER_SETTINGS)){
            return FeatureState.is(fConfigUpdated);
        }
        return super.getFeatureState(featureId);
    } // getFeature(String):boolean    public XMLEntityResolver getEntityResolver(){
        return (XMLEntityResolver)fProperties.get(ENTITY_RESOLVER);
    } // getEntityResolver():XMLEntityResolver

    public PropertyState getPropertyState(String propertyId)
            throws XMLConfigurationException{
        if(LOCALE.equals(propertyId)){
            return PropertyState.is(getLocale());
        }
        return super.getPropertyState(propertyId);
    }    public void setErrorHandler(XMLErrorHandler errorHandler){
        fProperties.put(ERROR_HANDLER,errorHandler);
    } // setErrorHandler(XMLErrorHandler)

    protected FeatureState checkFeature(String featureId) throws XMLConfigurationException{
        //
        // Xerces Features
        //
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
            //
            // http://apache.org/xml/features/validation/dynamic
            //   Allows the parser to validate a document only when it
            //   contains a grammar. Validation is turned on/off based
            //   on each document instance, automatically.
            //
            if(suffixLength==Constants.DYNAMIC_VALIDATION_FEATURE.length()&&
                    featureId.endsWith(Constants.DYNAMIC_VALIDATION_FEATURE)){
                return FeatureState.RECOGNIZED;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if(suffixLength==Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE.length()&&
                    featureId.endsWith(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)){
                // REVISIT
                return FeatureState.NOT_SUPPORTED;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if(suffixLength==Constants.VALIDATE_CONTENT_MODELS_FEATURE.length()&&
                    featureId.endsWith(Constants.VALIDATE_CONTENT_MODELS_FEATURE)){
                // REVISIT
                return FeatureState.NOT_SUPPORTED;
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-dtd-grammar
            //
            if(suffixLength==Constants.LOAD_DTD_GRAMMAR_FEATURE.length()&&
                    featureId.endsWith(Constants.LOAD_DTD_GRAMMAR_FEATURE)){
                return FeatureState.RECOGNIZED;
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-external-dtd
            //
            if(suffixLength==Constants.LOAD_EXTERNAL_DTD_FEATURE.length()&&
                    featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)){
                return FeatureState.RECOGNIZED;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if(suffixLength==Constants.VALIDATE_DATATYPES_FEATURE.length()&&
                    featureId.endsWith(Constants.VALIDATE_DATATYPES_FEATURE)){
                return FeatureState.NOT_SUPPORTED;
            }
            //
            // http://apache.org/xml/features/validation/schema
            //   Lets the user turn Schema validation support on/off.
            //
            if(suffixLength==Constants.SCHEMA_VALIDATION_FEATURE.length()&&
                    featureId.endsWith(Constants.SCHEMA_VALIDATION_FEATURE)){
                return FeatureState.RECOGNIZED;
            }
            // activate full schema checking
            if(suffixLength==Constants.SCHEMA_FULL_CHECKING.length()&&
                    featureId.endsWith(Constants.SCHEMA_FULL_CHECKING)){
                return FeatureState.RECOGNIZED;
            }
            // Feature identifier: expose schema normalized value
            //  http://apache.org/xml/features/validation/schema/normalized-value
            if(suffixLength==Constants.SCHEMA_NORMALIZED_VALUE.length()&&
                    featureId.endsWith(Constants.SCHEMA_NORMALIZED_VALUE)){
                return FeatureState.RECOGNIZED;
            }
            // Feature identifier: send element default value via characters()
            // http://apache.org/xml/features/validation/schema/element-default
            if(suffixLength==Constants.SCHEMA_ELEMENT_DEFAULT.length()&&
                    featureId.endsWith(Constants.SCHEMA_ELEMENT_DEFAULT)){
                return FeatureState.RECOGNIZED;
            }
            // special performance feature: only component manager is allowed to set it.
            if(suffixLength==Constants.PARSER_SETTINGS.length()&&
                    featureId.endsWith(Constants.PARSER_SETTINGS)){
                return FeatureState.NOT_SUPPORTED;
            }
        }
        //
        // Not recognized
        //
        return super.checkFeature(featureId);
    } // checkFeature(String)    public XMLErrorHandler getErrorHandler(){
        // REVISIT: Should this be a property?
        return (XMLErrorHandler)fProperties.get(ERROR_HANDLER);
    } // getErrorHandler():XMLErrorHandler

    protected PropertyState checkProperty(String propertyId) throws XMLConfigurationException{
        //
        // Xerces Properties
        //
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.DTD_SCANNER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)){
                return PropertyState.RECOGNIZED;
            }
            if(suffixLength==Constants.SCHEMA_LOCATION.length()&&
                    propertyId.endsWith(Constants.SCHEMA_LOCATION)){
                return PropertyState.RECOGNIZED;
            }
            if(suffixLength==Constants.SCHEMA_NONS_LOCATION.length()&&
                    propertyId.endsWith(Constants.SCHEMA_NONS_LOCATION)){
                return PropertyState.RECOGNIZED;
            }
        }
        if(propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.JAXP_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.SCHEMA_SOURCE.length()&&
                    propertyId.endsWith(Constants.SCHEMA_SOURCE)){
                return PropertyState.RECOGNIZED;
            }
        }
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
        //
        // Not recognized
        //
        return super.checkProperty(propertyId);
    } // checkProperty(String)

    protected void reset() throws XNIException{
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fComponents.get(i);
            c.reset(this);
        }
    } // reset()

    protected void resetCommon() throws XNIException{
        // reset common components
        int count=fCommonComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fCommonComponents.get(i);
            c.reset(this);
        }
    } // resetCommon()

    protected void resetXML11() throws XNIException{
        // reset every component
        int count=fXML11Components.size();
        for(int i=0;i<count;i++){
            XMLComponent c=fXML11Components.get(i);
            c.reset(this);
        }
    } // resetXML11()

    protected void configureXML11Pipeline(){
        if(fCurrentDVFactory!=fXML11DatatypeFactory){
            fCurrentDVFactory=fXML11DatatypeFactory;
            setProperty(DATATYPE_VALIDATOR_FACTORY,fCurrentDVFactory);
        }
        if(fCurrentDTDScanner!=fXML11DTDScanner){
            fCurrentDTDScanner=fXML11DTDScanner;
            setProperty(DTD_SCANNER,fCurrentDTDScanner);
            setProperty(DTD_PROCESSOR,fXML11DTDProcessor);
        }
        fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDHandler(fDTDHandler);
        if(fDTDHandler!=null){
            fDTDHandler.setDTDSource(fXML11DTDProcessor);
        }
        fXML11DTDScanner.setDTDContentModelHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDContentModelSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
        if(fDTDContentModelHandler!=null){
            fDTDContentModelHandler.setDTDContentModelSource(fXML11DTDProcessor);
        }
        // setup XML 1.1 document pipeline
        if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
            if(fCurrentScanner!=fXML11NSDocScanner){
                fCurrentScanner=fXML11NSDocScanner;
                setProperty(DOCUMENT_SCANNER,fXML11NSDocScanner);
                setProperty(DTD_VALIDATOR,fXML11NSDTDValidator);
            }
            fXML11NSDocScanner.setDTDValidator(fXML11NSDTDValidator);
            fXML11NSDocScanner.setDocumentHandler(fXML11NSDTDValidator);
            fXML11NSDTDValidator.setDocumentSource(fXML11NSDocScanner);
            fXML11NSDTDValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fXML11NSDTDValidator);
            }
            fLastComponent=fXML11NSDTDValidator;
        }else{
            // create components
            if(fXML11DocScanner==null){
                // non namespace document pipeline
                fXML11DocScanner=new XML11DocumentScannerImpl();
                addXML11Component(fXML11DocScanner);
                fXML11DTDValidator=new XML11DTDValidator();
                addXML11Component(fXML11DTDValidator);
            }
            if(fCurrentScanner!=fXML11DocScanner){
                fCurrentScanner=fXML11DocScanner;
                setProperty(DOCUMENT_SCANNER,fXML11DocScanner);
                setProperty(DTD_VALIDATOR,fXML11DTDValidator);
            }
            fXML11DocScanner.setDocumentHandler(fXML11DTDValidator);
            fXML11DTDValidator.setDocumentSource(fXML11DocScanner);
            fXML11DTDValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fXML11DTDValidator);
            }
            fLastComponent=fXML11DTDValidator;
        }
        // setup document pipeline
        if(fFeatures.get(XMLSCHEMA_VALIDATION)==Boolean.TRUE){
            // If schema validator was not in the pipeline insert it.
            if(fSchemaValidator==null){
                fSchemaValidator=new XMLSchemaValidator();
                // add schema component
                setProperty(SCHEMA_VALIDATOR,fSchemaValidator);
                addCommonComponent(fSchemaValidator);
                fSchemaValidator.reset(this);
                // add schema message formatter
                if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN)==null){
                    XSMessageFormatter xmft=new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,xmft);
                }
            }
            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent=fSchemaValidator;
        }
    } // configureXML11Pipeline()

    protected void configurePipeline(){
        if(fCurrentDVFactory!=fDatatypeValidatorFactory){
            fCurrentDVFactory=fDatatypeValidatorFactory;
            // use XML 1.0 datatype library
            setProperty(DATATYPE_VALIDATOR_FACTORY,fCurrentDVFactory);
        }
        // setup DTD pipeline
        if(fCurrentDTDScanner!=fDTDScanner){
            fCurrentDTDScanner=fDTDScanner;
            setProperty(DTD_SCANNER,fCurrentDTDScanner);
            setProperty(DTD_PROCESSOR,fDTDProcessor);
        }
        fDTDScanner.setDTDHandler(fDTDProcessor);
        fDTDProcessor.setDTDSource(fDTDScanner);
        fDTDProcessor.setDTDHandler(fDTDHandler);
        if(fDTDHandler!=null){
            fDTDHandler.setDTDSource(fDTDProcessor);
        }
        fDTDScanner.setDTDContentModelHandler(fDTDProcessor);
        fDTDProcessor.setDTDContentModelSource(fDTDScanner);
        fDTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
        if(fDTDContentModelHandler!=null){
            fDTDContentModelHandler.setDTDContentModelSource(fDTDProcessor);
        }
        // setup document pipeline
        if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
            if(fCurrentScanner!=fNamespaceScanner){
                fCurrentScanner=fNamespaceScanner;
                setProperty(DOCUMENT_SCANNER,fNamespaceScanner);
                setProperty(DTD_VALIDATOR,fDTDValidator);
            }
            fNamespaceScanner.setDTDValidator(fDTDValidator);
            fNamespaceScanner.setDocumentHandler(fDTDValidator);
            fDTDValidator.setDocumentSource(fNamespaceScanner);
            fDTDValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fDTDValidator);
            }
            fLastComponent=fDTDValidator;
        }else{
            // create components
            if(fNonNSScanner==null){
                fNonNSScanner=new XMLDocumentScannerImpl();
                fNonNSDTDValidator=new XMLDTDValidator();
                // add components
                addComponent((XMLComponent)fNonNSScanner);
                addComponent((XMLComponent)fNonNSDTDValidator);
            }
            if(fCurrentScanner!=fNonNSScanner){
                fCurrentScanner=fNonNSScanner;
                setProperty(DOCUMENT_SCANNER,fNonNSScanner);
                setProperty(DTD_VALIDATOR,fNonNSDTDValidator);
            }
            fNonNSScanner.setDocumentHandler(fNonNSDTDValidator);
            fNonNSDTDValidator.setDocumentSource(fNonNSScanner);
            fNonNSDTDValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fNonNSDTDValidator);
            }
            fLastComponent=fNonNSDTDValidator;
        }
        // add XML Schema validator if needed
        if(fFeatures.get(XMLSCHEMA_VALIDATION)==Boolean.TRUE){
            // If schema validator was not in the pipeline insert it.
            if(fSchemaValidator==null){
                fSchemaValidator=new XMLSchemaValidator();
                // add schema component
                setProperty(SCHEMA_VALIDATOR,fSchemaValidator);
                addCommonComponent(fSchemaValidator);
                fSchemaValidator.reset(this);
                // add schema message formatter
                if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN)==null){
                    XSMessageFormatter xmft=new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,xmft);
                }
            }
            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent=fSchemaValidator;
        }
    } // configurePipeline()

    protected void addXML11Component(XMLComponent component){
        // don't add a component more than once
        if(fXML11Components.contains(component)){
            return;
        }
        fXML11Components.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } // addXML11Component(XMLComponent)

    private void initXML11Components(){
        if(!f11Initialized){
            // create datatype factory
            fXML11DatatypeFactory=DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
            // setup XML 1.1 DTD pipeline
            fXML11DTDScanner=new XML11DTDScannerImpl();
            addXML11Component(fXML11DTDScanner);
            fXML11DTDProcessor=new XML11DTDProcessor();
            addXML11Component(fXML11DTDProcessor);
            // setup XML 1.1. document pipeline - namespace aware
            fXML11NSDocScanner=new XML11NSDocumentScannerImpl();
            addXML11Component(fXML11NSDocScanner);
            fXML11NSDTDValidator=new XML11NSDTDValidator();
            addXML11Component(fXML11NSDTDValidator);
            f11Initialized=true;
        }
    }    public Locale getLocale(){
        return fLocale;
    } // getLocale():Locale

    FeatureState getFeatureState0(String featureId)
            throws XMLConfigurationException{
        return super.getFeatureState(featureId);
    }








    // features and properties
















} // class XML11Configuration
