/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2004 The Apache Software Foundation.
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
 * Copyright 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.util;

import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public final class XSNamedMap4Types extends XSNamedMapImpl{
    // the type of component stored here: complex or simple type
    private final short fType;

    public XSNamedMap4Types(String namespace,SymbolHash map,short type){
        super(namespace,map);
        fType=type;
    }

    public XSNamedMap4Types(String[] namespaces,SymbolHash[] maps,int num,short type){
        super(namespaces,maps,num);
        fType=type;
    }

    public synchronized int getLength(){
        if(fLength==-1){
            // first get the number of components for all types
            int length=0;
            for(int i=0;i<fNSNum;i++){
                length+=fMaps[i].getLength();
            }
            // then copy all types to an temporary array
            int pos=0;
            XSObject[] array=new XSObject[length];
            for(int i=0;i<fNSNum;i++){
                pos+=fMaps[i].getValues(array,pos);
            }
            // then copy either simple or complex types to fArray,
            // depending on which kind is required
            fLength=0;
            fArray=new XSObject[length];
            XSTypeDefinition type;
            for(int i=0;i<length;i++){
                type=(XSTypeDefinition)array[i];
                if(type.getTypeCategory()==fType){
                    fArray[fLength++]=type;
                }
            }
        }
        return fLength;
    }

    public XSObject itemByName(String namespace,String localName){
        for(int i=0;i<fNSNum;i++){
            if(isEqual(namespace,fNamespaces[i])){
                XSTypeDefinition type=(XSTypeDefinition)fMaps[i].get(localName);
                // only return it if it matches the required type
                if(type!=null&&type.getTypeCategory()==fType){
                    return type;
                }
                return null;
            }
        }
        return null;
    }

    public synchronized XSObject item(int index){
        if(fArray==null){
            getLength();
        }
        if(index<0||index>=fLength){
            return null;
        }
        return fArray[index];
    }
} // class XSNamedMapImpl
