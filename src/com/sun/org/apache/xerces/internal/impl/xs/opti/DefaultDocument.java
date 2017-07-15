/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.opti;

import org.w3c.dom.*;

public class DefaultDocument extends NodeImpl
        implements Document{
    private String fDocumentURI=null;

    // default constructor
    public DefaultDocument(){
    }
    //
    // org.w3c.dom.Document methods
    //

    public DocumentType getDoctype(){
        return null;
    }

    public DOMImplementation getImplementation(){
        return null;
    }

    public Element getDocumentElement(){
        return null;
    }

    public Element createElement(String tagName) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public DocumentFragment createDocumentFragment(){
        return null;
    }

    public Text createTextNode(String data){
        return null;
    }

    public Comment createComment(String data){
        return null;
    }

    public CDATASection createCDATASection(String data) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public ProcessingInstruction createProcessingInstruction(String target,String data) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public Attr createAttribute(String name) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public EntityReference createEntityReference(String name) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public NodeList getElementsByTagName(String tagname){
        return null;
    }

    public Node importNode(Node importedNode,boolean deep) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public Element createElementNS(String namespaceURI,String qualifiedName) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,String localName){
        return null;
    }

    public Element getElementById(String elementId){
        return null;
    }
    // DOM Level 3 methods.

    public String getInputEncoding(){
        return null;
    }

    public String getXmlEncoding(){
        return null;
    }

    public boolean getXmlStandalone(){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void setXmlStandalone(boolean standalone){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public String getXmlVersion(){
        return null;
    }

    public void setXmlVersion(String version) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public boolean getStrictErrorChecking(){
        return false;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public String getDocumentURI(){
        return fDocumentURI;
    }

    public void setDocumentURI(String documentURI){
        fDocumentURI=documentURI;
    }

    public Node adoptNode(Node source) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public DOMConfiguration getDomConfig(){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public void normalizeDocument(){
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }

    public Node renameNode(Node n,String namespaceURI,String name) throws DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Method not supported");
    }
}
