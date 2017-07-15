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

import org.w3c.dom.Node;

public class DeferredElementDefinitionImpl
        extends ElementDefinitionImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=6703238199538041591L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredElementDefinitionImpl(DeferredDocumentImpl ownerDocument,
                                  int nodeIndex){
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
                (DeferredDocumentImpl)this.ownerDocument;
        name=ownerDocument.getNodeName(fNodeIndex);
    } // synchronizeData()

    protected void synchronizeChildren(){
        // we don't want to generate any event for this so turn them off
        boolean orig=ownerDocument.getMutationEvents();
        ownerDocument.setMutationEvents(false);
        // attributes are now synced
        needsSyncChildren(false);
        // create attributes node map
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)this.ownerDocument;
        attributes=new NamedNodeMapImpl(ownerDocument);
        // Default attributes dangle as children of the element
        // definition "node" in the internal fast table.
        for(int nodeIndex=ownerDocument.getLastChild(fNodeIndex);
            nodeIndex!=-1;
            nodeIndex=ownerDocument.getPrevSibling(nodeIndex)){
            Node attr=ownerDocument.getNodeObject(nodeIndex);
            attributes.setNamedItem(attr);
        }
        // set mutation events flag back to its original value
        ownerDocument.setMutationEvents(orig);
    } // synchronizeChildren()
} // class DeferredElementDefinitionImpl
