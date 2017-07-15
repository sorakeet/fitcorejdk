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
package com.sun.org.apache.xerces.internal.xpointer;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

class ShortHandPointer implements XPointerPart{
    int fMatchingChildCount=0;
    // The name of the ShortHand pointer
    private String fShortHandPointer;
    // The name of the ShortHand pointer
    private boolean fIsFragmentResolved=false;
    // SymbolTable
    private SymbolTable fSymbolTable;

    //
    // Constructors
    //
    public ShortHandPointer(){
    }

    public ShortHandPointer(SymbolTable symbolTable){
        fSymbolTable=symbolTable;
    }

    public void parseXPointer(String part) throws XNIException{
        fShortHandPointer=part;
        // reset fIsFragmentResolved
        fIsFragmentResolved=false;
    }

    public boolean resolveXPointer(QName element,XMLAttributes attributes,
                                   Augmentations augs,int event) throws XNIException{
        // reset fIsFragmentResolved
        if(fMatchingChildCount==0){
            fIsFragmentResolved=false;
        }
        // On startElement or emptyElement, if no matching elements or parent
        // elements were found, check for a matching idenfitier.
        if(event==XPointerPart.EVENT_ELEMENT_START){
            if(fMatchingChildCount==0){
                fIsFragmentResolved=hasMatchingIdentifier(element,attributes,augs,
                        event);
            }
            if(fIsFragmentResolved){
                fMatchingChildCount++;
            }
        }else if(event==XPointerPart.EVENT_ELEMENT_EMPTY){
            if(fMatchingChildCount==0){
                fIsFragmentResolved=hasMatchingIdentifier(element,attributes,augs,
                        event);
            }
        }else{
            // On endElement, decrease the matching child count if the child or
            // its parent was resolved.
            if(fIsFragmentResolved){
                fMatchingChildCount--;
            }
        }
        return fIsFragmentResolved;
    }

    private boolean hasMatchingIdentifier(QName element,
                                          XMLAttributes attributes,Augmentations augs,int event)
            throws XNIException{
        String normalizedValue=null;
        // The identifiers of an element are determined by the
        // ShortHand Pointer as follows:
        if(attributes!=null){
            for(int i=0;i<attributes.getLength();i++){
                // 1. If an element information item has an attribute information item
                // among its [attributes] that is a schema-determined ID, then it is
                // identified by the value of that attribute information item's
                // [schema normalized value] property;
                normalizedValue=getSchemaDeterminedID(attributes,i);
                if(normalizedValue!=null){
                    break;
                }
                // 2. If an element information item has an element information item among
                // its [children] that is a schema-determined ID, then it is identified by
                // the value of that element information item's [schema normalized value] property;
                // ???
                normalizedValue=getChildrenSchemaDeterminedID(attributes,i);
                if(normalizedValue!=null){
                    break;
                }
                // 3. If an element information item has an attribute information item among
                // its [attributes] that is a DTD-determined ID, then it is identified by the
                // value of that attribute information item's [normalized value] property.
                // An attribute information item is a DTD-determined ID if and only if it has
                // a [type definition] property whose value is equal to ID.
                normalizedValue=getDTDDeterminedID(attributes,i);
                if(normalizedValue!=null){
                    break;
                }
                // 4. No externally determined ID's
            }
        }
        if(normalizedValue!=null
                &&normalizedValue.equals(fShortHandPointer)){
            return true;
        }
        return false;
    }

    public String getDTDDeterminedID(XMLAttributes attributes,int index)
            throws XNIException{
        if(attributes.getType(index).equals("ID")){
            return attributes.getValue(index);
        }
        return null;
    }

    public String getSchemaDeterminedID(XMLAttributes attributes,int index)
            throws XNIException{
        Augmentations augs=attributes.getAugmentations(index);
        AttributePSVI attrPSVI=(AttributePSVI)augs
                .getItem(Constants.ATTRIBUTE_PSVI);
        if(attrPSVI!=null){
            // An element or attribute information item is a schema-determined
            // ID if and only if one of the following is true:]
            // 1. It has a [member type definition] or [type definition] property
            // whose value in turn has [name] equal to ID and [target namespace]
            // equal to http://www.w3.org/2001/XMLSchema;
            // 2. It has a [base type definition] whose value has that [name] and [target namespace];
            // 3. It has a [base type definition] whose value has a [base type definition]
            // whose value has that [name] and [target namespace], and so on following
            // the [base type definition] property recursively;
            XSTypeDefinition typeDef=attrPSVI.getMemberTypeDefinition();
            if(typeDef!=null){
                typeDef=attrPSVI.getTypeDefinition();
            }
            //
            if(typeDef!=null&&((XSSimpleType)typeDef).isIDType()){
                return attrPSVI.getSchemaNormalizedValue();
            }
            // 4 & 5 NA
        }
        return null;
    }

    public String getChildrenSchemaDeterminedID(XMLAttributes attributes,
                                                int index) throws XNIException{
        return null;
    }

    public boolean isFragmentResolved(){
        return fIsFragmentResolved;
    }

    public boolean isChildFragmentResolved(){
        return fIsFragmentResolved&(fMatchingChildCount>0);
    }

    public String getSchemeName(){
        return fShortHandPointer;
    }

    public String getSchemeData(){
        return null;
    }

    public void setSchemeData(String schemeData){
        // NA
    }

    public void setSchemeName(String schemeName){
        fShortHandPointer=schemeName;
    }
}
