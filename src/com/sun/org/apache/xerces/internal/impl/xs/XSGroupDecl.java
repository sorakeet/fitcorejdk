/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * Copyright 1999-2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSGroupDecl implements XSModelGroupDefinition{
    // name of the group
    public String fName=null;
    // target namespace of the group
    public String fTargetNamespace=null;
    // model group of the group
    public XSModelGroupImpl fModelGroup=null;
    // optional annotations
    public XSObjectList fAnnotations=null;
    // The namespace schema information item corresponding to the target namespace
    // of the model group definition, if it is globally declared; or null otherwise.
    private XSNamespaceItem fNamespaceItem=null;

    public short getType(){
        return XSConstants.MODEL_GROUP_DEFINITION;
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

    public XSModelGroup getModelGroup(){
        return fModelGroup;
    }

    public XSAnnotation getAnnotation(){
        return (fAnnotations!=null)?(XSAnnotation)fAnnotations.item(0):null;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }
} // class XSGroupDecl
