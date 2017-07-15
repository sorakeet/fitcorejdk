/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class XMLGrammarPreparser{
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String GRAMMAR_POOL=
            Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;
    //
    // Constants
    //
    // feature:  continue-after-fatal-error
    private final static String CONTINUE_AFTER_FATAL_ERROR=
            Constants.XERCES_FEATURE_PREFIX+Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    // the "built-in" grammar loaders
    private static final Map<String,String> KNOWN_LOADERS;
    private static final String[] RECOGNIZED_PROPERTIES={
            SYMBOL_TABLE,
            ERROR_REPORTER,
            ERROR_HANDLER,
            ENTITY_RESOLVER,
            GRAMMAR_POOL,
    };

    static{
        Map<String,String> loaders=new HashMap<>();
        loaders.put(XMLGrammarDescription.XML_SCHEMA,
                "com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader");
        loaders.put(XMLGrammarDescription.XML_DTD,
                "com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader");
        KNOWN_LOADERS=Collections.unmodifiableMap(loaders);
    }

    // Data
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected XMLGrammarPool fGrammarPool;
    protected Locale fLocale;
    // Map holding our loaders
    private Map<String,XMLGrammarLoader> fLoaders;
    //
    // Constructors
    //

    public XMLGrammarPreparser(){
        this(new SymbolTable());
    } // <init>()

    public XMLGrammarPreparser(SymbolTable symbolTable){
        fSymbolTable=symbolTable;
        fLoaders=new HashMap<>();
        fErrorReporter=new XMLErrorReporter();
        setLocale(Locale.getDefault());
        fEntityResolver=new XMLEntityManager();
        // those are all the basic properties...
    } // <init>(SymbolTable)
    //
    // Public methods
    //

    public boolean registerPreparser(String grammarType,XMLGrammarLoader loader){
        if(loader==null){ // none specified!
            if(KNOWN_LOADERS.containsKey(grammarType)){
                // got one; just instantiate it...
                String loaderName=(String)KNOWN_LOADERS.get(grammarType);
                try{
                    XMLGrammarLoader gl=(XMLGrammarLoader)(ObjectFactory.newInstance(loaderName,true));
                    fLoaders.put(grammarType,gl);
                }catch(Exception e){
                    return false;
                }
                return true;
            }
            return false;
        }
        // were given one
        fLoaders.put(grammarType,loader);
        return true;
    } // registerPreparser(String, XMLGrammarLoader):  boolean

    public Grammar preparseGrammar(String type,XMLInputSource
            is) throws XNIException, IOException{
        if(fLoaders.containsKey(type)){
            XMLGrammarLoader gl=fLoaders.get(type);
            // make sure gl's been set up with all the "basic" properties:
            gl.setProperty(SYMBOL_TABLE,fSymbolTable);
            gl.setProperty(ENTITY_RESOLVER,fEntityResolver);
            gl.setProperty(ERROR_REPORTER,fErrorReporter);
            // potentially, not all will support this one...
            if(fGrammarPool!=null){
                try{
                    gl.setProperty(GRAMMAR_POOL,fGrammarPool);
                }catch(Exception e){
                    // too bad...
                }
            }
            return gl.loadGrammar(is);
        }
        return null;
    } // preparseGrammar(String, XMLInputSource):  Grammar

    public Locale getLocale(){
        return fLocale;
    } // getLocale():  Locale

    public void setLocale(Locale locale){
        fLocale=locale;
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    public XMLErrorHandler getErrorHandler(){
        return fErrorReporter.getErrorHandler();
    } // getErrorHandler():  XMLErrorHandler

    public void setErrorHandler(XMLErrorHandler errorHandler){
        fErrorReporter.setProperty(ERROR_HANDLER,errorHandler);
    } // setErrorHandler(XMLErrorHandler)

    public XMLEntityResolver getEntityResolver(){
        return fEntityResolver;
    } // getEntityResolver():  XMLEntityResolver

    public void setEntityResolver(XMLEntityResolver entityResolver){
        fEntityResolver=entityResolver;
    } // setEntityResolver(XMLEntityResolver)

    public XMLGrammarPool getGrammarPool(){
        return fGrammarPool;
    } // getGrammarPool():  XMLGrammarPool

    public void setGrammarPool(XMLGrammarPool grammarPool){
        fGrammarPool=grammarPool;
    } // setGrammarPool(XMLGrammarPool)

    // it's possible the application may want access to a certain loader to do
    // some custom work.
    public XMLGrammarLoader getLoader(String type){
        return fLoaders.get(type);
    } // getLoader(String):  XMLGrammarLoader

    // set a feature.  This method tries to set it on all
    // registered loaders; it eats any resulting exceptions.  If
    // an app needs to know if a particular feature is supported
    // by a grammar loader of a particular type, it will have
    // to retrieve that loader and use the loader's setFeature method.
    public void setFeature(String featureId,boolean value){
        for(Map.Entry<String,XMLGrammarLoader> entry : fLoaders.entrySet()){
            try{
                XMLGrammarLoader gl=entry.getValue();
                gl.setFeature(featureId,value);
            }catch(Exception e){
                // eat it up...
            }
        }
        // since our error reporter is a property we set later,
        // make sure features it understands are also set.
        if(featureId.equals(CONTINUE_AFTER_FATAL_ERROR)){
            fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR,value);
        }
    } //setFeature(String, boolean)

    // set a property.  This method tries to set it on all
    // registered loaders; it eats any resulting exceptions.  If
    // an app needs to know if a particular property is supported
    // by a grammar loader of a particular type, it will have
    // to retrieve that loader and use the loader's setProperty method.
    // <p> <strong>An application should use the explicit method
    // in this class to set "standard" properties like error handler etc.</strong>
    public void setProperty(String propId,Object value){
        for(Map.Entry<String,XMLGrammarLoader> entry : fLoaders.entrySet()){
            try{
                XMLGrammarLoader gl=entry.getValue();
                gl.setProperty(propId,value);
            }catch(Exception e){
                // eat it up...
            }
        }
    } //setProperty(String, Object)

    // get status of feature in a particular loader.  This
    // catches no exceptions--including NPE's--so the application had
    // better make sure the loader exists and knows about this feature.
    // @param type type of grammar to look for the feature in.
    // @param featureId the feature string to query.
    // @return the value of the feature.
    public boolean getFeature(String type,String featureId){
        XMLGrammarLoader gl=fLoaders.get(type);
        return gl.getFeature(featureId);
    } // getFeature (String, String):  boolean

    // get status of property in a particular loader.  This
    // catches no exceptions--including NPE's--so the application had
    // better make sure the loader exists and knows about this property.
    // <strong>For standard properties--that will be supported
    // by all loaders--the specific methods should be queried!</strong>
    // @param type type of grammar to look for the property in.
    // @param propertyId the property string to query.
    // @return the value of the property.
    public Object getProperty(String type,String propertyId){
        XMLGrammarLoader gl=fLoaders.get(type);
        return gl.getProperty(propertyId);
    } // getProperty(String, String):  Object
} // class XMLGrammarPreparser
