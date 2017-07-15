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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.util.NamespaceSupport;

public class SchemaNamespaceSupport
        extends NamespaceSupport{
    public SchemaNamespaceSupport(){
        super();
    } // constructor

    // more effecient than NamespaceSupport(NamespaceContext)
    public SchemaNamespaceSupport(SchemaNamespaceSupport nSupport){
        fNamespaceSize=nSupport.fNamespaceSize;
        if(fNamespace.length<fNamespaceSize)
            fNamespace=new String[fNamespaceSize];
        System.arraycopy(nSupport.fNamespace,0,fNamespace,0,fNamespaceSize);
        fCurrentContext=nSupport.fCurrentContext;
        if(fContext.length<=fCurrentContext)
            fContext=new int[fCurrentContext+1];
        System.arraycopy(nSupport.fContext,0,fContext,0,fCurrentContext+1);
    } // end constructor

    public void setEffectiveContext(String[] namespaceDecls){
        if(namespaceDecls==null||namespaceDecls.length==0) return;
        pushContext();
        int newSize=fNamespaceSize+namespaceDecls.length;
        if(fNamespace.length<newSize){
            // expand namespace's size...
            String[] tempNSArray=new String[newSize];
            System.arraycopy(fNamespace,0,tempNSArray,0,fNamespace.length);
            fNamespace=tempNSArray;
        }
        System.arraycopy(namespaceDecls,0,fNamespace,fNamespaceSize,
                namespaceDecls.length);
        fNamespaceSize=newSize;
    } // setEffectiveContext(String):void

    public String[] getEffectiveLocalContext(){
        // the trick here is to recognize that all local contexts
        // happen to start at fContext[3].
        // context 1: empty
        // context 2: decls for xml and xmlns;
        // context 3: decls on <xs:schema>: the global ones
        String[] returnVal=null;
        if(fCurrentContext>=3){
            int bottomLocalContext=fContext[3];
            int copyCount=fNamespaceSize-bottomLocalContext;
            if(copyCount>0){
                returnVal=new String[copyCount];
                System.arraycopy(fNamespace,bottomLocalContext,returnVal,0,
                        copyCount);
            }
        }
        return returnVal;
    } // getEffectiveLocalContext():String

    // This method removes from this object all the namespaces
    // returned by getEffectiveLocalContext.
    public void makeGlobal(){
        if(fCurrentContext>=3){
            fCurrentContext=3;
            fNamespaceSize=fContext[3];
        }
    } // makeGlobal
} // class NamespaceSupport
