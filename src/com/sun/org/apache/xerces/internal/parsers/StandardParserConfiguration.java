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

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

public class StandardParserConfiguration
        extends DTDConfiguration{
    //
    // Constants
    //
    // feature identifiers
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
    // property identifiers
    protected static final String SCHEMA_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String SCHEMA_LOCATION=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_LOCATION;
    protected static final String SCHEMA_NONS_LOCATION=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_NONS_LOCATION;
    protected static final String SCHEMA_DV_FACTORY=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_DV_FACTORY_PROPERTY;
    //
    // Data
    //
    // components (non-configurable)
    protected XMLSchemaValidator fSchemaValidator;
    //
    // Constructors
    //

    public StandardParserConfiguration(){
        this(null,null,null);
    } // <init>()

    public StandardParserConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool,
                                       XMLComponentManager parentSettings){
        super(symbolTable,grammarPool,parentSettings);
        // add default recognized features
        final String[] recognizedFeatures={
                NORMALIZE_DATA,
                SCHEMA_ELEMENT_DEFAULT,
                SCHEMA_AUGMENT_PSVI,
                GENERATE_SYNTHETIC_ANNOTATIONS,
                VALIDATE_ANNOTATIONS,
                HONOUR_ALL_SCHEMALOCATIONS,
                NAMESPACE_GROWTH,
                TOLERATE_DUPLICATES,
                // NOTE: These shouldn't really be here but since the XML Schema
                //       validator is constructed dynamically, its recognized
                //       features might not have been set and it would cause a
                //       not-recognized exception to be thrown. -Ac
                XMLSCHEMA_VALIDATION,
                XMLSCHEMA_FULL_CHECKING,
        };
        addRecognizedFeatures(recognizedFeatures);
        // set state for default features
        setFeature(SCHEMA_ELEMENT_DEFAULT,true);
        setFeature(NORMALIZE_DATA,true);
        setFeature(SCHEMA_AUGMENT_PSVI,true);
        setFeature(GENERATE_SYNTHETIC_ANNOTATIONS,false);
        setFeature(VALIDATE_ANNOTATIONS,false);
        setFeature(HONOUR_ALL_SCHEMALOCATIONS,false);
        setFeature(NAMESPACE_GROWTH,false);
        setFeature(TOLERATE_DUPLICATES,false);
        // add default recognized properties
        final String[] recognizedProperties={
                // NOTE: These shouldn't really be here but since the XML Schema
                //       validator is constructed dynamically, its recognized
                //       properties might not have been set and it would cause a
                //       not-recognized exception to be thrown. -Ac
                SCHEMA_LOCATION,
                SCHEMA_NONS_LOCATION,
                SCHEMA_DV_FACTORY,
        };
        addRecognizedProperties(recognizedProperties);
    } // <init>(SymbolTable,XMLGrammarPool)

    public StandardParserConfiguration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)

    public StandardParserConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)
    //
    // Public methods
    //

    protected void configurePipeline(){
        super.configurePipeline();
        if(getFeature(XMLSCHEMA_VALIDATION)){
            // If schema validator was not in the pipeline insert it.
            if(fSchemaValidator==null){
                fSchemaValidator=new XMLSchemaValidator();
                // add schema component
                fProperties.put(SCHEMA_VALIDATOR,fSchemaValidator);
                addComponent(fSchemaValidator);
                // add schema message formatter
                if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN)==null){
                    XSMessageFormatter xmft=new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,xmft);
                }
            }
            fLastComponent=fSchemaValidator;
            fNamespaceBinder.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            fSchemaValidator.setDocumentSource(fNamespaceBinder);
        }
    } // configurePipeline()
    // features and properties

    protected FeatureState checkFeature(String featureId)
            throws XMLConfigurationException{
        //
        // Xerces Features
        //
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
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
        }
        //
        // Not recognized
        //
        return super.checkFeature(featureId);
    } // checkFeature(String)

    protected PropertyState checkProperty(String propertyId)
            throws XMLConfigurationException{
        //
        // Xerces Properties
        //
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
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
        //
        // Not recognized
        //
        return super.checkProperty(propertyId);
    } // checkProperty(String)
} // class StandardParserConfiguration
