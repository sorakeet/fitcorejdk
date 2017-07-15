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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

public class TreeWalkerImpl implements TreeWalker{
    int fWhatToShow=NodeFilter.SHOW_ALL;
    NodeFilter fNodeFilter;
    Node fCurrentNode;
    Node fRoot;
    //
    // Data
    //
    private boolean fEntityReferenceExpansion=false;
    //
    // Implementation Note: No state is kept except the data above
    // (fWhatToShow, fNodeFilter, fCurrentNode, fRoot) such that
    // setters could be created for these data values and the
    // implementation will still work.
    //
    // Constructor
    //

    public TreeWalkerImpl(Node root,
                          int whatToShow,
                          NodeFilter nodeFilter,
                          boolean entityReferenceExpansion){
        fCurrentNode=root;
        fRoot=root;
        fWhatToShow=whatToShow;
        fNodeFilter=nodeFilter;
        fEntityReferenceExpansion=entityReferenceExpansion;
    }

    public Node getRoot(){
        return fRoot;
    }

    public int getWhatToShow(){
        return fWhatToShow;
    }

    public NodeFilter getFilter(){
        return fNodeFilter;
    }

    public boolean getExpandEntityReferences(){
        return fEntityReferenceExpansion;
    }

    public Node getCurrentNode(){
        return fCurrentNode;
    }

    public void setCurrentNode(Node node){
        if(node==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        fCurrentNode=node;
    }

    public Node parentNode(){
        if(fCurrentNode==null) return null;
        Node node=getParentNode(fCurrentNode);
        if(node!=null){
            fCurrentNode=node;
        }
        return node;
    }

    public Node firstChild(){
        if(fCurrentNode==null) return null;
        Node node=getFirstChild(fCurrentNode);
        if(node!=null){
            fCurrentNode=node;
        }
        return node;
    }

    public Node lastChild(){
        if(fCurrentNode==null) return null;
        Node node=getLastChild(fCurrentNode);
        if(node!=null){
            fCurrentNode=node;
        }
        return node;
    }

    public Node previousSibling(){
        if(fCurrentNode==null) return null;
        Node node=getPreviousSibling(fCurrentNode);
        if(node!=null){
            fCurrentNode=node;
        }
        return node;
    }

    public Node nextSibling(){
        if(fCurrentNode==null) return null;
        Node node=getNextSibling(fCurrentNode);
        if(node!=null){
            fCurrentNode=node;
        }
        return node;
    }

    public Node previousNode(){
        Node result;
        if(fCurrentNode==null) return null;
        // get sibling
        result=getPreviousSibling(fCurrentNode);
        if(result==null){
            result=getParentNode(fCurrentNode);
            if(result!=null){
                fCurrentNode=result;
                return fCurrentNode;
            }
            return null;
        }
        // get the lastChild of result.
        Node lastChild=getLastChild(result);
        Node prev=lastChild;
        while(lastChild!=null){
            prev=lastChild;
            lastChild=getLastChild(prev);
        }
        lastChild=prev;
        // if there is a lastChild which passes filters return it.
        if(lastChild!=null){
            fCurrentNode=lastChild;
            return fCurrentNode;
        }
        // otherwise return the previous sibling.
        if(result!=null){
            fCurrentNode=result;
            return fCurrentNode;
        }
        // otherwise return null.
        return null;
    }

    public Node nextNode(){
        if(fCurrentNode==null) return null;
        Node result=getFirstChild(fCurrentNode);
        if(result!=null){
            fCurrentNode=result;
            return result;
        }
        result=getNextSibling(fCurrentNode);
        if(result!=null){
            fCurrentNode=result;
            return result;
        }
        // return parent's 1st sibling.
        Node parent=getParentNode(fCurrentNode);
        while(parent!=null){
            result=getNextSibling(parent);
            if(result!=null){
                fCurrentNode=result;
                return result;
            }else{
                parent=getParentNode(parent);
            }
        }
        // end , return null
        return null;
    }

    public void setWhatShow(int whatToShow){
        fWhatToShow=whatToShow;
    }

    Node getParentNode(Node node){
        if(node==null||node==fRoot) return null;
        Node newNode=node.getParentNode();
        if(newNode==null) return null;
        int accept=acceptNode(newNode);
        if(accept==NodeFilter.FILTER_ACCEPT)
            return newNode;
        else
        //if (accept == NodeFilter.SKIP_NODE) // and REJECT too.
        {
            return getParentNode(newNode);
        }
    }

    Node getNextSibling(Node node){
        return getNextSibling(node,fRoot);
    }

    Node getNextSibling(Node node,Node root){
        if(node==null||node==root) return null;
        Node newNode=node.getNextSibling();
        if(newNode==null){
            newNode=node.getParentNode();
            if(newNode==null||newNode==root) return null;
            int parentAccept=acceptNode(newNode);
            if(parentAccept==NodeFilter.FILTER_SKIP){
                return getNextSibling(newNode,root);
            }
            return null;
        }
        int accept=acceptNode(newNode);
        if(accept==NodeFilter.FILTER_ACCEPT)
            return newNode;
        else if(accept==NodeFilter.FILTER_SKIP){
            Node fChild=getFirstChild(newNode);
            if(fChild==null){
                return getNextSibling(newNode,root);
            }
            return fChild;
        }else
        //if (accept == NodeFilter.REJECT_NODE)
        {
            return getNextSibling(newNode,root);
        }
    } // getNextSibling(Node node) {

    Node getPreviousSibling(Node node){
        return getPreviousSibling(node,fRoot);
    }

    Node getPreviousSibling(Node node,Node root){
        if(node==null||node==root) return null;
        Node newNode=node.getPreviousSibling();
        if(newNode==null){
            newNode=node.getParentNode();
            if(newNode==null||newNode==root) return null;
            int parentAccept=acceptNode(newNode);
            if(parentAccept==NodeFilter.FILTER_SKIP){
                return getPreviousSibling(newNode,root);
            }
            return null;
        }
        int accept=acceptNode(newNode);
        if(accept==NodeFilter.FILTER_ACCEPT)
            return newNode;
        else if(accept==NodeFilter.FILTER_SKIP){
            Node fChild=getLastChild(newNode);
            if(fChild==null){
                return getPreviousSibling(newNode,root);
            }
            return fChild;
        }else
        //if (accept == NodeFilter.REJECT_NODE)
        {
            return getPreviousSibling(newNode,root);
        }
    } // getPreviousSibling(Node node) {

    Node getFirstChild(Node node){
        if(node==null) return null;
        if(!fEntityReferenceExpansion
                &&node.getNodeType()==Node.ENTITY_REFERENCE_NODE)
            return null;
        Node newNode=node.getFirstChild();
        if(newNode==null) return null;
        int accept=acceptNode(newNode);
        if(accept==NodeFilter.FILTER_ACCEPT)
            return newNode;
        else if(accept==NodeFilter.FILTER_SKIP
                &&newNode.hasChildNodes()){
            Node fChild=getFirstChild(newNode);
            if(fChild==null){
                return getNextSibling(newNode,node);
            }
            return fChild;
        }else
        //if (accept == NodeFilter.REJECT_NODE)
        {
            return getNextSibling(newNode,node);
        }
    }

    Node getLastChild(Node node){
        if(node==null) return null;
        if(!fEntityReferenceExpansion
                &&node.getNodeType()==Node.ENTITY_REFERENCE_NODE)
            return null;
        Node newNode=node.getLastChild();
        if(newNode==null) return null;
        int accept=acceptNode(newNode);
        if(accept==NodeFilter.FILTER_ACCEPT)
            return newNode;
        else if(accept==NodeFilter.FILTER_SKIP
                &&newNode.hasChildNodes()){
            Node lChild=getLastChild(newNode);
            if(lChild==null){
                return getPreviousSibling(newNode,node);
            }
            return lChild;
        }else
        //if (accept == NodeFilter.REJECT_NODE)
        {
            return getPreviousSibling(newNode,node);
        }
    }

    short acceptNode(Node node){
        /***
         7.1.2.4. Filters and whatToShow flags

         Iterator and TreeWalker apply whatToShow flags before applying Filters. If a node is rejected by the
         active whatToShow flags, a Filter will not be called to evaluate that node. When a node is rejected by
         the active whatToShow flags, children of that node will still be considered, and Filters may be called to
         evaluate them.
         ***/
        if(fNodeFilter==null){
            if((fWhatToShow&(1<<node.getNodeType()-1))!=0){
                return NodeFilter.FILTER_ACCEPT;
            }else{
                return NodeFilter.FILTER_SKIP;
            }
        }else{
            if((fWhatToShow&(1<<node.getNodeType()-1))!=0){
                return fNodeFilter.acceptNode(node);
            }else{
                // What to show has failed. See above excerpt from spec.
                // Equivalent to FILTER_SKIP.
                return NodeFilter.FILTER_SKIP;
            }
        }
    }
}
