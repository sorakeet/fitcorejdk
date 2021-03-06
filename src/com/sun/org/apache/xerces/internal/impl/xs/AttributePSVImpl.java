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

import com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public class AttributePSVImpl implements AttributePSVI{
    protected XSAttributeDeclaration fDeclaration=null;
    protected XSTypeDefinition fTypeDecl=null;
    protected boolean fSpecified=false;
    protected String fNormalizedValue=null;
    protected Object fActualValue=null;
    protected short fActualValueType=XSConstants.UNAVAILABLE_DT;
    protected ShortList fItemValueTypes=null;
    protected XSSimpleTypeDefinition fMemberType=null;
    protected short fValidationAttempted=AttributePSVI.VALIDATION_NONE;
    protected short fValidity=AttributePSVI.VALIDITY_NOTKNOWN;
    protected String[] fErrorCodes=null;
    protected String fValidationContext=null;
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
        if(fErrorCodes==null)
            return null;
        return new StringListImpl(fErrorCodes,fErrorCodes.length);
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

    public void reset(){
        fNormalizedValue=null;
        fActualValue=null;
        fActualValueType=XSConstants.UNAVAILABLE_DT;
        fItemValueTypes=null;
        fDeclaration=null;
        fTypeDecl=null;
        fSpecified=false;
        fMemberType=null;
        fValidationAttempted=AttributePSVI.VALIDATION_NONE;
        fValidity=AttributePSVI.VALIDITY_NOTKNOWN;
        fErrorCodes=null;
        fValidationContext=null;
    }
}
