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
import org.w3c.dom.NodeList;

import java.util.Vector;

public class DeepNodeListImpl
        implements NodeList{
    //
    // Data
    //
    protected NodeImpl rootNode; // Where the search started
    protected String tagName;   // Or "*" to mean all-tags-acceptable
    protected int changes=0;
    protected Vector nodes;
    protected String nsName;
    protected boolean enableNS=false;
    //
    // Constructors
    //

    public DeepNodeListImpl(NodeImpl rootNode,
                            String nsName,String tagName){
        this(rootNode,tagName);
        this.nsName=(nsName!=null&&!nsName.equals(""))?nsName:null;
        enableNS=true;
    }

    public DeepNodeListImpl(NodeImpl rootNode,String tagName){
        this.rootNode=rootNode;
        this.tagName=tagName;
        nodes=new Vector();
    }
    //
    // NodeList methods
    //

    public int getLength(){
        // Preload all matching elements. (Stops when we run out of subtree!)
        item(Integer.MAX_VALUE);
        return nodes.size();
    }

    public Node item(int index){
        Node thisNode;
        // Tree changed. Do it all from scratch!
        if(rootNode.changes()!=changes){
            nodes=new Vector();
            changes=rootNode.changes();
        }
        // In the cache
        if(index<nodes.size())
            return (Node)nodes.elementAt(index);
            // Not yet seen
        else{
            // Pick up where we left off (Which may be the beginning)
            if(nodes.size()==0)
                thisNode=rootNode;
            else
                thisNode=(NodeImpl)(nodes.lastElement());
            // Add nodes up to the one we're looking for
            while(thisNode!=null&&index>=nodes.size()){
                thisNode=nextMatchingElementAfter(thisNode);
                if(thisNode!=null)
                    nodes.addElement(thisNode);
            }
            // Either what we want, or null (not avail.)
            return thisNode;
        }
    } // item(int):Node
    //
    // Protected methods (might be overridden by an extending DOM)
    //

    protected Node nextMatchingElementAfter(Node current){
        Node next;
        while(current!=null){
            // Look down to first child.
            if(current.hasChildNodes()){
                current=(current.getFirstChild());
            }
            // Look right to sibling (but not from root!)
            else if(current!=rootNode&&null!=(next=current.getNextSibling())){
                current=next;
            }
            // Look up and right (but not past root!)
            else{
                next=null;
                for(;current!=rootNode; // Stop when we return to starting point
                    current=current.getParentNode()){
                    next=current.getNextSibling();
                    if(next!=null)
                        break;
                }
                current=next;
            }
            // Have we found an Element with the right tagName?
            // ("*" matches anything.)
            if(current!=rootNode
                    &&current!=null
                    &&current.getNodeType()==Node.ELEMENT_NODE){
                if(!enableNS){
                    if(tagName.equals("*")||
                            ((ElementImpl)current).getTagName().equals(tagName)){
                        return current;
                    }
                }else{
                    // DOM2: Namespace logic.
                    if(tagName.equals("*")){
                        if(nsName!=null&&nsName.equals("*")){
                            return current;
                        }else{
                            ElementImpl el=(ElementImpl)current;
                            if((nsName==null
                                    &&el.getNamespaceURI()==null)
                                    ||(nsName!=null
                                    &&nsName.equals(el.getNamespaceURI()))){
                                return current;
                            }
                        }
                    }else{
                        ElementImpl el=(ElementImpl)current;
                        if(el.getLocalName()!=null
                                &&el.getLocalName().equals(tagName)){
                            if(nsName!=null&&nsName.equals("*")){
                                return current;
                            }else{
                                if((nsName==null
                                        &&el.getNamespaceURI()==null)
                                        ||(nsName!=null&&
                                        nsName.equals(el.getNamespaceURI()))){
                                    return current;
                                }
                            }
                        }
                    }
                }
            }
            // Otherwise continue walking the tree
        }
        // Fell out of tree-walk; no more instances found
        return null;
    } // nextMatchingElementAfter(int):Node
} // class DeepNodeListImpl
