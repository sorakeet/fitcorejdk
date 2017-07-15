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
 * $Id: ToSAXHandler.java,v 1.2.4.1 2005/09/22 11:03:15 pvedula Exp $
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
 * $Id: ToSAXHandler.java,v 1.2.4.1 2005/09/22 11:03:15 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import java.util.Vector;

public abstract class ToSAXHandler extends SerializerBase{
    protected ContentHandler m_saxHandler;
    protected LexicalHandler m_lexHandler;
    protected TransformStateSetter m_state=null;
    private boolean m_shouldGenerateNSAttribute=true;
    public ToSAXHandler(){
    }
    public ToSAXHandler(
            ContentHandler hdlr,
            LexicalHandler lex,
            String encoding){
        setContentHandler(hdlr);
        setLexHandler(lex);
        setEncoding(encoding);
    }

    public void setLexHandler(LexicalHandler _lexHandler){
        this.m_lexHandler=_lexHandler;
    }

    public void setContentHandler(ContentHandler _saxHandler){
        this.m_saxHandler=_saxHandler;
        if(m_lexHandler==null&&_saxHandler instanceof LexicalHandler){
            // we are not overwriting an existing LexicalHandler, and _saxHandler
            // is also implements LexicalHandler, so lets use it
            m_lexHandler=(LexicalHandler)_saxHandler;
        }
    }

    public void flushPending() throws SAXException{
        if(m_needToCallStartDocument){
            startDocumentInternal();
            m_needToCallStartDocument=false;
        }
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }
        if(m_cdataTagOpen){
            closeCDATA();
            m_cdataTagOpen=false;
        }
    }

    protected void closeStartTag() throws SAXException{
    }

    protected void closeCDATA() throws SAXException{
        // Redefined in SAXXMLOutput
    }

    public ToSAXHandler(ContentHandler handler,String encoding){
        setContentHandler(handler);
        setEncoding(encoding);
    }

    public void startDTD(String arg0,String arg1,String arg2)
            throws SAXException{
        // do nothing for now
    }

    public void comment(String comment) throws SAXException{
        flushPending();
        // Ignore if a lexical handler has not been set
        if(m_lexHandler!=null){
            final int len=comment.length();
            if(len>m_charsBuff.length){
                m_charsBuff=new char[len*2+1];
            }
            comment.getChars(0,len,m_charsBuff,0);
            m_lexHandler.comment(m_charsBuff,0,len);
            // time to fire off comment event
            if(m_tracer!=null)
                super.fireCommentEvent(m_charsBuff,0,len);
        }
    }

    public void characters(org.w3c.dom.Node node)
            throws SAXException{
        // remember the current node
        if(m_state!=null){
            m_state.setCurrentNode(node);
        }
        // Get the node's value as a String and use that String as if
        // it were an input character notification.
        String data=node.getNodeValue();
        if(data!=null){
            this.characters(data);
        }
    }

    public void characters(String characters) throws SAXException{
        final int len=characters.length();
        if(len>m_charsBuff.length){
            m_charsBuff=new char[len*2+1];
        }
        characters.getChars(0,len,m_charsBuff,0);
        characters(m_charsBuff,0,len);
    }

    public void startElement(String uri,String localName,String qName)
            throws SAXException{
        if(m_state!=null){
            m_state.resetState(getTransformer());
        }
        // fire off the start element event
        if(m_tracer!=null)
            super.fireStartElem(qName);
    }

    public void startElement(String qName) throws SAXException{
        if(m_state!=null){
            m_state.resetState(getTransformer());
        }
        // fire off the start element event
        if(m_tracer!=null)
            super.fireStartElem(qName);
    }

    public void addUniqueAttribute(String qName,String value,int flags)
            throws SAXException{
        addAttribute(qName,value);
    }

    public void error(SAXParseException exc) throws SAXException{
        super.error(exc);
        if(m_saxHandler instanceof ErrorHandler)
            ((ErrorHandler)m_saxHandler).error(exc);
    }

    public void fatalError(SAXParseException exc) throws SAXException{
        super.fatalError(exc);
        m_needToCallStartDocument=false;
        if(m_saxHandler instanceof ErrorHandler){
            ((ErrorHandler)m_saxHandler).fatalError(exc);
        }
    }

    public void warning(SAXParseException exc) throws SAXException{
        super.warning(exc);
        if(m_saxHandler instanceof ErrorHandler)
            ((ErrorHandler)m_saxHandler).warning(exc);
    }

    protected void startDocumentInternal() throws SAXException{
        if(m_needToCallStartDocument){
            super.startDocumentInternal();
            m_saxHandler.startDocument();
            m_needToCallStartDocument=false;
        }
    }

    public boolean reset(){
        boolean wasReset=false;
        if(super.reset()){
            resetToSAXHandler();
            wasReset=true;
        }
        return wasReset;
    }

    private void resetToSAXHandler(){
        this.m_lexHandler=null;
        this.m_saxHandler=null;
        this.m_state=null;
        this.m_shouldGenerateNSAttribute=false;
    }

    public void startElement(
            String arg0,
            String arg1,
            String arg2,
            Attributes arg3)
            throws SAXException{
        if(m_state!=null){
            m_state.resetState(getTransformer());
        }
        // fire off the start element event
        if(m_tracer!=null)
            super.fireStartElem(arg2);
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        // Redefined in SAXXMLOutput
    }

    public void setCdataSectionElements(Vector URI_and_localNames){
        // do nothing
    }

    boolean getShouldOutputNSAttr(){
        return m_shouldGenerateNSAttribute;
    }

    public void setShouldOutputNSAttr(boolean doOutputNSAttr){
        m_shouldGenerateNSAttribute=doOutputNSAttr;
    }

    public void setTransformState(TransformStateSetter ts){
        this.m_state=ts;
    }
}
