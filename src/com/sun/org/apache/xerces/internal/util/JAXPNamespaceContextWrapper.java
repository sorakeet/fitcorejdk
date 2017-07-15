/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.NamespaceContext;

import javax.xml.XMLConstants;
import java.util.*;

public final class JAXPNamespaceContextWrapper implements NamespaceContext{
    private final Vector fAllPrefixes=new Vector();
    private javax.xml.namespace.NamespaceContext fNamespaceContext;
    private SymbolTable fSymbolTable;
    private List fPrefixes;
    private int[] fContext=new int[8];
    private int fCurrentContext;

    public JAXPNamespaceContextWrapper(SymbolTable symbolTable){
        setSymbolTable(symbolTable);
    }

    public javax.xml.namespace.NamespaceContext getNamespaceContext(){
        return fNamespaceContext;
    }

    public void setNamespaceContext(javax.xml.namespace.NamespaceContext context){
        fNamespaceContext=context;
    }

    public SymbolTable getSymbolTable(){
        return fSymbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable){
        fSymbolTable=symbolTable;
    }

    public List getDeclaredPrefixes(){
        return fPrefixes;
    }

    public void setDeclaredPrefixes(List prefixes){
        fPrefixes=prefixes;
    }

    public void pushContext(){
        // extend the array, if necessary
        if(fCurrentContext+1==fContext.length){
            int[] contextarray=new int[fContext.length*2];
            System.arraycopy(fContext,0,contextarray,0,fContext.length);
            fContext=contextarray;
        }
        // push context
        fContext[++fCurrentContext]=fAllPrefixes.size();
        if(fPrefixes!=null){
            fAllPrefixes.addAll(fPrefixes);
        }
    }

    public void popContext(){
        fAllPrefixes.setSize(fContext[fCurrentContext--]);
    }

    public boolean declarePrefix(String prefix,String uri){
        return true;
    }

    public String getURI(String prefix){
        if(fNamespaceContext!=null){
            String uri=fNamespaceContext.getNamespaceURI(prefix);
            if(uri!=null&&!XMLConstants.NULL_NS_URI.equals(uri)){
                return (fSymbolTable!=null)?fSymbolTable.addSymbol(uri):uri.intern();
            }
        }
        return null;
    }

    public String getPrefix(String uri){
        if(fNamespaceContext!=null){
            if(uri==null){
                uri=XMLConstants.NULL_NS_URI;
            }
            String prefix=fNamespaceContext.getPrefix(uri);
            if(prefix==null){
                prefix=XMLConstants.DEFAULT_NS_PREFIX;
            }
            return (fSymbolTable!=null)?fSymbolTable.addSymbol(prefix):prefix.intern();
        }
        return null;
    }

    public int getDeclaredPrefixCount(){
        return (fPrefixes!=null)?fPrefixes.size():0;
    }

    public String getDeclaredPrefixAt(int index){
        return (String)fPrefixes.get(index);
    }

    public Enumeration getAllPrefixes(){
        // There may be duplicate prefixes in the list so we
        // first transfer them to a set to ensure uniqueness.
        return Collections.enumeration(new TreeSet(fAllPrefixes));
    }

    public void reset(){
        fCurrentContext=0;
        fContext[fCurrentContext]=0;
        fAllPrefixes.clear();
    }
} // JAXPNamespaceContextWrapper
