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
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public abstract class BasicParserConfiguration
        extends ParserConfigurationSettings
        implements XMLParserConfiguration{
    //
    // Constants
    //
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
    //
    // Data
    //
    // components (non-configurable)
    protected SymbolTable fSymbolTable;
    // data
    protected Locale fLocale;
    protected ArrayList fComponents;
    // handlers
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDocumentSource fLastComponent;
    //
    // Constructors
    //

    protected BasicParserConfiguration(){
        this(null,null);
    } // <init>()

    protected BasicParserConfiguration(SymbolTable symbolTable,
                                       XMLComponentManager parentSettings){
        super(parentSettings);
        // create a vector to hold all the components in use
        fComponents=new ArrayList();
        // create table for features and properties
        fFeatures=new HashMap();
        fProperties=new HashMap();
        // add default recognized features
        final String[] recognizedFeatures={
                PARSER_SETTINGS,
                VALIDATION,
                NAMESPACES,
                EXTERNAL_GENERAL_ENTITIES,
                EXTERNAL_PARAMETER_ENTITIES,
        };
        addRecognizedFeatures(recognizedFeatures);
        fFeatures.put(PARSER_SETTINGS,Boolean.TRUE);
        // set state for default features
        fFeatures.put(VALIDATION,Boolean.FALSE);
        fFeatures.put(NAMESPACES,Boolean.TRUE);
        fFeatures.put(EXTERNAL_GENERAL_ENTITIES,Boolean.TRUE);
        fFeatures.put(EXTERNAL_PARAMETER_ENTITIES,Boolean.TRUE);
        // add default recognized properties
        final String[] recognizedProperties={
                XML_STRING,
                SYMBOL_TABLE,
                ERROR_HANDLER,
                ENTITY_RESOLVER,
        };
        addRecognizedProperties(recognizedProperties);
        if(symbolTable==null){
            symbolTable=new SymbolTable();
        }
        fSymbolTable=symbolTable;
        fProperties.put(SYMBOL_TABLE,fSymbolTable);
    } // <init>(SymbolTable)

    protected BasicParserConfiguration(SymbolTable symbolTable){
        this(symbolTable,null);
    } // <init>(SymbolTable)

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
        // set default values
        if(recognizedFeatures!=null){
            for(int i=0;i<recognizedFeatures.length;i++){
                String featureId=recognizedFeatures[i];
                Boolean state=component.getFeatureDefault(featureId);
                if(state!=null){
                    super.setFeature(featureId,state.booleanValue());
                }
            }
        }
        if(recognizedProperties!=null){
            for(int i=0;i<recognizedProperties.length;i++){
                String propertyId=recognizedProperties[i];
                Object value=component.getPropertyDefault(propertyId);
                if(value!=null){
                    super.setProperty(propertyId,value);
                }
            }
        }
    } // addComponent(XMLComponent)
    //
    // XMLParserConfiguration methods
    //

    public abstract void parse(XMLInputSource inputSource)
            throws XNIException, IOException;

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        // forward to every component
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=(XMLComponent)fComponents.get(i);
            c.setFeature(featureId,state);
        }
        // save state if noone "objects"
        super.setFeature(featureId,state);
    } // setFeature(String,boolean)    public void setDocumentHandler(XMLDocumentHandler documentHandler){
        fDocumentHandler=documentHandler;
        if(fLastComponent!=null){
            fLastComponent.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fLastComponent);
            }
        }
    } // setDocumentHandler(XMLDocumentHandler)

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // forward to every component
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=(XMLComponent)fComponents.get(i);
            c.setProperty(propertyId,value);
        }
        // store value if noone "objects"
        super.setProperty(propertyId,value);
    } // setProperty(String,Object)    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    protected FeatureState checkFeature(String featureId)
            throws XMLConfigurationException{
        //
        // Xerces Features
        //
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
            //
            // special performance feature: no one by component manager is allowed to set it
            //
            if(suffixLength==Constants.PARSER_SETTINGS.length()&&
                    featureId.endsWith(Constants.PARSER_SETTINGS)){
                return FeatureState.NOT_SUPPORTED;
            }
        }
        return super.checkFeature(featureId);
    } // checkFeature(String)    public void setDTDHandler(XMLDTDHandler dtdHandler){
        fDTDHandler=dtdHandler;
    } // setDTDHandler(XMLDTDHandler)

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
    } // checkProperty(String)    public XMLDTDHandler getDTDHandler(){
        return fDTDHandler;
    } // getDTDHandler():XMLDTDHandler

    protected void reset() throws XNIException{
        // reset every component
        int count=fComponents.size();
        for(int i=0;i<count;i++){
            XMLComponent c=(XMLComponent)fComponents.get(i);
            c.reset(this);
        }
    } // reset()    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler){
        fDTDContentModelHandler=handler;
    } // setDTDContentModelHandler(XMLDTDContentModelHandler)

    public XMLDTDContentModelHandler getDTDContentModelHandler(){
        return fDTDContentModelHandler;
    } // getDTDContentModelHandler():XMLDTDContentModelHandler

    public void setEntityResolver(XMLEntityResolver resolver){
        // REVISIT: Should this be a property?
        fProperties.put(ENTITY_RESOLVER,resolver);
    } // setEntityResolver(XMLEntityResolver)

    public XMLEntityResolver getEntityResolver(){
        // REVISIT: Should this be a property?
        return (XMLEntityResolver)fProperties.get(ENTITY_RESOLVER);
    } // getEntityResolver():XMLEntityResolver

    public void setErrorHandler(XMLErrorHandler errorHandler){
        // REVISIT: Should this be a property?
        fProperties.put(ERROR_HANDLER,errorHandler);
    } // setErrorHandler(XMLErrorHandler)

    public XMLErrorHandler getErrorHandler(){
        // REVISIT: Should this be a property?
        return (XMLErrorHandler)fProperties.get(ERROR_HANDLER);
    } // getErrorHandler():XMLErrorHandler





    public void setLocale(Locale locale) throws XNIException{
        fLocale=locale;
    } // setLocale(Locale)

    public Locale getLocale(){
        return fLocale;
    } // getLocale():Locale
    //
    // Protected methods
    //






} // class BasicParserConfiguration
