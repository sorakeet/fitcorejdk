/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.w3c.dom.*;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public abstract class NodeImpl
        implements Node, NodeList, EventTarget, Cloneable, Serializable{
    //
    // Constants
    //
    // TreePosition Constants.
    // Taken from DOM L3 Node interface.
    public static final short TREE_POSITION_PRECEDING=0x01;
    public static final short TREE_POSITION_FOLLOWING=0x02;
    public static final short TREE_POSITION_ANCESTOR=0x04;
    public static final short TREE_POSITION_DESCENDANT=0x08;
    public static final short TREE_POSITION_EQUIVALENT=0x10;
    public static final short TREE_POSITION_SAME_NODE=0x20;
    public static final short TREE_POSITION_DISCONNECTED=0x00;
    // DocumentPosition
    public static final short DOCUMENT_POSITION_DISCONNECTED=0x01;
    public static final short DOCUMENT_POSITION_PRECEDING=0x02;
    public static final short DOCUMENT_POSITION_FOLLOWING=0x04;
    public static final short DOCUMENT_POSITION_CONTAINS=0x08;
    public static final short DOCUMENT_POSITION_IS_CONTAINED=0x10;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC=0x20;
    // public
    public static final short ELEMENT_DEFINITION_NODE=21;
    protected final static short READONLY=0x1<<0;
    protected final static short SYNCDATA=0x1<<1;
    protected final static short SYNCCHILDREN=0x1<<2;
    protected final static short OWNED=0x1<<3;
    protected final static short FIRSTCHILD=0x1<<4;
    protected final static short SPECIFIED=0x1<<5;
    protected final static short IGNORABLEWS=0x1<<6;
    protected final static short HASSTRING=0x1<<7;
    protected final static short NORMALIZED=0x1<<8;
    protected final static short ID=0x1<<9;
    static final long serialVersionUID=-6316591992167219696L;
    //
    // Data
    //
    // links
    protected NodeImpl ownerNode; // typically the parent but not always!
    // data
    protected short flags;
    //
    // Constructors
    //

    protected NodeImpl(CoreDocumentImpl ownerDocument){
        // as long as we do not have any owner, ownerNode is our ownerDocument
        ownerNode=ownerDocument;
    } // <init>(CoreDocumentImpl)

    public NodeImpl(){
    }
    //
    // Node methods
    //

    NodeImpl parentNode(){
        return null;
    }    public abstract short getNodeType();

    ChildNode previousSibling(){
        return null;            // default behavior, overriden in ChildNode
    }    public abstract String getNodeName();

    public Node item(int index){
        return null;
    }    public String getNodeValue()
            throws DOMException{
        return null;            // overridden in some subclasses
    }

    public int getLength(){
        return 0;
    }    public void setNodeValue(String x)
            throws DOMException{
        // Default behavior is to do nothing, overridden in some subclasses
    }

    public void addEventListener(String type,EventListener listener,
                                 boolean useCapture){
        // simply forward to Document
        ownerDocument().addEventListener(this,type,listener,useCapture);
    }    public Node appendChild(Node newChild) throws DOMException{
        return insertBefore(newChild,null);
    }

    public void removeEventListener(String type,EventListener listener,
                                    boolean useCapture){
        // simply forward to Document
        ownerDocument().removeEventListener(this,type,listener,useCapture);
    }    public Node cloneNode(boolean deep){
        if(needsSyncData()){
            synchronizeData();
        }
        NodeImpl newnode;
        try{
            newnode=(NodeImpl)clone();
        }catch(CloneNotSupportedException e){
            // if we get here we have an error in our program we may as well
            // be vocal about it, so that people can take appropriate action.
            throw new RuntimeException("**Internal Error**"+e);
        }
        // Need to break the association w/ original kids
        newnode.ownerNode=ownerDocument();
        newnode.isOwned(false);
        // By default we make all clones readwrite,
        // this is overriden in readonly subclasses
        newnode.isReadOnly(false);
        ownerDocument().callUserDataHandlers(this,newnode,
                UserDataHandler.NODE_CLONED);
        return newnode;
    } // cloneNode(boolean):Node

    public boolean dispatchEvent(Event event){
        // simply forward to Document
        return ownerDocument().dispatchEvent(this,event);
    }    public Document getOwnerDocument(){
        // if we have an owner simply forward the request
        // otherwise ownerNode is our ownerDocument
        if(isOwned()){
            return ownerNode.ownerDocument();
        }else{
            return (Document)ownerNode;
        }
    }

    public short compareTreePosition(Node other){
        // Questions of clarification for this method - to be answered by the
        // DOM WG.   Current assumptions listed - LM
        //
        // 1. How do ENTITY nodes compare?
        //    Current assumption: TREE_POSITION_DISCONNECTED, as ENTITY nodes
        //    aren't really 'in the tree'
        //
        // 2. How do NOTATION nodes compare?
        //    Current assumption: TREE_POSITION_DISCONNECTED, as NOTATION nodes
        //    aren't really 'in the tree'
        //
        // 3. Are TREE_POSITION_ANCESTOR and TREE_POSITION_DESCENDANT
        //    only relevant for nodes that are "part of the document tree"?
        //     <outer>
        //         <inner  myattr="true"/>
        //     </outer>
        //    Is the element node "outer" considered an ancestor of "myattr"?
        //    Current assumption: No.
        //
        // 4. How do children of ATTRIBUTE nodes compare (with eachother, or
        //    with children of other attribute nodes with the same element)
        //    Current assumption: Children of ATTRIBUTE nodes are treated as if
        //    they they are the attribute node itself, unless the 2 nodes
        //    are both children of the same attribute.
        //
        // 5. How does an ENTITY_REFERENCE node compare with it's children?
        //    Given the DOM, it should precede its children as an ancestor.
        //    Given "document order",  does it represent the same position?
        //    Current assumption: An ENTITY_REFERENCE node is an ancestor of its
        //    children.
        //
        // 6. How do children of a DocumentFragment compare?
        //    Current assumption: If both nodes are part of the same document
        //    fragment, there are compared as if they were part of a document.
        // If the nodes are the same...
        if(this==other)
            return (TREE_POSITION_SAME_NODE|TREE_POSITION_EQUIVALENT);
        // If either node is of type ENTITY or NOTATION, compare as disconnected
        short thisType=this.getNodeType();
        short otherType=other.getNodeType();
        // If either node is of type ENTITY or NOTATION, compare as disconnected
        if(thisType==Node.ENTITY_NODE||
                thisType==Node.NOTATION_NODE||
                otherType==Node.ENTITY_NODE||
                otherType==Node.NOTATION_NODE){
            return TREE_POSITION_DISCONNECTED;
        }
        // Find the ancestor of each node, and the distance each node is from
        // its ancestor.
        // During this traversal, look for ancestor/descendent relationships
        // between the 2 nodes in question.
        // We do this now, so that we get this info correct for attribute nodes
        // and their children.
        Node node;
        Node thisAncestor=this;
        Node otherAncestor=other;
        int thisDepth=0;
        int otherDepth=0;
        for(node=this;node!=null;node=node.getParentNode()){
            thisDepth+=1;
            if(node==other)
                // The other node is an ancestor of this one.
                return (TREE_POSITION_ANCESTOR|TREE_POSITION_PRECEDING);
            thisAncestor=node;
        }
        for(node=other;node!=null;node=node.getParentNode()){
            otherDepth+=1;
            if(node==this)
                // The other node is a descendent of the reference node.
                return (TREE_POSITION_DESCENDANT|TREE_POSITION_FOLLOWING);
            otherAncestor=node;
        }
        Node thisNode=this;
        Node otherNode=other;
        int thisAncestorType=thisAncestor.getNodeType();
        int otherAncestorType=otherAncestor.getNodeType();
        // if the ancestor is an attribute, get owning element.
        // we are now interested in the owner to determine position.
        if(thisAncestorType==Node.ATTRIBUTE_NODE){
            thisNode=((AttrImpl)thisAncestor).getOwnerElement();
        }
        if(otherAncestorType==Node.ATTRIBUTE_NODE){
            otherNode=((AttrImpl)otherAncestor).getOwnerElement();
        }
        // Before proceeding, we should check if both ancestor nodes turned
        // out to be attributes for the same element
        if(thisAncestorType==Node.ATTRIBUTE_NODE&&
                otherAncestorType==Node.ATTRIBUTE_NODE&&
                thisNode==otherNode)
            return TREE_POSITION_EQUIVALENT;
        // Now, find the ancestor of the owning element, if the original
        // ancestor was an attribute
        // Note:  the following 2 loops are quite close to the ones above.
        // May want to common them up.  LM.
        if(thisAncestorType==Node.ATTRIBUTE_NODE){
            thisDepth=0;
            for(node=thisNode;node!=null;node=node.getParentNode()){
                thisDepth+=1;
                if(node==otherNode)
                // The other node is an ancestor of the owning element
                {
                    return TREE_POSITION_PRECEDING;
                }
                thisAncestor=node;
            }
        }
        // Now, find the ancestor of the owning element, if the original
        // ancestor was an attribute
        if(otherAncestorType==Node.ATTRIBUTE_NODE){
            otherDepth=0;
            for(node=otherNode;node!=null;node=node.getParentNode()){
                otherDepth+=1;
                if(node==thisNode)
                    // The other node is a descendent of the reference
                    // node's element
                    return TREE_POSITION_FOLLOWING;
                otherAncestor=node;
            }
        }
        // thisAncestor and otherAncestor must be the same at this point,
        // otherwise, we are not in the same tree or document fragment
        if(thisAncestor!=otherAncestor)
            return TREE_POSITION_DISCONNECTED;
        // Go up the parent chain of the deeper node, until we find a node
        // with the same depth as the shallower node
        if(thisDepth>otherDepth){
            for(int i=0;i<thisDepth-otherDepth;i++)
                thisNode=thisNode.getParentNode();
            // Check if the node we have reached is in fact "otherNode". This can
            // happen in the case of attributes.  In this case, otherNode
            // "precedes" this.
            if(thisNode==otherNode)
                return TREE_POSITION_PRECEDING;
        }else{
            for(int i=0;i<otherDepth-thisDepth;i++)
                otherNode=otherNode.getParentNode();
            // Check if the node we have reached is in fact "thisNode".  This can
            // happen in the case of attributes.  In this case, otherNode
            // "follows" this.
            if(otherNode==thisNode)
                return TREE_POSITION_FOLLOWING;
        }
        // We now have nodes at the same depth in the tree.  Find a common
        // ancestor.
        Node thisNodeP, otherNodeP;
        for(thisNodeP=thisNode.getParentNode(),
                    otherNodeP=otherNode.getParentNode();
            thisNodeP!=otherNodeP;){
            thisNode=thisNodeP;
            otherNode=otherNodeP;
            thisNodeP=thisNodeP.getParentNode();
            otherNodeP=otherNodeP.getParentNode();
        }
        // At this point, thisNode and otherNode are direct children of
        // the common ancestor.
        // See whether thisNode or otherNode is the leftmost
        for(Node current=thisNodeP.getFirstChild();
            current!=null;
            current=current.getNextSibling()){
            if(current==otherNode){
                return TREE_POSITION_PRECEDING;
            }else if(current==thisNode){
                return TREE_POSITION_FOLLOWING;
            }
        }
        // REVISIT:  shouldn't get here.   Should probably throw an
        // exception
        return 0;
    }    CoreDocumentImpl ownerDocument(){
        // if we have an owner simply forward the request
        // otherwise ownerNode is our ownerDocument
        if(isOwned()){
            return ownerNode.ownerDocument();
        }else{
            return (CoreDocumentImpl)ownerNode;
        }
    }

    // internal method taking a StringBuffer in parameter
    void getTextContent(StringBuffer buf) throws DOMException{
        String content=getNodeValue();
        if(content!=null){
            buf.append(content);
        }
    }    void setOwnerDocument(CoreDocumentImpl doc){
        if(needsSyncData()){
            synchronizeData();
        }
        // if we have an owner we rely on it to have it right
        // otherwise ownerNode is our ownerDocument
        if(!isOwned()){
            ownerNode=doc;
        }
    }

    Node getElementAncestor(Node currentNode){
        Node parent=currentNode.getParentNode();
        if(parent!=null){
            short type=parent.getNodeType();
            if(type==Node.ELEMENT_NODE){
                return parent;
            }
            return getElementAncestor(parent);
        }
        return null;
    }    protected int getNodeNumber(){
        int nodeNumber;
        CoreDocumentImpl cd=(CoreDocumentImpl)(this.getOwnerDocument());
        nodeNumber=cd.getNodeNumber(this);
        return nodeNumber;
    }

    String lookupNamespacePrefix(String namespaceURI,ElementImpl el){
        String namespace=this.getNamespaceURI();
        // REVISIT: if no prefix is available is it null or empty string, or
        //          could be both?
        String prefix=this.getPrefix();
        if(namespace!=null&&namespace.equals(namespaceURI)){
            if(prefix!=null){
                String foundNamespace=el.lookupNamespaceURI(prefix);
                if(foundNamespace!=null&&foundNamespace.equals(namespaceURI)){
                    return prefix;
                }
            }
        }
        if(this.hasAttributes()){
            NamedNodeMap map=this.getAttributes();
            int length=map.getLength();
            for(int i=0;i<length;i++){
                Node attr=map.item(i);
                String attrPrefix=attr.getPrefix();
                String value=attr.getNodeValue();
                namespace=attr.getNamespaceURI();
                if(namespace!=null&&namespace.equals("http://www.w3.org/2000/xmlns/")){
                    // DOM Level 2 nodes
                    if(((attr.getNodeName().equals("xmlns"))||
                            (attrPrefix!=null&&attrPrefix.equals("xmlns"))&&
                                    value.equals(namespaceURI))){
                        String localname=attr.getLocalName();
                        String foundNamespace=el.lookupNamespaceURI(localname);
                        if(foundNamespace!=null&&foundNamespace.equals(namespaceURI)){
                            return localname;
                        }
                    }
                }
            }
        }
        NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
        if(ancestor!=null){
            return ancestor.lookupNamespacePrefix(namespaceURI,el);
        }
        return null;
    }    public Node getParentNode(){
        return null;            // overriden by ChildNode
    }

    protected Map<String,ParentNode.UserDataRecord> getUserDataRecord(){
        return ownerDocument().getUserDataRecord(this);
    }

    public void setReadOnly(boolean readOnly,boolean deep){
        if(needsSyncData()){
            synchronizeData();
        }
        isReadOnly(readOnly);
    } // setReadOnly(boolean,boolean)    public Node getNextSibling(){
        return null;            // default behavior, overriden in ChildNode
    }

    final void isReadOnly(boolean value){
        flags=(short)(value?flags|READONLY:flags&~READONLY);
    }    public Node getPreviousSibling(){
        return null;            // default behavior, overriden in ChildNode
    }

    public boolean getReadOnly(){
        if(needsSyncData()){
            synchronizeData();
        }
        return isReadOnly();
    } // getReadOnly():boolean

    final boolean isReadOnly(){
        return (flags&READONLY)!=0;
    }    public NamedNodeMap getAttributes(){
        return null; // overridden in ElementImpl
    }

    public Object getUserData(){
        return ownerDocument().getUserData(this);
    }    public boolean hasAttributes(){
        return false;           // overridden in ElementImpl
    }

    public void setUserData(Object data){
        ownerDocument().setUserData(this,data);
    }    public boolean hasChildNodes(){
        return false;
    }

    protected void changed(){
        // we do not actually store this information on every node, we only
        // have a global indicator on the Document. Doing otherwise cost us too
        // much for little gain.
        ownerDocument().changed();
    }    public NodeList getChildNodes(){
        return this;
    }

    protected int changes(){
        // we do not actually store this information on every node, we only
        // have a global indicator on the Document. Doing otherwise cost us too
        // much for little gain.
        return ownerDocument().changes();
    }    public Node getFirstChild(){
        return null;
    }

    protected Node getContainer(){
        return null;
    }    public Node getLastChild(){
        return null;
    }

    final boolean needsSyncChildren(){
        return (flags&SYNCCHILDREN)!=0;
    }    public Node insertBefore(Node newChild,Node refChild)
            throws DOMException{
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR",null));
    }

    public final void needsSyncChildren(boolean value){
        flags=(short)(value?flags|SYNCCHILDREN:flags&~SYNCCHILDREN);
    }    public Node removeChild(Node oldChild)
            throws DOMException{
        throw new DOMException(DOMException.NOT_FOUND_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                        "NOT_FOUND_ERR",null));
    }

    final void isOwned(boolean value){
        flags=(short)(value?flags|OWNED:flags&~OWNED);
    }    public Node replaceChild(Node newChild,Node oldChild)
            throws DOMException{
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR",null));
    }
    //
    // NodeList methods
    //

    final boolean isFirstChild(){
        return (flags&FIRSTCHILD)!=0;
    }

    final void isFirstChild(boolean value){
        flags=(short)(value?flags|FIRSTCHILD:flags&~FIRSTCHILD);
    }
    //
    // DOM2: methods, getters, setters
    //

    final boolean isSpecified(){
        return (flags&SPECIFIED)!=0;
    }    public void normalize(){
        /** by default we do not have any children,
         ParentNode overrides this behavior */
    }

    final void isSpecified(boolean value){
        flags=(short)(value?flags|SPECIFIED:flags&~SPECIFIED);
    }    public boolean isSupported(String feature,String version){
        return ownerDocument().getImplementation().hasFeature(feature,
                version);
    }

    // inconsistent name to avoid clash with public method on TextImpl
    final boolean internalIsIgnorableWhitespace(){
        return (flags&IGNORABLEWS)!=0;
    }    public String getNamespaceURI(){
        return null;
    }

    final void isIgnorableWhitespace(boolean value){
        flags=(short)(value?flags|IGNORABLEWS:flags&~IGNORABLEWS);
    }    public String getPrefix(){
        return null;
    }

    final boolean hasStringValue(){
        return (flags&HASSTRING)!=0;
    }    public void setPrefix(String prefix)
            throws DOMException{
        throw new DOMException(DOMException.NAMESPACE_ERR,
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                        "NAMESPACE_ERR",null));
    }

    final void hasStringValue(boolean value){
        flags=(short)(value?flags|HASSTRING:flags&~HASSTRING);
    }    public String getLocalName(){
        return null;
    }
    //
    // EventTarget support
    //

    final void isNormalized(boolean value){
        // See if flag should propagate to parent.
        if(!value&&isNormalized()&&ownerNode!=null){
            ownerNode.isNormalized(false);
        }
        flags=(short)(value?flags|NORMALIZED:flags&~NORMALIZED);
    }

    final boolean isNormalized(){
        return (flags&NORMALIZED)!=0;
    }

    final boolean isIdAttribute(){
        return (flags&ID)!=0;
    }
    //
    // Public DOM Level 3 methods
    //

    final void isIdAttribute(boolean value){
        flags=(short)(value?flags|ID:flags&~ID);
    }    public String getBaseURI(){
        return null;
    }

    public String toString(){
        return "["+getNodeName()+": "+getNodeValue()+"]";
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        // synchronize data
        if(needsSyncData()){
            synchronizeData();
        }
        // write object
        out.defaultWriteObject();
    } // writeObject(ObjectOutputStream)    public short compareDocumentPosition(Node other) throws DOMException{
        // If the nodes are the same, no flags should be set
        if(this==other)
            return 0;
        // check if other is from a different implementation
        try{
            NodeImpl node=(NodeImpl)other;
        }catch(ClassCastException e){
            // other comes from a different implementation
            String msg=DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        Document thisOwnerDoc, otherOwnerDoc;
        // get the respective Document owners.
        if(this.getNodeType()==Node.DOCUMENT_NODE)
            thisOwnerDoc=(Document)this;
        else
            thisOwnerDoc=this.getOwnerDocument();
        if(other.getNodeType()==Node.DOCUMENT_NODE)
            otherOwnerDoc=(Document)other;
        else
            otherOwnerDoc=other.getOwnerDocument();
        // If from different documents, we know they are disconnected.
        // and have an implementation dependent order
        if(thisOwnerDoc!=otherOwnerDoc&&
                thisOwnerDoc!=null&&
                otherOwnerDoc!=null){
            int otherDocNum=((CoreDocumentImpl)otherOwnerDoc).getNodeNumber();
            int thisDocNum=((CoreDocumentImpl)thisOwnerDoc).getNodeNumber();
            if(otherDocNum>thisDocNum)
                return DOCUMENT_POSITION_DISCONNECTED|
                        DOCUMENT_POSITION_FOLLOWING|
                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
            else
                return DOCUMENT_POSITION_DISCONNECTED|
                        DOCUMENT_POSITION_PRECEDING|
                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }
        // Find the ancestor of each node, and the distance each node is from
        // its ancestor.
        // During this traversal, look for ancestor/descendent relationships
        // between the 2 nodes in question.
        // We do this now, so that we get this info correct for attribute nodes
        // and their children.
        Node node;
        Node thisAncestor=this;
        Node otherAncestor=other;
        int thisDepth=0;
        int otherDepth=0;
        for(node=this;node!=null;node=node.getParentNode()){
            thisDepth+=1;
            if(node==other)
                // The other node is an ancestor of this one.
                return (DOCUMENT_POSITION_CONTAINS|
                        DOCUMENT_POSITION_PRECEDING);
            thisAncestor=node;
        }
        for(node=other;node!=null;node=node.getParentNode()){
            otherDepth+=1;
            if(node==this)
                // The other node is a descendent of the reference node.
                return (DOCUMENT_POSITION_IS_CONTAINED|
                        DOCUMENT_POSITION_FOLLOWING);
            otherAncestor=node;
        }
        int thisAncestorType=thisAncestor.getNodeType();
        int otherAncestorType=otherAncestor.getNodeType();
        Node thisNode=this;
        Node otherNode=other;
        // Special casing for ENTITY, NOTATION, DOCTYPE and ATTRIBUTES
        // LM:  should rewrite this.
        switch(thisAncestorType){
            case Node.NOTATION_NODE:
            case Node.ENTITY_NODE:{
                DocumentType container=thisOwnerDoc.getDoctype();
                if(container==otherAncestor) return
                        (DOCUMENT_POSITION_CONTAINS|DOCUMENT_POSITION_PRECEDING);
                switch(otherAncestorType){
                    case Node.NOTATION_NODE:
                    case Node.ENTITY_NODE:{
                        if(thisAncestorType!=otherAncestorType)
                            // the nodes are of different types
                            return ((thisAncestorType>otherAncestorType)?
                                    DOCUMENT_POSITION_PRECEDING:DOCUMENT_POSITION_FOLLOWING);
                        else{
                            // the nodes are of the same type.  Find order.
                            if(thisAncestorType==Node.NOTATION_NODE)
                                if(((NamedNodeMapImpl)container.getNotations()).precedes(otherAncestor,thisAncestor))
                                    return (DOCUMENT_POSITION_PRECEDING|
                                            DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                                else
                                    return (DOCUMENT_POSITION_FOLLOWING|
                                            DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                            else if(((NamedNodeMapImpl)container.getEntities()).precedes(otherAncestor,thisAncestor))
                                return (DOCUMENT_POSITION_PRECEDING|
                                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                            else
                                return (DOCUMENT_POSITION_FOLLOWING|
                                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                        }
                    }
                }
                thisNode=thisAncestor=thisOwnerDoc;
                break;
            }
            case Node.DOCUMENT_TYPE_NODE:{
                if(otherNode==thisOwnerDoc)
                    return (DOCUMENT_POSITION_PRECEDING|
                            DOCUMENT_POSITION_CONTAINS);
                else if(thisOwnerDoc!=null&&thisOwnerDoc==otherOwnerDoc)
                    return (DOCUMENT_POSITION_FOLLOWING);
                break;
            }
            case Node.ATTRIBUTE_NODE:{
                thisNode=((AttrImpl)thisAncestor).getOwnerElement();
                if(otherAncestorType==Node.ATTRIBUTE_NODE){
                    otherNode=((AttrImpl)otherAncestor).getOwnerElement();
                    if(otherNode==thisNode){
                        if(((NamedNodeMapImpl)thisNode.getAttributes()).precedes(other,this))
                            return (DOCUMENT_POSITION_PRECEDING|
                                    DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                        else
                            return (DOCUMENT_POSITION_FOLLOWING|
                                    DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
                    }
                }
                // Now, find the ancestor of the element
                thisDepth=0;
                for(node=thisNode;node!=null;node=node.getParentNode()){
                    thisDepth+=1;
                    if(node==otherNode){
                        // The other node is an ancestor of the owning element
                        return (DOCUMENT_POSITION_CONTAINS|
                                DOCUMENT_POSITION_PRECEDING);
                    }
                    thisAncestor=node;
                }
            }
        }
        switch(otherAncestorType){
            case Node.NOTATION_NODE:
            case Node.ENTITY_NODE:{
                DocumentType container=thisOwnerDoc.getDoctype();
                if(container==this) return (DOCUMENT_POSITION_IS_CONTAINED|
                        DOCUMENT_POSITION_FOLLOWING);
                otherNode=otherAncestor=thisOwnerDoc;
                break;
            }
            case Node.DOCUMENT_TYPE_NODE:{
                if(thisNode==otherOwnerDoc)
                    return (DOCUMENT_POSITION_FOLLOWING|
                            DOCUMENT_POSITION_IS_CONTAINED);
                else if(otherOwnerDoc!=null&&thisOwnerDoc==otherOwnerDoc)
                    return (DOCUMENT_POSITION_PRECEDING);
                break;
            }
            case Node.ATTRIBUTE_NODE:{
                otherDepth=0;
                otherNode=((AttrImpl)otherAncestor).getOwnerElement();
                for(node=otherNode;node!=null;node=node.getParentNode()){
                    otherDepth+=1;
                    if(node==thisNode)
                        // The other node is a descendent of the reference
                        // node's element
                        return DOCUMENT_POSITION_FOLLOWING|
                                DOCUMENT_POSITION_IS_CONTAINED;
                    otherAncestor=node;
                }
            }
        }
        // thisAncestor and otherAncestor must be the same at this point,
        // otherwise, the original nodes are disconnected
        if(thisAncestor!=otherAncestor){
            int thisAncestorNum, otherAncestorNum;
            thisAncestorNum=((NodeImpl)thisAncestor).getNodeNumber();
            otherAncestorNum=((NodeImpl)otherAncestor).getNodeNumber();
            if(thisAncestorNum>otherAncestorNum)
                return DOCUMENT_POSITION_DISCONNECTED|
                        DOCUMENT_POSITION_FOLLOWING|
                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
            else
                return DOCUMENT_POSITION_DISCONNECTED|
                        DOCUMENT_POSITION_PRECEDING|
                        DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }
        // Go up the parent chain of the deeper node, until we find a node
        // with the same depth as the shallower node
        if(thisDepth>otherDepth){
            for(int i=0;i<thisDepth-otherDepth;i++)
                thisNode=thisNode.getParentNode();
            // Check if the node we have reached is in fact "otherNode". This can
            // happen in the case of attributes.  In this case, otherNode
            // "precedes" this.
            if(thisNode==otherNode){
                return DOCUMENT_POSITION_PRECEDING;
            }
        }else{
            for(int i=0;i<otherDepth-thisDepth;i++)
                otherNode=otherNode.getParentNode();
            // Check if the node we have reached is in fact "thisNode".  This can
            // happen in the case of attributes.  In this case, otherNode
            // "follows" this.
            if(otherNode==thisNode)
                return DOCUMENT_POSITION_FOLLOWING;
        }
        // We now have nodes at the same depth in the tree.  Find a common
        // ancestor.
        Node thisNodeP, otherNodeP;
        for(thisNodeP=thisNode.getParentNode(),
                    otherNodeP=otherNode.getParentNode();
            thisNodeP!=otherNodeP;){
            thisNode=thisNodeP;
            otherNode=otherNodeP;
            thisNodeP=thisNodeP.getParentNode();
            otherNodeP=otherNodeP.getParentNode();
        }
        // At this point, thisNode and otherNode are direct children of
        // the common ancestor.
        // See whether thisNode or otherNode is the leftmost
        for(Node current=thisNodeP.getFirstChild();
            current!=null;
            current=current.getNextSibling()){
            if(current==otherNode){
                return DOCUMENT_POSITION_PRECEDING;
            }else if(current==thisNode){
                return DOCUMENT_POSITION_FOLLOWING;
            }
        }
        // REVISIT:  shouldn't get here.   Should probably throw an
        // exception
        return 0;
    }

    public String getTextContent() throws DOMException{
        return getNodeValue();  // overriden in some subclasses
    }



    public void setTextContent(String textContent)
            throws DOMException{
        setNodeValue(textContent);
    }

    public boolean isSameNode(Node other){
        // we do not use any wrapper so the answer is obvious
        return this==other;
    }

    public boolean isDefaultNamespace(String namespaceURI){
        // REVISIT: remove casts when DOM L3 becomes REC.
        short type=this.getNodeType();
        switch(type){
            case Node.ELEMENT_NODE:{
                String namespace=this.getNamespaceURI();
                String prefix=this.getPrefix();
                // REVISIT: is it possible that prefix is empty string?
                if(prefix==null||prefix.length()==0){
                    if(namespaceURI==null){
                        return (namespace==namespaceURI);
                    }
                    return namespaceURI.equals(namespace);
                }
                if(this.hasAttributes()){
                    ElementImpl elem=(ElementImpl)this;
                    NodeImpl attr=(NodeImpl)elem.getAttributeNodeNS("http://www.w3.org/2000/xmlns/","xmlns");
                    if(attr!=null){
                        String value=attr.getNodeValue();
                        if(namespaceURI==null){
                            return (namespace==value);
                        }
                        return namespaceURI.equals(value);
                    }
                }
                NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
                if(ancestor!=null){
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
            case Node.DOCUMENT_NODE:{
                return ((NodeImpl)((Document)this).getDocumentElement()).isDefaultNamespace(namespaceURI);
            }
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return false;
            case Node.ATTRIBUTE_NODE:{
                if(this.ownerNode.getNodeType()==Node.ELEMENT_NODE){
                    return ownerNode.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
            default:{
                NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
                if(ancestor!=null){
                    return ancestor.isDefaultNamespace(namespaceURI);
                }
                return false;
            }
        }
    }

    public String lookupPrefix(String namespaceURI){
        // REVISIT: When Namespaces 1.1 comes out this may not be true
        // Prefix can't be bound to null namespace
        if(namespaceURI==null){
            return null;
        }
        short type=this.getNodeType();
        switch(type){
            case Node.ELEMENT_NODE:{
                String namespace=this.getNamespaceURI(); // to flip out children
                return lookupNamespacePrefix(namespaceURI,(ElementImpl)this);
            }
            case Node.DOCUMENT_NODE:{
                return ((NodeImpl)((Document)this).getDocumentElement()).lookupPrefix(namespaceURI);
            }
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;
            case Node.ATTRIBUTE_NODE:{
                if(this.ownerNode.getNodeType()==Node.ELEMENT_NODE){
                    return ownerNode.lookupPrefix(namespaceURI);
                }
                return null;
            }
            default:{
                NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
                if(ancestor!=null){
                    return ancestor.lookupPrefix(namespaceURI);
                }
                return null;
            }
        }
    }

    public String lookupNamespaceURI(String specifiedPrefix){
        short type=this.getNodeType();
        switch(type){
            case Node.ELEMENT_NODE:{
                String namespace=this.getNamespaceURI();
                String prefix=this.getPrefix();
                if(namespace!=null){
                    // REVISIT: is it possible that prefix is empty string?
                    if(specifiedPrefix==null&&prefix==specifiedPrefix){
                        // looking for default namespace
                        return namespace;
                    }else if(prefix!=null&&prefix.equals(specifiedPrefix)){
                        // non default namespace
                        return namespace;
                    }
                }
                if(this.hasAttributes()){
                    NamedNodeMap map=this.getAttributes();
                    int length=map.getLength();
                    for(int i=0;i<length;i++){
                        Node attr=map.item(i);
                        String attrPrefix=attr.getPrefix();
                        String value=attr.getNodeValue();
                        namespace=attr.getNamespaceURI();
                        if(namespace!=null&&namespace.equals("http://www.w3.org/2000/xmlns/")){
                            // at this point we are dealing with DOM Level 2 nodes only
                            if(specifiedPrefix==null&&
                                    attr.getNodeName().equals("xmlns")){
                                // default namespace
                                return value;
                            }else if(attrPrefix!=null&&
                                    attrPrefix.equals("xmlns")&&
                                    attr.getLocalName().equals(specifiedPrefix)){
                                // non default namespace
                                return value;
                            }
                        }
                    }
                }
                NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
                if(ancestor!=null){
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
            case Node.DOCUMENT_NODE:{
                return ((NodeImpl)((Document)this).getDocumentElement()).lookupNamespaceURI(specifiedPrefix);
            }
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;
            case Node.ATTRIBUTE_NODE:{
                if(this.ownerNode.getNodeType()==Node.ELEMENT_NODE){
                    return ownerNode.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
            default:{
                NodeImpl ancestor=(NodeImpl)getElementAncestor(this);
                if(ancestor!=null){
                    return ancestor.lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        }
    }





    public boolean isEqualNode(Node arg){
        if(arg==this){
            return true;
        }
        if(arg.getNodeType()!=getNodeType()){
            return false;
        }
        // in theory nodeName can't be null but better be careful
        // who knows what other implementations may be doing?...
        if(getNodeName()==null){
            if(arg.getNodeName()!=null){
                return false;
            }
        }else if(!getNodeName().equals(arg.getNodeName())){
            return false;
        }
        if(getLocalName()==null){
            if(arg.getLocalName()!=null){
                return false;
            }
        }else if(!getLocalName().equals(arg.getLocalName())){
            return false;
        }
        if(getNamespaceURI()==null){
            if(arg.getNamespaceURI()!=null){
                return false;
            }
        }else if(!getNamespaceURI().equals(arg.getNamespaceURI())){
            return false;
        }
        if(getPrefix()==null){
            if(arg.getPrefix()!=null){
                return false;
            }
        }else if(!getPrefix().equals(arg.getPrefix())){
            return false;
        }
        if(getNodeValue()==null){
            if(arg.getNodeValue()!=null){
                return false;
            }
        }else if(!getNodeValue().equals(arg.getNodeValue())){
            return false;
        }
        return true;
    }

    public Object getFeature(String feature,String version){
        // we don't have any alternate node, either this node does the job
        // or we don't have anything that does
        return isSupported(feature,version)?this:null;
    }

    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler){
        return ownerDocument().setUserData(this,key,data,handler);
    }

    public Object getUserData(String key){
        return ownerDocument().getUserData(this,key);
    }


    //
    // Public methods
    //








    //
    // Protected methods
    //





    protected void synchronizeData(){
        // By default just change the flag to avoid calling this method again
        needsSyncData(false);
    }







    final boolean needsSyncData(){
        return (flags&SYNCDATA)!=0;
    }

    final void needsSyncData(boolean value){
        flags=(short)(value?flags|SYNCDATA:flags&~SYNCDATA);
    }





    final boolean isOwned(){
        return (flags&OWNED)!=0;
    }


























    //
    // Object methods
    //


    //
    // Serialization methods
    //


} // class NodeImpl
