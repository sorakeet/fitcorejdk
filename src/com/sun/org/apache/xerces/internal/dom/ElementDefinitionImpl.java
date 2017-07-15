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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ElementDefinitionImpl
        extends ParentNode{
    //
    // Constants
    //
    static final long serialVersionUID=-8373890672670022714L;
    //
    // Data
    //
    protected String name;
    protected NamedNodeMapImpl attributes;
    //
    // Constructors
    //

    public ElementDefinitionImpl(CoreDocumentImpl ownerDocument,String name){
        super(ownerDocument);
        this.name=name;
        attributes=new NamedNodeMapImpl(ownerDocument);
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return NodeImpl.ELEMENT_DEFINITION_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    public NamedNodeMap getAttributes(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return attributes;
    } // getAttributes():NamedNodeMap

    public Node cloneNode(boolean deep){
        ElementDefinitionImpl newnode=
                (ElementDefinitionImpl)super.cloneNode(deep);
        // NamedNodeMap must be explicitly replicated to avoid sharing
        newnode.attributes=attributes.cloneMap(newnode);
        return newnode;
    } // cloneNode(boolean):Node
} // class ElementDefinitionImpl
