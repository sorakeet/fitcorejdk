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
import com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSElementDecl implements XSElementDeclaration{
    // scopes
    public final static short SCOPE_ABSENT=0;
    public final static short SCOPE_GLOBAL=1;
    public final static short SCOPE_LOCAL=2;
    // identity constraints
    static final int INITIAL_SIZE=2;
    private static final short CONSTRAINT_MASK=3;
    private static final short NILLABLE=4;
    private static final short ABSTRACT=8;
    // name of the element
    public String fName=null;
    // target namespace of the element
    public String fTargetNamespace=null;
    // type of the element
    public XSTypeDefinition fType=null;
    public QName fUnresolvedTypeName=null;
    public short fScope=XSConstants.SCOPE_ABSENT;
    // block set (disallowed substitutions) of the element
    public short fBlock=XSConstants.DERIVATION_NONE;
    // final set (substitution group exclusions) of the element
    public short fFinal=XSConstants.DERIVATION_NONE;
    // optional annotation
    public XSObjectList fAnnotations=null;
    // value constraint value
    public ValidatedInfo fDefault=null;
    // the substitution group affiliation of the element
    public XSElementDecl fSubGroup=null;
    // misc flag of the element: nillable/abstract/fixed
    short fMiscFlags=0;
    // enclosing complex type, when the scope is local
    XSComplexTypeDecl fEnclosingCT=null;
    int fIDCPos=0;
    IdentityConstraint[] fIDConstraints=new IdentityConstraint[INITIAL_SIZE];
    // The namespace schema information item corresponding to the target namespace
    // of the element declaration, if it is globally declared; or null otherwise.
    private XSNamespaceItem fNamespaceItem=null;
    private String fDescription=null;

    public void setIsNillable(){
        fMiscFlags|=NILLABLE;
    }

    public void setIsAbstract(){
        fMiscFlags|=ABSTRACT;
    }

    public void setIsGlobal(){
        fScope=SCOPE_GLOBAL;
    }

    public void setIsLocal(XSComplexTypeDecl enclosingCT){
        fScope=SCOPE_LOCAL;
        fEnclosingCT=enclosingCT;
    }

    public void addIDConstraint(IdentityConstraint idc){
        if(fIDCPos==fIDConstraints.length){
            fIDConstraints=resize(fIDConstraints,fIDCPos*2);
        }
        fIDConstraints[fIDCPos++]=idc;
    }

    static final IdentityConstraint[] resize(IdentityConstraint[] oldArray,int newSize){
        IdentityConstraint[] newArray=new IdentityConstraint[newSize];
        System.arraycopy(oldArray,0,newArray,0,Math.min(oldArray.length,newSize));
        return newArray;
    }

    public IdentityConstraint[] getIDConstraints(){
        if(fIDCPos==0){
            return null;
        }
        if(fIDCPos<fIDConstraints.length){
            fIDConstraints=resize(fIDConstraints,fIDCPos);
        }
        return fIDConstraints;
    }

    public int hashCode(){
        int code=fName.hashCode();
        if(fTargetNamespace!=null)
            code=(code<<16)+fTargetNamespace.hashCode();
        return code;
    }

    public boolean equals(Object o){
        return o==this;
    }

    public String toString(){
        if(fDescription==null){
            if(fTargetNamespace!=null){
                StringBuffer buffer=new StringBuffer(
                        fTargetNamespace.length()+
                                ((fName!=null)?fName.length():4)+3);
                buffer.append('"');
                buffer.append(fTargetNamespace);
                buffer.append('"');
                buffer.append(':');
                buffer.append(fName);
                fDescription=buffer.toString();
            }else{
                fDescription=fName;
            }
        }
        return fDescription;
    }

    public void reset(){
        fScope=XSConstants.SCOPE_ABSENT;
        fName=null;
        fTargetNamespace=null;
        fType=null;
        fUnresolvedTypeName=null;
        fMiscFlags=0;
        fBlock=XSConstants.DERIVATION_NONE;
        fFinal=XSConstants.DERIVATION_NONE;
        fDefault=null;
        fAnnotations=null;
        fSubGroup=null;
        // reset identity constraints
        for(int i=0;i<fIDCPos;i++){
            fIDConstraints[i]=null;
        }
        fIDCPos=0;
    }

    public short getType(){
        return XSConstants.ELEMENT_DECLARATION;
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

    public XSTypeDefinition getTypeDefinition(){
        return fType;
    }

    public short getScope(){
        return fScope;
    }

    public XSComplexTypeDefinition getEnclosingCTDefinition(){
        return fEnclosingCT;
    }

    public short getConstraintType(){
        return (short)(fMiscFlags&CONSTRAINT_MASK);
    }

    // methods to get/set misc flag
    public void setConstraintType(short constraintType){
        // first clear the bits
        fMiscFlags^=(fMiscFlags&CONSTRAINT_MASK);
        // then set the proper one
        fMiscFlags|=(constraintType&CONSTRAINT_MASK);
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

    public boolean getNillable(){
        return ((fMiscFlags&NILLABLE)!=0);
    }

    public XSNamedMap getIdentityConstraints(){
        return new XSNamedMapImpl(fIDConstraints,fIDCPos);
    }

    public XSElementDeclaration getSubstitutionGroupAffiliation(){
        return fSubGroup;
    }

    public boolean isSubstitutionGroupExclusion(short exclusion){
        return (fFinal&exclusion)!=0;
    }

    public short getSubstitutionGroupExclusions(){
        return fFinal;
    }

    public boolean isDisallowedSubstitution(short disallowed){
        return (fBlock&disallowed)!=0;
    }

    public short getDisallowedSubstitutions(){
        return fBlock;
    }

    public boolean getAbstract(){
        return ((fMiscFlags&ABSTRACT)!=0);
    }

    public XSAnnotation getAnnotation(){
        return (fAnnotations!=null)?(XSAnnotation)fAnnotations.item(0):null;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }
} // class XSElementDecl
