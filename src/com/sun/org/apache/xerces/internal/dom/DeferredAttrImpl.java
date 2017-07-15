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
 * <p>
 * WARNING: because java doesn't support multi-inheritance some code is
 * duplicated. If you're changing this file you probably want to change
 * DeferredAttrNSImpl.java at the same time.
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
/**
 * WARNING: because java doesn't support multi-inheritance some code is
 * duplicated. If you're changing this file you probably want to change
 * DeferredAttrNSImpl.java at the same time.
 */
package com.sun.org.apache.xerces.internal.dom;

public final class DeferredAttrImpl
        extends AttrImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=6903232312469148636L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredAttrImpl(DeferredDocumentImpl ownerDocument,int nodeIndex){
        super(ownerDocument,null);
        fNodeIndex=nodeIndex;
        needsSyncData(true);
        needsSyncChildren(true);
    } // <init>(DeferredDocumentImpl,int)
    //
    // DeferredNode methods
    //

    public int getNodeIndex(){
        return fNodeIndex;
    }
    //
    // Protected methods
    //

    protected void synchronizeData(){
        // no need to sync in the future
        needsSyncData(false);
        // fluff data
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)ownerDocument();
        name=ownerDocument.getNodeName(fNodeIndex);
        int extra=ownerDocument.getNodeExtra(fNodeIndex);
        isSpecified((extra&SPECIFIED)!=0);
        isIdAttribute((extra&ID)!=0);
        int extraNode=ownerDocument.getLastChild(fNodeIndex);
        type=ownerDocument.getTypeInfo(extraNode);
    } // synchronizeData()

    protected void synchronizeChildren(){
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)ownerDocument();
        ownerDocument.synchronizeChildren(this,fNodeIndex);
    } // synchronizeChildren()
} // class DeferredAttrImpl
