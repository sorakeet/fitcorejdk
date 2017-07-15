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
 * DeferredElementNSImpl.java at the same time.
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
 * DeferredElementNSImpl.java at the same time.
 */
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.NamedNodeMap;

public class DeferredElementImpl
        extends ElementImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=-7670981133940934842L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredElementImpl(DeferredDocumentImpl ownerDoc,int nodeIndex){
        super(ownerDoc,null);
        fNodeIndex=nodeIndex;
        needsSyncChildren(true);
    } // <init>(DocumentImpl,int)
    //
    // DeferredNode methods
    //

    public final int getNodeIndex(){
        return fNodeIndex;
    }
    //
    // Protected methods
    //

    protected final void synchronizeData(){
        // no need to sync in the future
        needsSyncData(false);
        // fluff data
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)this.ownerDocument;
        // we don't want to generate any event for this so turn them off
        boolean orig=ownerDocument.mutationEvents;
        ownerDocument.mutationEvents=false;
        name=ownerDocument.getNodeName(fNodeIndex);
        // attributes
        setupDefaultAttributes();
        int index=ownerDocument.getNodeExtra(fNodeIndex);
        if(index!=-1){
            NamedNodeMap attrs=getAttributes();
            do{
                NodeImpl attr=(NodeImpl)ownerDocument.getNodeObject(index);
                attrs.setNamedItem(attr);
                index=ownerDocument.getPrevSibling(index);
            }while(index!=-1);
        }
        // set mutation events flag back to its original value
        ownerDocument.mutationEvents=orig;
    } // synchronizeData()

    protected final void synchronizeChildren(){
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)ownerDocument();
        ownerDocument.synchronizeChildren(this,fNodeIndex);
    } // synchronizeChildren()
} // class DeferredElementImpl
