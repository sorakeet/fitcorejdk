/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ToTextSAXHandler.java,v 1.3 2005/09/28 13:49:08 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ToTextSAXHandler.java,v 1.3 2005/09/28 13:49:08 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

public final class ToTextSAXHandler extends ToSAXHandler{
    public ToTextSAXHandler(ContentHandler hdlr,LexicalHandler lex,String encoding){
        super(hdlr,lex,encoding);
    }

    public ToTextSAXHandler(ContentHandler handler,String encoding){
        super(handler,encoding);
    }

    public void endElement(String elemName) throws SAXException{
        if(m_tracer!=null)
            super.fireEndElem(elemName);
    }

    public boolean startPrefixMapping(
            String prefix,
            String uri,
            boolean shouldFlush)
            throws SAXException{
        // no namespace support for HTML
        return false;
    }

    public void indent(int n) throws SAXException{
    }

    public void serialize(Node node) throws IOException{
    }

    public boolean setEscaping(boolean escape){
        return false;
    }    public Properties getOutputFormat(){
        return null;
    }

    public void elementDecl(String arg0,String arg1) throws SAXException{
    }    public OutputStream getOutputStream(){
        return null;
    }

    public void attributeDecl(
            String arg0,
            String arg1,
            String arg2,
            String arg3,
            String arg4)
            throws SAXException{
    }    public Writer getWriter(){
        return null;
    }

    public void internalEntityDecl(String arg0,String arg1)
            throws SAXException{
    }

    public void externalEntityDecl(String arg0,String arg1,String arg2)
            throws SAXException{
    }

    public void setDocumentLocator(Locator arg0){
        super.setDocumentLocator(arg0);
    }

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean XSLAttribute){
    }

    public void addAttribute(String name,String value){
        // do nothing
    }

    public void setIndent(boolean indent){
    }    public void setOutputFormat(Properties format){
    }

    public void namespaceAfterStartElement(
            final String prefix,
            final String uri)
            throws SAXException{
        // no namespace support for HTML
    }    public void setOutputStream(OutputStream output){
    }

    public void endDTD() throws SAXException{
    }    public void setWriter(Writer writer){
    }

    public void startEntity(String arg0) throws SAXException{
    }

    public void startCDATA() throws SAXException{
    }

    public void endCDATA() throws SAXException{
    }

    public void comment(char ch[],int start,int length)
            throws SAXException{
        if(m_tracer!=null)
            super.fireCommentEvent(ch,start,length);
    }

    public void endDocument() throws SAXException{
        flushPending();
        m_saxHandler.endDocument();
        if(m_tracer!=null)
            super.fireEndDoc();
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        // no namespace support for HTML
    }

    public void endPrefixMapping(String arg0) throws SAXException{
    }

    public void endElement(String arg0,String arg1,String arg2)
            throws SAXException{
        if(m_tracer!=null)
            super.fireEndElem(arg2);
    }

    public void characters(char[] characters,int offset,int length)
            throws SAXException{
        m_saxHandler.characters(characters,offset,length);
        // time to fire off characters event
        if(m_tracer!=null)
            super.fireCharEvent(characters,offset,length);
    }

    public void ignorableWhitespace(char[] arg0,int arg1,int arg2)
            throws SAXException{
    }

    public void skippedEntity(String arg0) throws SAXException{
    }

    public void characters(String characters)
            throws SAXException{
        final int length=characters.length();
        if(length>m_charsBuff.length){
            m_charsBuff=new char[length*2+1];
        }
        characters.getChars(0,length,m_charsBuff,0);
        m_saxHandler.characters(m_charsBuff,0,length);
    }

    public void comment(String data) throws SAXException{
        final int length=data.length();
        if(length>m_charsBuff.length){
            m_charsBuff=new char[length*2+1];
        }
        data.getChars(0,length,m_charsBuff,0);
        comment(m_charsBuff,0,length);
    }

    public void processingInstruction(String arg0,String arg1)
            throws SAXException{
        if(m_tracer!=null)
            super.fireEscapingEvent(arg0,arg1);
    }

    public void startElement(
            String arg0,
            String arg1,
            String arg2,
            Attributes arg3)
            throws SAXException{
        flushPending();
        super.startElement(arg0,arg1,arg2,arg3);
    }

    public void startElement(
            String elementNamespaceURI,
            String elementLocalName,
            String elementName) throws SAXException{
        super.startElement(elementNamespaceURI,elementLocalName,elementName);
    }

    public void startElement(
            String elementName) throws SAXException{
        super.startElement(elementName);
    }

    public boolean reset(){
        return false;
    }












}
