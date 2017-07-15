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
package com.sun.org.apache.xerces.internal.util;

public final class ShadowedSymbolTable
        extends SymbolTable{
    //
    // Data
    //
    protected SymbolTable fSymbolTable;
    //
    // Constructors
    //

    public ShadowedSymbolTable(SymbolTable symbolTable){
        fSymbolTable=symbolTable;
    } // <init>(SymbolTable)
    //
    // SymbolTable methods
    //

    public String addSymbol(String symbol){
        if(fSymbolTable.containsSymbol(symbol)){
            return fSymbolTable.addSymbol(symbol);
        }
        return super.addSymbol(symbol);
    } // addSymbol(String)

    public String addSymbol(char[] buffer,int offset,int length){
        if(fSymbolTable.containsSymbol(buffer,offset,length)){
            return fSymbolTable.addSymbol(buffer,offset,length);
        }
        return super.addSymbol(buffer,offset,length);
    } // addSymbol(char[],int,int):String

    public int hash(String symbol){
        return fSymbolTable.hash(symbol);
    } // hash(String):int

    public int hash(char[] buffer,int offset,int length){
        return fSymbolTable.hash(buffer,offset,length);
    } // hash(char[],int,int):int
} // class ShadowedSymbolTable
