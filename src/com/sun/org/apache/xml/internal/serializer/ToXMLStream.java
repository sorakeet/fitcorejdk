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
 * $Id: ToXMLStream.java,v 1.2.4.2 2005/09/15 12:01:25 suresh_emailid Exp $
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
 * $Id: ToXMLStream.java,v 1.2.4.2 2005/09/15 12:01:25 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import org.xml.sax.SAXException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public final class ToXMLStream extends ToStream{
    private static CharInfo m_xmlcharInfo=
//      new CharInfo(CharInfo.XML_ENTITIES_RESOURCE);
            CharInfo.getCharInfoInternal(CharInfo.XML_ENTITIES_RESOURCE,Method.XML);
    boolean m_cdataTagOpen=false;

    public ToXMLStream(){
        m_charInfo=m_xmlcharInfo;
        initCDATA();
        // initialize namespaces
        m_prefixMap=new NamespaceMappings();
    }

    public void CopyFrom(ToXMLStream xmlListener){
        m_writer=xmlListener.m_writer;
        // m_outputStream = xmlListener.m_outputStream;
        String encoding=xmlListener.getEncoding();
        setEncoding(encoding);
        setOmitXMLDeclaration(xmlListener.getOmitXMLDeclaration());
        m_ispreserve=xmlListener.m_ispreserve;
        m_preserves=xmlListener.m_preserves;
        m_isprevtext=xmlListener.m_isprevtext;
        m_doIndent=xmlListener.m_doIndent;
        setIndentAmount(xmlListener.getIndentAmount());
        m_startNewLine=xmlListener.m_startNewLine;
        m_needToOutputDocTypeDecl=xmlListener.m_needToOutputDocTypeDecl;
        setDoctypeSystem(xmlListener.getDoctypeSystem());
        setDoctypePublic(xmlListener.getDoctypePublic());
        setStandalone(xmlListener.getStandalone());
        setMediaType(xmlListener.getMediaType());
        m_maxCharacter=xmlListener.m_maxCharacter;
        m_encodingInfo=xmlListener.m_encodingInfo;
        m_spaceBeforeClose=xmlListener.m_spaceBeforeClose;
        m_cdataStartCalled=xmlListener.m_cdataStartCalled;
    }

    public void endDocument() throws SAXException{
        flushPending();
        if(m_doIndent&&!m_isprevtext){
            try{
                outputLineSep();
            }catch(IOException e){
                throw new SAXException(e);
            }
        }
        flushWriter();
        if(m_tracer!=null)
            super.fireEndDoc();
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(m_inEntityRef)
            return;
        flushPending();
        if(target.equals(Result.PI_DISABLE_OUTPUT_ESCAPING)){
            startNonEscaping();
        }else if(target.equals(Result.PI_ENABLE_OUTPUT_ESCAPING)){
            endNonEscaping();
        }else{
            try{
                if(m_elemContext.m_startTagOpen){
                    closeStartTag();
                    m_elemContext.m_startTagOpen=false;
                }else if(m_needToCallStartDocument)
                    startDocumentInternal();
                if(shouldIndent())
                    indent();
                final java.io.Writer writer=m_writer;
                writer.write("<?");
                writer.write(target);
                if(data.length()>0
                        &&!Character.isSpaceChar(data.charAt(0)))
                    writer.write(' ');
                int indexOfQLT=data.indexOf("?>");
                if(indexOfQLT>=0){
                    // See XSLT spec on error recovery of "?>" in PIs.
                    if(indexOfQLT>0){
                        writer.write(data.substring(0,indexOfQLT));
                    }
                    writer.write("? >"); // add space between.
                    if((indexOfQLT+2)<data.length()){
                        writer.write(data.substring(indexOfQLT+2));
                    }
                }else{
                    writer.write(data);
                }
                writer.write('?');
                writer.write('>');
                /**
                 * Before Xalan 1497, a newline char was printed out if not inside of an
                 * element. The whitespace is not significant is the output is standalone
                 */
                if(m_elemContext.m_currentElemDepth<=0&&m_isStandalone)
                    writer.write(m_lineSep,0,m_lineSepLen);
                /**
                 * Don't write out any indentation whitespace now,
                 * because there may be non-whitespace text after this.
                 *
                 * Simply mark that at this point if we do decide
                 * to indent that we should
                 * add a newline on the end of the current line before
                 * the indentation at the start of the next line.
                 */
                m_startNewLine=true;
            }catch(IOException e){
                throw new SAXException(e);
            }
        }
        if(m_tracer!=null)
            super.fireEscapingEvent(target,data);
    }

    public void startPreserving() throws SAXException{
        // Not sure this is really what we want.  -sb
        m_preserves.push(true);
        m_ispreserve=true;
    }

    public void endPreserving() throws SAXException{
        // Not sure this is really what we want.  -sb
        m_ispreserve=m_preserves.isEmpty()?false:m_preserves.pop();
    }

    public void addUniqueAttribute(String name,String value,int flags)
            throws SAXException{
        if(m_elemContext.m_startTagOpen){
            try{
                final String patchedName=patchName(name);
                final java.io.Writer writer=m_writer;
                if((flags&NO_BAD_CHARS)>0&&m_xmlcharInfo.onlyQuotAmpLtGt){
                    // "flags" has indicated that the characters
                    // '>'  '<'   '&'  and '"' are not in the value and
                    // m_htmlcharInfo has recorded that there are no other
                    // entities in the range 32 to 127 so we write out the
                    // value directly
                    writer.write(' ');
                    writer.write(patchedName);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write('"');
                }else{
                    writer.write(' ');
                    writer.write(patchedName);
                    writer.write("=\"");
                    writeAttrString(writer,value,this.getEncoding());
                    writer.write('"');
                }
            }catch(IOException e){
                throw new SAXException(e);
            }
        }
    }

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean xslAttribute)
            throws SAXException{
        if(m_elemContext.m_startTagOpen){
            boolean was_added=addAttributeAlways(uri,localName,rawName,type,value,xslAttribute);
            /**
             * We don't run this block of code if:
             * 1. The attribute value was only replaced (was_added is false).
             * 2. The attribute is from an xsl:attribute element (that is handled
             *    in the addAttributeAlways() call just above.
             * 3. The name starts with "xmlns", i.e. it is a namespace declaration.
             */
            if(was_added&&!xslAttribute&&!rawName.startsWith("xmlns")){
                String prefixUsed=
                        ensureAttributesNamespaceIsDeclared(
                                uri,
                                localName,
                                rawName);
                if(prefixUsed!=null
                        &&rawName!=null
                        &&!rawName.startsWith(prefixUsed)){
                    // use a different raw name, with the prefix used in the
                    // generated namespace declaration
                    rawName=prefixUsed+":"+localName;
                }
            }
            addAttributeAlways(uri,localName,rawName,type,value,xslAttribute);
        }else{
            /**
             * The startTag is closed, yet we are adding an attribute?
             *
             * Section: 7.1.3 Creating Attributes Adding an attribute to an
             * element after a PI (for example) has been added to it is an
             * error. The attributes can be ignored. The spec doesn't explicitly
             * say this is disallowed, as it does for child elements, but it
             * makes sense to have the same treatment.
             *
             * We choose to ignore the attribute which is added too late.
             */
            // Generate a warning of the ignored attributes
            // Create the warning message
            String msg=Utils.messages.createMessage(
                    MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,new Object[]{localName});
            try{
                // Prepare to issue the warning message
                Transformer tran=super.getTransformer();
                ErrorListener errHandler=tran.getErrorListener();
                // Issue the warning message
                if(null!=errHandler&&m_sourceLocator!=null)
                    errHandler.warning(new TransformerException(msg,m_sourceLocator));
                else
                    System.out.println(msg);
            }catch(Exception e){
            }
        }
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
        return;
    }

    public void entityReference(String name) throws SAXException{
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }
        try{
            if(shouldIndent())
                indent();
            final java.io.Writer writer=m_writer;
            writer.write('&');
            writer.write(name);
            writer.write(';');
        }catch(IOException e){
            throw new SAXException(e);
        }
        if(m_tracer!=null)
            super.fireEntityReference(name);
    }

    public void startDocumentInternal() throws SAXException{
        if(m_needToCallStartDocument){
            super.startDocumentInternal();
            m_needToCallStartDocument=false;
            if(m_inEntityRef)
                return;
            m_needToOutputDocTypeDecl=true;
            m_startNewLine=false;
            /** The call to getXMLVersion() might emit an error message
             * and we should emit this message regardless of if we are
             * writing out an XML header or not.
             */
            if(getOmitXMLDeclaration()==false){
                String encoding=Encodings.getMimeEncoding(getEncoding());
                String version=getVersion();
                if(version==null)
                    version="1.0";
                String standalone;
                if(m_standaloneWasSpecified){
                    standalone=" standalone=\""+getStandalone()+"\"";
                }else{
                    standalone="";
                }
                try{
                    final java.io.Writer writer=m_writer;
                    writer.write("<?xml version=\"");
                    writer.write(version);
                    writer.write("\" encoding=\"");
                    writer.write(encoding);
                    writer.write('\"');
                    writer.write(standalone);
                    writer.write("?>");
                    if(m_doIndent){
                        if(m_standaloneWasSpecified
                                ||getDoctypePublic()!=null
                                ||getDoctypeSystem()!=null
                                ||m_isStandalone){
                            // We almost never put a newline after the XML
                            // header because this XML could be used as
                            // an extenal general parsed entity
                            // and we don't know the context into which it
                            // will be used in the future.  Only when
                            // standalone, or a doctype system or public is
                            // specified are we free to insert a new line
                            // after the header.  Is it even worth bothering
                            // in these rare cases?
                            writer.write(m_lineSep,0,m_lineSepLen);
                        }
                    }
                }catch(IOException e){
                    throw new SAXException(e);
                }
            }
        }
    }

    public void endElement(String elemName) throws SAXException{
        endElement(null,null,elemName);
    }

    public boolean reset(){
        boolean wasReset=false;
        if(super.reset()){
            resetToXMLStream();
            wasReset=true;
        }
        return wasReset;
    }

    private void resetToXMLStream(){
        this.m_cdataTagOpen=false;
    }

    protected boolean pushNamespace(String prefix,String uri){
        try{
            if(m_prefixMap.pushNamespace(
                    prefix,uri,m_elemContext.m_currentElemDepth)){
                startPrefixMapping(prefix,uri);
                return true;
            }
        }catch(SAXException e){
            // falls through
        }
        return false;
    }

    private String getXMLVersion(){
        String xmlVersion=getVersion();
        if(xmlVersion==null||xmlVersion.equals(XMLVERSION10)){
            xmlVersion=XMLVERSION10;
        }else if(xmlVersion.equals(XMLVERSION11)){
            xmlVersion=XMLVERSION11;
        }else{
            String msg=Utils.messages.createMessage(
                    MsgKey.ER_XML_VERSION_NOT_SUPPORTED,new Object[]{xmlVersion});
            try{
                // Prepare to issue the warning message
                Transformer tran=super.getTransformer();
                ErrorListener errHandler=tran.getErrorListener();
                // Issue the warning message
                if(null!=errHandler&&m_sourceLocator!=null)
                    errHandler.warning(new TransformerException(msg,m_sourceLocator));
                else
                    System.out.println(msg);
            }catch(Exception e){
            }
            xmlVersion=XMLVERSION10;
        }
        return xmlVersion;
    }
}
