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
package com.sun.org.apache.xerces.internal.dom.events;

import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;

public class MutationEventImpl
        extends EventImpl
        implements MutationEvent{
    // NON-DOM CONSTANTS: Storage efficiency, avoid risk of typos.
    public static final String DOM_SUBTREE_MODIFIED="DOMSubtreeModified";
    public static final String DOM_NODE_INSERTED="DOMNodeInserted";
    public static final String DOM_NODE_REMOVED="DOMNodeRemoved";
    public static final String DOM_NODE_REMOVED_FROM_DOCUMENT="DOMNodeRemovedFromDocument";
    public static final String DOM_NODE_INSERTED_INTO_DOCUMENT="DOMNodeInsertedIntoDocument";
    public static final String DOM_ATTR_MODIFIED="DOMAttrModified";
    public static final String DOM_CHARACTER_DATA_MODIFIED="DOMCharacterDataModified";
    // REVISIT: The DOM Level 2 PR has a bug: the init method should let this
    // attribute be specified. Since it doesn't we have to give write access.
    public short attrChange;
    Node relatedNode=null;
    String prevValue=null, newValue=null, attrName=null;

    public Node getRelatedNode(){
        return relatedNode;
    }

    public String getPrevValue(){
        return prevValue;
    }

    public String getNewValue(){
        return newValue;
    }

    public String getAttrName(){
        return attrName;
    }

    public short getAttrChange(){
        return attrChange;
    }

    public void initMutationEvent(String typeArg,boolean canBubbleArg,
                                  boolean cancelableArg,Node relatedNodeArg,String prevValueArg,
                                  String newValueArg,String attrNameArg,short attrChangeArg){
        relatedNode=relatedNodeArg;
        prevValue=prevValueArg;
        newValue=newValueArg;
        attrName=attrNameArg;
        attrChange=attrChangeArg;
        super.initEvent(typeArg,canBubbleArg,cancelableArg);
    }
}
