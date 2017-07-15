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
 * $Id: DOMBuilder.java,v 1.2.4.1 2005/09/15 08:15:39 suresh_emailid Exp $
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
 * $Id: DOMBuilder.java,v 1.2.4.1 2005/09/15 08:15:39 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

import java.util.Stack;

public class DOMBuilder
        implements ContentHandler, LexicalHandler{
    public Document m_doc;
    public DocumentFragment m_docFrag=null;
    protected Node m_currentNode=null;
    protected Node m_root=null;
    protected Node m_nextSibling=null;
    protected Stack m_elemStack=new Stack();
    protected boolean m_inCData=false;

    public DOMBuilder(Document doc,Node node){
        m_doc=doc;
        m_currentNode=m_root=node;
        if(node instanceof Element)
            m_elemStack.push(node);
    }

    public DOMBuilder(Document doc,DocumentFragment docFrag){
        m_doc=doc;
        m_docFrag=docFrag;
    }

    public DOMBuilder(Document doc){
        m_doc=doc;
    }

    public Node getRootDocument(){
        return (null!=m_docFrag)?(Node)m_docFrag:(Node)m_doc;
    }

    public Node getRootNode(){
        return m_root;
    }

    public Node getCurrentNode(){
        return m_currentNode;
    }

    public Node getNextSibling(){
        return m_nextSibling;
    }

    public void setNextSibling(Node nextSibling){
        m_nextSibling=nextSibling;
    }

    public java.io.Writer getWriter(){
        return null;
    }

    public void setDocumentLocator(Locator locator){
        // No action for the moment.
    }

    public void startDocument() throws org.xml.sax.SAXException{
        // No action for the moment.
    }

    public void endDocument() throws org.xml.sax.SAXException{
        // No action for the moment.
    }

    public void startPrefixMapping(String prefix,String uri)
            throws org.xml.sax.SAXException{
        /**
         // Not sure if this is needed or wanted
         // Also, it fails in the stree.
         if((null != m_currentNode)
         && (m_currentNode.getNodeType() == Node.ELEMENT_NODE))
         {
         String qname;
         if(((null != prefix) && (prefix.length() == 0))
         || (null == prefix))
         qname = "xmlns";
         else
         qname = "xmlns:"+prefix;

         Element elem = (Element)m_currentNode;
         String val = elem.getAttribute(qname); // Obsolete, should be DOM2...?
         if(val == null)
         {
         elem.setAttributeNS("http://www.w3.org/XML/1998/namespace",
         qname, uri);
         }
         }
         */
    }

    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException{
    }

    public void startElement(
            String ns,String localName,String name,Attributes atts)
            throws org.xml.sax.SAXException{
        Element elem;
        // Note that the namespace-aware call must be used to correctly
        // construct a Level 2 DOM, even for non-namespaced nodes.
        if((null==ns)||(ns.length()==0))
            elem=m_doc.createElementNS(null,name);
        else
            elem=m_doc.createElementNS(ns,name);
        append(elem);
        try{
            int nAtts=atts.getLength();
            if(0!=nAtts){
                for(int i=0;i<nAtts;i++){
                    //System.out.println("type " + atts.getType(i) + " name " + atts.getLocalName(i) );
                    // First handle a possible ID attribute
                    if(atts.getType(i).equalsIgnoreCase("ID"))
                        setIDAttribute(atts.getValue(i),elem);
                    String attrNS=atts.getURI(i);
                    if("".equals(attrNS))
                        attrNS=null; // DOM represents no-namespace as null
                    // System.out.println("attrNS: "+attrNS+", localName: "+atts.getQName(i)
                    //                   +", qname: "+atts.getQName(i)+", value: "+atts.getValue(i));
                    // Crimson won't let us set an xmlns: attribute on the DOM.
                    String attrQName=atts.getQName(i);
                    // In SAX, xmlns[:] attributes have an empty namespace, while in DOM they
                    // should have the xmlns namespace
                    if(attrQName.startsWith("xmlns:")||attrQName.equals("xmlns")){
                        attrNS="http://www.w3.org/2000/xmlns/";
                    }
                    // ALWAYS use the DOM Level 2 call!
                    elem.setAttributeNS(attrNS,attrQName,atts.getValue(i));
                }
            }
            // append(elem);
            m_elemStack.push(elem);
            m_currentNode=elem;
            // append(elem);
        }catch(Exception de){
            // de.printStackTrace();
            throw new org.xml.sax.SAXException(de);
        }
    }

    protected void append(Node newNode) throws org.xml.sax.SAXException{
        Node currentNode=m_currentNode;
        if(null!=currentNode){
            if(currentNode==m_root&&m_nextSibling!=null)
                currentNode.insertBefore(newNode,m_nextSibling);
            else
                currentNode.appendChild(newNode);
            // System.out.println(newNode.getNodeName());
        }else if(null!=m_docFrag){
            if(m_nextSibling!=null)
                m_docFrag.insertBefore(newNode,m_nextSibling);
            else
                m_docFrag.appendChild(newNode);
        }else{
            boolean ok=true;
            short type=newNode.getNodeType();
            if(type==Node.TEXT_NODE){
                String data=newNode.getNodeValue();
                if((null!=data)&&(data.trim().length()>0)){
                    throw new org.xml.sax.SAXException(
                            XMLMessages.createXMLMessage(
                                    XMLErrorResources.ER_CANT_OUTPUT_TEXT_BEFORE_DOC,null));  //"Warning: can't output text before document element!  Ignoring...");
                }
                ok=false;
            }else if(type==Node.ELEMENT_NODE){
                if(m_doc.getDocumentElement()!=null){
                    ok=false;
                    throw new org.xml.sax.SAXException(
                            XMLMessages.createXMLMessage(
                                    XMLErrorResources.ER_CANT_HAVE_MORE_THAN_ONE_ROOT,null));  //"Can't have more than one root on a DOM!");
                }
            }
            if(ok){
                if(m_nextSibling!=null)
                    m_doc.insertBefore(newNode,m_nextSibling);
                else
                    m_doc.appendChild(newNode);
            }
        }
    }

    public void endElement(String ns,String localName,String name)
            throws org.xml.sax.SAXException{
        m_elemStack.pop();
        m_currentNode=m_elemStack.isEmpty()?null:(Node)m_elemStack.peek();
    }

    public void characters(char ch[],int start,int length) throws org.xml.sax.SAXException{
        if(isOutsideDocElem()
                &&XMLCharacterRecognizer.isWhiteSpace(ch,start,length))
            return;  // avoid DOM006 Hierarchy request error
        if(m_inCData){
            cdata(ch,start,length);
            return;
        }
        String s=new String(ch,start,length);
        Node childNode;
        childNode=m_currentNode!=null?m_currentNode.getLastChild():null;
        if(childNode!=null&&childNode.getNodeType()==Node.TEXT_NODE){
            ((Text)childNode).appendData(s);
        }else{
            Text text=m_doc.createTextNode(s);
            append(text);
        }
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws org.xml.sax.SAXException{
        if(isOutsideDocElem())
            return;  // avoid DOM006 Hierarchy request error
        String s=new String(ch,start,length);
        append(m_doc.createTextNode(s));
    }

    public void processingInstruction(String target,String data)
            throws org.xml.sax.SAXException{
        append(m_doc.createProcessingInstruction(target,data));
    }

    public void skippedEntity(String name) throws org.xml.sax.SAXException{
    }

    private boolean isOutsideDocElem(){
        return (null==m_docFrag)&&m_elemStack.size()==0&&(null==m_currentNode||m_currentNode.getNodeType()==Node.DOCUMENT_NODE);
    }

    public void cdata(char ch[],int start,int length) throws org.xml.sax.SAXException{
        if(isOutsideDocElem()
                &&XMLCharacterRecognizer.isWhiteSpace(ch,start,length))
            return;  // avoid DOM006 Hierarchy request error
        String s=new String(ch,start,length);
        CDATASection section=(CDATASection)m_currentNode.getLastChild();
        section.appendData(s);
    }

    public void setIDAttribute(String id,Element elem){
        // Do nothing. This method is meant to be overiden.
    }

    public void charactersRaw(char ch[],int start,int length)
            throws org.xml.sax.SAXException{
        if(isOutsideDocElem()
                &&XMLCharacterRecognizer.isWhiteSpace(ch,start,length))
            return;  // avoid DOM006 Hierarchy request error
        String s=new String(ch,start,length);
        append(m_doc.createProcessingInstruction("xslt-next-is-raw",
                "formatter-to-dom"));
        append(m_doc.createTextNode(s));
    }

    public void entityReference(String name) throws org.xml.sax.SAXException{
        append(m_doc.createEntityReference(name));
    }

    public void startDTD(String name,String publicId,String systemId)
            throws org.xml.sax.SAXException{
        // Do nothing for now.
    }

    public void endDTD() throws org.xml.sax.SAXException{
        // Do nothing for now.
    }

    public void startEntity(String name) throws org.xml.sax.SAXException{
        // Almost certainly the wrong behavior...
        // entityReference(name);
    }

    public void endEntity(String name) throws org.xml.sax.SAXException{
    }

    public void startCDATA() throws org.xml.sax.SAXException{
        m_inCData=true;
        append(m_doc.createCDATASection(""));
    }

    public void endCDATA() throws org.xml.sax.SAXException{
        m_inCData=false;
    }

    public void comment(char ch[],int start,int length) throws org.xml.sax.SAXException{
        append(m_doc.createComment(new String(ch,start,length)));
    }
}
