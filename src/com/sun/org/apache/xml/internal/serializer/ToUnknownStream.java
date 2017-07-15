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
 * $Id: ToUnknownStream.java,v 1.3 2005/09/28 13:49:08 pvedula Exp $
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
 * $Id: ToUnknownStream.java,v 1.3 2005/09/28 13:49:08 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import java.util.Vector;

public final class ToUnknownStream extends SerializerBase{
    private static final String EMPTYSTRING="";
    private SerializationHandler m_handler;
    private boolean m_wrapped_handler_not_initialized=false;
    private String m_firstElementPrefix;
    private String m_firstElementName;
    private String m_firstElementURI;
    private String m_firstElementLocalName=null;
    private boolean m_firstTagNotEmitted=true;
    private Vector m_namespaceURI=null;
    private Vector m_namespacePrefix=null;
    private boolean m_needToCallStartDocument=false;
    private boolean m_setVersion_called=false;
    private boolean m_setDoctypeSystem_called=false;
    private boolean m_setDoctypePublic_called=false;
    private boolean m_setMediaType_called=false;

    public ToUnknownStream(){
        m_handler=new ToXMLStream();
    }

    public void characters(String chars) throws SAXException{
        final int length=chars.length();
        if(length>m_charsBuff.length){
            m_charsBuff=new char[length*2+1];
        }
        chars.getChars(0,length,m_charsBuff,0);
        this.characters(m_charsBuff,0,length);
    }

    public void endElement(String elementName) throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.endElement(elementName);
    }

    public void startElement(String namespaceURI,String localName,String qName) throws SAXException{
        this.startElement(namespaceURI,localName,qName,null);
    }    public Properties getOutputFormat(){
        return m_handler.getOutputFormat();
    }

    public void startElement(String qName) throws SAXException{
        this.startElement(null,null,qName,null);
    }    public OutputStream getOutputStream(){
        return m_handler.getOutputStream();
    }

    public boolean startPrefixMapping(String prefix,String uri,boolean shouldFlush)
            throws SAXException{
        boolean pushed=false;
        if(m_firstTagNotEmitted){
            if(m_firstElementName!=null&&shouldFlush){
                /** we've already seen a startElement, and this is a prefix mapping
                 * for the up coming element, so flush the old element
                 * then send this event on its way.
                 */
                flush();
                pushed=m_handler.startPrefixMapping(prefix,uri,shouldFlush);
            }else{
                if(m_namespacePrefix==null){
                    m_namespacePrefix=new Vector();
                    m_namespaceURI=new Vector();
                }
                m_namespacePrefix.addElement(prefix);
                m_namespaceURI.addElement(uri);
                if(m_firstElementURI==null){
                    if(prefix.equals(m_firstElementPrefix))
                        m_firstElementURI=uri;
                }
            }
        }else{
            pushed=m_handler.startPrefixMapping(prefix,uri,shouldFlush);
        }
        return pushed;
    }    public Writer getWriter(){
        return m_handler.getWriter();
    }

    public void addUniqueAttribute(String rawName,String value,int flags)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.addUniqueAttribute(rawName,value,flags);
    }    public boolean reset(){
        return m_handler.reset();
    }

    public void comment(String comment) throws SAXException{
        if(m_firstTagNotEmitted&&m_firstElementName!=null){
            emitFirstTag();
        }else if(m_needToCallStartDocument){
            m_handler.startDocument();
            m_needToCallStartDocument=false;
        }
        m_handler.comment(comment);
    }

    public void setDocumentLocator(Locator locator){
        super.setDocumentLocator(locator);
        m_handler.setDocumentLocator(locator);
    }

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean XSLAttribute)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.addAttribute(uri,localName,rawName,type,value,XSLAttribute);
    }    public void setOutputFormat(Properties format){
        m_handler.setOutputFormat(format);
    }

    public void addAttribute(String rawName,String value){
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.addAttribute(rawName,value);
    }    public void setOutputStream(OutputStream output){
        m_handler.setOutputStream(output);
    }

    public void addAttributes(Attributes atts) throws SAXException{
        m_handler.addAttributes(atts);
    }    public void setWriter(Writer writer){
        m_handler.setWriter(writer);
    }

    public ContentHandler asContentHandler() throws IOException{
        /** don't return the real handler ( m_handler ) because
         * that would expose the real handler to the outside.
         * Keep m_handler private so it can be internally swapped
         * to an HTML handler.
         */
        return this;
    }    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value)
            throws SAXException{
        addAttribute(uri,localName,rawName,type,value,false);
    }

    public void endEntity(String name) throws SAXException{
        if(m_firstTagNotEmitted){
            emitFirstTag();
        }
        m_handler.endEntity(name);
    }

    public void close(){
        m_handler.close();
    }

    public String getEncoding(){
        return m_handler.getEncoding();
    }

    public void setEncoding(String encoding){
        m_handler.setEncoding(encoding);
    }

    public void elementDecl(String arg0,String arg1) throws SAXException{
        if(m_firstTagNotEmitted){
            emitFirstTag();
        }
        m_handler.elementDecl(arg0,arg1);
    }

    public void attributeDecl(
            String arg0,
            String arg1,
            String arg2,
            String arg3,
            String arg4)
            throws SAXException{
        m_handler.attributeDecl(arg0,arg1,arg2,arg3,arg4);
    }

    public void internalEntityDecl(String arg0,String arg1)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.internalEntityDecl(arg0,arg1);
    }    public void namespaceAfterStartElement(String prefix,String uri)
            throws SAXException{
        // hack for XSLTC with finding URI for default namespace
        if(m_firstTagNotEmitted&&m_firstElementURI==null&&m_firstElementName!=null){
            String prefix1=getPrefixPart(m_firstElementName);
            if(prefix1==null&&EMPTYSTRING.equals(prefix)){
                // the elements URI is not known yet, and it
                // doesn't have a prefix, and we are currently
                // setting the uri for prefix "", so we have
                // the uri for the element... lets remember it
                m_firstElementURI=uri;
            }
        }
        startPrefixMapping(prefix,uri,false);
    }

    public void externalEntityDecl(
            String name,
            String publicId,
            String systemId)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.externalEntityDecl(name,publicId,systemId);
    }

    public void endDocument() throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.endDocument();
    }    public void setVersion(String version){
        m_handler.setVersion(version);
        // Cache call to setVersion()
        //       super.setVersion(version);
        m_setVersion_called=true;
    }

    public void startPrefixMapping(String prefix,String uri) throws SAXException{
        this.startPrefixMapping(prefix,uri,true);
    }    public void startDocument() throws SAXException{
        m_needToCallStartDocument=true;
    }

    public void endPrefixMapping(String prefix) throws SAXException{
        m_handler.endPrefixMapping(prefix);
    }

    public void startElement(
            String namespaceURI,
            String localName,
            String elementName,
            Attributes atts) throws SAXException{
        if(m_needToCallSetDocumentInfo){
            super.setDocumentInfo();
            m_needToCallSetDocumentInfo=false;
        }
        /** we are notified of the start of an element */
        if(m_firstTagNotEmitted){
            /** we have not yet sent the first element on its way */
            if(m_firstElementName!=null){
                /** this is not the first element, but a later one.
                 * But we have the old element pending, so flush it out,
                 * then send this one on its way.
                 */
                flush();
                m_handler.startElement(namespaceURI,localName,elementName,atts);
            }else{
                /** this is the very first element that we have seen,
                 * so save it for flushing later.  We may yet get to know its
                 * URI due to added attributes.
                 */
                m_wrapped_handler_not_initialized=true;
                m_firstElementName=elementName;
                // null if not known
                m_firstElementPrefix=getPrefixPartUnknown(elementName);
                // null if not known
                m_firstElementURI=namespaceURI;
                // null if not known
                m_firstElementLocalName=localName;
                if(m_tracer!=null)
                    firePseudoElement(elementName);
                /** we don't want to call our own addAttributes, which
                 * merely delegates to the wrapped handler, but we want to
                 * add these attributes to m_attributes. So me must call super.
                 * addAttributes() In this case m_attributes is only used for the
                 * first element, after that this class totally delegates to the
                 * wrapped handler which is either XML or HTML.
                 */
                if(atts!=null)
                    super.addAttributes(atts);
                // if there are attributes, then lets make the flush()
                // call the startElement on the handler and send the
                // attributes on their way.
                if(atts!=null)
                    flush();
            }
        }else{
            // this is not the first element, but a later one, so just
            // send it on its way.
            m_handler.startElement(namespaceURI,localName,elementName,atts);
        }
    }

    public void endElement(String namespaceURI,String localName,String qName)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
            if(namespaceURI==null&&m_firstElementURI!=null)
                namespaceURI=m_firstElementURI;
            if(localName==null&&m_firstElementLocalName!=null)
                localName=m_firstElementLocalName;
        }
        m_handler.endElement(namespaceURI,localName,qName);
    }

    public void characters(char[] characters,int offset,int length)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.characters(characters,offset,length);
    }

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.ignorableWhitespace(ch,start,length);
    }    public String getDoctypePublic(){
        return m_handler.getDoctypePublic();
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.processingInstruction(target,data);
    }    public String getDoctypeSystem(){
        return m_handler.getDoctypeSystem();
    }

    public void skippedEntity(String name) throws SAXException{
        m_handler.skippedEntity(name);
    }

    private String getPrefixPartUnknown(String qname){
        final int index=qname.indexOf(':');
        return (index>0)?qname.substring(0,index):EMPTYSTRING;
    }    public boolean getIndent(){
        return m_handler.getIndent();
    }

    protected void firePseudoElement(String elementName){
        if(m_tracer!=null){
            StringBuffer sb=new StringBuffer();
            sb.append('<');
            sb.append(elementName);
            // convert the StringBuffer to a char array and
            // emit the trace event that these characters "might"
            // be written
            char ch[]=sb.toString().toCharArray();
            m_tracer.fireGenerateEvent(
                    SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                    ch,
                    0,
                    ch.length);
        }
    }    public int getIndentAmount(){
        return m_handler.getIndentAmount();
    }

    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        m_handler.startDTD(name,publicId,systemId);
    }    public String getMediaType(){
        return m_handler.getMediaType();
    }

    public void endDTD() throws SAXException{
        m_handler.endDTD();
    }    public boolean getOmitXMLDeclaration(){
        return m_handler.getOmitXMLDeclaration();
    }

    public void startEntity(String name) throws SAXException{
        m_handler.startEntity(name);
    }    public String getStandalone(){
        return m_handler.getStandalone();
    }

    public void startCDATA() throws SAXException{
        m_handler.startCDATA();
    }    public String getVersion(){
        return m_handler.getVersion();
    }

    public void endCDATA() throws SAXException{
        m_handler.endCDATA();
    }    public void setDoctype(String system,String pub){
        m_handler.setDoctypePublic(pub);
        m_handler.setDoctypeSystem(system);
    }

    public void comment(char[] ch,int start,int length) throws SAXException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.comment(ch,start,length);
    }    public void setDoctypePublic(String doctype){
        m_handler.setDoctypePublic(doctype);
        m_setDoctypePublic_called=true;
    }

    public void setCdataSectionElements(Vector URI_and_localNames){
        m_handler.setCdataSectionElements(URI_and_localNames);
    }    public void setDoctypeSystem(String doctype){
        m_handler.setDoctypeSystem(doctype);
        m_setDoctypeSystem_called=true;
    }

    public void setContentHandler(ContentHandler ch){
        m_handler.setContentHandler(ch);
    }

    public void serialize(Node node) throws IOException{
        if(m_firstTagNotEmitted){
            flush();
        }
        m_handler.serialize(node);
    }    public void setIndent(boolean indent){
        m_handler.setIndent(indent);
    }

    public boolean setEscaping(boolean escape) throws SAXException{
        return m_handler.setEscaping(escape);
    }    public void setIndentAmount(int value){
        m_handler.setIndentAmount(value);
    }

    public void flushPending() throws SAXException{
        flush();
        m_handler.flushPending();
    }    public void setMediaType(String mediaType){
        m_handler.setMediaType(mediaType);
        m_setMediaType_called=true;
    }

    private void flush(){
        try{
            if(m_firstTagNotEmitted){
                emitFirstTag();
            }
            if(m_needToCallStartDocument){
                m_handler.startDocument();
                m_needToCallStartDocument=false;
            }
        }catch(SAXException e){
            throw new RuntimeException(e.toString());
        }
    }    public void setOmitXMLDeclaration(boolean b){
        m_handler.setOmitXMLDeclaration(b);
    }

    private void emitFirstTag() throws SAXException{
        if(m_firstElementName!=null){
            if(m_wrapped_handler_not_initialized){
                initStreamOutput();
                m_wrapped_handler_not_initialized=false;
            }
            // Output first tag
            m_handler.startElement(m_firstElementURI,null,m_firstElementName,m_attributes);
            // don't need the collected attributes of the first element anymore.
            m_attributes=null;
            // Output namespaces of first tag
            if(m_namespacePrefix!=null){
                final int n=m_namespacePrefix.size();
                for(int i=0;i<n;i++){
                    final String prefix=
                            (String)m_namespacePrefix.elementAt(i);
                    final String uri=(String)m_namespaceURI.elementAt(i);
                    m_handler.startPrefixMapping(prefix,uri,false);
                }
                m_namespacePrefix=null;
                m_namespaceURI=null;
            }
            m_firstTagNotEmitted=false;
        }
    }    public void setStandalone(String standalone){
        m_handler.setStandalone(standalone);
    }

    private void initStreamOutput() throws SAXException{
        // Try to rule out if this is an not to be an HTML document based on prefix
        boolean firstElementIsHTML=isFirstElemHTML();
        if(firstElementIsHTML){
            // create an HTML output handler, and initialize it
            // keep a reference to the old handler, ... it will soon be gone
            SerializationHandler oldHandler=m_handler;
            /** We have to make sure we get an output properties with the proper
             * defaults for the HTML method.  The easiest way to do this is to
             * have the OutputProperties class do it.
             */
            Properties htmlProperties=
                    OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
            Serializer serializer=
                    SerializerFactory.getSerializer(htmlProperties);
            // The factory should be returning a ToStream
            // Don't know what to do if it doesn't
            // i.e. the user has over-ridden the content-handler property
            // for html
            m_handler=(SerializationHandler)serializer;
            //m_handler = new ToHTMLStream();
            Writer writer=oldHandler.getWriter();
            if(null!=writer)
                m_handler.setWriter(writer);
            else{
                OutputStream os=oldHandler.getOutputStream();
                if(null!=os)
                    m_handler.setOutputStream(os);
            }
            // need to copy things from the old handler to the new one here
            //            if (_setVersion_called)
            //            {
            m_handler.setVersion(oldHandler.getVersion());
            //            }
            //            if (_setDoctypeSystem_called)
            //            {
            m_handler.setDoctypeSystem(oldHandler.getDoctypeSystem());
            //            }
            //            if (_setDoctypePublic_called)
            //            {
            m_handler.setDoctypePublic(oldHandler.getDoctypePublic());
            //            }
            //            if (_setMediaType_called)
            //            {
            m_handler.setMediaType(oldHandler.getMediaType());
            //            }
            m_handler.setTransformer(oldHandler.getTransformer());
        }
        /** Now that we have a real wrapped handler (XML or HTML) lets
         * pass any cached calls to it
         */
        // Call startDocument() if necessary
        if(m_needToCallStartDocument){
            m_handler.startDocument();
            m_needToCallStartDocument=false;
        }
        // the wrapped handler is now fully initialized
        m_wrapped_handler_not_initialized=false;
    }

    private boolean isFirstElemHTML(){
        boolean isHTML;
        // is the first tag html, not considering the prefix ?
        isHTML=
                getLocalNameUnknown(m_firstElementName).equalsIgnoreCase("html");
        // Try to rule out if this is not to be an HTML document based on URI
        if(isHTML
                &&m_firstElementURI!=null
                &&!EMPTYSTRING.equals(m_firstElementURI)){
            // the <html> element has a non-trivial namespace
            isHTML=false;
        }
        // Try to rule out if this is an not to be an HTML document based on prefix
        if(isHTML&&m_namespacePrefix!=null){
            /** the first element has a name of "html", but lets check the prefix.
             * If the prefix points to a namespace with a URL that is not ""
             * then the doecument doesn't start with an <html> tag, and isn't html
             */
            final int max=m_namespacePrefix.size();
            for(int i=0;i<max;i++){
                final String prefix=(String)m_namespacePrefix.elementAt(i);
                final String uri=(String)m_namespaceURI.elementAt(i);
                if(m_firstElementPrefix!=null
                        &&m_firstElementPrefix.equals(prefix)
                        &&!EMPTYSTRING.equals(uri)){
                    // The first element has a prefix, so it can't be <html>
                    isHTML=false;
                    break;
                }
            }
        }
        return isHTML;
    }

    private String getLocalNameUnknown(String value){
        int idx=value.lastIndexOf(':');
        if(idx>=0)
            value=value.substring(idx+1);
        idx=value.lastIndexOf('@');
        if(idx>=0)
            value=value.substring(idx+1);
        return (value);
    }











































    public DOMSerializer asDOMSerializer() throws IOException{
        return m_handler.asDOMSerializer();
    }





    public NamespaceMappings getNamespaceMappings(){
        NamespaceMappings mappings=null;
        if(m_handler!=null){
            mappings=m_handler.getNamespaceMappings();
        }
        return mappings;
    }





    public String getPrefix(String namespaceURI){
        return m_handler.getPrefix(namespaceURI);
    }

    public void entityReference(String entityName) throws SAXException{
        m_handler.entityReference(entityName);
    }

    public String getNamespaceURI(String qname,boolean isElement){
        return m_handler.getNamespaceURI(qname,isElement);
    }

    public String getNamespaceURIFromPrefix(String prefix){
        return m_handler.getNamespaceURIFromPrefix(prefix);
    }

    public void setTransformer(Transformer t){
        m_handler.setTransformer(t);
        if((t instanceof SerializerTrace)&&
                (((SerializerTrace)t).hasTraceListeners())){
            m_tracer=(SerializerTrace)t;
        }else{
            m_tracer=null;
        }
    }

    public Transformer getTransformer(){
        return m_handler.getTransformer();
    }



    public void setSourceLocator(SourceLocator locator){
        m_handler.setSourceLocator(locator);
    }


}
