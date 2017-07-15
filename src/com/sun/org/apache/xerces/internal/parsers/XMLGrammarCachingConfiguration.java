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

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.IOException;

public class XMLGrammarCachingConfiguration
        extends XIncludeAwareParserConfiguration{
    //
    // Constants
    //
    // a larg(ish) prime to use for a symbol table to be shared
    // among
    // potentially man parsers.  Start one as close to 2K (20
    // times larger than normal) and see what happens...
    public static final int BIG_PRIME=2039;
    // the static symbol table to be shared amongst parsers
    protected static final SynchronizedSymbolTable fStaticSymbolTable=
            new SynchronizedSymbolTable(BIG_PRIME);
    // the Grammar Pool to be shared similarly
    protected static final XMLGrammarPoolImpl fStaticGrammarPool=
            new XMLGrammarPoolImpl();
    // schema full checking constant
    protected static final String SCHEMA_FULL_CHECKING=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_FULL_CHECKING;
    // Data
    // variables needed for caching schema grammars.
    protected XMLSchemaLoader fSchemaLoader;
    // the DTD grammar loader
    protected XMLDTDLoader fDTDLoader;
    //
    // Constructors
    //

    public XMLGrammarCachingConfiguration(){
        this(fStaticSymbolTable,fStaticGrammarPool,null);
    } // <init>()

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable,
                                          XMLGrammarPool grammarPool,
                                          XMLComponentManager parentSettings){
        super(symbolTable,grammarPool,parentSettings);
        // REVISIT:  may need to add some features/properties
        // specific to this configuration at some point...
        // add default recognized features
        // set state for default features
        // add default recognized properties
        // create and register missing components
        fSchemaLoader=new XMLSchemaLoader(fSymbolTable);
        fSchemaLoader.setProperty(XMLGRAMMAR_POOL,fGrammarPool);
        // and set up the DTD loader too:
        fDTDLoader=new XMLDTDLoader(fSymbolTable,fGrammarPool);
    } // <init>(SymbolTable,XMLGrammarPool, XMLComponentManager)

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable){
        this(symbolTable,fStaticGrammarPool,null);
    } // <init>(SymbolTable)

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable,
                                          XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)
    //
    // Public methods
    //

    public void lockGrammarPool(){
        fGrammarPool.lockPool();
    } // lockGrammarPool()

    public void clearGrammarPool(){
        fGrammarPool.clear();
    } // clearGrammarPool()

    public void unlockGrammarPool(){
        fGrammarPool.unlockPool();
    } // unlockGrammarPool()

    public Grammar parseGrammar(String type,String uri)
            throws XNIException, IOException{
        XMLInputSource source=new XMLInputSource(null,uri,null);
        return parseGrammar(type,source);
    }

    public Grammar parseGrammar(String type,XMLInputSource
            is) throws XNIException, IOException{
        if(type.equals(XMLGrammarDescription.XML_SCHEMA)){
            // by default, make all XMLGrammarPoolImpl's schema grammars available to fSchemaHandler
            return parseXMLSchema(is);
        }else if(type.equals(XMLGrammarDescription.XML_DTD)){
            return parseDTD(is);
        }
        // don't know this grammar...
        return null;
    } // parseGrammar(String, XMLInputSource):  Grammar
    //
    // Protected methods
    //
    // package-protected methods

    SchemaGrammar parseXMLSchema(XMLInputSource is)
            throws IOException{
        XMLEntityResolver resolver=getEntityResolver();
        if(resolver!=null){
            fSchemaLoader.setEntityResolver(resolver);
        }
        if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN)==null){
            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,new XSMessageFormatter());
        }
        fSchemaLoader.setProperty(ERROR_REPORTER,fErrorReporter);
        String propPrefix=Constants.XERCES_PROPERTY_PREFIX;
        String propName=propPrefix+Constants.SCHEMA_LOCATION;
        fSchemaLoader.setProperty(propName,getProperty(propName));
        propName=propPrefix+Constants.SCHEMA_NONS_LOCATION;
        fSchemaLoader.setProperty(propName,getProperty(propName));
        propName=Constants.JAXP_PROPERTY_PREFIX+Constants.SCHEMA_SOURCE;
        fSchemaLoader.setProperty(propName,getProperty(propName));
        fSchemaLoader.setFeature(SCHEMA_FULL_CHECKING,getFeature(SCHEMA_FULL_CHECKING));
        // Should check whether the grammar with this namespace is already in
        // the grammar resolver. But since we don't know the target namespace
        // of the document here, we leave such check to XSDHandler
        SchemaGrammar grammar=(SchemaGrammar)fSchemaLoader.loadGrammar(is);
        // by default, hand it off to the grammar pool
        if(grammar!=null){
            fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_SCHEMA,
                    new Grammar[]{grammar});
        }
        return grammar;
    } // parseXMLSchema(XMLInputSource) :  SchemaGrammar

    DTDGrammar parseDTD(XMLInputSource is)
            throws IOException{
        XMLEntityResolver resolver=getEntityResolver();
        if(resolver!=null){
            fDTDLoader.setEntityResolver(resolver);
        }
        fDTDLoader.setProperty(ERROR_REPORTER,fErrorReporter);
        // Should check whether the grammar with this namespace is already in
        // the grammar resolver. But since we don't know the target namespace
        // of the document here, we leave such check to the application...
        DTDGrammar grammar=(DTDGrammar)fDTDLoader.loadGrammar(is);
        // by default, hand it off to the grammar pool
        if(grammar!=null){
            fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_DTD,
                    new Grammar[]{grammar});
        }
        return grammar;
    } // parseXMLDTD(XMLInputSource) :  DTDGrammar
} // class XMLGrammarCachingConfiguration
