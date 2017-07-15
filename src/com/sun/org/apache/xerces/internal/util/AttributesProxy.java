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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import org.xml.sax.AttributeList;
import org.xml.sax.ext.Attributes2;

public final class AttributesProxy
        implements AttributeList, Attributes2{
    //
    // Data
    //
    private XMLAttributes fAttributes;
    //
    // Constructors
    //

    public AttributesProxy(XMLAttributes attributes){
        fAttributes=attributes;
    }
    //
    // Public methods
    //

    public XMLAttributes getAttributes(){
        return fAttributes;
    }

    public void setAttributes(XMLAttributes attributes){
        fAttributes=attributes;
    } // setAttributes(XMLAttributes)

    public int getLength(){
        return fAttributes.getLength();
    }

    public String getName(int i){
        return fAttributes.getQName(i);
    }

    public String getType(int i){
        return fAttributes.getType(i);
    }

    public String getValue(int i){
        return fAttributes.getValue(i);
    }

    public String getType(String name){
        return fAttributes.getType(name);
    }

    public String getValue(String name){
        return fAttributes.getValue(name);
    }

    public String getURI(int index){
        // This hides the fact that internally we use null instead of empty string
        // SAX requires the URI to be a string or an empty string
        String uri=fAttributes.getURI(index);
        return uri!=null?uri:XMLSymbols.EMPTY_STRING;
    }

    public String getLocalName(int index){
        return fAttributes.getLocalName(index);
    }

    public String getQName(int index){
        return fAttributes.getQName(index);
    }

    public int getIndex(String uri,String localPart){
        return uri.equals(XMLSymbols.EMPTY_STRING)?
                fAttributes.getIndex(null,localPart):
                fAttributes.getIndex(uri,localPart);
    }

    public int getIndex(String qName){
        return fAttributes.getIndex(qName);
    }

    public String getType(String uri,String localName){
        return uri.equals(XMLSymbols.EMPTY_STRING)?
                fAttributes.getType(null,localName):
                fAttributes.getType(uri,localName);
    }

    public String getValue(String uri,String localName){
        return uri.equals(XMLSymbols.EMPTY_STRING)?
                fAttributes.getValue(null,localName):
                fAttributes.getValue(uri,localName);
    }

    public boolean isDeclared(int index){
        if(index<0||index>=fAttributes.getLength()){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return Boolean.TRUE.equals(
                fAttributes.getAugmentations(index).getItem(
                        Constants.ATTRIBUTE_DECLARED));
    }

    public boolean isDeclared(String qName){
        int index=getIndex(qName);
        if(index==-1){
            throw new IllegalArgumentException(qName);
        }
        return Boolean.TRUE.equals(
                fAttributes.getAugmentations(index).getItem(
                        Constants.ATTRIBUTE_DECLARED));
    }

    public boolean isDeclared(String uri,String localName){
        int index=getIndex(uri,localName);
        if(index==-1){
            throw new IllegalArgumentException(localName);
        }
        return Boolean.TRUE.equals(
                fAttributes.getAugmentations(index).getItem(
                        Constants.ATTRIBUTE_DECLARED));
    }

    public boolean isSpecified(int index){
        if(index<0||index>=fAttributes.getLength()){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return fAttributes.isSpecified(index);
    }

    public boolean isSpecified(String uri,String localName){
        int index=getIndex(uri,localName);
        if(index==-1){
            throw new IllegalArgumentException(localName);
        }
        return fAttributes.isSpecified(index);
    }

    public boolean isSpecified(String qName){
        int index=getIndex(qName);
        if(index==-1){
            throw new IllegalArgumentException(qName);
        }
        return fAttributes.isSpecified(index);
    }
}
