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

import com.sun.org.apache.xerces.internal.util.URI;
import org.w3c.dom.*;

public class ElementImpl
        extends ParentNode
        implements Element, TypeInfo{
    //
    // Constants
    //
    static final long serialVersionUID=3717253516652722278L;
    //
    // Data
    //
    protected String name;
    protected AttributeMap attributes;
    //
    // Constructors
    //

    public ElementImpl(CoreDocumentImpl ownerDoc,String name){
        super(ownerDoc);
        this.name=name;
        needsSyncData(true);    // synchronizeData will initialize attributes
    }

    // for ElementNSImpl
    protected ElementImpl(){
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. CoreDocumentImpl
    // does all the work.
    void rename(String name){
        if(needsSyncData()){
            synchronizeData();
        }
        this.name=name;
        reconcileDefaultAttributes();
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return Node.ELEMENT_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    public NamedNodeMap getAttributes(){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            attributes=new AttributeMap(this,null);
        }
        return attributes;
    } // getAttributes():NamedNodeMap

    public boolean hasAttributes(){
        if(needsSyncData()){
            synchronizeData();
        }
        return (attributes!=null&&attributes.getLength()!=0);
    }

    public String getBaseURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        // Absolute base URI is computed according to
        // XML Base (http://www.w3.org/TR/xmlbase/#granularity)
        // 1. The base URI specified by an xml:base attribute on the element,
        // if one exists
        if(attributes!=null){
            Attr attrNode=(Attr)attributes.getNamedItem("xml:base");
            if(attrNode!=null){
                String uri=attrNode.getNodeValue();
                if(uri.length()!=0){// attribute value is always empty string
                    try{
                        uri=new URI(uri).toString();
                    }catch(URI.MalformedURIException e){
                        // This may be a relative URI.
                        // Make any parentURI into a URI object to use with the URI(URI, String) constructor
                        String parentBaseURI=(this.ownerNode!=null)?this.ownerNode.getBaseURI():null;
                        if(parentBaseURI!=null){
                            try{
                                uri=new URI(new URI(parentBaseURI),uri).toString();
                            }catch(URI.MalformedURIException ex){
                                // This should never happen: parent should have checked the URI and returned null if invalid.
                                return null;
                            }
                            return uri;
                        }
                        return null;
                    }
                    return uri;
                }
            }
        }
        // 2.the base URI of the element's parent element within the
        // document or external entity, if one exists
        // 3. the base URI of the document entity or external entity
        // containing the element
        // ownerNode serves as a parent or as document
        String baseURI=(this.ownerNode!=null)?this.ownerNode.getBaseURI():null;
        //base URI of parent element is not null
        if(baseURI!=null){
            try{
                //return valid absolute base URI
                return new URI(baseURI).toString();
            }catch(URI.MalformedURIException e){
                return null;
            }
        }
        return null;
    } //getBaseURI

    protected void synchronizeData(){
        // no need to sync in the future
        needsSyncData(false);
        // we don't want to generate any event for this so turn them off
        boolean orig=ownerDocument.getMutationEvents();
        ownerDocument.setMutationEvents(false);
        // attributes
        setupDefaultAttributes();
        // set mutation events flag back to its original value
        ownerDocument.setMutationEvents(orig);
    } // synchronizeData()
    //
    // Element methods
    //

    public Node cloneNode(boolean deep){
        ElementImpl newnode=(ElementImpl)super.cloneNode(deep);
        // Replicate NamedNodeMap rather than sharing it.
        if(attributes!=null){
            newnode.attributes=(AttributeMap)attributes.cloneMap(newnode);
        }
        return newnode;
    } // cloneNode(boolean):Node

    void setOwnerDocument(CoreDocumentImpl doc){
        super.setOwnerDocument(doc);
        if(attributes!=null){
            attributes.setOwnerDocument(doc);
        }
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
            // Otherwise it might be an Element, which is handled recursively
            else if(kid.getNodeType()==Node.ELEMENT_NODE){
                kid.normalize();
            }
        }
        // We must also normalize all of the attributes
        if(attributes!=null){
            for(int i=0;i<attributes.getLength();++i){
                Node attr=attributes.item(i);
                attr.normalize();
            }
        }
        // changed() will have occurred when the removeChild() was done,
        // so does not have to be reissued.
        isNormalized(true);
    } // normalize()

    public boolean isEqualNode(Node arg){
        if(!super.isEqualNode(arg)){
            return false;
        }
        boolean hasAttrs=hasAttributes();
        if(hasAttrs!=((Element)arg).hasAttributes()){
            return false;
        }
        if(hasAttrs){
            NamedNodeMap map1=getAttributes();
            NamedNodeMap map2=((Element)arg).getAttributes();
            int len=map1.getLength();
            if(len!=map2.getLength()){
                return false;
            }
            for(int i=0;i<len;i++){
                Node n1=map1.item(i);
                if(n1.getLocalName()==null){ // DOM Level 1 Node
                    Node n2=map2.getNamedItem(n1.getNodeName());
                    if(n2==null||!((NodeImpl)n1).isEqualNode(n2)){
                        return false;
                    }
                }else{
                    Node n2=map2.getNamedItemNS(n1.getNamespaceURI(),
                            n1.getLocalName());
                    if(n2==null||!((NodeImpl)n1).isEqualNode(n2)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setReadOnly(boolean readOnly,boolean deep){
        super.setReadOnly(readOnly,deep);
        if(attributes!=null){
            attributes.setReadOnly(readOnly,true);
        }
    }

    public String getTagName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    public String getAttribute(String name){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return "";
        }
        Attr attr=(Attr)(attributes.getNamedItem(name));
        return (attr==null)?"":attr.getValue();
    } // getAttribute(String):String

    public void setAttribute(String name,String value){
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR",
                            null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        Attr newAttr=getAttributeNode(name);
        if(newAttr==null){
            newAttr=getOwnerDocument().createAttribute(name);
            if(attributes==null){
                attributes=new AttributeMap(this,null);
            }
            newAttr.setNodeValue(value);
            attributes.setNamedItem(newAttr);
        }else{
            newAttr.setNodeValue(value);
        }
    } // setAttribute(String,String)

    public void removeAttribute(String name){
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return;
        }
        attributes.safeRemoveNamedItem(name);
    } // removeAttribute(String)
    //
    // DOM2: Namespace methods
    //

    public Attr getAttributeNode(String name){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return null;
        }
        return (Attr)attributes.getNamedItem(name);
    } // getAttributeNode(String):Attr

    public Attr setAttributeNode(Attr newAttr)
            throws DOMException{
        if(needsSyncData()){
            synchronizeData();
        }
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(newAttr.getOwnerDocument()!=ownerDocument){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
        }
        if(attributes==null){
            attributes=new AttributeMap(this,null);
        }
        // This will throw INUSE if necessary
        return (Attr)attributes.setNamedItem(newAttr);
    } // setAttributeNode(Attr):Attr

    public Attr removeAttributeNode(Attr oldAttr)
            throws DOMException{
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        return (Attr)attributes.removeItem(oldAttr,true);
    } // removeAttributeNode(Attr):Attr

    public NodeList getElementsByTagName(String tagname){
        return new DeepNodeListImpl(this,tagname);
    }

    public String getAttributeNS(String namespaceURI,String localName){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return "";
        }
        Attr attr=(Attr)(attributes.getNamedItemNS(namespaceURI,localName));
        return (attr==null)?"":attr.getValue();
    } // getAttributeNS(String,String):String

    public void setAttributeNS(String namespaceURI,String qualifiedName,
                               String value){
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR",
                            null);
            throw new DOMException(
                    DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        int index=qualifiedName.indexOf(':');
        String prefix, localName;
        if(index<0){
            prefix=null;
            localName=qualifiedName;
        }else{
            prefix=qualifiedName.substring(0,index);
            localName=qualifiedName.substring(index+1);
        }
        Attr newAttr=getAttributeNodeNS(namespaceURI,localName);
        if(newAttr==null){
            // REVISIT: this is not efficient, we are creating twice the same
            //          strings for prefix and localName.
            newAttr=getOwnerDocument().createAttributeNS(
                    namespaceURI,
                    qualifiedName);
            if(attributes==null){
                attributes=new AttributeMap(this,null);
            }
            newAttr.setNodeValue(value);
            attributes.setNamedItemNS(newAttr);
        }else{
            if(newAttr instanceof AttrNSImpl){
                String origNodeName=((AttrNSImpl)newAttr).name;
                String newName=(prefix!=null)?(prefix+":"+localName):localName;
                ((AttrNSImpl)newAttr).name=newName;
                if(!newName.equals(origNodeName)){
                    // Note: we can't just change the name of the attribute. Names have to be in sorted
                    // order in the attributes vector because a binary search is used to locate them.
                    // If the new name has a different prefix, the list may become unsorted.
                    // Maybe it would be better to resort the list, but the simplest
                    // fix seems to be to remove the old attribute and re-insert it.
                    // -- Norman.Walsh@Sun.COM, 2 Feb 2007
                    newAttr=(Attr)attributes.removeItem(newAttr,false);
                    attributes.addItem(newAttr);
                }
            }else{
                // This case may happen if user calls:
                //      elem.setAttribute("name", "value");
                //      elem.setAttributeNS(null, "name", "value");
                // This case is not defined by the DOM spec, we choose
                // to create a new attribute in this case and remove an old one from the tree
                // note this might cause events to be propagated or user data to be lost
                newAttr=new AttrNSImpl((CoreDocumentImpl)getOwnerDocument(),namespaceURI,qualifiedName,localName);
                attributes.setNamedItemNS(newAttr);
            }
            newAttr.setNodeValue(value);
        }
    } // setAttributeNS(String,String,String)

    public void removeAttributeNS(String namespaceURI,String localName){
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return;
        }
        attributes.safeRemoveNamedItemNS(namespaceURI,localName);
    } // removeAttributeNS(String,String)

    public Attr getAttributeNodeNS(String namespaceURI,String localName){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return null;
        }
        return (Attr)attributes.getNamedItemNS(namespaceURI,localName);
    } // getAttributeNodeNS(String,String):Attr

    public Attr setAttributeNodeNS(Attr newAttr)
            throws DOMException{
        if(needsSyncData()){
            synchronizeData();
        }
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(newAttr.getOwnerDocument()!=ownerDocument){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
        }
        if(attributes==null){
            attributes=new AttributeMap(this,null);
        }
        // This will throw INUSE if necessary
        return (Attr)attributes.setNamedItemNS(newAttr);
    } // setAttributeNodeNS(Attr):Attr

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName){
        return new DeepNodeListImpl(this,namespaceURI,localName);
    }

    public boolean hasAttribute(String name){
        return getAttributeNode(name)!=null;
    }

    public boolean hasAttributeNS(String namespaceURI,String localName){
        return getAttributeNodeNS(namespaceURI,localName)!=null;
    }

    public TypeInfo getSchemaTypeInfo(){
        if(needsSyncData()){
            synchronizeData();
        }
        return this;
    }

    public void setIdAttribute(String name,boolean makeId){
        if(needsSyncData()){
            synchronizeData();
        }
        Attr at=getAttributeNode(name);
        if(at==null){
            String msg=DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(at.getOwnerElement()!=this){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if(!makeId){
            ownerDocument.removeIdentifier(at.getValue());
        }else{
            ownerDocument.putIdentifier(at.getValue(),this);
        }
    }

    public void setIdAttributeNS(String namespaceURI,String localName,
                                 boolean makeId){
        if(needsSyncData()){
            synchronizeData();
        }
        //if namespace uri is empty string, set it to 'null'
        if(namespaceURI!=null){
            namespaceURI=(namespaceURI.length()==0)?null:namespaceURI;
        }
        Attr at=getAttributeNodeNS(namespaceURI,localName);
        if(at==null){
            String msg=DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(at.getOwnerElement()!=this){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if(!makeId){
            ownerDocument.removeIdentifier(at.getValue());
        }else{
            ownerDocument.putIdentifier(at.getValue(),this);
        }
    }

    public void setIdAttributeNode(Attr at,boolean makeId){
        if(needsSyncData()){
            synchronizeData();
        }
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(at.getOwnerElement()!=this){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }
        }
        ((AttrImpl)at).isIdAttribute(makeId);
        if(!makeId){
            ownerDocument.removeIdentifier(at.getValue());
        }else{
            ownerDocument.putIdentifier(at.getValue(),this);
        }
    }

    protected int setXercesAttributeNode(Attr attr){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            attributes=new AttributeMap(this,null);
        }
        return attributes.addItem(attr);
    }

    protected int getXercesAttribute(String namespaceURI,String localName){
        if(needsSyncData()){
            synchronizeData();
        }
        if(attributes==null){
            return -1;
        }
        return attributes.getNamedItemIndex(namespaceURI,localName);
    }

    public String getTypeName(){
        return null;
    }
    //
    // Public methods
    //

    public String getTypeNamespace(){
        return null;
    }
    //
    // Protected methods
    //

    public boolean isDerivedFrom(String typeNamespaceArg,
                                 String typeNameArg,
                                 int derivationMethod){
        return false;
    }

    // support for DOM Level 3 renameNode method
    // @param el The element from which to take the attributes
    void moveSpecifiedAttributes(ElementImpl el){
        if(needsSyncData()){
            synchronizeData();
        }
        if(el.hasAttributes()){
            if(attributes==null){
                attributes=new AttributeMap(this,null);
            }
            attributes.moveSpecifiedAttributes(el.attributes);
        }
    }

    protected void setupDefaultAttributes(){
        NamedNodeMapImpl defaults=getDefaultAttributes();
        if(defaults!=null){
            attributes=new AttributeMap(this,defaults);
        }
    }

    protected void reconcileDefaultAttributes(){
        if(attributes!=null){
            NamedNodeMapImpl defaults=getDefaultAttributes();
            attributes.reconcileDefaults(defaults);
        }
    }

    protected NamedNodeMapImpl getDefaultAttributes(){
        DocumentTypeImpl doctype=
                (DocumentTypeImpl)ownerDocument.getDoctype();
        if(doctype==null){
            return null;
        }
        ElementDefinitionImpl eldef=
                (ElementDefinitionImpl)doctype.getElements()
                        .getNamedItem(getNodeName());
        if(eldef==null){
            return null;
        }
        return (NamedNodeMapImpl)eldef.getAttributes();
    } // getDefaultAttributes()
} // class ElementImpl
