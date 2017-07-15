/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * Copyright 2001-2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.models.CMBuilder;
import com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;
import org.w3c.dom.TypeInfo;

public class XSComplexTypeDecl implements XSComplexTypeDefinition, TypeInfo{
    // DOM Level 3 TypeInfo Derivation Method constants
    static final int DERIVATION_ANY=0;
    static final int DERIVATION_RESTRICTION=1;
    static final int DERIVATION_EXTENSION=2;
    static final int DERIVATION_UNION=4;
    static final int DERIVATION_LIST=8;
    // flags for the misc flag
    private static final short CT_IS_ABSTRACT=1;
    private static final short CT_HAS_TYPE_ID=2;
    private static final short CT_IS_ANONYMOUS=4;
    // name of the complexType
    String fName=null;
    // target namespace of the complexType
    String fTargetNamespace=null;
    // base type of the complexType
    XSTypeDefinition fBaseType=null;
    // derivation method of the complexType
    short fDerivedBy=XSConstants.DERIVATION_RESTRICTION;
    // final set of the complexType
    short fFinal=XSConstants.DERIVATION_NONE;
    // block set (prohibited substitution) of the complexType
    short fBlock=XSConstants.DERIVATION_NONE;
    // flags: whether is abstract; whether contains ID type;
    //        whether it's an anonymous tpye
    short fMiscFlags=0;
    // the attribute group that holds the attribute uses and attribute wildcard
    XSAttributeGroupDecl fAttrGrp=null;
    // the content type of the complexType
    short fContentType=CONTENTTYPE_EMPTY;
    // if the content type is simple, then the corresponding simpleType
    XSSimpleType fXSSimpleType=null;
    // if the content type is element or mixed, the particle
    XSParticleDecl fParticle=null;
    // if there is a particle, the content model corresponding to that particle
    volatile XSCMValidator fCMValidator=null;
    // the content model that's sufficient for computing UPA
    XSCMValidator fUPACMValidator=null;
    // list of annotations affiliated with this type
    XSObjectListImpl fAnnotations=null;
    // The namespace schema information item corresponding to the target namespace
    // of the complex type definition, if it is globally declared; or null otherwise.
    private XSNamespaceItem fNamespaceItem=null;

    public XSComplexTypeDecl(){
        // do-nothing constructor for now.
    }

    public void setValues(String name,String targetNamespace,
                          XSTypeDefinition baseType,short derivedBy,short schemaFinal,
                          short block,short contentType,
                          boolean isAbstract,XSAttributeGroupDecl attrGrp,
                          XSSimpleType simpleType,XSParticleDecl particle,
                          XSObjectListImpl annotations){
        fTargetNamespace=targetNamespace;
        fBaseType=baseType;
        fDerivedBy=derivedBy;
        fFinal=schemaFinal;
        fBlock=block;
        fContentType=contentType;
        if(isAbstract)
            fMiscFlags|=CT_IS_ABSTRACT;
        fAttrGrp=attrGrp;
        fXSSimpleType=simpleType;
        fParticle=particle;
        fAnnotations=annotations;
    }

    public short getTypeCategory(){
        return COMPLEX_TYPE;
    }

    public XSTypeDefinition getBaseType(){
        return fBaseType;
    }

    public boolean isFinal(short derivation){
        return (fFinal&derivation)!=0;
    }

    public short getFinal(){
        return fFinal;
    }

    public boolean getAnonymous(){
        return ((fMiscFlags&CT_IS_ANONYMOUS)!=0);
    }
    // methods to get/set misc flag

    public boolean derivedFromType(XSTypeDefinition ancestor,short derivationMethod){
        // ancestor is null, retur false
        if(ancestor==null)
            return false;
        // ancestor is anyType, return true
        if(ancestor==SchemaGrammar.fAnyType)
            return true;
        // recursively get base, and compare it with ancestor
        XSTypeDefinition type=this;
        while(type!=ancestor&&                     // compare with ancestor
                type!=SchemaGrammar.fAnySimpleType&&  // reached anySimpleType
                type!=SchemaGrammar.fAnyType){        // reached anyType
            type=type.getBaseType();
        }
        return type==ancestor;
    }

    public boolean derivedFrom(String ancestorNS,String ancestorName,short derivationMethod){
        // ancestor is null, retur false
        if(ancestorName==null)
            return false;
        // ancestor is anyType, return true
        if(ancestorNS!=null&&
                ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)&&
                ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)){
            return true;
        }
        // recursively get base, and compare it with ancestor
        XSTypeDefinition type=this;
        while(!(ancestorName.equals(type.getName())&&
                ((ancestorNS==null&&type.getNamespace()==null)||
                        (ancestorNS!=null&&ancestorNS.equals(type.getNamespace()))))&&   // compare with ancestor
                type!=SchemaGrammar.fAnySimpleType&&  // reached anySimpleType
                type!=SchemaGrammar.fAnyType){        // reached anyType
            type=(XSTypeDefinition)type.getBaseType();
        }
        return type!=SchemaGrammar.fAnySimpleType&&
                type!=SchemaGrammar.fAnyType;
    }

    public short getFinalSet(){
        return fFinal;
    }

    public String getTargetNamespace(){
        return fTargetNamespace;
    }

    public void setIsAbstractType(){
        fMiscFlags|=CT_IS_ABSTRACT;
    }
    // some utility methods:

    public void setContainsTypeID(){
        fMiscFlags|=CT_HAS_TYPE_ID;
    }

    public void setIsAnonymous(){
        fMiscFlags|=CT_IS_ANONYMOUS;
    }

    public XSCMValidator getContentModel(CMBuilder cmBuilder){
        // for complex type with empty or simple content,
        // there is no content model validator
        if(fContentType==XSComplexTypeDecl.CONTENTTYPE_SIMPLE||
                fContentType==XSComplexTypeDecl.CONTENTTYPE_EMPTY){
            return null;
        }
        if(fCMValidator==null)
            synchronized(this){
                if(fCMValidator==null){
                    fCMValidator=cmBuilder.getContentModel(this);
                }
            }
        return fCMValidator;
    }

    // return the attribute group for this complex type
    public XSAttributeGroupDecl getAttrGrp(){
        return fAttrGrp;
    }

    public String toString(){
        StringBuilder str=new StringBuilder(192);
        appendTypeInfo(str);
        return str.toString();
    }

    void appendTypeInfo(StringBuilder str){
        String contentType[]={"EMPTY","SIMPLE","ELEMENT","MIXED"};
        String derivedBy[]={"EMPTY","EXTENSION","RESTRICTION"};
        str.append("Complex type name='").append(fTargetNamespace).append(',').append(getTypeName()).append("', ");
        if(fBaseType!=null){
            str.append(" base type name='").append(fBaseType.getName()).append("', ");
        }
        str.append(" content type='").append(contentType[fContentType]).append("', ");
        str.append(" isAbstract='").append(getAbstract()).append("', ");
        str.append(" hasTypeId='").append(containsTypeID()).append("', ");
        str.append(" final='").append(fFinal).append("', ");
        str.append(" block='").append(fBlock).append("', ");
        if(fParticle!=null){
            str.append(" particle='").append(fParticle.toString()).append("', ");
        }
        str.append(" derivedBy='").append(derivedBy[fDerivedBy]).append("'. ");
    }

    public String getTypeName(){
        return fName;
    }

    public String getTypeNamespace(){
        return getNamespace();
    }

    public boolean isDerivedFrom(String typeNamespaceArg,String typeNameArg,int derivationMethod){
        return isDOMDerivedFrom(typeNamespaceArg,typeNameArg,derivationMethod);
    }

    public boolean containsTypeID(){
        return ((fMiscFlags&CT_HAS_TYPE_ID)!=0);
    }

    public boolean isDOMDerivedFrom(String ancestorNS,String ancestorName,
                                    int derivationMethod){
        // ancestor is null, retur false
        if(ancestorName==null)
            return false;
        // ancestor is anyType, return true
        if(ancestorNS!=null
                &&ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)
                &&(derivationMethod==DERIVATION_RESTRICTION
                &&derivationMethod==DERIVATION_EXTENSION)){
            return true;
        }
        // restriction
        if((derivationMethod&DERIVATION_RESTRICTION)!=0){
            if(isDerivedByRestriction(ancestorNS,ancestorName,
                    derivationMethod,this)){
                return true;
            }
        }
        // extension
        if((derivationMethod&DERIVATION_EXTENSION)!=0){
            if(isDerivedByExtension(ancestorNS,ancestorName,
                    derivationMethod,this)){
                return true;
            }
        }
        // list or union
        if((((derivationMethod&DERIVATION_LIST)!=0)||((derivationMethod&DERIVATION_UNION)!=0))
                &&((derivationMethod&DERIVATION_RESTRICTION)==0)
                &&((derivationMethod&DERIVATION_EXTENSION)==0)){
            if(ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                    &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)){
                ancestorName=SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
            }
            if(!(fName.equals(SchemaSymbols.ATTVAL_ANYTYPE)
                    &&fTargetNamespace.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA))){
                if(fBaseType!=null&&fBaseType instanceof XSSimpleTypeDecl){
                    return ((XSSimpleTypeDecl)fBaseType).isDOMDerivedFrom(ancestorNS,
                            ancestorName,derivationMethod);
                }else if(fBaseType!=null
                        &&fBaseType instanceof XSComplexTypeDecl){
                    return ((XSComplexTypeDecl)fBaseType).isDOMDerivedFrom(
                            ancestorNS,ancestorName,derivationMethod);
                }
            }
        }
        // If the value of the parameter is 0 i.e. no bit (corresponding to
        // restriction, list, extension or union) is set to 1 for the
        // derivationMethod parameter.
        if(((derivationMethod&DERIVATION_EXTENSION)==0)
                &&(((derivationMethod&DERIVATION_RESTRICTION)==0)
                &&((derivationMethod&DERIVATION_LIST)==0)
                &&((derivationMethod&DERIVATION_UNION)==0))){
            return isDerivedByAny(ancestorNS,ancestorName,derivationMethod,this);
        }
        return false;
    }

    private boolean isDerivedByAny(String ancestorNS,String ancestorName,
                                   int derivationMethod,XSTypeDefinition type){
        XSTypeDefinition oldType=null;
        boolean derivedFrom=false;
        while(type!=null&&type!=oldType){
            // If the ancestor type is reached or is the same as this type.
            if((ancestorName.equals(type.getName()))
                    &&((ancestorNS==null&&type.getNamespace()==null)
                    ||(ancestorNS!=null&&ancestorNS.equals(type.getNamespace())))){
                derivedFrom=true;
                break;
            }
            // Check if this type is derived from the base by restriction or
            // extension
            if(isDerivedByRestriction(ancestorNS,ancestorName,
                    derivationMethod,type)){
                return true;
            }else if(!isDerivedByExtension(ancestorNS,ancestorName,
                    derivationMethod,type)){
                return true;
            }
            oldType=type;
            type=type.getBaseType();
        }
        return derivedFrom;
    }

    private boolean isDerivedByRestriction(String ancestorNS,
                                           String ancestorName,int derivationMethod,XSTypeDefinition type){
        XSTypeDefinition oldType=null;
        while(type!=null&&type!=oldType){
            // ancestor is anySimpleType, return false
            if(ancestorNS!=null
                    &&ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                    &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYSIMPLETYPE)){
                return false;
            }
            // if the name and namespace of this type is the same as the
            // ancestor return true
            if((ancestorName.equals(type.getName()))
                    &&(ancestorNS!=null&&ancestorNS.equals(type.getNamespace()))
                    ||((type.getNamespace()==null&&ancestorNS==null))){
                return true;
            }
            // If the base type is a complexType with simpleContent
            if(type instanceof XSSimpleTypeDecl){
                if(ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                        &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)){
                    ancestorName=SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
                }
                return ((XSSimpleTypeDecl)type).isDOMDerivedFrom(ancestorNS,
                        ancestorName,derivationMethod);
            }else{
                // If the base type is a complex type
                // Every derivation step till the base type should be
                // restriction. If not return false
                if(((XSComplexTypeDecl)type).getDerivationMethod()!=XSConstants.DERIVATION_RESTRICTION){
                    return false;
                }
            }
            oldType=type;
            type=type.getBaseType();
        }
        return false;
    }

    private boolean isDerivedByExtension(String ancestorNS,
                                         String ancestorName,int derivationMethod,XSTypeDefinition type){
        boolean extension=false;
        XSTypeDefinition oldType=null;
        while(type!=null&&type!=oldType){
            // If ancestor is anySimpleType return false.
            if(ancestorNS!=null
                    &&ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                    &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYSIMPLETYPE)
                    &&SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(type.getNamespace())
                    &&SchemaSymbols.ATTVAL_ANYTYPE.equals(type.getName())){
                break;
            }
            if((ancestorName.equals(type.getName()))
                    &&((ancestorNS==null&&type.getNamespace()==null)
                    ||(ancestorNS!=null&&ancestorNS.equals(type.getNamespace())))){
                // returns true if atleast one derivation step was extension
                return extension;
            }
            // If the base type is a complexType with simpleContent
            if(type instanceof XSSimpleTypeDecl){
                if(ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)
                        &&ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)){
                    ancestorName=SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
                }
                // derivationMethod extension will always return false for a
                // simpleType,
                // we treat it like a restriction
                if((derivationMethod&DERIVATION_EXTENSION)!=0){
                    return extension
                            &((XSSimpleTypeDecl)type).isDOMDerivedFrom(
                            ancestorNS,ancestorName,
                            (derivationMethod&DERIVATION_RESTRICTION));
                }else{
                    return extension
                            &((XSSimpleTypeDecl)type).isDOMDerivedFrom(
                            ancestorNS,ancestorName,derivationMethod);
                }
            }else{
                // If the base type is a complex type
                // At least one derivation step upto the ancestor type should be
                // extension.
                if(((XSComplexTypeDecl)type).getDerivationMethod()==XSConstants.DERIVATION_EXTENSION){
                    extension=extension|true;
                }
            }
            oldType=type;
            type=type.getBaseType();
        }
        return false;
    }

    public void reset(){
        fName=null;
        fTargetNamespace=null;
        fBaseType=null;
        fDerivedBy=XSConstants.DERIVATION_RESTRICTION;
        fFinal=XSConstants.DERIVATION_NONE;
        fBlock=XSConstants.DERIVATION_NONE;
        fMiscFlags=0;
        // reset attribute group
        fAttrGrp.reset();
        fContentType=CONTENTTYPE_EMPTY;
        fXSSimpleType=null;
        fParticle=null;
        fCMValidator=null;
        fUPACMValidator=null;
        if(fAnnotations!=null){
            // help out the garbage collector
            fAnnotations.clearXSObjectList();
        }
        fAnnotations=null;
    }

    public short getType(){
        return XSConstants.TYPE_DEFINITION;
    }

    public String getName(){
        return getAnonymous()?null:fName;
    }

    public void setName(String name){
        fName=name;
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

    public short getDerivationMethod(){
        return fDerivedBy;
    }

    public boolean getAbstract(){
        return ((fMiscFlags&CT_IS_ABSTRACT)!=0);
    }

    public XSObjectList getAttributeUses(){
        return fAttrGrp.getAttributeUses();
    }

    public XSWildcard getAttributeWildcard(){
        return fAttrGrp.getAttributeWildcard();
    }

    public short getContentType(){
        return fContentType;
    }

    public XSSimpleTypeDefinition getSimpleType(){
        return fXSSimpleType;
    }

    public XSParticle getParticle(){
        return fParticle;
    }

    public boolean isProhibitedSubstitution(short prohibited){
        return (fBlock&prohibited)!=0;
    }

    public short getProhibitedSubstitutions(){
        return fBlock;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }

    public XSAttributeUse getAttributeUse(String namespace,String name){
        return fAttrGrp.getAttributeUse(namespace,name);
    }
} // class XSComplexTypeDecl
