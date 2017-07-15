/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: XPathNamespaceImpl.java,v 1.2.4.1 2005/09/10 04:10:02 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: XPathNamespaceImpl.java,v 1.2.4.1 2005/09/10 04:10:02 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.domapi;

import org.w3c.dom.*;
import org.w3c.dom.xpath.XPathNamespace;

class XPathNamespaceImpl implements XPathNamespace{
    // Node that XPathNamespaceImpl wraps
    final private Node m_attributeNode;
    private String textContent;

    XPathNamespaceImpl(Node node){
        m_attributeNode=node;
    }

    public Element getOwnerElement(){
        return ((Attr)m_attributeNode).getOwnerElement();
    }

    public String getNodeName(){
        return "#namespace";
    }

    public String getNodeValue() throws DOMException{
        return m_attributeNode.getNodeValue();
    }

    public void setNodeValue(String arg0) throws DOMException{
    }

    public short getNodeType(){
        return XPathNamespace.XPATH_NAMESPACE_NODE;
    }

    public Node getParentNode(){
        return m_attributeNode.getParentNode();
    }

    public NodeList getChildNodes(){
        return m_attributeNode.getChildNodes();
    }

    public Node getFirstChild(){
        return m_attributeNode.getFirstChild();
    }

    public Node getLastChild(){
        return m_attributeNode.getLastChild();
    }

    public Node getPreviousSibling(){
        return m_attributeNode.getPreviousSibling();
    }

    public Node getNextSibling(){
        return m_attributeNode.getNextSibling();
    }

    public NamedNodeMap getAttributes(){
        return m_attributeNode.getAttributes();
    }

    public Document getOwnerDocument(){
        return m_attributeNode.getOwnerDocument();
    }

    public Node insertBefore(Node arg0,Node arg1) throws DOMException{
        return null;
    }

    public Node replaceChild(Node arg0,Node arg1) throws DOMException{
        return null;
    }

    public Node removeChild(Node arg0) throws DOMException{
        return null;
    }

    public Node appendChild(Node arg0) throws DOMException{
        return null;
    }

    public boolean hasChildNodes(){
        return false;
    }

    public Node cloneNode(boolean arg0){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,null);
    }

    public void normalize(){
        m_attributeNode.normalize();
    }

    public boolean isSupported(String arg0,String arg1){
        return m_attributeNode.isSupported(arg0,arg1);
    }

    public String getNamespaceURI(){
        // For namespace node, the namespaceURI is the namespace URI
        // of the namespace represented by the node.
        return m_attributeNode.getNodeValue();
    }

    public String getPrefix(){
        return m_attributeNode.getPrefix();
    }

    public void setPrefix(String arg0) throws DOMException{
    }

    public String getLocalName(){
        // For namespace node, the local name is the same as the prefix
        return m_attributeNode.getPrefix();
    }

    public boolean hasAttributes(){
        return m_attributeNode.hasAttributes();
    }

    public String getBaseURI(){
        return null;
    }

    public short compareDocumentPosition(Node other) throws DOMException{
        return 0;
    }

    public String getTextContent() throws DOMException{
        return textContent;
    }

    public void setTextContent(String textContent) throws DOMException{
        this.textContent=textContent;
    }

    public boolean isSameNode(Node other){
        return false;
    }

    public String lookupPrefix(String namespaceURI){
        return ""; //PENDING
    }

    public boolean isDefaultNamespace(String namespaceURI){
        return false;
    }

    public String lookupNamespaceURI(String prefix){
        return null;
    }

    public boolean isEqualNode(Node arg){
        return false;
    }

    public Object getFeature(String feature,String version){
        return null; //PENDING
    }

    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler){
        return null; //PENDING
    }

    public Object getUserData(String key){
        return null;
    }
}
