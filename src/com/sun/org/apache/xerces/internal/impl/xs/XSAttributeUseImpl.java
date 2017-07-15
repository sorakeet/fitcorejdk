/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSAttributeUseImpl implements XSAttributeUse{
    // the referred attribute decl
    public XSAttributeDecl fAttrDecl=null;
    // use information: SchemaSymbols.USE_OPTIONAL, REQUIRED, PROHIBITED
    public short fUse=SchemaSymbols.USE_OPTIONAL;
    // value constraint type: default, fixed or !specified
    public short fConstraintType=XSConstants.VC_NONE;
    // value constraint value
    public ValidatedInfo fDefault=null;
    // optional annotation
    public XSObjectList fAnnotations=null;

    public void reset(){
        fDefault=null;
        fAttrDecl=null;
        fUse=SchemaSymbols.USE_OPTIONAL;
        fConstraintType=XSConstants.VC_NONE;
        fAnnotations=null;
    }

    public short getType(){
        return XSConstants.ATTRIBUTE_USE;
    }

    public String getName(){
        return null;
    }

    public String getNamespace(){
        return null;
    }

    public XSNamespaceItem getNamespaceItem(){
        return null;
    }

    public boolean getRequired(){
        return fUse==SchemaSymbols.USE_REQUIRED;
    }

    public XSAttributeDeclaration getAttrDeclaration(){
        return fAttrDecl;
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

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }
} // class XSAttributeUseImpl
