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
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class DocumentFragmentImpl
        extends ParentNode
        implements DocumentFragment{
    //
    // Constants
    //
    static final long serialVersionUID=-7596449967279236746L;
    //
    // Constructors
    //

    public DocumentFragmentImpl(CoreDocumentImpl ownerDoc){
        super(ownerDoc);
    }

    public DocumentFragmentImpl(){
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return Node.DOCUMENT_FRAGMENT_NODE;
    }

    public String getNodeName(){
        return "#document-fragment";
    }

    public void normalize(){
        // No need to normalize if already normalized.
        if(isNormalized()){
            return;
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        ChildNode kid, next;
        for(kid=firstChild;kid!=null;kid=next){
            next=kid.nextSibling;
            // If kid is a text node, we need to check for one of two
            // conditions:
            //   1) There is an adjacent text node
            //   2) There is no adjacent text node, but kid is
            //      an empty text node.
            if(kid.getNodeType()==Node.TEXT_NODE){
                // If an adjacent text node, merge it with kid
                if(next!=null&&next.getNodeType()==Node.TEXT_NODE){
                    ((Text)kid).appendData(next.getNodeValue());
                    removeChild(next);
                    next=kid; // Don't advance; there might be another.
                }else{
                    // If kid is empty, remove it
                    if(kid.getNodeValue()==null||kid.getNodeValue().length()==0){
                        removeChild(kid);
                    }
                }
            }
            kid.normalize();
        }
        isNormalized(true);
    }
} // class DocumentFragmentImpl
