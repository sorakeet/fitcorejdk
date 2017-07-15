/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * $Id$
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/** Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 */
/**
 * $Id$
 */
package com.sun.org.apache.xml.internal.security.signature.reference;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;

public class ReferenceSubTreeData implements ReferenceNodeSetData{
    private boolean excludeComments;
    private Node root;

    public ReferenceSubTreeData(Node root,boolean excludeComments){
        this.root=root;
        this.excludeComments=excludeComments;
    }

    public Iterator<Node> iterator(){
        return new DelayedNodeIterator(root,excludeComments);
    }

    public Node getRoot(){
        return root;
    }

    public boolean excludeComments(){
        return excludeComments;
    }

    static class DelayedNodeIterator implements Iterator<Node>{
        private Node root;
        private List<Node> nodeSet;
        private ListIterator<Node> li;
        private boolean withComments;

        DelayedNodeIterator(Node root,boolean excludeComments){
            this.root=root;
            this.withComments=!excludeComments;
        }

        public boolean hasNext(){
            if(nodeSet==null){
                nodeSet=dereferenceSameDocumentURI(root);
                li=nodeSet.listIterator();
            }
            return li.hasNext();
        }

        public Node next(){
            if(nodeSet==null){
                nodeSet=dereferenceSameDocumentURI(root);
                li=nodeSet.listIterator();
            }
            if(li.hasNext()){
                return li.next();
            }else{
                throw new NoSuchElementException();
            }
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }

        private List<Node> dereferenceSameDocumentURI(Node node){
            List<Node> nodeSet=new ArrayList<Node>();
            if(node!=null){
                nodeSetMinusCommentNodes(node,nodeSet,null);
            }
            return nodeSet;
        }

        @SuppressWarnings("fallthrough")
        private void nodeSetMinusCommentNodes(Node node,List<Node> nodeSet,
                                              Node prevSibling){
            switch(node.getNodeType()){
                case Node.ELEMENT_NODE:
                    nodeSet.add(node);
                    NamedNodeMap attrs=node.getAttributes();
                    if(attrs!=null){
                        for(int i=0, len=attrs.getLength();i<len;i++){
                            nodeSet.add(attrs.item(i));
                        }
                    }
                    Node pSibling=null;
                    for(Node child=node.getFirstChild();child!=null;
                        child=child.getNextSibling()){
                        nodeSetMinusCommentNodes(child,nodeSet,pSibling);
                        pSibling=child;
                    }
                    break;
                case Node.DOCUMENT_NODE:
                    pSibling=null;
                    for(Node child=node.getFirstChild();child!=null;
                        child=child.getNextSibling()){
                        nodeSetMinusCommentNodes(child,nodeSet,pSibling);
                        pSibling=child;
                    }
                    break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    // emulate XPath which only returns the first node in
                    // contiguous text/cdata nodes
                    if(prevSibling!=null&&
                            (prevSibling.getNodeType()==Node.TEXT_NODE||
                                    prevSibling.getNodeType()==Node.CDATA_SECTION_NODE)){
                        return;
                    }
                    nodeSet.add(node);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    nodeSet.add(node);
                    break;
                case Node.COMMENT_NODE:
                    if(withComments){
                        nodeSet.add(node);
                    }
            }
        }
    }
}
