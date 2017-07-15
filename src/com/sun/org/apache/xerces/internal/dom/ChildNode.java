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
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.Node;

public abstract class ChildNode
        extends NodeImpl{
    //
    // Constants
    //
    static final long serialVersionUID=-6112455738802414002L;
    //
    // Data
    //
    protected ChildNode previousSibling;
    protected ChildNode nextSibling;
    transient StringBuffer fBufferStr=null;
    //
    // Constructors
    //

    protected ChildNode(CoreDocumentImpl ownerDocument){
        super(ownerDocument);
    } // <init>(CoreDocumentImpl)

    public ChildNode(){
    }
    //
    // Node methods
    //

    public Node cloneNode(boolean deep){
        ChildNode newnode=(ChildNode)super.cloneNode(deep);
        // Need to break the association w/ original kids
        newnode.previousSibling=null;
        newnode.nextSibling=null;
        newnode.isFirstChild(false);
        return newnode;
    } // cloneNode(boolean):Node

    public Node getParentNode(){
        // if we have an owner, ownerNode is our parent, otherwise it's
        // our ownerDocument and we don't have a parent
        return isOwned()?ownerNode:null;
    }

    final NodeImpl parentNode(){
        // if we have an owner, ownerNode is our parent, otherwise it's
        // our ownerDocument and we don't have a parent
        return isOwned()?ownerNode:null;
    }

    public Node getNextSibling(){
        return nextSibling;
    }

    public Node getPreviousSibling(){
        // if we are the firstChild, previousSibling actually refers to our
        // parent's lastChild, but we hide that
        return isFirstChild()?null:previousSibling;
    }

    final ChildNode previousSibling(){
        // if we are the firstChild, previousSibling actually refers to our
        // parent's lastChild, but we hide that
        return isFirstChild()?null:previousSibling;
    }
} // class ChildNode
