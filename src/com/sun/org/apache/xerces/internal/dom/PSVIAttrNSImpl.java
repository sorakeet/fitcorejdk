/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * Copyright 2002-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.xs.*;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PSVIAttrNSImpl extends AttrNSImpl implements AttributePSVI{
    static final long serialVersionUID=-3241738699421018889L;
    protected XSAttributeDeclaration fDeclaration=null;
    protected XSTypeDefinition fTypeDecl=null;
    protected boolean fSpecified=true;
    protected String fNormalizedValue=null;
    protected Object fActualValue=null;
    protected short fActualValueType=XSConstants.UNAVAILABLE_DT;
    protected ShortList fItemValueTypes=null;
    protected XSSimpleTypeDefinition fMemberType=null;
    protected short fValidationAttempted=AttributePSVI.VALIDATION_NONE;
    protected short fValidity=AttributePSVI.VALIDITY_NOTKNOWN;
    protected StringList fErrorCodes=null;
    protected String fValidationContext=null;
    public PSVIAttrNSImpl(CoreDocumentImpl ownerDocument,String namespaceURI,
                          String qualifiedName,String localName){
        super(ownerDocument,namespaceURI,qualifiedName,localName);
    }
    public PSVIAttrNSImpl(CoreDocumentImpl ownerDocument,String namespaceURI,
                          String qualifiedName){
        super(ownerDocument,namespaceURI,qualifiedName);
    }
    //
    // AttributePSVI methods
    //

    // This is the only information we can provide in a pipeline.
    public String getValidationContext(){
        return fValidationContext;
    }

    public short getValidity(){
        return fValidity;
    }

    public short getValidationAttempted(){
        return fValidationAttempted;
    }

    public StringList getErrorCodes(){
        return fErrorCodes;
    }

    public String getSchemaNormalizedValue(){
        return fNormalizedValue;
    }

    public Object getActualNormalizedValue(){
        return this.fActualValue;
    }

    public short getActualNormalizedValueType(){
        return this.fActualValueType;
    }

    public ShortList getItemValueTypes(){
        return this.fItemValueTypes;
    }

    public XSTypeDefinition getTypeDefinition(){
        return fTypeDecl;
    }

    public XSSimpleTypeDefinition getMemberTypeDefinition(){
        return fMemberType;
    }

    public String getSchemaDefault(){
        return fDeclaration==null?null:fDeclaration.getConstraintValue();
    }

    public boolean getIsSchemaSpecified(){
        return fSpecified;
    }

    public XSAttributeDeclaration getAttributeDeclaration(){
        return fDeclaration;
    }

    public void setPSVI(AttributePSVI attr){
        this.fDeclaration=attr.getAttributeDeclaration();
        this.fValidationContext=attr.getValidationContext();
        this.fValidity=attr.getValidity();
        this.fValidationAttempted=attr.getValidationAttempted();
        this.fErrorCodes=attr.getErrorCodes();
        this.fNormalizedValue=attr.getSchemaNormalizedValue();
        this.fActualValue=attr.getActualNormalizedValue();
        this.fActualValueType=attr.getActualNormalizedValueType();
        this.fItemValueTypes=attr.getItemValueTypes();
        this.fTypeDecl=attr.getTypeDefinition();
        this.fMemberType=attr.getMemberTypeDefinition();
        this.fSpecified=attr.getIsSchemaSpecified();
    }
    // REVISIT: Forbid serialization of PSVI DOM until
    // we support object serialization of grammars -- mrglavas

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        throw new NotSerializableException(getClass().getName());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        throw new NotSerializableException(getClass().getName());
    }
}
