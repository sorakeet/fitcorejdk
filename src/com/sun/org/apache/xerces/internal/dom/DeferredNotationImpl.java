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
package com.sun.org.apache.xerces.internal.dom;

public class DeferredNotationImpl
        extends NotationImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=5705337172887990848L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredNotationImpl(DeferredDocumentImpl ownerDocument,int nodeIndex){
        super(ownerDocument,null);
        fNodeIndex=nodeIndex;
        needsSyncData(true);
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
        // no need to synchronize again
        needsSyncData(false);
        // name
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)this.ownerDocument();
        name=ownerDocument.getNodeName(fNodeIndex);
        ownerDocument.getNodeType(fNodeIndex);
        // public and system ids
        publicId=ownerDocument.getNodeValue(fNodeIndex);
        systemId=ownerDocument.getNodeURI(fNodeIndex);
        int extraDataIndex=ownerDocument.getNodeExtra(fNodeIndex);
        ownerDocument.getNodeType(extraDataIndex);
        baseURI=ownerDocument.getNodeName(extraDataIndex);
    } // synchronizeData()
} // class DeferredNotationImpl
