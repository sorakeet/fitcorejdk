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
package com.sun.org.apache.xerces.internal.impl.dv.xs;

import com.sun.org.apache.xerces.internal.impl.dv.*;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSSimpleTypeDelegate
        implements XSSimpleType{
    protected final XSSimpleType type;

    public XSSimpleTypeDelegate(XSSimpleType type){
        if(type==null){
            throw new NullPointerException();
        }
        this.type=type;
    }

    public XSSimpleType getWrappedXSSimpleType(){
        return type;
    }

    public short getVariety(){
        return type.getVariety();
    }

    public XSSimpleTypeDefinition getPrimitiveType(){
        return type.getPrimitiveType();
    }

    public short getBuiltInKind(){
        return type.getBuiltInKind();
    }

    public XSSimpleTypeDefinition getItemType(){
        return type.getItemType();
    }

    public XSObjectList getMemberTypes(){
        return type.getMemberTypes();
    }

    public short getDefinedFacets(){
        return type.getDefinedFacets();
    }

    public boolean isDefinedFacet(short facetName){
        return type.isDefinedFacet(facetName);
    }

    public short getFixedFacets(){
        return type.getFixedFacets();
    }

    public boolean isFixedFacet(short facetName){
        return type.isFixedFacet(facetName);
    }

    public String getLexicalFacetValue(short facetName){
        return type.getLexicalFacetValue(facetName);
    }

    public StringList getLexicalEnumeration(){
        return type.getLexicalEnumeration();
    }

    public StringList getLexicalPattern(){
        return type.getLexicalPattern();
    }

    public short getOrdered(){
        return type.getOrdered();
    }

    public boolean getFinite(){
        return type.getFinite();
    }

    public boolean getBounded(){
        return type.getBounded();
    }

    public boolean getNumeric(){
        return type.getNumeric();
    }

    public XSObjectList getFacets(){
        return type.getFacets();
    }

    public XSObjectList getMultiValueFacets(){
        return type.getMultiValueFacets();
    }

    public XSObjectList getAnnotations(){
        return type.getAnnotations();
    }

    public short getTypeCategory(){
        return type.getTypeCategory();
    }

    public XSTypeDefinition getBaseType(){
        return type.getBaseType();
    }

    public boolean isFinal(short restriction){
        return type.isFinal(restriction);
    }

    public short getFinal(){
        return type.getFinal();
    }

    public boolean getAnonymous(){
        return type.getAnonymous();
    }

    public boolean derivedFromType(XSTypeDefinition ancestorType,short derivationMethod){
        return type.derivedFromType(ancestorType,derivationMethod);
    }

    public boolean derivedFrom(String namespace,String name,short derivationMethod){
        return type.derivedFrom(namespace,name,derivationMethod);
    }

    public short getType(){
        return type.getType();
    }

    public String getName(){
        return type.getName();
    }

    public String getNamespace(){
        return type.getNamespace();
    }

    public XSNamespaceItem getNamespaceItem(){
        return type.getNamespaceItem();
    }

    public short getPrimitiveKind(){
        return type.getPrimitiveKind();
    }

    public Object validate(String content,ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException{
        return type.validate(content,context,validatedInfo);
    }

    public Object validate(Object content,ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException{
        return type.validate(content,context,validatedInfo);
    }

    public void validate(ValidationContext context,ValidatedInfo validatedInfo)
            throws InvalidDatatypeValueException{
        type.validate(context,validatedInfo);
    }

    public void applyFacets(XSFacets facets,short presentFacet,short fixedFacet,ValidationContext context)
            throws InvalidDatatypeFacetException{
        type.applyFacets(facets,presentFacet,fixedFacet,context);
    }

    public boolean isEqual(Object value1,Object value2){
        return type.isEqual(value1,value2);
    }

    public boolean isIDType(){
        return type.isIDType();
    }

    public short getWhitespace() throws DatatypeException{
        return type.getWhitespace();
    }

    public String toString(){
        return type.toString();
    }
}
