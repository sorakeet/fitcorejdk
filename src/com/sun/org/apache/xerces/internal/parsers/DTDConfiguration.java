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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.*;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.*;

import java.io.IOException;
import java.util.Locale;

public class DTDConfiguration
        extends BasicParserConfiguration
        implements XMLPullParserConfiguration{
    //
    // Constants
    //
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
    // property identifiers
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
    protected static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    // debugging
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE=false;
    private static final String SECURITY_MANAGER=Constants.SECURITY_MANAGER;
    //
    // Data
    //
    // components (non-configurable)
    protected XMLGrammarPool fGrammarPool;
    protected DTDDVFactory fDatatypeValidatorFactory;
    // components (configurable)
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLDocumentScanner fScanner;
    protected XMLInputSource fInputSource;
    protected XMLDTDScanner fDTDScanner;
    protected XMLDTDProcessor fDTDProcessor;
    protected XMLDTDValidator fDTDValidator;
    protected XMLNamespaceBinder fNamespaceBinder;
    protected ValidationManager fValidationManager;
    // state
    protected XMLLocator fLocator;
    protected boolean fParseInProgress=false;
    //
    // Constructors
    //

    public DTDConfiguration(){
        this(null,null,null);
    } // <init>()

    public DTDConfiguration(SymbolTable symbolTable,
                            XMLGrammarPool grammarPool,
                            XMLComponentManager parentSettings){
        super(symbolTable,parentSettings);
        // add default recognized features
        final String[] recognizedFeatures={
                //WARN_ON_DUPLICATE_ATTDEF,     // from XMLDTDScannerImpl
                //WARN_ON_UNDECLARED_ELEMDEF,   // from XMLDTDScannerImpl
                //ALLOW_JAVA_ENCODINGS,         // from XMLEntityManager
                CONTINUE_AFTER_FATAL_ERROR,
                LOAD_EXTERNAL_DTD,    // from XMLDTDScannerImpl
                //NOTIFY_BUILTIN_REFS,  // from XMLDocumentFragmentScannerImpl
                //NOTIFY_CHAR_REFS,         // from XMLDocumentFragmentScannerImpl
                //WARN_ON_DUPLICATE_ENTITYDEF,  // from XMLEntityManager
        };
        addRecognizedFeatures(recognizedFeatures);
        // set state for default features
        //setFeature(WARN_ON_DUPLICATE_ATTDEF, false);  // from XMLDTDScannerImpl
        //setFeature(WARN_ON_UNDECLARED_ELEMDEF, false);  // from XMLDTDScannerImpl
        //setFeature(ALLOW_JAVA_ENCODINGS, false);      // from XMLEntityManager
        setFeature(CONTINUE_AFTER_FATAL_ERROR,false);
        setFeature(LOAD_EXTERNAL_DTD,true);      // from XMLDTDScannerImpl
        //setFeature(NOTIFY_BUILTIN_REFS, false);   // from XMLDocumentFragmentScannerImpl
        //setFeature(NOTIFY_CHAR_REFS, false);      // from XMLDocumentFragmentScannerImpl
        //setFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);   // from XMLEntityManager
        // add default recognized properties
        final String[] recognizedProperties={
                ERROR_REPORTER,
                ENTITY_MANAGER,
                DOCUMENT_SCANNER,
                DTD_SCANNER,
                DTD_PROCESSOR,
                DTD_VALIDATOR,
                NAMESPACE_BINDER,
                XMLGRAMMAR_POOL,
                DATATYPE_VALIDATOR_FACTORY,
                VALIDATION_MANAGER,
                JAXP_SCHEMA_SOURCE,
                JAXP_SCHEMA_LANGUAGE,
                LOCALE,
                SECURITY_MANAGER,
                XML_SECURITY_PROPERTY_MANAGER
        };
        addRecognizedProperties(recognizedProperties);
        fGrammarPool=grammarPool;
        if(fGrammarPool!=null){
            setProperty(XMLGRAMMAR_POOL,fGrammarPool);
        }
        fEntityManager=createEntityManager();
        setProperty(ENTITY_MANAGER,fEntityManager);
        addComponent(fEntityManager);
        fErrorReporter=createErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
        setProperty(ERROR_REPORTER,fErrorReporter);
        addComponent(fErrorReporter);
        fScanner=createDocumentScanner();
        setProperty(DOCUMENT_SCANNER,fScanner);
        if(fScanner instanceof XMLComponent){
            addComponent((XMLComponent)fScanner);
        }
        fDTDScanner=createDTDScanner();
        if(fDTDScanner!=null){
            setProperty(DTD_SCANNER,fDTDScanner);
            if(fDTDScanner instanceof XMLComponent){
                addComponent((XMLComponent)fDTDScanner);
            }
        }
        fDTDProcessor=createDTDProcessor();
        if(fDTDProcessor!=null){
            setProperty(DTD_PROCESSOR,fDTDProcessor);
            if(fDTDProcessor instanceof XMLComponent){
                addComponent((XMLComponent)fDTDProcessor);
            }
        }
        fDTDValidator=createDTDValidator();
        if(fDTDValidator!=null){
            setProperty(DTD_VALIDATOR,fDTDValidator);
            addComponent(fDTDValidator);
        }
        fNamespaceBinder=createNamespaceBinder();
        if(fNamespaceBinder!=null){
            setProperty(NAMESPACE_BINDER,fNamespaceBinder);
            addComponent(fNamespaceBinder);
        }
        fDatatypeValidatorFactory=createDatatypeValidatorFactory();
        if(fDatatypeValidatorFactory!=null){
            setProperty(DATATYPE_VALIDATOR_FACTORY,
                    fDatatypeValidatorFactory);
        }
        fValidationManager=createValidationManager();
        if(fValidationManager!=null){
            setProperty(VALIDATION_MANAGER,fValidationManager);
        }
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
        setProperty(XML_SECURITY_PROPERTY_MANAGER,new XMLSecurityPropertyManager());
    } // <init>(SymbolTable,XMLGrammarPool)

    protected XMLEntityManager createEntityManager(){
        return new XMLEntityManager();
    } // createEntityManager():XMLEntityManager

    protected XMLErrorReporter createErrorReporter(){
        return new XMLErrorReporter();
    } // createErrorReporter():XMLErrorReporter
    //
    // Public methods
    //

    protected XMLDocumentScanner createDocumentScanner(){
        return new XMLDocumentScannerImpl();
    } // createDocumentScanner():XMLDocumentScanner

    protected XMLDTDScanner createDTDScanner(){
        return new XMLDTDScannerImpl();
    } // createDTDScanner():XMLDTDScanner

    protected XMLDTDProcessor createDTDProcessor(){
        return new XMLDTDProcessor();
    } // createDTDProcessor():XMLDTDProcessor
    //
    // XMLPullParserConfiguration methods
    //
    // parsing

    protected XMLDTDValidator createDTDValidator(){
        return new XMLDTDValidator();
    } // createDTDValidator():XMLDTDValidator

    protected XMLNamespaceBinder createNamespaceBinder(){
        return new XMLNamespaceBinder();
    } // createNamespaceBinder():XMLNamespaceBinder

    protected DTDDVFactory createDatatypeValidatorFactory(){
        return DTDDVFactory.getInstance();
    } // createDatatypeValidatorFactory():DatatypeValidatorFactory
    //
    // XMLParserConfiguration methods
    //

    protected ValidationManager createValidationManager(){
        return new ValidationManager();
    }
    //
    // Protected methods
    //

    public DTDConfiguration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)

    public DTDConfiguration(SymbolTable symbolTable,
                            XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)

    public PropertyState getPropertyState(String propertyId)
            throws XMLConfigurationException{
        if(LOCALE.equals(propertyId)){
            return PropertyState.is(getLocale());
        }
        return super.getPropertyState(propertyId);
    }
    // features and properties

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
    } // parse(InputSource)

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        if(LOCALE.equals(propertyId)){
            setLocale((Locale)value);
        }
        super.setProperty(propertyId,value);
    }
    // factory methods

    public void setLocale(Locale locale) throws XNIException{
        super.setLocale(locale);
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    protected void reset() throws XNIException{
        if(fValidationManager!=null)
            fValidationManager.reset();
        // configure the pipeline and initialize the components
        configurePipeline();
        super.reset();
    } // reset()

    protected void configurePipeline(){
        // REVISIT: This should be better designed. In other words, we
        //          need to figure out what is the best way for people to
        //          re-use *most* of the standard configuration but do
        //          things common things such as remove a component (e.g.
        //          the validator), insert a new component (e.g. XInclude),
        //          etc... -Ac
        // setup document pipeline
        if(fDTDValidator!=null){
            fScanner.setDocumentHandler(fDTDValidator);
            if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
                // filters
                fDTDValidator.setDocumentHandler(fNamespaceBinder);
                fDTDValidator.setDocumentSource(fScanner);
                fNamespaceBinder.setDocumentHandler(fDocumentHandler);
                fNamespaceBinder.setDocumentSource(fDTDValidator);
                fLastComponent=fNamespaceBinder;
            }else{
                fDTDValidator.setDocumentHandler(fDocumentHandler);
                fDTDValidator.setDocumentSource(fScanner);
                fLastComponent=fDTDValidator;
            }
        }else{
            if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
                fScanner.setDocumentHandler(fNamespaceBinder);
                fNamespaceBinder.setDocumentHandler(fDocumentHandler);
                fNamespaceBinder.setDocumentSource(fScanner);
                fLastComponent=fNamespaceBinder;
            }else{
                fScanner.setDocumentHandler(fDocumentHandler);
                fLastComponent=fScanner;
            }
        }
        configureDTDPipeline();
    } // configurePipeline()

    protected void configureDTDPipeline(){
        // setup dtd pipeline
        if(fDTDScanner!=null){
            fProperties.put(DTD_SCANNER,fDTDScanner);
            if(fDTDProcessor!=null){
                fProperties.put(DTD_PROCESSOR,fDTDProcessor);
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
            }else{
                fDTDScanner.setDTDHandler(fDTDHandler);
                if(fDTDHandler!=null){
                    fDTDHandler.setDTDSource(fDTDScanner);
                }
                fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
                if(fDTDContentModelHandler!=null){
                    fDTDContentModelHandler.setDTDContentModelSource(fDTDScanner);
                }
            }
        }
    }

    protected PropertyState checkProperty(String propertyId)
            throws XMLConfigurationException{
        //
        // Xerces Properties
        //
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.DTD_SCANNER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)){
                return PropertyState.RECOGNIZED;
            }
        }
        //
        // Not recognized
        //
        return super.checkProperty(propertyId);
    } // checkProperty(String)

    protected FeatureState checkFeature(String featureId)
            throws XMLConfigurationException{
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
        }
        //
        // Not recognized
        //
        return super.checkFeature(featureId);
    } // checkFeature(String)

    public void setInputSource(XMLInputSource inputSource)
            throws XMLConfigurationException, IOException{
        // REVISIT: this method used to reset all the components and
        //          construct the pipeline. Now reset() is called
        //          in parse (boolean) just before we parse the document
        //          Should this method still throw exceptions..?
        fInputSource=inputSource;
    } // setInputSource(XMLInputSource)

    public boolean parse(boolean complete) throws XNIException, IOException{
        //
        // reset and configure pipeline and set InputSource.
        if(fInputSource!=null){
            try{
                // resets and sets the pipeline.
                reset();
                fScanner.setInputSource(fInputSource);
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
            return fScanner.scanDocument(complete);
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
    } // parse(boolean):boolean

    public void cleanup(){
        fEntityManager.closeReaders();
    }
} // class DTDConfiguration
