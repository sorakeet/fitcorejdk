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

public class NonValidatingConfiguration
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
    protected static final String NORMALIZE_DATA=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_ELEMENT_DEFAULT;
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
    protected static final String DTD_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DTD_VALIDATOR_PROPERTY;
    protected static final String NAMESPACE_BINDER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.NAMESPACE_BINDER_PROPERTY;
    protected static final String DATATYPE_VALIDATOR_FACTORY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String SCHEMA_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String LOCALE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.LOCALE_PROPERTY;
    protected static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    private static final String SECURITY_MANAGER=Constants.SECURITY_MANAGER;
    // debugging
    private static final boolean PRINT_EXCEPTION_STACK_TRACE=false;
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
    protected ValidationManager fValidationManager;
    protected boolean fConfigUpdated=false;
    // state
    protected XMLLocator fLocator;
    protected boolean fParseInProgress=false;
    // private data
    private XMLNSDocumentScannerImpl fNamespaceScanner;
    private XMLDocumentScannerImpl fNonNSScanner;
    //
    // Constructors
    //

    public NonValidatingConfiguration(){
        this(null,null,null);
    } // <init>()

    public NonValidatingConfiguration(SymbolTable symbolTable,
                                      XMLGrammarPool grammarPool,
                                      XMLComponentManager parentSettings){
        super(symbolTable,parentSettings);
        // add default recognized features
        final String[] recognizedFeatures={
                PARSER_SETTINGS,
                NAMESPACES,
                //WARN_ON_DUPLICATE_ATTDEF,     // from XMLDTDScannerImpl
                //WARN_ON_UNDECLARED_ELEMDEF,   // from XMLDTDScannerImpl
                //ALLOW_JAVA_ENCODINGS,         // from XMLEntityManager
                CONTINUE_AFTER_FATAL_ERROR,
                //LOAD_EXTERNAL_DTD,    // from XMLDTDScannerImpl
                //NOTIFY_BUILTIN_REFS,  // from XMLDocumentFragmentScannerImpl
                //NOTIFY_CHAR_REFS,         // from XMLDocumentFragmentScannerImpl
                //WARN_ON_DUPLICATE_ENTITYDEF   // from XMLEntityManager
        };
        addRecognizedFeatures(recognizedFeatures);
        // set state for default features
        //setFeature(WARN_ON_DUPLICATE_ATTDEF, false);  // from XMLDTDScannerImpl
        //setFeature(WARN_ON_UNDECLARED_ELEMDEF, false);    // from XMLDTDScannerImpl
        //setFeature(ALLOW_JAVA_ENCODINGS, false);      // from XMLEntityManager
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR,Boolean.FALSE);
        fFeatures.put(PARSER_SETTINGS,Boolean.TRUE);
        fFeatures.put(NAMESPACES,Boolean.TRUE);
        //setFeature(LOAD_EXTERNAL_DTD, true);      // from XMLDTDScannerImpl
        //setFeature(NOTIFY_BUILTIN_REFS, false);   // from XMLDocumentFragmentScannerImpl
        //setFeature(NOTIFY_CHAR_REFS, false);      // from XMLDocumentFragmentScannerImpl
        //setFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);   // from XMLEntityManager
        // add default recognized properties
        final String[] recognizedProperties={
                ERROR_REPORTER,
                ENTITY_MANAGER,
                DOCUMENT_SCANNER,
                DTD_SCANNER,
                DTD_VALIDATOR,
                NAMESPACE_BINDER,
                XMLGRAMMAR_POOL,
                DATATYPE_VALIDATOR_FACTORY,
                VALIDATION_MANAGER,
                LOCALE,
                SECURITY_MANAGER,
                XML_SECURITY_PROPERTY_MANAGER
        };
        addRecognizedProperties(recognizedProperties);
        fGrammarPool=grammarPool;
        if(fGrammarPool!=null){
            fProperties.put(XMLGRAMMAR_POOL,fGrammarPool);
        }
        fEntityManager=createEntityManager();
        fProperties.put(ENTITY_MANAGER,fEntityManager);
        addComponent(fEntityManager);
        fErrorReporter=createErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
        fProperties.put(ERROR_REPORTER,fErrorReporter);
        addComponent(fErrorReporter);
        // this configuration delays creation of the scanner
        // till it is known if namespace processing should be performed
        fDTDScanner=createDTDScanner();
        if(fDTDScanner!=null){
            fProperties.put(DTD_SCANNER,fDTDScanner);
            if(fDTDScanner instanceof XMLComponent){
                addComponent((XMLComponent)fDTDScanner);
            }
        }
        fDatatypeValidatorFactory=createDatatypeValidatorFactory();
        if(fDatatypeValidatorFactory!=null){
            fProperties.put(DATATYPE_VALIDATOR_FACTORY,
                    fDatatypeValidatorFactory);
        }
        fValidationManager=createValidationManager();
        if(fValidationManager!=null){
            fProperties.put(VALIDATION_MANAGER,fValidationManager);
        }
        // add message formatters
        if(fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN)==null){
            XMLMessageFormatter xmft=new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN,xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN,xmft);
        }
        fConfigUpdated=false;
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

    protected XMLDTDScanner createDTDScanner(){
        return new XMLDTDScannerImpl();
    } // createDTDScanner():XMLDTDScanner

    protected DTDDVFactory createDatatypeValidatorFactory(){
        return DTDDVFactory.getInstance();
    } // createDatatypeValidatorFactory():DatatypeValidatorFactory

    protected ValidationManager createValidationManager(){
        return new ValidationManager();
    }

    public NonValidatingConfiguration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)

    public NonValidatingConfiguration(SymbolTable symbolTable,
                                      XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)
    //
    // XMLPullParserConfiguration methods
    //
    // parsing

    public FeatureState getFeatureState(String featureId)
            throws XMLConfigurationException{
        // make this feature special
        if(featureId.equals(PARSER_SETTINGS)){
            return FeatureState.is(fConfigUpdated);
        }
        return super.getFeatureState(featureId);
    } // getFeature(String):boolean

    public PropertyState getPropertyState(String propertyId)
            throws XMLConfigurationException{
        if(LOCALE.equals(propertyId)){
            return PropertyState.is(getLocale());
        }
        return super.getPropertyState(propertyId);
    }

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
    //
    // XMLParserConfiguration methods
    //

    //
    // Public methods
    //
    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        fConfigUpdated=true;
        super.setFeature(featureId,state);
    }
    //
    // Protected methods
    //

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        fConfigUpdated=true;
        if(LOCALE.equals(propertyId)){
            setLocale((Locale)value);
        }
        super.setProperty(propertyId,value);
    }

    public void setLocale(Locale locale) throws XNIException{
        super.setLocale(locale);
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)
    // features and properties

    protected void reset() throws XNIException{
        if(fValidationManager!=null)
            fValidationManager.reset();
        // configure the pipeline and initialize the components
        configurePipeline();
        super.reset();
    } // reset()

    protected void configurePipeline(){
        // create appropriate scanner
        // and register it as one of the components.
        if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
            if(fNamespaceScanner==null){
                fNamespaceScanner=new XMLNSDocumentScannerImpl();
                addComponent((XMLComponent)fNamespaceScanner);
            }
            fProperties.put(DOCUMENT_SCANNER,fNamespaceScanner);
            fNamespaceScanner.setDTDValidator(null);
            fScanner=fNamespaceScanner;
        }else{
            if(fNonNSScanner==null){
                fNonNSScanner=new XMLDocumentScannerImpl();
                addComponent((XMLComponent)fNonNSScanner);
            }
            fProperties.put(DOCUMENT_SCANNER,fNonNSScanner);
            fScanner=fNonNSScanner;
        }
        fScanner.setDocumentHandler(fDocumentHandler);
        fLastComponent=fScanner;
        // setup dtd pipeline
        if(fDTDScanner!=null){
            fDTDScanner.setDTDHandler(fDTDHandler);
            fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        }
    } // configurePipeline()
    // factory methods

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
        if(propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.JAXP_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.SCHEMA_SOURCE.length()&&
                    propertyId.endsWith(Constants.SCHEMA_SOURCE)){
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

    protected XMLDocumentScanner createDocumentScanner(){
        return null;
    } // createDocumentScanner():XMLDocumentScanner
} // class NonValidatingConfiguration
