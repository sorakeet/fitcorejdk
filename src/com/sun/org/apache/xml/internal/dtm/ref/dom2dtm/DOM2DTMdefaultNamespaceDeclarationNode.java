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
 * <p>
 * $Id: DOM2DTMdefaultNamespaceDeclarationNode.java,v 1.2.4.1 2005/09/15 08:15:11 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: DOM2DTMdefaultNamespaceDeclarationNode.java,v 1.2.4.1 2005/09/15 08:15:11 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref.dom2dtm;

import com.sun.org.apache.xml.internal.dtm.DTMException;
import org.w3c.dom.*;

public class DOM2DTMdefaultNamespaceDeclarationNode implements Attr, TypeInfo{
    final String NOT_SUPPORTED_ERR="Unsupported operation on pseudonode";
    Element pseudoparent;
    String prefix, uri, nodename;
    int handle;

    DOM2DTMdefaultNamespaceDeclarationNode(Element pseudoparent,String prefix,String uri,int handle){
        this.pseudoparent=pseudoparent;
        this.prefix=prefix;
        this.uri=uri;
        this.handle=handle;
        this.nodename="xmlns:"+prefix;
    }

    public String getName(){
        return nodename;
    }    public String getNodeName(){
        return nodename;
    }

    public boolean getSpecified(){
        return false;
    }

    public String getValue(){
        return uri;
    }    public String getNamespaceURI(){
        return "http://www.w3.org/2000/xmlns/";
    }

    public void setValue(String value){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }    public String getPrefix(){
        return prefix;
    }

    public Element getOwnerElement(){
        return pseudoparent;
    }    public String getLocalName(){
        return prefix;
    }

    public TypeInfo getSchemaTypeInfo(){
        return this;
    }    public String getNodeValue(){
        return uri;
    }

    public boolean isId(){
        return false;
    }

    public int getHandleOfNode(){
        return handle;
    }

    public String getTypeName(){
        return null;
    }    public boolean isSupported(String feature,String version){
        return false;
    }

    public String getTypeNamespace(){
        return null;
    }    public boolean hasChildNodes(){
        return false;
    }

    public boolean isDerivedFrom(String ns,String localName,int derivationMethod){
        return false;
    }    public boolean hasAttributes(){
        return false;
    }

    public Node getParentNode(){
        return null;
    }

    public Node getFirstChild(){
        return null;
    }

    public Node getLastChild(){
        return null;
    }

    public Node getPreviousSibling(){
        return null;
    }

    public Node getNextSibling(){
        return null;
    }



    public void normalize(){
        return;
    }

    public NodeList getChildNodes(){
        return null;
    }

    public NamedNodeMap getAttributes(){
        return null;
    }

    public short getNodeType(){
        return Node.ATTRIBUTE_NODE;
    }

    public void setNodeValue(String value){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }



    public void setPrefix(String value){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }

    public Node insertBefore(Node a,Node b){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }

    public Node replaceChild(Node a,Node b){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }

    public Node appendChild(Node a){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }

    public Node removeChild(Node a){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }

    public Document getOwnerDocument(){
        return pseudoparent.getOwnerDocument();
    }

    public Node cloneNode(boolean deep){
        throw new DTMException(NOT_SUPPORTED_ERR);
    }


    //RAMESH: PENDING=> Add proper implementation for the below DOM L3 additions











    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler){
        return getOwnerDocument().setUserData(key,data,handler);
    }

    public Object getUserData(String key){
        return getOwnerDocument().getUserData(key);
    }

    public Object getFeature(String feature,String version){
        // we don't have any alternate node, either this node does the job
        // or we don't have anything that does
        return isSupported(feature,version)?this:null;
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
        /**
         if (getBaseURI() == null) {
         if (((NodeImpl) arg).getBaseURI() != null) {
         return false;
         }
         }
         else if (!getBaseURI().equals(((NodeImpl) arg).getBaseURI())) {
         return false;
         }
         */
        return true;
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
                /**
                 NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                 if (ancestor != null) {
                 return ancestor.lookupNamespaceURI(specifiedPrefix);
                 }
                 */
                return null;
            }
/**
 case Node.DOCUMENT_NODE : {
 return((NodeImpl)((Document)this).getDocumentElement()).lookupNamespaceURI(specifiedPrefix) ;
 }
 */
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;
            case Node.ATTRIBUTE_NODE:{
                if(this.getOwnerElement().getNodeType()==Node.ELEMENT_NODE){
                    return getOwnerElement().lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
            default:{
                /**
                 NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
                 if (ancestor != null) {
                 return ancestor.lookupNamespaceURI(specifiedPrefix);
                 }
                 */
                return null;
            }
        }
    }

    public boolean isDefaultNamespace(String namespaceURI){
        /**
         // REVISIT: remove casts when DOM L3 becomes REC.
         short type = this.getNodeType();
         switch (type) {
         case Node.ELEMENT_NODE: {
         String namespace = this.getNamespaceURI();
         String prefix = this.getPrefix();

         // REVISIT: is it possible that prefix is empty string?
         if (prefix == null || prefix.length() == 0) {
         if (namespaceURI == null) {
         return (namespace == namespaceURI);
         }
         return namespaceURI.equals(namespace);
         }
         if (this.hasAttributes()) {
         ElementImpl elem = (ElementImpl)this;
         NodeImpl attr = (NodeImpl)elem.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns");
         if (attr != null) {
         String value = attr.getNodeValue();
         if (namespaceURI == null) {
         return (namespace == value);
         }
         return namespaceURI.equals(value);
         }
         }

         NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
         if (ancestor != null) {
         return ancestor.isDefaultNamespace(namespaceURI);
         }
         return false;
         }
         case Node.DOCUMENT_NODE:{
         return((NodeImpl)((Document)this).getDocumentElement()).isDefaultNamespace(namespaceURI);
         }

         case Node.ENTITY_NODE :
         case Node.NOTATION_NODE:
         case Node.DOCUMENT_FRAGMENT_NODE:
         case Node.DOCUMENT_TYPE_NODE:
         // type is unknown
         return false;
         case Node.ATTRIBUTE_NODE:{
         if (this.ownerNode.getNodeType() == Node.ELEMENT_NODE) {
         return ownerNode.isDefaultNamespace(namespaceURI);

         }
         return false;
         }
         default:{
         NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
         if (ancestor != null) {
         return ancestor.isDefaultNamespace(namespaceURI);
         }
         return false;
         }

         }
         */
        return false;
    }

    public String lookupPrefix(String namespaceURI){
        // REVISIT: When Namespaces 1.1 comes out this may not be true
        // Prefix can't be bound to null namespace
        if(namespaceURI==null){
            return null;
        }
        short type=this.getNodeType();
        switch(type){
/**
 case Node.ELEMENT_NODE: {

 String namespace = this.getNamespaceURI(); // to flip out children
 return lookupNamespacePrefix(namespaceURI, (ElementImpl)this);
 }

 case Node.DOCUMENT_NODE:{
 return((NodeImpl)((Document)this).getDocumentElement()).lookupPrefix(namespaceURI);
 }
 */
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
                // type is unknown
                return null;
            case Node.ATTRIBUTE_NODE:{
                if(this.getOwnerElement().getNodeType()==Node.ELEMENT_NODE){
                    return getOwnerElement().lookupPrefix(namespaceURI);
                }
                return null;
            }
            default:{
/**
 NodeImpl ancestor = (NodeImpl)getElementAncestor(this);
 if (ancestor != null) {
 return ancestor.lookupPrefix(namespaceURI);
 }
 */
                return null;
            }
        }
    }

    public boolean isSameNode(Node other){
        // we do not use any wrapper so the answer is obvious
        return this==other;
    }

    public void setTextContent(String textContent)
            throws DOMException{
        setNodeValue(textContent);
    }

    public String getTextContent() throws DOMException{
        return getNodeValue();  // overriden in some subclasses
    }

    public short compareDocumentPosition(Node other) throws DOMException{
        return 0;
    }

    public String getBaseURI(){
        return null;
    }
}
