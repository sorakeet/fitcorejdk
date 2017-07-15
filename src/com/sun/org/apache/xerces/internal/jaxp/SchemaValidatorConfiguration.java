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
package com.sun.org.apache.xerces.internal.jaxp;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

final class SchemaValidatorConfiguration implements XMLComponentManager{
    // feature identifiers
    private static final String SCHEMA_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    private static final String USE_GRAMMAR_POOL_ONLY=
            Constants.XERCES_FEATURE_PREFIX+Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    private static final String PARSER_SETTINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.PARSER_SETTINGS;
    // property identifiers
    private static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    private static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String XMLGRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    //
    // Data
    //
    private final XMLComponentManager fParentComponentManager;
    private final XMLGrammarPool fGrammarPool;
    private final boolean fUseGrammarPoolOnly;
    private final ValidationManager fValidationManager;

    public SchemaValidatorConfiguration(XMLComponentManager parentManager,
                                        XSGrammarPoolContainer grammarContainer,ValidationManager validationManager){
        fParentComponentManager=parentManager;
        fGrammarPool=grammarContainer.getGrammarPool();
        fUseGrammarPoolOnly=grammarContainer.isFullyComposed();
        fValidationManager=validationManager;
        // add schema message formatter to error reporter
        try{
            XMLErrorReporter errorReporter=(XMLErrorReporter)fParentComponentManager.getProperty(ERROR_REPORTER);
            if(errorReporter!=null){
                errorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,new XSMessageFormatter());
            }
        }
        // Ignore exception.
        catch(XMLConfigurationException exc){
        }
    }

    public boolean getFeature(String featureId)
            throws XMLConfigurationException{
        FeatureState state=getFeatureState(featureId);
        if(state.isExceptional()){
            throw new XMLConfigurationException(state.status,featureId);
        }
        return state.state;
    }

    public boolean getFeature(String featureId,boolean defaultValue){
        FeatureState state=getFeatureState(featureId);
        if(state.isExceptional()){
            return defaultValue;
        }
        return state.state;
    }

    public Object getProperty(String propertyId)
            throws XMLConfigurationException{
        PropertyState state=getPropertyState(propertyId);
        if(state.isExceptional()){
            throw new XMLConfigurationException(state.status,propertyId);
        }
        return state.state;
    }

    public Object getProperty(String propertyId,Object defaultValue){
        PropertyState state=getPropertyState(propertyId);
        if(state.isExceptional()){
            return defaultValue;
        }
        return state.state;
    }

    public FeatureState getFeatureState(String featureId){
        if(PARSER_SETTINGS.equals(featureId)){
            return fParentComponentManager.getFeatureState(featureId);
        }else if(VALIDATION.equals(featureId)||SCHEMA_VALIDATION.equals(featureId)){
            return FeatureState.is(true);
        }else if(USE_GRAMMAR_POOL_ONLY.equals(featureId)){
            return FeatureState.is(fUseGrammarPoolOnly);
        }
        return fParentComponentManager.getFeatureState(featureId);
    }

    public PropertyState getPropertyState(String propertyId){
        if(XMLGRAMMAR_POOL.equals(propertyId)){
            return PropertyState.is(fGrammarPool);
        }else if(VALIDATION_MANAGER.equals(propertyId)){
            return PropertyState.is(fValidationManager);
        }
        return fParentComponentManager.getPropertyState(propertyId);
    }
}
