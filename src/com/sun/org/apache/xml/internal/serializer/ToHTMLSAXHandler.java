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
 * $Id: ToHTMLSAXHandler.java,v 1.3 2005/09/28 13:49:07 pvedula Exp $
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
 * $Id: ToHTMLSAXHandler.java,v 1.3 2005/09/28 13:49:07 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

public final class ToHTMLSAXHandler extends ToSAXHandler{
    protected boolean m_escapeSetting=true;
    private boolean m_dtdHandled=false;

    public ToHTMLSAXHandler(ContentHandler handler,String encoding){
        super(handler,encoding);
    }    public Properties getOutputFormat(){
        return null;
    }

    public ToHTMLSAXHandler(
            ContentHandler handler,
            LexicalHandler lex,
            String encoding){
        super(handler,lex,encoding);
    }    public OutputStream getOutputStream(){
        return null;
    }

    public void indent(int n) throws SAXException{
    }    public Writer getWriter(){
        return null;
    }

    public void serialize(Node node) throws IOException{
        return;
    }

    public boolean setEscaping(boolean escape) throws SAXException{
        boolean oldEscapeSetting=m_escapeSetting;
        m_escapeSetting=escape;
        if(escape){
            processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING,"");
        }else{
            processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING,"");
        }
        return oldEscapeSetting;
    }

    public void elementDecl(String name,String model) throws SAXException{
        return;
    }

    public void attributeDecl(
            String eName,
            String aName,
            String type,
            String valueDefault,
            String value)
            throws SAXException{
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
    }    public void setOutputFormat(Properties format){
    }

    public void externalEntityDecl(String arg0,String arg1,String arg2)
            throws SAXException{
    }    public void setOutputStream(OutputStream output){
    }

    public void setDocumentLocator(Locator arg0){
        super.setDocumentLocator(arg0);
    }    public void setWriter(Writer writer){
    }

    public void close(){
        return;
    }

    public void setIndent(boolean indent){
    }

    public void namespaceAfterStartElement(
            final String prefix,
            final String uri)
            throws SAXException{
        // hack for XSLTC with finding URI for default namespace
        if(m_elemContext.m_elementURI==null){
            String prefix1=getPrefixPart(m_elemContext.m_elementName);
            if(prefix1==null&&EMPTYSTRING.equals(prefix)){
                // the elements URI is not known yet, and it
                // doesn't have a prefix, and we are currently
                // setting the uri for prefix "", so we have
                // the uri for the element... lets remember it
                m_elemContext.m_elementURI=uri;
            }
        }
        startPrefixMapping(prefix,uri,false);
    }

    public void endDTD() throws SAXException{
    }

    public void startEntity(String arg0) throws SAXException{
    }

    public void startCDATA() throws SAXException{
    }

    public void endCDATA() throws SAXException{
        return;
    }

    public void comment(char[] ch,int start,int length) throws SAXException{
        flushPending();
        if(m_lexHandler!=null)
            m_lexHandler.comment(ch,start,length);
        // time to fire off comment event
        if(m_tracer!=null)
            super.fireCommentEvent(ch,start,length);
        return;
    }

    public void endDocument() throws SAXException{
        flushPending();
        // Close output document
        m_saxHandler.endDocument();
        if(m_tracer!=null)
            super.fireEndDoc();
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        startPrefixMapping(prefix,uri,true);
    }

    public void endPrefixMapping(String prefix) throws SAXException{
    }

    public void endElement(String uri,String localName,String qName)
            throws SAXException{
        flushPending();
        m_saxHandler.endElement(uri,localName,qName);
        // time to fire off endElement event
        if(m_tracer!=null)
            super.fireEndElem(qName);
    }

    public void characters(char[] ch,int off,int len) throws SAXException{
        flushPending();
        m_saxHandler.characters(ch,off,len);
        // time to fire off characters event
        if(m_tracer!=null)
            super.fireCharEvent(ch,off,len);
    }

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
    }

    public void skippedEntity(String arg0) throws SAXException{
    }

    public void characters(final String chars) throws SAXException{
        final int length=chars.length();
        if(length>m_charsBuff.length){
            m_charsBuff=new char[length*2+1];
        }
        chars.getChars(0,length,m_charsBuff,0);
        this.characters(m_charsBuff,0,length);
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        flushPending();
        m_saxHandler.processingInstruction(target,data);
        // time to fire off processing instruction event
        if(m_tracer!=null)
            super.fireEscapingEvent(target,data);
    }

    protected void closeStartTag() throws SAXException{
        m_elemContext.m_startTagOpen=false;
        // Now is time to send the startElement event
        m_saxHandler.startElement(
                EMPTYSTRING,
                m_elemContext.m_elementName,
                m_elemContext.m_elementName,
                m_attributes);
        m_attributes.clear();
    }

    public void startElement(
            String namespaceURI,
            String localName,
            String qName,
            Attributes atts)
            throws SAXException{
        flushPending();
        super.startElement(namespaceURI,localName,qName,atts);
        m_saxHandler.startElement(namespaceURI,localName,qName,atts);
        m_elemContext.m_startTagOpen=false;
    }

    public void flushPending() throws SAXException{
        if(m_needToCallStartDocument){
            startDocumentInternal();
            m_needToCallStartDocument=false;
        }
        // Close any open element
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }
    }

    public void startElement(
            String elementNamespaceURI,
            String elementLocalName,
            String elementName) throws SAXException{
        super.startElement(elementNamespaceURI,elementLocalName,elementName);
        flushPending();
        // Handle document type declaration (for first element only)
        if(!m_dtdHandled){
            String doctypeSystem=getDoctypeSystem();
            String doctypePublic=getDoctypePublic();
            if((doctypeSystem!=null)||(doctypePublic!=null)){
                if(m_lexHandler!=null)
                    m_lexHandler.startDTD(
                            elementName,
                            doctypePublic,
                            doctypeSystem);
            }
            m_dtdHandled=true;
        }
        m_elemContext=m_elemContext.push(elementNamespaceURI,elementLocalName,elementName);
    }

    public void startElement(String elementName) throws SAXException{
        this.startElement(null,null,elementName);
    }

    public boolean reset(){
        boolean wasReset=false;
        if(super.reset()){
            resetToHTMLSAXHandler();
            wasReset=true;
        }
        return wasReset;
    }

    private void resetToHTMLSAXHandler(){
        this.m_escapeSetting=true;
    }

    public void endElement(String elementName) throws SAXException{
        flushPending();
        m_saxHandler.endElement(EMPTYSTRING,elementName,elementName);
        // time to fire off endElement event
        if(m_tracer!=null)
            super.fireEndElem(elementName);
    }

    public boolean startPrefixMapping(
            String prefix,
            String uri,
            boolean shouldFlush)
            throws SAXException{
        // no namespace support for HTML
        if(shouldFlush)
            flushPending();
        m_saxHandler.startPrefixMapping(prefix,uri);
        return false;
    }












}
