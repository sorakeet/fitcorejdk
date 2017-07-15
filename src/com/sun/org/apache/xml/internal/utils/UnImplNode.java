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
 * $Id: UnImplNode.java,v
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
 * $Id: UnImplNode.java,v
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.w3c.dom.*;

public class UnImplNode implements Node, Element, NodeList, Document{
    protected String fDocumentURI;
    protected String actualEncoding;    public void error(String msg){
        System.out.println("DOM ERROR! class: "+this.getClass().getName());
        throw new RuntimeException(XMLMessages.createXMLMessage(msg,null));
    }
    private String xmlEncoding;
    private boolean xmlStandalone;    public Node appendChild(Node newChild) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"appendChild not supported!");
        return null;
    }
    private String xmlVersion;    public boolean hasChildNodes(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"hasChildNodes not supported!");
        return false;
    }

    public UnImplNode(){
    }    public short getNodeType(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getNodeType not supported!");
        return 0;
    }

    public void error(String msg,Object[] args){
        System.out.println("DOM ERROR! class: "+this.getClass().getName());
        throw new RuntimeException(XMLMessages.createXMLMessage(msg,args));  //"UnImplNode error: "+msg);
    }    public Node getParentNode(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getParentNode not supported!");
        return null;
    }

    public Node item(int index){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"item not supported!");
        return null;
    }  // item(int):Node    public NodeList getChildNodes(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getChildNodes not supported!");
        return null;
    }

    public int getLength(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getLength not supported!");
        return 0;
    }  // getLength():int    public Node getFirstChild(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getFirstChild not supported!");
        return null;
    }

    public String getTagName(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getTagName not supported!");
        return null;
    }    public Node getLastChild(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getLastChild not supported!");
        return null;
    }

    public String getAttribute(String name){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getAttribute not supported!");
        return null;
    }    public Node getNextSibling(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getNextSibling not supported!");
        return null;
    }

    public void setAttribute(String name,String value) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setAttribute not supported!");
    }

    public void removeAttribute(String name) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"removeAttribute not supported!");
    }

    public Attr getAttributeNode(String name){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getAttributeNode not supported!");
        return null;
    }    public Document getOwnerDocument(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getOwnerDocument not supported!");
        return null;
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setAttributeNode not supported!");
        return null;
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"removeAttributeNode not supported!");
        return null;
    }    public String getNodeName(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getNodeName not supported!");
        return null;
    }

    public NodeList getElementsByTagName(String name){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getElementsByTagName not supported!");
        return null;
    }    public void normalize(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"normalize not supported!");
    }

    public String getAttributeNS(String namespaceURI,String localName){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getAttributeNS not supported!");
        return null;
    }

    public void setAttributeNS(
            String namespaceURI,String qualifiedName,String value)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setAttributeNS not supported!");
    }

    public void removeAttributeNS(String namespaceURI,String localName)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"removeAttributeNS not supported!");
    }

    public Attr getAttributeNodeNS(String namespaceURI,String localName){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getAttributeNodeNS not supported!");
        return null;
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setAttributeNodeNS not supported!");
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getElementsByTagNameNS not supported!");
        return null;
    }

    public boolean hasAttribute(String name){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"hasAttribute not supported!");
        return false;
    }

    public boolean hasAttributeNS(String name,String x){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"hasAttributeNS not supported!");
        return false;
    }

    public TypeInfo getSchemaTypeInfo(){
        return null; //PENDING
    }

    public void setIdAttribute(String name,boolean makeId){
        //PENDING
    }    public boolean hasAttributes(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"hasAttributes not supported!");
        return false;
    }

    public void setIdAttributeNS(String namespaceURI,String localName,
                                 boolean makeId){
        //PENDING
    }

    public void setIdAttributeNode(Attr at,boolean makeId){
        //PENDING
    }

    public void setValue(String value) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setValue not supported!");
    }

    public boolean getSpecified(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setValue not supported!");
        return false;
    }

    public DocumentType getDoctype(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public DOMImplementation getImplementation(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public Element getDocumentElement(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Node getPreviousSibling(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getPreviousSibling not supported!");
        return null;
    }

    public Element createElement(String tagName) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Node cloneNode(boolean deep){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"cloneNode not supported!");
        return null;
    }

    public DocumentFragment createDocumentFragment(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public String getNodeValue() throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getNodeValue not supported!");
        return null;
    }

    public Text createTextNode(String data){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public void setNodeValue(String nodeValue) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setNodeValue not supported!");
    }
    // public String getValue ()
    // {
    //  error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED); //"getValue not supported!");
    //  return null;
    // }

    public Comment createComment(String data){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }
    // public String getName()
    // {
    //  return this.getNodeName();
    // }

    public CDATASection createCDATASection(String data) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Element getOwnerElement(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getOwnerElement not supported!");
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(
            String target,String data) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public Attr createAttribute(String name) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public NamedNodeMap getAttributes(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getAttributes not supported!");
        return null;
    }

    public EntityReference createEntityReference(String name)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Node insertBefore(Node newChild,Node refChild) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"insertBefore not supported!");
        return null;
    }

    public Node importNode(Node importedNode,boolean deep) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Node replaceChild(Node newChild,Node oldChild) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"replaceChild not supported!");
        return null;
    }

    public Element createElementNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public Node removeChild(Node oldChild) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"replaceChild not supported!");
        return null;
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public boolean isSupported(String feature,String version){
        return false;
    }

    public Element getElementById(String elementId){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public String getNamespaceURI(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getNamespaceURI not supported!");
        return null;
    }

    public String getInputEncoding(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }    public String getPrefix(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getPrefix not supported!");
        return null;
    }

    public void setInputEncoding(String encoding){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }    public void setPrefix(String prefix) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"setPrefix not supported!");
    }

    public String getXmlEncoding(){
        return xmlEncoding;
    }    public String getLocalName(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);  //"getLocalName not supported!");
        return null;
    }

    public void setXmlEncoding(String xmlEncoding){
        this.xmlEncoding=xmlEncoding;
    }

    public boolean getXmlStandalone(){
        return xmlStandalone;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException{
        this.xmlStandalone=xmlStandalone;
    }

    public String getXmlVersion(){
        return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException{
        this.xmlVersion=xmlVersion;
    }

    public boolean getStrictErrorChecking(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return false;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public String getDocumentURI(){
        return fDocumentURI;
    }

    public void setDocumentURI(String documentURI){
        fDocumentURI=documentURI;
    }

    public Node adoptNode(Node source) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public DOMConfiguration getDomConfig(){
        return null;
    }

    public void normalizeDocument(){
    }

    public Node renameNode(Node n,
                           String namespaceURI,
                           String name)
            throws DOMException{
        return n;
    }

    public void setData(String data) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public String substringData(int offset,int count) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public void appendData(String arg) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public void insertData(int offset,String arg) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public void deleteData(int offset,int count) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public void replaceData(int offset,int count,String arg)
            throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public Text splitText(int offset) throws DOMException{
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public boolean getStandalone(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return false;
    }

    public void setStandalone(boolean standalone){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public String getVersion(){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    public void setVersion(String version){
        error(XMLErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    }

    public String getActualEncoding(){
        return actualEncoding;
    }

    public void setActualEncoding(String value){
        actualEncoding=value;
    }

    public Text replaceWholeText(String content)
            throws DOMException{
/**

 if (needsSyncData()) {
 synchronizeData();
 }

 // make sure we can make the replacement
 if (!canModify(nextSibling)) {
 throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
 DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
 }

 Node parent = this.getParentNode();
 if (content == null || content.length() == 0) {
 // remove current node
 if (parent !=null) { // check if node in the tree
 parent.removeChild(this);
 return null;
 }
 }
 Text currentNode = null;
 if (isReadOnly()){
 Text newNode = this.ownerDocument().createTextNode(content);
 if (parent !=null) { // check if node in the tree
 parent.insertBefore(newNode, this);
 parent.removeChild(this);
 currentNode = newNode;
 } else {
 return newNode;
 }
 }  else {
 this.setData(content);
 currentNode = this;
 }
 Node sibling =  currentNode.getNextSibling();
 while ( sibling !=null) {
 parent.removeChild(sibling);
 sibling = currentNode.getNextSibling();
 }

 return currentNode;
 */
        return null; //Pending
    }

    public String getWholeText(){
/**
 if (needsSyncData()) {
 synchronizeData();
 }
 if (nextSibling == null) {
 return data;
 }
 StringBuffer buffer = new StringBuffer();
 if (data != null && data.length() != 0) {
 buffer.append(data);
 }
 getWholeText(nextSibling, buffer);
 return buffer.toString();
 */
        return null; // PENDING
    }

    public boolean isWhitespaceInElementContent(){
        return false;
    }

    public void setIdAttribute(boolean id){
        //PENDING
    }

    public boolean isId(){
        return false; //PENDING
    }
//RAMESH : Pending proper implementation of DOM Level 3

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
