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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class ParentNode
        extends ChildNode{
    static final long serialVersionUID=2815829867152120872L;
    protected CoreDocumentImpl ownerDocument;
    protected ChildNode firstChild=null;
    // transients
    protected transient NodeListCache fNodeListCache=null;
    //
    // Constructors
    //

    protected ParentNode(CoreDocumentImpl ownerDocument){
        super(ownerDocument);
        this.ownerDocument=ownerDocument;
    }

    public ParentNode(){
    }
    //
    // NodeList methods
    //

    public Node cloneNode(boolean deep){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        ParentNode newnode=(ParentNode)super.cloneNode(deep);
        // set owner document
        newnode.ownerDocument=ownerDocument;
        // Need to break the association w/ original kids
        newnode.firstChild=null;
        // invalidate cache for children NodeList
        newnode.fNodeListCache=null;
        // Then, if deep, clone the kids too.
        if(deep){
            for(ChildNode child=firstChild;
                child!=null;
                child=child.nextSibling){
                newnode.appendChild(child.cloneNode(true));
            }
        }
        return newnode;
    } // cloneNode(boolean):Node

    protected void synchronizeChildren(){
        // By default just change the flag to avoid calling this method again
        needsSyncChildren(false);
    }

    public Document getOwnerDocument(){
        return ownerDocument;
    }

    CoreDocumentImpl ownerDocument(){
        return ownerDocument;
    }

    void setOwnerDocument(CoreDocumentImpl doc){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        for(ChildNode child=firstChild;
            child!=null;child=child.nextSibling){
            child.setOwnerDocument(doc);
        }
        /** setting the owner document of self, after it's children makes the
         data of children available to the new document. */
        super.setOwnerDocument(doc);
        ownerDocument=doc;
    }

    public boolean hasChildNodes(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return firstChild!=null;
    }

    public NodeList getChildNodes(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return this;
    } // getChildNodes():NodeList

    public Node getFirstChild(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return firstChild;
    }   // getFirstChild():Node

    public Node getLastChild(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return lastChild();
    } // getLastChild():Node

    final ChildNode lastChild(){
        // last child is stored as the previous sibling of first child
        return firstChild!=null?firstChild.previousSibling:null;
    }

    public Node insertBefore(Node newChild,Node refChild)
            throws DOMException{
        // Tail-call; optimizer should be able to do good things with.
        return internalInsertBefore(newChild,refChild,false);
    } // insertBefore(Node,Node):Node

    public Node removeChild(Node oldChild)
            throws DOMException{
        // Tail-call, should be optimizable
        return internalRemoveChild(oldChild,false);
    } // removeChild(Node) :Node

    Node internalRemoveChild(Node oldChild,boolean replace)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null));
            }
            if(oldChild!=null&&oldChild.getParentNode()!=this){
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null));
            }
        }
        ChildNode oldInternal=(ChildNode)oldChild;
        // notify document
        ownerDocument.removingNode(this,oldInternal,replace);
        // update cached length if we have any
        if(fNodeListCache!=null){
            if(fNodeListCache.fLength!=-1){
                fNodeListCache.fLength--;
            }
            if(fNodeListCache.fChildIndex!=-1){
                // if the removed node is the cached node
                // move the cache to its (soon former) previous sibling
                if(fNodeListCache.fChild==oldInternal){
                    fNodeListCache.fChildIndex--;
                    fNodeListCache.fChild=oldInternal.previousSibling();
                }else{
                    // otherwise just invalidate the cache
                    fNodeListCache.fChildIndex=-1;
                }
            }
        }
        // Patch linked list around oldChild
        // Note: lastChild == firstChild.previousSibling
        if(oldInternal==firstChild){
            // removing first child
            oldInternal.isFirstChild(false);
            firstChild=oldInternal.nextSibling;
            if(firstChild!=null){
                firstChild.isFirstChild(true);
                firstChild.previousSibling=oldInternal.previousSibling;
            }
        }else{
            ChildNode prev=oldInternal.previousSibling;
            ChildNode next=oldInternal.nextSibling;
            prev.nextSibling=next;
            if(next==null){
                // removing last child
                firstChild.previousSibling=prev;
            }else{
                // removing some other child in the middle
                next.previousSibling=prev;
            }
        }
        // Save previous sibling for normalization checking.
        ChildNode oldPreviousSibling=oldInternal.previousSibling();
        // Remove oldInternal's references to tree
        oldInternal.ownerNode=ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.nextSibling=null;
        oldInternal.previousSibling=null;
        changed();
        // notify document
        ownerDocument.removedNode(this,replace);
        checkNormalizationAfterRemove(oldPreviousSibling);
        return oldInternal;
    } // internalRemoveChild(Node,boolean):Node

    void checkNormalizationAfterRemove(ChildNode previousSibling){
        // See if removal caused this node to be unnormalized.
        // If the adjacent siblings of the removed child were both text nodes,
        // flag this node as unnormalized.
        if(previousSibling!=null&&
                previousSibling.getNodeType()==Node.TEXT_NODE){
            ChildNode next=previousSibling.nextSibling;
            if(next!=null&&next.getNodeType()==Node.TEXT_NODE){
                isNormalized(false);
            }
        }
    } // checkNormalizationAfterRemove(Node)

    public Node replaceChild(Node newChild,Node oldChild)
            throws DOMException{
        // If Mutation Events are being generated, this operation might
        // throw aggregate events twice when modifying an Attr -- once
        // on insertion and once on removal. DOM Level 2 does not specify
        // this as either desirable or undesirable, but hints that
        // aggregations should be issued only once per user request.
        // notify document
        ownerDocument.replacingNode(this);
        internalInsertBefore(newChild,oldChild,true);
        if(newChild!=oldChild){
            internalRemoveChild(oldChild,true);
        }
        // notify document
        ownerDocument.replacedNode(this);
        return oldChild;
    }

    public int getLength(){
        return nodeListGetLength();
    }

    private int nodeListGetLength(){
        if(fNodeListCache==null){
            // get rid of trivial cases
            if(firstChild==null){
                return 0;
            }
            if(firstChild==lastChild()){
                return 1;
            }
            // otherwise request a cache object
            fNodeListCache=ownerDocument.getNodeListCache(this);
        }
        if(fNodeListCache.fLength==-1){ // is the cached length invalid ?
            int l;
            ChildNode n;
            // start from the cached node if we have one
            if(fNodeListCache.fChildIndex!=-1&&
                    fNodeListCache.fChild!=null){
                l=fNodeListCache.fChildIndex;
                n=fNodeListCache.fChild;
            }else{
                n=firstChild;
                l=0;
            }
            while(n!=null){
                l++;
                n=n.nextSibling;
            }
            fNodeListCache.fLength=l;
        }
        return fNodeListCache.fLength;
    } // nodeListGetLength():int

    public Node item(int index){
        return nodeListItem(index);
    } // item(int):Node

    private Node nodeListItem(int index){
        if(fNodeListCache==null){
            // get rid of trivial case
            if(firstChild==lastChild()){
                return index==0?firstChild:null;
            }
            // otherwise request a cache object
            fNodeListCache=ownerDocument.getNodeListCache(this);
        }
        int i=fNodeListCache.fChildIndex;
        ChildNode n=fNodeListCache.fChild;
        boolean firstAccess=true;
        // short way
        if(i!=-1&&n!=null){
            firstAccess=false;
            if(i<index){
                while(i<index&&n!=null){
                    i++;
                    n=n.nextSibling;
                }
            }else if(i>index){
                while(i>index&&n!=null){
                    i--;
                    n=n.previousSibling();
                }
            }
        }else{
            // long way
            if(index<0){
                return null;
            }
            n=firstChild;
            for(i=0;i<index&&n!=null;i++){
                n=n.nextSibling;
            }
        }
        // release cache if reaching last child or first child
        if(!firstAccess&&(n==firstChild||n==lastChild())){
            fNodeListCache.fChildIndex=-1;
            fNodeListCache.fChild=null;
            ownerDocument.freeNodeListCache(fNodeListCache);
            // we can keep using the cache until it is actually reused
            // fNodeListCache will be nulled by the pool (document) if that
            // happens.
            // fNodeListCache = null;
        }else{
            // otherwise update it
            fNodeListCache.fChildIndex=i;
            fNodeListCache.fChild=n;
        }
        return n;
    } // nodeListItem(int):Node
    //
    // NodeList methods
    //

    public void normalize(){
        // No need to normalize if already normalized.
        if(isNormalized()){
            return;
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        ChildNode kid;
        for(kid=firstChild;kid!=null;kid=kid.nextSibling){
            kid.normalize();
        }
        isNormalized(true);
    }

    public String getTextContent() throws DOMException{
        Node child=getFirstChild();
        if(child!=null){
            Node next=child.getNextSibling();
            if(next==null){
                return hasTextContent(child)?((NodeImpl)child).getTextContent():"";
            }
            if(fBufferStr==null){
                fBufferStr=new StringBuffer();
            }else{
                fBufferStr.setLength(0);
            }
            getTextContent(fBufferStr);
            return fBufferStr.toString();
        }
        return "";
    }

    // internal method taking a StringBuffer in parameter
    void getTextContent(StringBuffer buf) throws DOMException{
        Node child=getFirstChild();
        while(child!=null){
            if(hasTextContent(child)){
                ((NodeImpl)child).getTextContent(buf);
            }
            child=child.getNextSibling();
        }
    }

    // internal method returning whether to take the given node's text content
    final boolean hasTextContent(Node child){
        return child.getNodeType()!=Node.COMMENT_NODE&&
                child.getNodeType()!=Node.PROCESSING_INSTRUCTION_NODE&&
                (child.getNodeType()!=Node.TEXT_NODE||
                        ((TextImpl)child).isIgnorableWhitespace()==false);
    }

    public void setTextContent(String textContent)
            throws DOMException{
        // get rid of any existing children
        Node child;
        while((child=getFirstChild())!=null){
            removeChild(child);
        }
        // create a Text node to hold the given content
        if(textContent!=null&&textContent.length()!=0){
            appendChild(ownerDocument().createTextNode(textContent));
        }
    }
    //
    // DOM2: methods, getters, setters
    //

    public boolean isEqualNode(Node arg){
        if(!super.isEqualNode(arg)){
            return false;
        }
        // there are many ways to do this test, and there isn't any way
        // better than another. Performance may vary greatly depending on
        // the implementations involved. This one should work fine for us.
        Node child1=getFirstChild();
        Node child2=arg.getFirstChild();
        while(child1!=null&&child2!=null){
            if(!((NodeImpl)child1).isEqualNode(child2)){
                return false;
            }
            child1=child1.getNextSibling();
            child2=child2.getNextSibling();
        }
        if(child1!=child2){
            return false;
        }
        return true;
    }

    public void setReadOnly(boolean readOnly,boolean deep){
        super.setReadOnly(readOnly,deep);
        if(deep){
            if(needsSyncChildren()){
                synchronizeChildren();
            }
            // Recursively set kids
            for(ChildNode mykid=firstChild;
                mykid!=null;
                mykid=mykid.nextSibling){
                if(mykid.getNodeType()!=Node.ENTITY_REFERENCE_NODE){
                    mykid.setReadOnly(readOnly,true);
                }
            }
        }
    } // setReadOnly(boolean,boolean)
    //
    // Public methods
    //

    final void lastChild(ChildNode node){
        // store lastChild as previous sibling of first child
        if(firstChild!=null){
            firstChild.previousSibling=node;
        }
    }
    //
    // Protected methods
    //

    Node internalInsertBefore(Node newChild,Node refChild,boolean replace)
            throws DOMException{
        boolean errorChecking=ownerDocument.errorChecking;
        if(newChild.getNodeType()==Node.DOCUMENT_FRAGMENT_NODE){
            // SLOW BUT SAFE: We could insert the whole subtree without
            // juggling so many next/previous pointers. (Wipe out the
            // parent's child-list, patch the parent pointers, set the
            // ends of the list.) But we know some subclasses have special-
            // case behavior they add to insertBefore(), so we don't risk it.
            // This approch also takes fewer bytecodes.
            // NOTE: If one of the children is not a legal child of this
            // node, throw HIERARCHY_REQUEST_ERR before _any_ of the children
            // have been transferred. (Alternative behaviors would be to
            // reparent up to the first failure point or reparent all those
            // which are acceptable to the target node, neither of which is
            // as robust. PR-DOM-0818 isn't entirely clear on which it
            // recommends?????
            // No need to check kids for right-document; if they weren't,
            // they wouldn't be kids of that DocFrag.
            if(errorChecking){
                for(Node kid=newChild.getFirstChild(); // Prescan
                    kid!=null;kid=kid.getNextSibling()){
                    if(!ownerDocument.isKidOK(this,kid)){
                        throw new DOMException(
                                DOMException.HIERARCHY_REQUEST_ERR,
                                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null));
                    }
                }
            }
            while(newChild.hasChildNodes()){
                insertBefore(newChild.getFirstChild(),refChild);
            }
            return newChild;
        }
        if(newChild==refChild){
            // stupid case that must be handled as a no-op triggering events...
            refChild=refChild.getNextSibling();
            removeChild(newChild);
            insertBefore(newChild,refChild);
            return newChild;
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        if(errorChecking){
            if(isReadOnly()){
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null));
            }
            if(newChild.getOwnerDocument()!=ownerDocument&&newChild!=ownerDocument){
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null));
            }
            if(!ownerDocument.isKidOK(this,newChild)){
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null));
            }
            // refChild must be a child of this node (or null)
            if(refChild!=null&&refChild.getParentNode()!=this){
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                        DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null));
            }
            // Prevent cycles in the tree
            // newChild cannot be ancestor of this Node,
            // and actually cannot be this
            if(ownerDocument.ancestorChecking){
                boolean treeSafe=true;
                for(NodeImpl a=this;treeSafe&&a!=null;a=a.parentNode()){
                    treeSafe=newChild!=a;
                }
                if(!treeSafe){
                    throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                            DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null));
                }
            }
        }
        // notify document
        ownerDocument.insertingNode(this,replace);
        // Convert to internal type, to avoid repeated casting
        ChildNode newInternal=(ChildNode)newChild;
        Node oldparent=newInternal.parentNode();
        if(oldparent!=null){
            oldparent.removeChild(newInternal);
        }
        // Convert to internal type, to avoid repeated casting
        ChildNode refInternal=(ChildNode)refChild;
        // Attach up
        newInternal.ownerNode=this;
        newInternal.isOwned(true);
        // Attach before and after
        // Note: firstChild.previousSibling == lastChild!!
        if(firstChild==null){
            // this our first and only child
            firstChild=newInternal;
            newInternal.isFirstChild(true);
            newInternal.previousSibling=newInternal;
        }else{
            if(refInternal==null){
                // this is an append
                ChildNode lastChild=firstChild.previousSibling;
                lastChild.nextSibling=newInternal;
                newInternal.previousSibling=lastChild;
                firstChild.previousSibling=newInternal;
            }else{
                // this is an insert
                if(refChild==firstChild){
                    // at the head of the list
                    firstChild.isFirstChild(false);
                    newInternal.nextSibling=firstChild;
                    newInternal.previousSibling=firstChild.previousSibling;
                    firstChild.previousSibling=newInternal;
                    firstChild=newInternal;
                    newInternal.isFirstChild(true);
                }else{
                    // somewhere in the middle
                    ChildNode prev=refInternal.previousSibling;
                    newInternal.nextSibling=refInternal;
                    prev.nextSibling=newInternal;
                    refInternal.previousSibling=newInternal;
                    newInternal.previousSibling=prev;
                }
            }
        }
        changed();
        // update cached length if we have any
        if(fNodeListCache!=null){
            if(fNodeListCache.fLength!=-1){
                fNodeListCache.fLength++;
            }
            if(fNodeListCache.fChildIndex!=-1){
                // if we happen to insert just before the cached node, update
                // the cache to the new node to match the cached index
                if(fNodeListCache.fChild==refInternal){
                    fNodeListCache.fChild=newInternal;
                }else{
                    // otherwise just invalidate the cache
                    fNodeListCache.fChildIndex=-1;
                }
            }
        }
        // notify document
        ownerDocument.insertedNode(this,newInternal,replace);
        checkNormalizationAfterInsert(newInternal);
        return newChild;
    } // internalInsertBefore(Node,Node,boolean):Node

    protected final NodeList getChildNodesUnoptimized(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return new NodeList(){
            /**
             * @see NodeList.item(int)
             */
            public Node item(int index){
                return nodeListItem(index);
            } // item(int):Node

            /**
             * @see NodeList.getLength()
             */
            public int getLength(){
                return nodeListGetLength();
            } // getLength():int
        };
    } // getChildNodesUnoptimized():NodeList

    void checkNormalizationAfterInsert(ChildNode insertedChild){
        // See if insertion caused this node to be unnormalized.
        if(insertedChild.getNodeType()==Node.TEXT_NODE){
            ChildNode prev=insertedChild.previousSibling();
            ChildNode next=insertedChild.nextSibling;
            // If an adjacent sibling of the new child is a text node,
            // flag this node as unnormalized.
            if((prev!=null&&prev.getNodeType()==Node.TEXT_NODE)||
                    (next!=null&&next.getNodeType()==Node.TEXT_NODE)){
                isNormalized(false);
            }
        }else{
            // If the new child is not normalized,
            // then this node is inherently not normalized.
            if(!insertedChild.isNormalized()){
                isNormalized(false);
            }
        }
    } // checkNormalizationAfterInsert(ChildNode)
    //
    // Serialization methods
    //

    private void writeObject(ObjectOutputStream out) throws IOException{
        // synchronize chilren
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        // write object
        out.defaultWriteObject();
    } // writeObject(ObjectOutputStream)

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException{
        // perform default deseralization
        ois.defaultReadObject();
        // hardset synchildren - so we don't try to sync - it does not make any
        // sense to try to synchildren when we just deserialize object.
        needsSyncChildren(false);
    } // readObject(ObjectInputStream)

    protected class UserDataRecord implements Serializable{
        private static final long serialVersionUID=3258126977134310455L;
        Object fData;
        UserDataHandler fHandler;

        UserDataRecord(Object data,UserDataHandler handler){
            fData=data;
            fHandler=handler;
        }
    }
} // class ParentNode
