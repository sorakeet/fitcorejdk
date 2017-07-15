/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSAttributeDecl implements XSAttributeDeclaration{
    // scopes
    public final static short SCOPE_ABSENT=0;
    public final static short SCOPE_GLOBAL=1;
    public final static short SCOPE_LOCAL=2;
    public QName fUnresolvedTypeName=null;
    // the name of the attribute
    String fName=null;
    // the target namespace of the attribute
    String fTargetNamespace=null;
    // the simple type of the attribute
    XSSimpleType fType=null;
    // value constraint type: default, fixed or !specified
    short fConstraintType=XSConstants.VC_NONE;
    // scope
    short fScope=XSConstants.SCOPE_ABSENT;
    // enclosing complex type, when the scope is local
    XSComplexTypeDecl fEnclosingCT=null;
    // optional annotations
    XSObjectList fAnnotations=null;
    // value constraint value
    ValidatedInfo fDefault=null;
    // The namespace schema information item corresponding to the target namespace
    // of the attribute declaration, if it is globally declared; or null otherwise.
    private XSNamespaceItem fNamespaceItem=null;

    public void setValues(String name,String targetNamespace,
                          XSSimpleType simpleType,short constraintType,short scope,
                          ValidatedInfo valInfo,XSComplexTypeDecl enclosingCT,
                          XSObjectList annotations){
        fName=name;
        fTargetNamespace=targetNamespace;
        fType=simpleType;
        fConstraintType=constraintType;
        fScope=scope;
        fDefault=valInfo;
        fEnclosingCT=enclosingCT;
        fAnnotations=annotations;
    }

    public void reset(){
        fName=null;
        fTargetNamespace=null;
        fType=null;
        fUnresolvedTypeName=null;
        fConstraintType=XSConstants.VC_NONE;
        fScope=XSConstants.SCOPE_ABSENT;
        fDefault=null;
        fAnnotations=null;
    }

    public short getType(){
        return XSConstants.ATTRIBUTE_DECLARATION;
    }

    public String getName(){
        return fName;
    }

    public String getNamespace(){
        return fTargetNamespace;
    }

    public XSNamespaceItem getNamespaceItem(){
        return fNamespaceItem;
    }

    void setNamespaceItem(XSNamespaceItem namespaceItem){
        fNamespaceItem=namespaceItem;
    }

    public XSSimpleTypeDefinition getTypeDefinition(){
        return fType;
    }

    public short getScope(){
        return fScope;
    }

    public XSComplexTypeDefinition getEnclosingCTDefinition(){
        return fEnclosingCT;
    }

    public short getConstraintType(){
        return fConstraintType;
    }

    public String getConstraintValue(){
        // REVISIT: SCAPI: what's the proper representation
        return getConstraintType()==XSConstants.VC_NONE?
                null:
                fDefault.stringValue();
    }

    public Object getActualVC(){
        return getConstraintType()==XSConstants.VC_NONE?
                null:
                fDefault.actualValue;
    }

    public short getActualVCType(){
        return getConstraintType()==XSConstants.VC_NONE?
                XSConstants.UNAVAILABLE_DT:
                fDefault.actualValueType;
    }

    public ShortList getItemValueTypes(){
        return getConstraintType()==XSConstants.VC_NONE?
                null:
                fDefault.itemValueTypes;
    }

    public XSAnnotation getAnnotation(){
        return (fAnnotations!=null)?(XSAnnotation)fAnnotations.item(0):null;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }

    public ValidatedInfo getValInfo(){
        return fDefault;
    }
} // class XSAttributeDecl
