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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class AttributeMap extends NamedNodeMapImpl{
    static final long serialVersionUID=8872606282138665383L;
    //
    // Constructors
    //

    protected AttributeMap(ElementImpl ownerNode,NamedNodeMapImpl defaults){
        super(ownerNode);
        if(defaults!=null){
            // initialize map with the defaults
            cloneContent(defaults);
            if(nodes!=null){
                hasDefaults(true);
            }
        }
    }

    Node safeRemoveNamedItem(String name){
        return internalRemoveNamedItem(name,false);
    }

    protected Node removeItem(Node item,boolean addDefault)
            throws DOMException{
        int index=-1;
        if(nodes!=null){
            final int size=nodes.size();
            for(int i=0;i<size;++i){
                if(nodes.get(i)==item){
                    index=i;
                    break;
                }
            }
        }
        if(index<0){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        return remove((AttrImpl)item,index,addDefault);
    }

    Node safeRemoveNamedItemNS(String namespaceURI,String name){
        return internalRemoveNamedItemNS(namespaceURI,name,false);
    }

    void moveSpecifiedAttributes(AttributeMap srcmap){
        int nsize=(srcmap.nodes!=null)?srcmap.nodes.size():0;
        for(int i=nsize-1;i>=0;i--){
            AttrImpl attr=(AttrImpl)srcmap.nodes.get(i);
            if(attr.isSpecified()){
                srcmap.remove(attr,i,false);
                if(attr.getLocalName()!=null){
                    setNamedItem(attr);
                }else{
                    setNamedItemNS(attr);
                }
            }
        }
    } // moveSpecifiedAttributes(AttributeMap):void

    public Node setNamedItem(Node arg)
            throws DOMException{
        boolean errCheck=ownerNode.ownerDocument().errorChecking;
        if(errCheck){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(arg.getOwnerDocument()!=ownerNode.ownerDocument()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
            if(arg.getNodeType()!=Node.ATTRIBUTE_NODE){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
            }
        }
        AttrImpl argn=(AttrImpl)arg;
        if(argn.isOwned()){
            if(errCheck&&argn.getOwnerElement()!=ownerNode){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INUSE_ATTRIBUTE_ERR",null);
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,msg);
            }
            // replacing an Attribute with itself does nothing
            return arg;
        }
        // set owner
        argn.ownerNode=ownerNode;
        argn.isOwned(true);
        int i=findNamePoint(argn.getNodeName(),0);
        AttrImpl previous=null;
        if(i>=0){
            previous=(AttrImpl)nodes.get(i);
            nodes.set(i,arg);
            previous.ownerNode=ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        }else{
            i=-1-i; // Insert point (may be end of list)
            if(null==nodes){
                nodes=new ArrayList(5);
            }
            nodes.add(i,arg);
        }
        // notify document
        ownerNode.ownerDocument().setAttrNode(argn,previous);
        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if(!argn.isNormalized()){
            ownerNode.isNormalized(false);
        }
        return previous;
    } // setNamedItem(Node):Node

    public Node setNamedItemNS(Node arg)
            throws DOMException{
        boolean errCheck=ownerNode.ownerDocument().errorChecking;
        if(errCheck){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(arg.getOwnerDocument()!=ownerNode.ownerDocument()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
            if(arg.getNodeType()!=Node.ATTRIBUTE_NODE){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
            }
        }
        AttrImpl argn=(AttrImpl)arg;
        if(argn.isOwned()){
            if(errCheck&&argn.getOwnerElement()!=ownerNode){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INUSE_ATTRIBUTE_ERR",null);
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,msg);
            }
            // replacing an Attribute with itself does nothing
            return arg;
        }
        // set owner
        argn.ownerNode=ownerNode;
        argn.isOwned(true);
        int i=findNamePoint(argn.getNamespaceURI(),argn.getLocalName());
        AttrImpl previous=null;
        if(i>=0){
            previous=(AttrImpl)nodes.get(i);
            nodes.set(i,arg);
            previous.ownerNode=ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
        }else{
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i=findNamePoint(arg.getNodeName(),0);
            if(i>=0){
                previous=(AttrImpl)nodes.get(i);
                nodes.add(i,arg);
            }else{
                i=-1-i; // Insert point (may be end of list)
                if(null==nodes){
                    nodes=new ArrayList(5);
                }
                nodes.add(i,arg);
            }
        }
        //      changed(true);
        // notify document
        ownerNode.ownerDocument().setAttrNode(argn,previous);
        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if(!argn.isNormalized()){
            ownerNode.isNormalized(false);
        }
        return previous;
    } // setNamedItemNS(Node):Node

    public Node removeNamedItem(String name)
            throws DOMException{
        return internalRemoveNamedItem(name,true);
    }

    final protected Node internalRemoveNamedItem(String name,boolean raiseEx){
        if(isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        int i=findNamePoint(name,0);
        if(i<0){
            if(raiseEx){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }else{
                return null;
            }
        }
        return remove((AttrImpl)nodes.get(i),i,true);
    } // internalRemoveNamedItem(String,boolean):Node

    private final Node remove(AttrImpl attr,int index,
                              boolean addDefault){
        CoreDocumentImpl ownerDocument=ownerNode.ownerDocument();
        String name=attr.getNodeName();
        if(attr.isIdAttribute()){
            ownerDocument.removeIdentifier(attr.getValue());
        }
        if(hasDefaults()&&addDefault){
            // If there's a default, add it instead
            NamedNodeMapImpl defaults=
                    ((ElementImpl)ownerNode).getDefaultAttributes();
            Node d;
            if(defaults!=null&&
                    (d=defaults.getNamedItem(name))!=null&&
                    findNamePoint(name,index+1)<0){
                NodeImpl clone=(NodeImpl)d.cloneNode(true);
                if(d.getLocalName()!=null){
                    // we must rely on the name to find a default attribute
                    // ("test:attr"), but while copying it from the DOCTYPE
                    // we should not loose namespace URI that was assigned
                    // to the attribute in the instance document.
                    ((AttrNSImpl)clone).namespaceURI=attr.getNamespaceURI();
                }
                clone.ownerNode=ownerNode;
                clone.isOwned(true);
                clone.isSpecified(false);
                nodes.set(index,clone);
                if(attr.isIdAttribute()){
                    ownerDocument.putIdentifier(clone.getNodeValue(),
                            (ElementImpl)ownerNode);
                }
            }else{
                nodes.remove(index);
            }
        }else{
            nodes.remove(index);
        }
        //        changed(true);
        // remove reference to owner
        attr.ownerNode=ownerDocument;
        attr.isOwned(false);
        // make sure it won't be mistaken with defaults in case it's
        // reused
        attr.isSpecified(true);
        attr.isIdAttribute(false);
        // notify document
        ownerDocument.removedAttrNode(attr,ownerNode,name);
        return attr;
    }

    public Node removeNamedItemNS(String namespaceURI,String name)
            throws DOMException{
        return internalRemoveNamedItemNS(namespaceURI,name,true);
    }
    //
    // Public methods
    //

    final protected Node internalRemoveNamedItemNS(String namespaceURI,
                                                   String name,
                                                   boolean raiseEx){
        CoreDocumentImpl ownerDocument=ownerNode.ownerDocument();
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        int i=findNamePoint(namespaceURI,name);
        if(i<0){
            if(raiseEx){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
                throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
            }else{
                return null;
            }
        }
        AttrImpl n=(AttrImpl)nodes.get(i);
        if(n.isIdAttribute()){
            ownerDocument.removeIdentifier(n.getValue());
        }
        // If there's a default, add it instead
        String nodeName=n.getNodeName();
        if(hasDefaults()){
            NamedNodeMapImpl defaults=((ElementImpl)ownerNode).getDefaultAttributes();
            Node d;
            if(defaults!=null
                    &&(d=defaults.getNamedItem(nodeName))!=null){
                int j=findNamePoint(nodeName,0);
                if(j>=0&&findNamePoint(nodeName,j+1)<0){
                    NodeImpl clone=(NodeImpl)d.cloneNode(true);
                    clone.ownerNode=ownerNode;
                    if(d.getLocalName()!=null){
                        // we must rely on the name to find a default attribute
                        // ("test:attr"), but while copying it from the DOCTYPE
                        // we should not loose namespace URI that was assigned
                        // to the attribute in the instance document.
                        ((AttrNSImpl)clone).namespaceURI=namespaceURI;
                    }
                    clone.isOwned(true);
                    clone.isSpecified(false);
                    nodes.set(i,clone);
                    if(clone.isIdAttribute()){
                        ownerDocument.putIdentifier(clone.getNodeValue(),
                                (ElementImpl)ownerNode);
                    }
                }else{
                    nodes.remove(i);
                }
            }else{
                nodes.remove(i);
            }
        }else{
            nodes.remove(i);
        }
        //        changed(true);
        // remove reference to owner
        n.ownerNode=ownerDocument;
        n.isOwned(false);
        // make sure it won't be mistaken with defaults in case it's
        // reused
        n.isSpecified(true);
        // update id table if needed
        n.isIdAttribute(false);
        // notify document
        ownerDocument.removedAttrNode(n,ownerNode,name);
        return n;
    } // internalRemoveNamedItemNS(String,String,boolean):Node

    public NamedNodeMapImpl cloneMap(NodeImpl ownerNode){
        AttributeMap newmap=
                new AttributeMap((ElementImpl)ownerNode,null);
        newmap.hasDefaults(hasDefaults());
        newmap.cloneContent(this);
        return newmap;
    } // cloneMap():AttributeMap

    protected void cloneContent(NamedNodeMapImpl srcmap){
        List srcnodes=srcmap.nodes;
        if(srcnodes!=null){
            int size=srcnodes.size();
            if(size!=0){
                if(nodes==null){
                    nodes=new ArrayList(size);
                }else{
                    nodes.clear();
                }
                for(int i=0;i<size;++i){
                    NodeImpl n=(NodeImpl)srcnodes.get(i);
                    NodeImpl clone=(NodeImpl)n.cloneNode(true);
                    clone.isSpecified(n.isSpecified());
                    nodes.add(clone);
                    clone.ownerNode=ownerNode;
                    clone.isOwned(true);
                }
            }
        }
    } // cloneContent():AttributeMap

    protected final int addItem(Node arg){
        final AttrImpl argn=(AttrImpl)arg;
        // set owner
        argn.ownerNode=ownerNode;
        argn.isOwned(true);
        int i=findNamePoint(argn.getNamespaceURI(),argn.getLocalName());
        if(i>=0){
            nodes.set(i,arg);
        }else{
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i=findNamePoint(argn.getNodeName(),0);
            if(i>=0){
                nodes.add(i,arg);
            }else{
                i=-1-i; // Insert point (may be end of list)
                if(null==nodes){
                    nodes=new ArrayList(5);
                }
                nodes.add(i,arg);
            }
        }
        // notify document
        ownerNode.ownerDocument().setAttrNode(argn,null);
        return i;
    }

    protected void reconcileDefaults(NamedNodeMapImpl defaults){
        // remove any existing default
        int nsize=(nodes!=null)?nodes.size():0;
        for(int i=nsize-1;i>=0;--i){
            AttrImpl attr=(AttrImpl)nodes.get(i);
            if(!attr.isSpecified()){
                remove(attr,i,false);
            }
        }
        // add the new defaults
        if(defaults==null){
            return;
        }
        if(nodes==null||nodes.size()==0){
            cloneContent(defaults);
        }else{
            int dsize=defaults.nodes.size();
            for(int n=0;n<dsize;++n){
                AttrImpl d=(AttrImpl)defaults.nodes.get(n);
                int i=findNamePoint(d.getNodeName(),0);
                if(i<0){
                    i=-1-i;
                    NodeImpl clone=(NodeImpl)d.cloneNode(true);
                    clone.ownerNode=ownerNode;
                    clone.isOwned(true);
                    clone.isSpecified(false);
                    nodes.add(i,clone);
                }
            }
        }
    } // reconcileDefaults()
} // class AttributeMap
