/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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

public final class SynchronizedSymbolTable
        extends SymbolTable{
    //
    // Data
    //
    protected SymbolTable fSymbolTable;
    //
    // Constructors
    //

    public SynchronizedSymbolTable(SymbolTable symbolTable){
        fSymbolTable=symbolTable;
    } // <init>(SymbolTable)

    // construct synchronized symbol table of default size
    public SynchronizedSymbolTable(){
        fSymbolTable=new SymbolTable();
    } // init()

    // construct synchronized symbol table of given size
    public SynchronizedSymbolTable(int size){
        fSymbolTable=new SymbolTable(size);
    } // init(int)
    //
    // SymbolTable methods
    //

    public String addSymbol(String symbol){
        synchronized(fSymbolTable){
            return fSymbolTable.addSymbol(symbol);
        }
    } // addSymbol(String)

    public String addSymbol(char[] buffer,int offset,int length){
        synchronized(fSymbolTable){
            return fSymbolTable.addSymbol(buffer,offset,length);
        }
    } // addSymbol(char[],int,int):String

    public boolean containsSymbol(String symbol){
        synchronized(fSymbolTable){
            return fSymbolTable.containsSymbol(symbol);
        }
    } // containsSymbol(String):boolean

    public boolean containsSymbol(char[] buffer,int offset,int length){
        synchronized(fSymbolTable){
            return fSymbolTable.containsSymbol(buffer,offset,length);
        }
    } // containsSymbol(char[],int,int):boolean
} // class SynchronizedSymbolTable
