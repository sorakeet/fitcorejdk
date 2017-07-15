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
 * DeferredElementImpl.java at the same time.
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
 * DeferredElementImpl.java at the same time.
 *
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import org.w3c.dom.NamedNodeMap;

public class DeferredElementNSImpl
        extends ElementNSImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=-5001885145370927385L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredElementNSImpl(DeferredDocumentImpl ownerDoc,int nodeIndex){
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
        // extract local part from QName
        int index=name.indexOf(':');
        if(index<0){
            localName=name;
        }else{
            localName=name.substring(index+1);
        }
        namespaceURI=ownerDocument.getNodeURI(fNodeIndex);
        type=(XSTypeDefinition)ownerDocument.getTypeInfo(fNodeIndex);
        // attributes
        setupDefaultAttributes();
        int attrIndex=ownerDocument.getNodeExtra(fNodeIndex);
        if(attrIndex!=-1){
            NamedNodeMap attrs=getAttributes();
            boolean seenSchemaDefault=false;
            do{
                AttrImpl attr=(AttrImpl)ownerDocument.getNodeObject(attrIndex);
                // Take special care of schema defaulted attributes. Calling the
                // non-namespace aware setAttributeNode() method could overwrite
                // another attribute with the same local name.
                if(!attr.getSpecified()&&(seenSchemaDefault||
                        (attr.getNamespaceURI()!=null&&
                                attr.getNamespaceURI()!=NamespaceContext.XMLNS_URI&&
                                attr.getName().indexOf(':')<0))){
                    seenSchemaDefault=true;
                    attrs.setNamedItemNS(attr);
                }else{
                    attrs.setNamedItem(attr);
                }
                attrIndex=ownerDocument.getPrevSibling(attrIndex);
            }while(attrIndex!=-1);
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
