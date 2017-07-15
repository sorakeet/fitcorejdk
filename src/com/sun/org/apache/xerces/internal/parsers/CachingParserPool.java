/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.util.ShadowedSymbolTable;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

public class CachingParserPool{
    //
    // Constants
    //
    public static final boolean DEFAULT_SHADOW_SYMBOL_TABLE=false;
    public static final boolean DEFAULT_SHADOW_GRAMMAR_POOL=false;
    //
    // Data
    //
    protected SymbolTable fSynchronizedSymbolTable;
    protected XMLGrammarPool fSynchronizedGrammarPool;
    protected boolean fShadowSymbolTable=DEFAULT_SHADOW_SYMBOL_TABLE;
    protected boolean fShadowGrammarPool=DEFAULT_SHADOW_GRAMMAR_POOL;
    //
    // Constructors
    //

    public CachingParserPool(){
        this(new SymbolTable(),new XMLGrammarPoolImpl());
    } // <init>()

    public CachingParserPool(SymbolTable symbolTable,XMLGrammarPool grammarPool){
        fSynchronizedSymbolTable=new SynchronizedSymbolTable(symbolTable);
        fSynchronizedGrammarPool=new SynchronizedGrammarPool(grammarPool);
    } // <init>(SymbolTable,XMLGrammarPool)
    //
    // Public methods
    //

    public SymbolTable getSymbolTable(){
        return fSynchronizedSymbolTable;
    } // getSymbolTable():SymbolTable

    public XMLGrammarPool getXMLGrammarPool(){
        return fSynchronizedGrammarPool;
    } // getXMLGrammarPool():XMLGrammarPool
    // setters and getters

    public void setShadowSymbolTable(boolean shadow){
        fShadowSymbolTable=shadow;
    } // setShadowSymbolTable(boolean)
    // factory methods

    public DOMParser createDOMParser(){
        SymbolTable symbolTable=fShadowSymbolTable
                ?new ShadowedSymbolTable(fSynchronizedSymbolTable)
                :fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool=fShadowGrammarPool
                ?new ShadowedGrammarPool(fSynchronizedGrammarPool)
                :fSynchronizedGrammarPool;
        return new DOMParser(symbolTable,grammarPool);
    } // createDOMParser():DOMParser

    public SAXParser createSAXParser(){
        SymbolTable symbolTable=fShadowSymbolTable
                ?new ShadowedSymbolTable(fSynchronizedSymbolTable)
                :fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool=fShadowGrammarPool
                ?new ShadowedGrammarPool(fSynchronizedGrammarPool)
                :fSynchronizedGrammarPool;
        return new SAXParser(symbolTable,grammarPool);
    } // createSAXParser():SAXParser
    //
    // Classes
    //

    public static final class SynchronizedGrammarPool
            implements XMLGrammarPool{
        //
        // Data
        //
        private XMLGrammarPool fGrammarPool;
        //
        // Constructors
        //

        public SynchronizedGrammarPool(XMLGrammarPool grammarPool){
            fGrammarPool=grammarPool;
        } // <init>(XMLGrammarPool)
        //
        // GrammarPool methods
        //

        // retrieve the initial set of grammars for the validator
        // to work with.
        // REVISIT:  does this need to be synchronized since it's just reading?
        // @param grammarType type of the grammars to be retrieved.
        // @return the initial grammar set the validator may place in its "bucket"
        public Grammar[] retrieveInitialGrammarSet(String grammarType){
            synchronized(fGrammarPool){
                return fGrammarPool.retrieveInitialGrammarSet(grammarType);
            }
        } // retrieveInitialGrammarSet(String):  Grammar[]

        // give the grammarPool the option of caching these grammars.
        // This certainly must be synchronized.
        // @param grammarType The type of the grammars to be cached.
        // @param grammars the Grammars that may be cached (unordered, Grammars previously
        //  given to the validator may be included).
        public void cacheGrammars(String grammarType,Grammar[] grammars){
            synchronized(fGrammarPool){
                fGrammarPool.cacheGrammars(grammarType,grammars);
            }
        } // cacheGrammars(String, Grammar[]);

        // retrieve a particular grammar.
        // REVISIT:  does this need to be synchronized since it's just reading?
        // @param gDesc description of the grammar to be retrieved
        // @return Grammar corresponding to gDesc, or null if none exists.
        public Grammar retrieveGrammar(XMLGrammarDescription gDesc){
            synchronized(fGrammarPool){
                return fGrammarPool.retrieveGrammar(gDesc);
            }
        } // retrieveGrammar(XMLGrammarDesc):  Grammar

        public void lockPool(){
            synchronized(fGrammarPool){
                fGrammarPool.lockPool();
            }
        } // lockPool()

        public void unlockPool(){
            synchronized(fGrammarPool){
                fGrammarPool.unlockPool();
            }
        } // unlockPool()

        public void clear(){
            synchronized(fGrammarPool){
                fGrammarPool.clear();
            }
        } // lockPool()
        /***
         * Methods corresponding to original (pre Xerces2.0.0final)
         * grammarPool have been commented out.
         */
        /**
         * Puts the specified grammar into the grammar pool.
         *
         * @param key Key to associate with grammar.
         * @param grammar Grammar object.
         */
        /******
         public void putGrammar(String key, Grammar grammar) {
         synchronized (fGrammarPool) {
         fGrammarPool.putGrammar(key, grammar);
         }
         } // putGrammar(String,Grammar)
         *******/
        /**
         * Returns the grammar associated to the specified key.
         *
         * @param key The key of the grammar.
         */
        /**********
         public Grammar getGrammar(String key) {
         synchronized (fGrammarPool) {
         return fGrammarPool.getGrammar(key);
         }
         } // getGrammar(String):Grammar
         ***********/
        /**
         * Removes the grammar associated to the specified key from the
         * grammar pool and returns the removed grammar.
         *
         * @param key The key of the grammar.
         */
        /**********
         public Grammar removeGrammar(String key) {
         synchronized (fGrammarPool) {
         return fGrammarPool.removeGrammar(key);
         }
         } // removeGrammar(String):Grammar
         ******/
        /**
         * Returns true if the grammar pool contains a grammar associated
         * to the specified key.
         *
         * @param key The key of the grammar.
         */
        /**********
         public boolean containsGrammar(String key) {
         synchronized (fGrammarPool) {
         return fGrammarPool.containsGrammar(key);
         }
         } // containsGrammar(String):boolean
         ********/
    } // class SynchronizedGrammarPool

    public static final class ShadowedGrammarPool
            extends XMLGrammarPoolImpl{
        //
        // Data
        //
        private XMLGrammarPool fGrammarPool;
        //
        // Constructors
        //

        public ShadowedGrammarPool(XMLGrammarPool grammarPool){
            fGrammarPool=grammarPool;
        } // <init>(GrammarPool)
        //
        // GrammarPool methods
        //

        public Grammar[] retrieveInitialGrammarSet(String grammarType){
            Grammar[] grammars=super.retrieveInitialGrammarSet(grammarType);
            if(grammars!=null) return grammars;
            return fGrammarPool.retrieveInitialGrammarSet(grammarType);
        } // retrieveInitialGrammarSet(String):  Grammar[]

        public void cacheGrammars(String grammarType,Grammar[] grammars){
            // better give both grammars a shot...
            super.cacheGrammars(grammarType,grammars);
            fGrammarPool.cacheGrammars(grammarType,grammars);
        } // cacheGrammars(grammarType, Grammar[]);

        public Grammar retrieveGrammar(XMLGrammarDescription gDesc){
            Grammar g=super.retrieveGrammar(gDesc);
            if(g!=null) return g;
            return fGrammarPool.retrieveGrammar(gDesc);
        } // retrieveGrammar(XMLGrammarDesc):  Grammar

        public Grammar getGrammar(XMLGrammarDescription desc){
            if(super.containsGrammar(desc)){
                return super.getGrammar(desc);
            }
            return null;
        } // getGrammar(XMLGrammarDescription):Grammar

        public boolean containsGrammar(XMLGrammarDescription desc){
            return super.containsGrammar(desc);
        } // containsGrammar(XMLGrammarDescription):boolean
    } // class ShadowedGrammarPool
} // class CachingParserPool
