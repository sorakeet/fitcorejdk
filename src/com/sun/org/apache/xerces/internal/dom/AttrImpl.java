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

import org.w3c.dom.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AttrImpl
        extends NodeImpl
        implements Attr, TypeInfo{
    //
    // Constants
    //
    static final long serialVersionUID=7277707688218972102L;
    static final String DTD_URI="http://www.w3.org/TR/REC-xml";
    //
    // Data
    //
    protected Object value=null;
    protected String name;
    protected TextImpl textNode=null;
    // REVISIT: we are losing the type information in DOM during serialization
    transient Object type;
    //
    // Constructors
    //

    protected AttrImpl(CoreDocumentImpl ownerDocument,String name){
        super(ownerDocument);
        this.name=name;
        /** False for default attributes. */
        isSpecified(true);
        hasStringValue(true);
    }

    // for AttrNSImpl
    protected AttrImpl(){
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. It is expected to be
    // called after the Attr has been detached for one thing.
    // CoreDocumentImpl does all the work.
    void rename(String name){
        if(needsSyncData()){
            synchronizeData();
        }
        this.name=name;
    }

    public void setIdAttribute(boolean id){
        if(needsSyncData()){
            synchronizeData();
        }
        isIdAttribute(id);
    }

    public short getNodeType(){
        return Node.ATTRIBUTE_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    public String getNodeValue(){
        return getValue();
    }
    //
    // Node methods
    //

    public void setNodeValue(String value) throws DOMException{
        setValue(value);
    }

    public Node cloneNode(boolean deep){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        AttrImpl clone=(AttrImpl)super.cloneNode(deep);
        // take care of case where there are kids
        if(!clone.hasStringValue()){
            // Need to break the association w/ original kids
            clone.value=null;
            // Cloning an Attribute always clones its children,
            // since they represent its value, no matter whether this
            // is a deep clone or not
            for(Node child=(Node)value;child!=null;
                child=child.getNextSibling()){
                clone.appendChild(child.cloneNode(true));
            }
        }
        clone.isSpecified(true);
        return clone;
    }

    void setOwnerDocument(CoreDocumentImpl doc){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        super.setOwnerDocument(doc);
        if(!hasStringValue()){
            for(ChildNode child=(ChildNode)value;
                child!=null;child=child.nextSibling){
                child.setOwnerDocument(doc);
            }
        }
    }

    public boolean hasChildNodes(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return value!=null;
    }

    public NodeList getChildNodes(){
        // JKESS: KNOWN ISSUE HERE
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return this;
    } // getChildNodes():NodeList

    public Node getFirstChild(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        makeChildNode();
        return (Node)value;
    }   // getFirstChild():Node

    // create a real text node as child if we don't have one yet
    protected void makeChildNode(){
        if(hasStringValue()){
            if(value!=null){
                TextImpl text=
                        (TextImpl)ownerDocument().createTextNode((String)value);
                value=text;
                text.isFirstChild(true);
                text.previousSibling=text;
                text.ownerNode=this;
                text.isOwned(true);
            }
            hasStringValue(false);
        }
    }

    public Node getLastChild(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return lastChild();
    } // getLastChild():Node
    //
    // Attr methods
    //

    final ChildNode lastChild(){
        // last child is stored as the previous sibling of first child
        makeChildNode();
        return value!=null?((ChildNode)value).previousSibling:null;
    }

    public Node insertBefore(Node newChild,Node refChild)
            throws DOMException{
        // Tail-call; optimizer should be able to do good things with.
        return internalInsertBefore(newChild,refChild,false);
    } // insertBefore(Node,Node):Node

    public Node removeChild(Node oldChild)
            throws DOMException{
        // Tail-call, should be optimizable
        if(hasStringValue()){
            // we don't have any child per say so it can't be one of them!
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        return internalRemoveChild(oldChild,false);
    } // removeChild(Node) :Node

    Node internalRemoveChild(Node oldChild,boolean replace)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(oldChild!=null&&oldChild.getParentNode()!=this){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
        }
        ChildNode oldInternal=(ChildNode)oldChild;
        // notify document
        ownerDocument.removingNode(this,oldInternal,replace);
        // Patch linked list around oldChild
        // Note: lastChild == firstChild.previousSibling
        if(oldInternal==value){ // oldInternal == firstChild
            // removing first child
            oldInternal.isFirstChild(false);
            // next line is: firstChild = oldInternal.nextSibling
            value=oldInternal.nextSibling;
            ChildNode firstChild=(ChildNode)value;
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
                ChildNode firstChild=(ChildNode)value;
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
    } // internalRemoveChild(Node,int):Node
    //
    // Attr2 methods
    //

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
    } // checkNormalizationAfterRemove(ChildNode)

    public Node replaceChild(Node newChild,Node oldChild)
            throws DOMException{
        makeChildNode();
        // If Mutation Events are being generated, this operation might
        // throw aggregate events twice when modifying an Attr -- once
        // on insertion and once on removal. DOM Level 2 does not specify
        // this as either desirable or undesirable, but hints that
        // aggregations should be issued only once per user request.
        // notify document
        CoreDocumentImpl ownerDocument=ownerDocument();
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
        if(hasStringValue()){
            return 1;
        }
        ChildNode node=(ChildNode)value;
        int length=0;
        for(;node!=null;node=node.nextSibling){
            length++;
        }
        return length;
    } // getLength():int
    //
    // Public methods
    //

    public Node item(int index){
        if(hasStringValue()){
            if(index!=0||value==null){
                return null;
            }else{
                makeChildNode();
                return (Node)value;
            }
        }
        if(index<0){
            return null;
        }
        ChildNode node=(ChildNode)value;
        for(int i=0;i<index&&node!=null;i++){
            node=node.nextSibling;
        }
        return node;
    } // item(int):Node

    public void normalize(){
        // No need to normalize if already normalized or
        // if value is kept as a String.
        if(isNormalized()||hasStringValue())
            return;
        Node kid, next;
        ChildNode firstChild=(ChildNode)value;
        for(kid=firstChild;kid!=null;kid=next){
            next=kid.getNextSibling();
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
        }
        isNormalized(true);
    } // normalize()
    //
    // Object methods
    //

    public boolean isEqualNode(Node arg){
        return super.isEqualNode(arg);
    }

    public void setReadOnly(boolean readOnly,boolean deep){
        super.setReadOnly(readOnly,deep);
        if(deep){
            if(needsSyncChildren()){
                synchronizeChildren();
            }
            if(hasStringValue()){
                return;
            }
            // Recursively set kids
            for(ChildNode mykid=(ChildNode)value;
                mykid!=null;
                mykid=mykid.nextSibling){
                if(mykid.getNodeType()!=Node.ENTITY_REFERENCE_NODE){
                    mykid.setReadOnly(readOnly,true);
                }
            }
        }
    } // setReadOnly(boolean,boolean)

    public String toString(){
        return getName()+"="+"\""+getValue()+"\"";
    }

    public String getName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    } // getName():String

    public boolean getSpecified(){
        if(needsSyncData()){
            synchronizeData();
        }
        return isSpecified();
    } // getSpecified():boolean

    public String getValue(){
        if(needsSyncData()){
            synchronizeData();
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        if(value==null){
            return "";
        }
        if(hasStringValue()){
            return (String)value;
        }
        ChildNode firstChild=((ChildNode)value);
        String data=null;
        if(firstChild.getNodeType()==Node.ENTITY_REFERENCE_NODE){
            data=((EntityReferenceImpl)firstChild).getEntityRefValue();
        }else{
            data=firstChild.getNodeValue();
        }
        ChildNode node=firstChild.nextSibling;
        if(node==null||data==null) return (data==null)?"":data;
        StringBuffer value=new StringBuffer(data);
        while(node!=null){
            if(node.getNodeType()==Node.ENTITY_REFERENCE_NODE){
                data=((EntityReferenceImpl)node).getEntityRefValue();
                if(data==null) return "";
                value.append(data);
            }else{
                value.append(node.getNodeValue());
            }
            node=node.nextSibling;
        }
        return value.toString();
    } // getValue():String

    public void setValue(String newvalue){
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        Element ownerElement=getOwnerElement();
        String oldvalue="";
        if(needsSyncData()){
            synchronizeData();
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        if(value!=null){
            if(ownerDocument.getMutationEvents()){
                // Can no longer just discard the kids; they may have
                // event listeners waiting for them to disconnect.
                if(hasStringValue()){
                    oldvalue=(String)value;
                    // create an actual text node as our child so
                    // that we can use it in the event
                    if(textNode==null){
                        textNode=(TextImpl)
                                ownerDocument.createTextNode((String)value);
                    }else{
                        textNode.data=(String)value;
                    }
                    value=textNode;
                    textNode.isFirstChild(true);
                    textNode.previousSibling=textNode;
                    textNode.ownerNode=this;
                    textNode.isOwned(true);
                    hasStringValue(false);
                    internalRemoveChild(textNode,true);
                }else{
                    oldvalue=getValue();
                    while(value!=null){
                        internalRemoveChild((Node)value,true);
                    }
                }
            }else{
                if(hasStringValue()){
                    oldvalue=(String)value;
                }else{
                    // simply discard children if any
                    oldvalue=getValue();
                    // remove ref from first child to last child
                    ChildNode firstChild=(ChildNode)value;
                    firstChild.previousSibling=null;
                    firstChild.isFirstChild(false);
                    firstChild.ownerNode=ownerDocument;
                }
                // then remove ref to current value
                value=null;
                needsSyncChildren(false);
            }
            if(isIdAttribute()&&ownerElement!=null){
                ownerDocument.removeIdentifier(oldvalue);
            }
        }
        // Create and add the new one, generating only non-aggregate events
        // (There are no listeners on the new Text, but there may be
        // capture/bubble listeners on the Attr.
        // Note that aggregate events are NOT dispatched here,
        // since we need to combine the remove and insert.
        isSpecified(true);
        if(ownerDocument.getMutationEvents()){
            // if there are any event handlers create a real node
            internalInsertBefore(ownerDocument.createTextNode(newvalue),
                    null,true);
            hasStringValue(false);
            // notify document
            ownerDocument.modifiedAttrValue(this,oldvalue);
        }else{
            // directly store the string
            value=newvalue;
            hasStringValue(true);
            changed();
        }
        if(isIdAttribute()&&ownerElement!=null){
            ownerDocument.putIdentifier(newvalue,ownerElement);
        }
    } // setValue(String)

    public Element getOwnerElement(){
        // if we have an owner, ownerNode is our ownerElement, otherwise it's
        // our ownerDocument and we don't have an ownerElement
        return (Element)(isOwned()?ownerNode:null);
    }

    public TypeInfo getSchemaTypeInfo(){
        return this;
    }

    public boolean isId(){
        // REVISIT: should an attribute that is not in the tree return
        // isID true?
        return isIdAttribute();
    }

    public void setSpecified(boolean arg){
        if(needsSyncData()){
            synchronizeData();
        }
        isSpecified(arg);
    } // setSpecified(boolean)

    protected void synchronizeChildren(){
        // By default just change the flag to avoid calling this method again
        needsSyncChildren(false);
    }
    //
    // NodeList methods
    //

    public String getTypeName(){
        return (String)type;
    }

    public String getTypeNamespace(){
        if(type!=null){
            return DTD_URI;
        }
        return null;
    }
    //
    // DOM3
    //

    public boolean isDerivedFrom(String typeNamespaceArg,
                                 String typeNameArg,
                                 int derivationMethod){
        return false;
    }

    public Element getElement(){
        // if we have an owner, ownerNode is our ownerElement, otherwise it's
        // our ownerDocument and we don't have an ownerElement
        return (Element)(isOwned()?ownerNode:null);
    }
    //
    // Public methods
    //

    public void setType(Object type){
        this.type=type;
    }
    //
    // Protected methods
    //

    final void lastChild(ChildNode node){
        // store lastChild as previous sibling of first child
        if(value!=null){
            ((ChildNode)value).previousSibling=node;
        }
    }

    Node internalInsertBefore(Node newChild,Node refChild,boolean replace)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
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
                        String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
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
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(newChild.getOwnerDocument()!=ownerDocument){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
            if(!ownerDocument.isKidOK(this,newChild)){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
            }
            // refChild must be a child of this node (or null)
            if(refChild!=null&&refChild.getParentNode()!=this){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
            // Prevent cycles in the tree
            // newChild cannot be ancestor of this Node,
            // and actually cannot be this
            boolean treeSafe=true;
            for(NodeImpl a=this;treeSafe&&a!=null;a=a.parentNode()){
                treeSafe=newChild!=a;
            }
            if(!treeSafe){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
            }
        }
        makeChildNode(); // make sure we have a node and not a string
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
        ChildNode firstChild=(ChildNode)value;
        if(firstChild==null){
            // this our first and only child
            value=newInternal; // firstchild = newInternal;
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
                    value=newInternal; // firstChild = newInternal;
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
        // notify document
        ownerDocument.insertedNode(this,newInternal,replace);
        checkNormalizationAfterInsert(newInternal);
        return newChild;
    } // internalInsertBefore(Node,Node,int):Node

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
        // hardset synchildren - so we don't try to sync -
        // it does not make any sense to try to synchildren when we just
        // deserialize object.
        needsSyncChildren(false);
    } // readObject(ObjectInputStream)
} // class AttrImpl
