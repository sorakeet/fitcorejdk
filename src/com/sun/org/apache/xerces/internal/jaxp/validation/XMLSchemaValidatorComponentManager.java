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
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

final class XMLSchemaValidatorComponentManager extends ParserConfigurationSettings implements
        XMLComponentManager{
    // feature identifiers
    private static final String SCHEMA_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    private static final String SCHEMA_ELEMENT_DEFAULT=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_ELEMENT_DEFAULT;
    private static final String USE_GRAMMAR_POOL_ONLY=
            Constants.XERCES_FEATURE_PREFIX+Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    // property identifiers
    private static final String ENTITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_MANAGER_PROPERTY;
    private static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    private static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    private static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    private static final String NAMESPACE_CONTEXT=
            Constants.XERCES_PROPERTY_PREFIX+Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String SCHEMA_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SECURITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SECURITY_MANAGER_PROPERTY;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    private static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String XMLGRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String LOCALE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.LOCALE_PROPERTY;
    private final HashMap fComponents=new HashMap();
    //
    // Configuration
    //
    private final HashMap fInitFeatures=new HashMap();
    private final HashMap fInitProperties=new HashMap();
    private final XMLSecurityPropertyManager fSecurityPropertyMgr;
    //
    // Data
    //
    private boolean _isSecureMode=false;
    private boolean fConfigUpdated=true;
    private boolean fUseGrammarPoolOnly;
    //
    // Components
    //
    private XMLEntityManager fEntityManager;
    private XMLErrorReporter fErrorReporter;
    private NamespaceContext fNamespaceContext;
    private XMLSchemaValidator fSchemaValidator;
    private ValidationManager fValidationManager;
    private XMLSecurityManager fInitSecurityManager;
    //
    // User Objects
    //
    private ErrorHandler fErrorHandler=null;
    private LSResourceResolver fResourceResolver=null;
    private Locale fLocale=null;

    public XMLSchemaValidatorComponentManager(XSGrammarPoolContainer grammarContainer){
        // setup components
        fEntityManager=new XMLEntityManager();
        fComponents.put(ENTITY_MANAGER,fEntityManager);
        fErrorReporter=new XMLErrorReporter();
        fComponents.put(ERROR_REPORTER,fErrorReporter);
        fNamespaceContext=new NamespaceSupport();
        fComponents.put(NAMESPACE_CONTEXT,fNamespaceContext);
        fSchemaValidator=new XMLSchemaValidator();
        fComponents.put(SCHEMA_VALIDATOR,fSchemaValidator);
        fValidationManager=new ValidationManager();
        fComponents.put(VALIDATION_MANAGER,fValidationManager);
        // setup other properties
        fComponents.put(ENTITY_RESOLVER,null);
        fComponents.put(ERROR_HANDLER,null);
        fComponents.put(SYMBOL_TABLE,new SymbolTable());
        // setup grammar pool
        fComponents.put(XMLGRAMMAR_POOL,grammarContainer.getGrammarPool());
        fUseGrammarPoolOnly=grammarContainer.isFullyComposed();
        // add schema message formatter to error reporter
        fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,new XSMessageFormatter());
        // add all recognized features and properties and apply their defaults
        addRecognizedParamsAndSetDefaults(fEntityManager,grammarContainer);
        addRecognizedParamsAndSetDefaults(fErrorReporter,grammarContainer);
        addRecognizedParamsAndSetDefaults(fSchemaValidator,grammarContainer);
        boolean secureProcessing=grammarContainer.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        if(System.getSecurityManager()!=null){
            _isSecureMode=true;
            secureProcessing=true;
        }
        fInitSecurityManager=(XMLSecurityManager)
                grammarContainer.getProperty(SECURITY_MANAGER);
        if(fInitSecurityManager!=null){
            fInitSecurityManager.setSecureProcessing(secureProcessing);
        }else{
            fInitSecurityManager=new XMLSecurityManager(secureProcessing);
        }
        setProperty(SECURITY_MANAGER,fInitSecurityManager);
        //pass on properties set on SchemaFactory
        fSecurityPropertyMgr=(XMLSecurityPropertyManager)
                grammarContainer.getProperty(Constants.XML_SECURITY_PROPERTY_MANAGER);
        setProperty(XML_SECURITY_PROPERTY_MANAGER,fSecurityPropertyMgr);
    }

    public void addRecognizedParamsAndSetDefaults(XMLComponent component,XSGrammarPoolContainer grammarContainer){
        // register component's recognized features
        final String[] recognizedFeatures=component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        // register component's recognized properties
        final String[] recognizedProperties=component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        // set default values
        setFeatureDefaults(component,recognizedFeatures,grammarContainer);
        setPropertyDefaults(component,recognizedProperties);
    }

    private void setFeatureDefaults(final XMLComponent component,
                                    final String[] recognizedFeatures,XSGrammarPoolContainer grammarContainer){
        if(recognizedFeatures!=null){
            for(int i=0;i<recognizedFeatures.length;++i){
                String featureId=recognizedFeatures[i];
                Boolean state=grammarContainer.getFeature(featureId);
                if(state==null){
                    state=component.getFeatureDefault(featureId);
                }
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
    }

    private void setPropertyDefaults(final XMLComponent component,final String[] recognizedProperties){
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

    public void setFeature(String featureId,boolean value) throws XMLConfigurationException{
        if(PARSER_SETTINGS.equals(featureId)){
            throw new XMLConfigurationException(Status.NOT_SUPPORTED,featureId);
        }else if(value==false&&(VALIDATION.equals(featureId)||SCHEMA_VALIDATION.equals(featureId))){
            throw new XMLConfigurationException(Status.NOT_SUPPORTED,featureId);
        }else if(USE_GRAMMAR_POOL_ONLY.equals(featureId)&&value!=fUseGrammarPoolOnly){
            throw new XMLConfigurationException(Status.NOT_SUPPORTED,featureId);
        }
        if(XMLConstants.FEATURE_SECURE_PROCESSING.equals(featureId)){
            if(_isSecureMode&&!value){
                throw new XMLConfigurationException(Status.NOT_ALLOWED,XMLConstants.FEATURE_SECURE_PROCESSING);
            }
            fInitSecurityManager.setSecureProcessing(value);
            setProperty(SECURITY_MANAGER,fInitSecurityManager);
            if(value&&Constants.IS_JDK8_OR_ABOVE){
                fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD,
                        XMLSecurityPropertyManager.State.FSP,Constants.EXTERNAL_ACCESS_DEFAULT_FSP);
                fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA,
                        XMLSecurityPropertyManager.State.FSP,Constants.EXTERNAL_ACCESS_DEFAULT_FSP);
                setProperty(XML_SECURITY_PROPERTY_MANAGER,fSecurityPropertyMgr);
            }
            return;
        }
        fConfigUpdated=true;
        fEntityManager.setFeature(featureId,value);
        fErrorReporter.setFeature(featureId,value);
        fSchemaValidator.setFeature(featureId,value);
        if(!fInitFeatures.containsKey(featureId)){
            boolean current=super.getFeature(featureId);
            fInitFeatures.put(featureId,current?Boolean.TRUE:Boolean.FALSE);
        }
        super.setFeature(featureId,value);
    }

    public void setProperty(String propertyId,Object value) throws XMLConfigurationException{
        if(ENTITY_MANAGER.equals(propertyId)||ERROR_REPORTER.equals(propertyId)||
                NAMESPACE_CONTEXT.equals(propertyId)||SCHEMA_VALIDATOR.equals(propertyId)||
                SYMBOL_TABLE.equals(propertyId)||VALIDATION_MANAGER.equals(propertyId)||
                XMLGRAMMAR_POOL.equals(propertyId)){
            throw new XMLConfigurationException(Status.NOT_SUPPORTED,propertyId);
        }
        fConfigUpdated=true;
        fEntityManager.setProperty(propertyId,value);
        fErrorReporter.setProperty(propertyId,value);
        fSchemaValidator.setProperty(propertyId,value);
        if(ENTITY_RESOLVER.equals(propertyId)||ERROR_HANDLER.equals(propertyId)||
                SECURITY_MANAGER.equals(propertyId)){
            fComponents.put(propertyId,value);
            return;
        }else if(LOCALE.equals(propertyId)){
            setLocale((Locale)value);
            fComponents.put(propertyId,value);
            return;
        }
        //check if the property is managed by security manager
        if(fInitSecurityManager==null||
                !fInitSecurityManager.setLimit(propertyId,XMLSecurityManager.State.APIPROPERTY,value)){
            //check if the property is managed by security property manager
            if(fSecurityPropertyMgr==null||
                    !fSecurityPropertyMgr.setValue(propertyId,XMLSecurityPropertyManager.State.APIPROPERTY,value)){
                //fall back to the existing property manager
                if(!fInitProperties.containsKey(propertyId)){
                    fInitProperties.put(propertyId,super.getProperty(propertyId));
                }
                super.setProperty(propertyId,value);
            }
        }
    }

    public FeatureState getFeatureState(String featureId)
            throws XMLConfigurationException{
        if(PARSER_SETTINGS.equals(featureId)){
            return FeatureState.is(fConfigUpdated);
        }else if(VALIDATION.equals(featureId)||SCHEMA_VALIDATION.equals(featureId)){
            return FeatureState.is(true);
        }else if(USE_GRAMMAR_POOL_ONLY.equals(featureId)){
            return FeatureState.is(fUseGrammarPoolOnly);
        }else if(XMLConstants.FEATURE_SECURE_PROCESSING.equals(featureId)){
            return FeatureState.is(fInitSecurityManager.isSecureProcessing());
        }else if(SCHEMA_ELEMENT_DEFAULT.equals(featureId)){
            return FeatureState.is(true); //pre-condition: VALIDATION and SCHEMA_VALIDATION are always true
        }
        return super.getFeatureState(featureId);
    }

    public PropertyState getPropertyState(String propertyId)
            throws XMLConfigurationException{
        if(LOCALE.equals(propertyId)){
            return PropertyState.is(getLocale());
        }
        final Object component=fComponents.get(propertyId);
        if(component!=null){
            return PropertyState.is(component);
        }else if(fComponents.containsKey(propertyId)){
            return PropertyState.is(null);
        }
        return super.getPropertyState(propertyId);
    }

    Locale getLocale(){
        return fLocale;
    }

    void setLocale(Locale locale){
        fLocale=locale;
        fErrorReporter.setLocale(locale);
    }

    public void reset() throws XNIException{
        fNamespaceContext.reset();
        fValidationManager.reset();
        fEntityManager.reset(this);
        fErrorReporter.reset(this);
        fSchemaValidator.reset(this);
        // Mark configuration as fixed.
        fConfigUpdated=false;
    }

    ErrorHandler getErrorHandler(){
        return fErrorHandler;
    }

    void setErrorHandler(ErrorHandler errorHandler){
        fErrorHandler=errorHandler;
        setProperty(ERROR_HANDLER,(errorHandler!=null)?new ErrorHandlerWrapper(errorHandler):
                new ErrorHandlerWrapper(DraconianErrorHandler.getInstance()));
    }

    LSResourceResolver getResourceResolver(){
        return fResourceResolver;
    }

    void setResourceResolver(LSResourceResolver resourceResolver){
        fResourceResolver=resourceResolver;
        setProperty(ENTITY_RESOLVER,new DOMEntityResolverWrapper(resourceResolver));
    }

    void restoreInitialState(){
        fConfigUpdated=true;
        // Remove error resolver and error handler
        fComponents.put(ENTITY_RESOLVER,null);
        fComponents.put(ERROR_HANDLER,null);
        // Set the Locale back to null.
        setLocale(null);
        fComponents.put(LOCALE,null);
        // Restore initial security manager
        fComponents.put(SECURITY_MANAGER,fInitSecurityManager);
        // Set the Locale back to null.
        setLocale(null);
        fComponents.put(LOCALE,null);
        // Reset feature and property values to their initial values
        if(!fInitFeatures.isEmpty()){
            Iterator iter=fInitFeatures.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry=(Map.Entry)iter.next();
                String name=(String)entry.getKey();
                boolean value=((Boolean)entry.getValue()).booleanValue();
                super.setFeature(name,value);
            }
            fInitFeatures.clear();
        }
        if(!fInitProperties.isEmpty()){
            Iterator iter=fInitProperties.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry=(Map.Entry)iter.next();
                String name=(String)entry.getKey();
                Object value=entry.getValue();
                super.setProperty(name,value);
            }
            fInitProperties.clear();
        }
    }
} // XMLSchemaValidatorComponentManager
