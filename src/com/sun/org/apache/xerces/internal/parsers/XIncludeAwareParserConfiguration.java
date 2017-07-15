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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XIncludeAwareParserConfiguration extends XML11Configuration{
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS=
            Constants.SAX_FEATURE_PREFIX+Constants.ALLOW_DTD_EVENTS_AFTER_ENDDTD_FEATURE;
    protected static final String XINCLUDE_FIXUP_BASE_URIS=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE;
    protected static final String XINCLUDE_FIXUP_LANGUAGE=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE;
    protected static final String XINCLUDE_FEATURE=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FEATURE;
    protected static final String XINCLUDE_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XINCLUDE_HANDLER_PROPERTY;
    protected static final String NAMESPACE_CONTEXT=
            Constants.XERCES_PROPERTY_PREFIX+Constants.NAMESPACE_CONTEXT_PROPERTY;
    //
    // Components
    //
    protected XIncludeHandler fXIncludeHandler;
    protected NamespaceSupport fNonXIncludeNSContext;
    protected XIncludeNamespaceSupport fXIncludeNSContext;
    protected NamespaceContext fCurrentNSContext;
    protected boolean fXIncludeEnabled=false;

    public XIncludeAwareParserConfiguration(){
        this(null,null,null);
    } // <init>()

    public XIncludeAwareParserConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool,
            XMLComponentManager parentSettings){
        super(symbolTable,grammarPool,parentSettings);
        final String[] recognizedFeatures={
                ALLOW_UE_AND_NOTATION_EVENTS,
                XINCLUDE_FIXUP_BASE_URIS,
                XINCLUDE_FIXUP_LANGUAGE
        };
        addRecognizedFeatures(recognizedFeatures);
        // add default recognized properties
        final String[] recognizedProperties=
                {XINCLUDE_HANDLER,NAMESPACE_CONTEXT};
        addRecognizedProperties(recognizedProperties);
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS,true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS,true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE,true);
        fNonXIncludeNSContext=new NamespaceSupport();
        fCurrentNSContext=fNonXIncludeNSContext;
        setProperty(NAMESPACE_CONTEXT,fNonXIncludeNSContext);
    }

    public XIncludeAwareParserConfiguration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)

    public XIncludeAwareParserConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)

    public FeatureState getFeatureState(String featureId)
            throws XMLConfigurationException{
        if(featureId.equals(PARSER_SETTINGS)){
            return FeatureState.is(fConfigUpdated);
        }else if(featureId.equals(XINCLUDE_FEATURE)){
            return FeatureState.is(fXIncludeEnabled);
        }
        return super.getFeatureState0(featureId);
    } // getFeature(String):boolean

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        if(featureId.equals(XINCLUDE_FEATURE)){
            fXIncludeEnabled=state;
            fConfigUpdated=true;
            return;
        }
        super.setFeature(featureId,state);
    }

    protected void configureXML11Pipeline(){
        super.configureXML11Pipeline();
        if(fXIncludeEnabled){
            // If the XInclude handler was not in the pipeline insert it.
            if(fXIncludeHandler==null){
                fXIncludeHandler=new XIncludeHandler();
                // add XInclude component
                setProperty(XINCLUDE_HANDLER,fXIncludeHandler);
                addCommonComponent(fXIncludeHandler);
                fXIncludeHandler.reset(this);
            }
            // Setup NamespaceContext
            if(fCurrentNSContext!=fXIncludeNSContext){
                if(fXIncludeNSContext==null){
                    fXIncludeNSContext=new XIncludeNamespaceSupport();
                }
                fCurrentNSContext=fXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT,fXIncludeNSContext);
            }
            // configure XML 1.1. DTD pipeline
            fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
            fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
            fXML11DTDProcessor.setDTDHandler(fXIncludeHandler);
            fXIncludeHandler.setDTDSource(fXML11DTDProcessor);
            fXIncludeHandler.setDTDHandler(fDTDHandler);
            if(fDTDHandler!=null){
                fDTDHandler.setDTDSource(fXIncludeHandler);
            }
            // configure XML document pipeline: insert after DTDValidator and
            // before XML Schema validator
            XMLDocumentSource prev=null;
            if(fFeatures.get(XMLSCHEMA_VALIDATION)==Boolean.TRUE){
                // we don't have to worry about fSchemaValidator being null, since
                // super.configurePipeline() instantiated it if the feature was set
                prev=fSchemaValidator.getDocumentSource();
            }
            // Otherwise, insert after the last component in the pipeline
            else{
                prev=fLastComponent;
                fLastComponent=fXIncludeHandler;
            }
            XMLDocumentHandler next=prev.getDocumentHandler();
            prev.setDocumentHandler(fXIncludeHandler);
            fXIncludeHandler.setDocumentSource(prev);
            if(next!=null){
                fXIncludeHandler.setDocumentHandler(next);
                next.setDocumentSource(fXIncludeHandler);
            }
        }else{
            // Setup NamespaceContext
            if(fCurrentNSContext!=fNonXIncludeNSContext){
                fCurrentNSContext=fNonXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT,fNonXIncludeNSContext);
            }
        }
    } // configureXML11Pipeline()

    protected void configurePipeline(){
        super.configurePipeline();
        if(fXIncludeEnabled){
            // If the XInclude handler was not in the pipeline insert it.
            if(fXIncludeHandler==null){
                fXIncludeHandler=new XIncludeHandler();
                // add XInclude component
                setProperty(XINCLUDE_HANDLER,fXIncludeHandler);
                addCommonComponent(fXIncludeHandler);
                fXIncludeHandler.reset(this);
            }
            // Setup NamespaceContext
            if(fCurrentNSContext!=fXIncludeNSContext){
                if(fXIncludeNSContext==null){
                    fXIncludeNSContext=new XIncludeNamespaceSupport();
                }
                fCurrentNSContext=fXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT,fXIncludeNSContext);
            }
            //configure DTD pipeline
            fDTDScanner.setDTDHandler(fDTDProcessor);
            fDTDProcessor.setDTDSource(fDTDScanner);
            fDTDProcessor.setDTDHandler(fXIncludeHandler);
            fXIncludeHandler.setDTDSource(fDTDProcessor);
            fXIncludeHandler.setDTDHandler(fDTDHandler);
            if(fDTDHandler!=null){
                fDTDHandler.setDTDSource(fXIncludeHandler);
            }
            // configure XML document pipeline: insert after DTDValidator and
            // before XML Schema validator
            XMLDocumentSource prev=null;
            if(fFeatures.get(XMLSCHEMA_VALIDATION)==Boolean.TRUE){
                // we don't have to worry about fSchemaValidator being null, since
                // super.configurePipeline() instantiated it if the feature was set
                prev=fSchemaValidator.getDocumentSource();
            }
            // Otherwise, insert after the last component in the pipeline
            else{
                prev=fLastComponent;
                fLastComponent=fXIncludeHandler;
            }
            XMLDocumentHandler next=prev.getDocumentHandler();
            prev.setDocumentHandler(fXIncludeHandler);
            fXIncludeHandler.setDocumentSource(prev);
            if(next!=null){
                fXIncludeHandler.setDocumentHandler(next);
                next.setDocumentSource(fXIncludeHandler);
            }
        }else{
            // Setup NamespaceContext
            if(fCurrentNSContext!=fNonXIncludeNSContext){
                fCurrentNSContext=fNonXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT,fNonXIncludeNSContext);
            }
        }
    } // configurePipeline()
}
