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
 * $Id: ToStream.java,v 1.4 2005/11/10 06:43:26 suresh_emailid Exp $
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
 * $Id: ToStream.java,v 1.4 2005/11/10 06:43:26 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
//import com.sun.media.sound.IESecurity;

abstract public class ToStream extends SerializerBase{
    private static final String COMMENT_BEGIN="<!--";
    private static final String COMMENT_END="-->";
    protected BoolStack m_disableOutputEscapingStates=new BoolStack();
    protected BoolStack m_preserves=new BoolStack();
    protected boolean m_ispreserve=false;
    protected boolean m_isprevtext=false;
    protected int m_maxCharacter=Encodings.getLastPrintable();
    protected char[] m_lineSep=
            SecuritySupport.getSystemProperty("line.separator").toCharArray();
    protected boolean m_lineSepUse=true;
    protected int m_lineSepLen=m_lineSep.length;
    protected CharInfo m_charInfo;
    protected boolean m_spaceBeforeClose=false;
    protected boolean m_inDoctype=false;
    protected Properties m_format;
    protected boolean m_cdataStartCalled=false;
    EncodingInfo m_encodingInfo=new EncodingInfo(null,null);
    java.lang.reflect.Method m_canConvertMeth;
    boolean m_triedToGetConverter=false;
    Object m_charToByteConverter=null;
    boolean m_shouldFlush=true;
    boolean m_startNewLine;
    boolean m_isUTF8=false;
    private boolean m_expandDTDEntities=true;
    private boolean m_escaping=true;

    public ToStream(){
    }

    static final boolean isUTF16Surrogate(char c){
        return (c&0xFC00)==0xD800;
    }

    protected final void flushWriter() throws SAXException{
        final Writer writer=m_writer;
        if(null!=writer){
            try{
                if(writer instanceof WriterToUTF8Buffered){
                    if(m_shouldFlush)
                        ((WriterToUTF8Buffered)writer).flush();
                    else
                        ((WriterToUTF8Buffered)writer).flushBuffer();
                }
                if(writer instanceof WriterToASCI){
                    if(m_shouldFlush)
                        writer.flush();
                }else{
                    // Flush always.
                    // Not a great thing if the writer was created
                    // by this class, but don't have a choice.
                    writer.flush();
                }
            }catch(IOException ioe){
                throw new SAXException(ioe);
            }
        }
    }

    public void elementDecl(String name,String model) throws SAXException{
        // Do not inline external DTD
        if(m_inExternalDTD)
            return;
        try{
            final Writer writer=m_writer;
            DTDprolog();
            writer.write("<!ELEMENT ");
            writer.write(name);
            writer.write(' ');
            writer.write(model);
            writer.write('>');
            writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    public void attributeDecl(
            String eName,
            String aName,
            String type,
            String valueDefault,
            String value)
            throws SAXException{
        // Do not inline external DTD
        if(m_inExternalDTD)
            return;
        try{
            final Writer writer=m_writer;
            DTDprolog();
            writer.write("<!ATTLIST ");
            writer.write(eName);
            writer.write(' ');
            writer.write(aName);
            writer.write(' ');
            writer.write(type);
            if(valueDefault!=null){
                writer.write(' ');
                writer.write(valueDefault);
            }
            //writer.write(" ");
            //writer.write(value);
            writer.write('>');
            writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
        // Do not inline external DTD
        if(m_inExternalDTD)
            return;
        try{
            DTDprolog();
            outputEntityDecl(name,value);
        }catch(IOException e){
            throw new SAXException(e);
        }
    }    public OutputStream getOutputStream(){
        if(m_writer instanceof WriterToUTF8Buffered)
            return ((WriterToUTF8Buffered)m_writer).getOutputStream();
        if(m_writer instanceof WriterToASCI)
            return ((WriterToASCI)m_writer).getOutputStream();
        else
            return null;
    }
    // Implement DeclHandler

    void outputEntityDecl(String name,String value) throws IOException{
        final Writer writer=m_writer;
        writer.write("<!ENTITY ");
        writer.write(name);
        writer.write(" \"");
        writer.write(value);
        writer.write("\">");
        writer.write(m_lineSep,0,m_lineSepLen);
    }

    public void externalEntityDecl(
            String name,
            String publicId,
            String systemId)
            throws SAXException{
        try{
            DTDprolog();
            m_writer.write("<!ENTITY ");
            m_writer.write(name);
            if(publicId!=null){
                m_writer.write(" PUBLIC \"");
                m_writer.write(publicId);
            }else{
                m_writer.write(" SYSTEM \"");
                m_writer.write(systemId);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void DTDprolog() throws SAXException, IOException{
        final Writer writer=m_writer;
        if(m_needToOutputDocTypeDecl){
            outputDocTypeDecl(m_elemContext.m_elementName,false);
            m_needToOutputDocTypeDecl=false;
        }
        if(m_inDoctype){
            writer.write(" [");
            writer.write(m_lineSep,0,m_lineSepLen);
            m_inDoctype=false;
        }
    }

    void outputDocTypeDecl(String name,boolean closeDecl) throws SAXException{
        if(m_cdataTagOpen)
            closeCDATA();
        try{
            final Writer writer=m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(name);
            String doctypePublic=getDoctypePublic();
            if(null!=doctypePublic){
                writer.write(" PUBLIC \"");
                writer.write(doctypePublic);
                writer.write('\"');
            }
            String doctypeSystem=getDoctypeSystem();
            if(null!=doctypeSystem){
                if(null==doctypePublic)
                    writer.write(" SYSTEM \"");
                else
                    writer.write(" \"");
                writer.write(doctypeSystem);
                if(closeDecl){
                    writer.write("\">");
                    writer.write(m_lineSep,0,m_lineSepLen);
                    closeDecl=false; // done closing
                }else
                    writer.write('\"');
            }
            boolean dothis=false;
            if(dothis){
                // at one point this code seemed right,
                // but not anymore - Brian M.
                if(closeDecl){
                    writer.write('>');
                    writer.write(m_lineSep,0,m_lineSepLen);
                }
            }
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    protected void closeCDATA() throws SAXException{
        try{
            m_writer.write(CDATA_DELIMITER_CLOSE);
            // write out a CDATA section closing "]]>"
            m_cdataTagOpen=false; // Remember that we have done so.
        }catch(IOException e){
            throw new SAXException(e);
        }
    }    public void setOutputFormat(Properties format){
        boolean shouldFlush=m_shouldFlush;
        init(m_writer,format,false,false);
        m_shouldFlush=shouldFlush;
    }

    private synchronized void init(Writer writer,Properties format){
        init(writer,format,false,false);
    }    private synchronized void init(
            Writer writer,
            Properties format,
            boolean defaultProperties,
            boolean shouldFlush){
        m_shouldFlush=shouldFlush;
        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if(m_tracer!=null
                &&!(writer instanceof SerializerTraceWriter))
            m_writer=new SerializerTraceWriter(writer,m_tracer);
        else
            m_writer=writer;
        m_format=format;
        //        m_cdataSectionNames =
        //            OutputProperties.getQNameProperties(
        //                OutputKeys.CDATA_SECTION_ELEMENTS,
        //                format);
        setCdataSectionElements(OutputKeys.CDATA_SECTION_ELEMENTS,format);
        setIndentAmount(
                OutputPropertyUtils.getIntProperty(
                        OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
                        format));
        setIndent(
                OutputPropertyUtils.getBooleanProperty(OutputKeys.INDENT,format));
        {
            String sep=
                    format.getProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR);
            if(sep!=null){
                m_lineSep=sep.toCharArray();
                m_lineSepLen=sep.length();
            }
        }
        boolean shouldNotWriteXMLHeader=
                OutputPropertyUtils.getBooleanProperty(
                        OutputKeys.OMIT_XML_DECLARATION,
                        format);
        setOmitXMLDeclaration(shouldNotWriteXMLHeader);
        setDoctypeSystem(format.getProperty(OutputKeys.DOCTYPE_SYSTEM));
        String doctypePublic=format.getProperty(OutputKeys.DOCTYPE_PUBLIC);
        setDoctypePublic(doctypePublic);
        // if standalone was explicitly specified
        if(format.get(OutputKeys.STANDALONE)!=null){
            String val=format.getProperty(OutputKeys.STANDALONE);
            if(defaultProperties)
                setStandaloneInternal(val);
            else
                setStandalone(val);
        }
        setMediaType(format.getProperty(OutputKeys.MEDIA_TYPE));
        if(null!=doctypePublic){
            if(doctypePublic.startsWith("-//W3C//DTD XHTML"))
                m_spaceBeforeClose=true;
        }
        /**
         * This code is added for XML 1.1 Version output.
         */
        String version=getVersion();
        if(null==version){
            version=format.getProperty(OutputKeys.VERSION);
            setVersion(version);
        }
        // initCharsMap();
        String encoding=getEncoding();
        if(null==encoding){
            encoding=
                    Encodings.getMimeEncoding(
                            format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }
        m_isUTF8=encoding.equals(Encodings.DEFAULT_MIME_ENCODING);
        // Access this only from the Hashtable level... we don't want to
        // get default properties.
        String entitiesFileName=
                (String)format.get(OutputPropertiesFactory.S_KEY_ENTITIES);
        if(null!=entitiesFileName){
            String method=
                    (String)format.get(OutputKeys.METHOD);
            m_charInfo=CharInfo.getCharInfo(entitiesFileName,method);
        }
    }

    public boolean setLineSepUse(boolean use_sytem_line_break){
        boolean oldValue=m_lineSepUse;
        m_lineSepUse=use_sytem_line_break;
        return oldValue;
    }

    protected int writeUTF16Surrogate(char c,char ch[],int i,int end)
            throws IOException{
        int codePoint=0;
        if(i+1>=end){
            throw new IOException(
                    Utils.messages.createMessage(
                            MsgKey.ER_INVALID_UTF16_SURROGATE,
                            new Object[]{Integer.toHexString((int)c)}));
        }
        final char high=c;
        final char low=ch[i+1];
        if(!Encodings.isLowUTF16Surrogate(low)){
            throw new IOException(
                    Utils.messages.createMessage(
                            MsgKey.ER_INVALID_UTF16_SURROGATE,
                            new Object[]{
                                    Integer.toHexString((int)c)
                                            +" "
                                            +Integer.toHexString(low)}));
        }
        final Writer writer=m_writer;
        // If we make it to here we have a valid high, low surrogate pair
        if(m_encodingInfo.isInEncoding(c,low)){
            // If the character formed by the surrogate pair
            // is in the encoding, so just write it out
            writer.write(ch,i,2);
        }else{
            // Don't know what to do with this char, it is
            // not in the encoding and not a high char in
            // a surrogate pair, so write out as an entity ref
            final String encoding=getEncoding();
            if(encoding!=null){
                /** The output encoding is known,
                 * so somthing is wrong.
                 */
                codePoint=Encodings.toCodePoint(high,low);
                // not in the encoding, so write out a character reference
                writer.write('&');
                writer.write('#');
                writer.write(Integer.toString(codePoint));
                writer.write(';');
            }else{
                /** The output encoding is not known,
                 * so just write it out as-is.
                 */
                writer.write(ch,i,2);
            }
        }
        // non-zero only if character reference was written out.
        return codePoint;
    }    protected synchronized void init(
            OutputStream output,
            Properties format,
            boolean defaultProperties)
            throws UnsupportedEncodingException{
        String encoding=getEncoding();
        if(encoding==null){
            // if not already set then get it from the properties
            encoding=
                    Encodings.getMimeEncoding(
                            format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }
        if(encoding.equalsIgnoreCase("UTF-8")){
            m_isUTF8=true;
            //            if (output instanceof java.io.BufferedOutputStream)
            //            {
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }
            //            else if (output instanceof java.io.FileOutputStream)
            //            {
            //                init(new WriterToUTF8Buffered(output), format, defaultProperties, true);
            //            }
            //            else
            //            {
            //                // Not sure what to do in this case.  I'm going to be conservative
            //                // and not buffer.
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }
            init(
                    new WriterToUTF8Buffered(output),
                    format,
                    defaultProperties,
                    true);
        }else if(
                encoding.equals("WINDOWS-1250")
                        ||encoding.equals("US-ASCII")
                        ||encoding.equals("ASCII")){
            init(new WriterToASCI(output),format,defaultProperties,true);
        }else{
            Writer osw;
            try{
                osw=Encodings.getWriter(output,encoding);
            }catch(UnsupportedEncodingException uee){
                System.out.println(
                        "Warning: encoding \""
                                +encoding
                                +"\" not supported"
                                +", using "
                                +Encodings.DEFAULT_MIME_ENCODING);
                encoding=Encodings.DEFAULT_MIME_ENCODING;
                setEncoding(encoding);
                osw=Encodings.getWriter(output,encoding);
            }
            init(osw,format,defaultProperties,true);
        }
    }

    void writeNormalizedChars(
            char ch[],
            int start,
            int length,
            boolean isCData,
            boolean useSystemLineSeparator)
            throws IOException, SAXException{
        final Writer writer=m_writer;
        int end=start+length;
        for(int i=start;i<end;i++){
            char c=ch[i];
            if(CharInfo.S_LINEFEED==c&&useSystemLineSeparator){
                writer.write(m_lineSep,0,m_lineSepLen);
            }else if(isCData&&(!escapingNotNeeded(c))){
                //                if (i != 0)
                if(m_cdataTagOpen)
                    closeCDATA();
                // This needs to go into a function...
                if(Encodings.isHighUTF16Surrogate(c)){
                    writeUTF16Surrogate(c,ch,i,end);
                    i++; // process two input characters
                }else{
                    writer.write("&#");
                    String intStr=Integer.toString((int)c);
                    writer.write(intStr);
                    writer.write(';');
                }
                //                if ((i != 0) && (i < (end - 1)))
                //                if (!m_cdataTagOpen && (i < (end - 1)))
                //                {
                //                    writer.write(CDATA_DELIMITER_OPEN);
                //                    m_cdataTagOpen = true;
                //                }
            }else if(
                    isCData
                            &&((i<(end-2))
                            &&(']'==c)
                            &&(']'==ch[i+1])
                            &&('>'==ch[i+2]))){
                writer.write(CDATA_CONTINUE);
                i+=2;
            }else{
                if(escapingNotNeeded(c)){
                    if(isCData&&!m_cdataTagOpen){
                        writer.write(CDATA_DELIMITER_OPEN);
                        m_cdataTagOpen=true;
                    }
                    writer.write(c);
                }
                // This needs to go into a function...
                else if(Encodings.isHighUTF16Surrogate(c)){
                    if(m_cdataTagOpen)
                        closeCDATA();
                    writeUTF16Surrogate(c,ch,i,end);
                    i++; // process two input characters
                }else{
                    if(m_cdataTagOpen)
                        closeCDATA();
                    writer.write("&#");
                    String intStr=Integer.toString((int)c);
                    writer.write(intStr);
                    writer.write(';');
                }
            }
        }
    }    public Properties getOutputFormat(){
        return m_format;
    }

    public void endNonEscaping() throws SAXException{
        m_disableOutputEscapingStates.pop();
    }    public void setWriter(Writer writer){
        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if(m_tracer!=null
                &&!(writer instanceof SerializerTraceWriter))
            m_writer=new SerializerTraceWriter(writer,m_tracer);
        else
            m_writer=writer;
    }

    public void startNonEscaping() throws SAXException{
        m_disableOutputEscapingStates.push(true);
    }

    protected void cdata(char ch[],int start,final int length)
            throws SAXException{
        try{
            final int old_start=start;
            if(m_elemContext.m_startTagOpen){
                closeStartTag();
                m_elemContext.m_startTagOpen=false;
            }
            m_ispreserve=true;
            if(shouldIndent())
                indent();
            boolean writeCDataBrackets=
                    (((length>=1)&&escapingNotNeeded(ch[start])));
            /** Write out the CDATA opening delimiter only if
             * we are supposed to, and if we are not already in
             * the middle of a CDATA section
             */
            if(writeCDataBrackets&&!m_cdataTagOpen){
                m_writer.write(CDATA_DELIMITER_OPEN);
                m_cdataTagOpen=true;
            }
            // writer.write(ch, start, length);
            if(isEscapingDisabled()){
                charactersRaw(ch,start,length);
            }else
                writeNormalizedChars(ch,start,length,true,m_lineSepUse);
            /** used to always write out CDATA closing delimiter here,
             * but now we delay, so that we can merge CDATA sections on output.
             * need to write closing delimiter later
             */
            if(writeCDataBrackets){
                /** if the CDATA section ends with ] don't leave it open
                 * as there is a chance that an adjacent CDATA sections
                 * starts with ]>.
                 * We don't want to merge ]] with > , or ] with ]>
                 */
                if(ch[start+length-1]==']')
                    closeCDATA();
            }
            // time to fire off CDATA event
            if(m_tracer!=null)
                super.fireCDATAEvent(ch,old_start,length);
        }catch(IOException ioe){
            throw new SAXException(
                    Utils.messages.createMessage(
                            MsgKey.ER_OIERROR,
                            null),
                    ioe);
            //"IO error", ioe);
        }
    }    public void setOutputStream(OutputStream output){
        try{
            Properties format;
            if(null==m_format)
                format=
                        OutputPropertiesFactory.getDefaultMethodProperties(
                                Method.XML);
            else
                format=m_format;
            init(output,format,true);
        }catch(UnsupportedEncodingException uee){
            // Should have been warned in init, I guess...
        }
    }

    private boolean isEscapingDisabled(){
        return m_disableOutputEscapingStates.peekOrFalse();
    }

    protected void charactersRaw(char ch[],int start,int length)
            throws SAXException{
        if(m_inEntityRef)
            return;
        try{
            if(m_elemContext.m_startTagOpen){
                closeStartTag();
                m_elemContext.m_startTagOpen=false;
            }
            m_ispreserve=true;
            m_writer.write(ch,start,length);
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    private int processDirty(
            char[] chars,
            int end,
            int i,
            char ch,
            int lastDirty,
            boolean fromTextNode) throws IOException{
        int startClean=lastDirty+1;
        // if we have some clean characters accumulated
        // process them before the dirty one.
        if(i>startClean){
            int lengthClean=i-startClean;
            m_writer.write(chars,startClean,lengthClean);
        }
        // process the "dirty" character
        if(CharInfo.S_LINEFEED==ch&&fromTextNode){
            m_writer.write(m_lineSep,0,m_lineSepLen);
        }else{
            startClean=
                    accumDefaultEscape(
                            m_writer,
                            (char)ch,
                            i,
                            chars,
                            end,
                            fromTextNode,
                            false);
            i=startClean-1;
        }
        // Return the index of the last character that we just processed
        // which is a dirty character.
        return i;
    }

    public void characters(String s) throws SAXException{
        if(m_inEntityRef&&!m_expandDTDEntities)
            return;
        final int length=s.length();
        if(length>m_charsBuff.length){
            m_charsBuff=new char[length*2+1];
        }
        s.getChars(0,length,m_charsBuff,0);
        characters(m_charsBuff,0,length);
    }

    public void endElement(String name) throws SAXException{
        endElement(null,null,name);
    }

    public void startElement(
            String elementNamespaceURI,
            String elementLocalName,
            String elementName)
            throws SAXException{
        startElement(elementNamespaceURI,elementLocalName,elementName,null);
    }    public Writer getWriter(){
        return m_writer;
    }

    public void startElement(String elementName) throws SAXException{
        startElement(null,null,elementName,null);
    }

    public boolean startPrefixMapping(
            String prefix,
            String uri,
            boolean shouldFlush)
            throws SAXException{
        /** Remember the mapping, and at what depth it was declared
         * This is one greater than the current depth because these
         * mappings will apply to the next depth. This is in
         * consideration that startElement() will soon be called
         */
        boolean pushed;
        int pushDepth;
        if(shouldFlush){
            flushPending();
            // the prefix mapping applies to the child element (one deeper)
            pushDepth=m_elemContext.m_currentElemDepth+1;
        }else{
            // the prefix mapping applies to the current element
            pushDepth=m_elemContext.m_currentElemDepth;
        }
        pushed=m_prefixMap.pushNamespace(prefix,uri,pushDepth);
        if(pushed){
            /** Brian M.: don't know if we really needto do this. The
             * callers of this object should have injected both
             * startPrefixMapping and the attributes.  We are
             * just covering our butt here.
             */
            String name;
            if(EMPTYSTRING.equals(prefix)){
                name="xmlns";
                addAttributeAlways(XMLNS_URI,name,name,"CDATA",uri,false);
            }else{
                if(!EMPTYSTRING.equals(uri))
                // hack for XSLTC attribset16 test
                { // that maps ns1 prefix to "" URI
                    name="xmlns:"+prefix;
                    /** for something like xmlns:abc="w3.pretend.org"
                     *  the      uri is the value, that is why we pass it in the
                     * value, or 5th slot of addAttributeAlways()
                     */
                    addAttributeAlways(XMLNS_URI,prefix,name,"CDATA",uri,false);
                }
            }
        }
        return pushed;
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        // the "true" causes the flush of any open tags
        startPrefixMapping(prefix,uri,true);
    }

    public void endPrefixMapping(String prefix) throws SAXException{ // do nothing
    }

    public void startElement(
            String namespaceURI,
            String localName,
            String name,
            Attributes atts)
            throws SAXException{
        if(m_inEntityRef)
            return;
        if(m_needToCallStartDocument){
            startDocumentInternal();
            m_needToCallStartDocument=false;
        }else if(m_cdataTagOpen)
            closeCDATA();
        try{
            if((true==m_needToOutputDocTypeDecl)
                    &&(null!=getDoctypeSystem())){
                outputDocTypeDecl(name,true);
            }
            m_needToOutputDocTypeDecl=false;
            /** before we over-write the current elementLocalName etc.
             * lets close out the old one (if we still need to)
             */
            if(m_elemContext.m_startTagOpen){
                closeStartTag();
                m_elemContext.m_startTagOpen=false;
            }
            if(namespaceURI!=null)
                ensurePrefixIsDeclared(namespaceURI,name);
            m_ispreserve=false;
            if(shouldIndent()&&m_startNewLine){
                indent();
            }
            m_startNewLine=true;
            final Writer writer=m_writer;
            writer.write('<');
            writer.write(name);
        }catch(IOException e){
            throw new SAXException(e);
        }
        // process the attributes now, because after this SAX call they might be gone
        if(atts!=null)
            addAttributes(atts);
        m_elemContext=m_elemContext.push(namespaceURI,localName,name);
        m_isprevtext=false;
        if(m_tracer!=null){
            firePseudoAttributes();
        }
    }

    public void endElement(String namespaceURI,String localName,String name)
            throws SAXException{
        if(m_inEntityRef)
            return;
        // namespaces declared at the current depth are no longer valid
        // so get rid of them
        m_prefixMap.popNamespaces(m_elemContext.m_currentElemDepth,null);
        try{
            final Writer writer=m_writer;
            if(m_elemContext.m_startTagOpen){
                if(m_tracer!=null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs=m_attributes.getLength();
                if(nAttrs>0){
                    processAttributes(m_writer,nAttrs);
                    // clear attributes object for re-use with next element
                    m_attributes.clear();
                }
                if(m_spaceBeforeClose)
                    writer.write(" />");
                else
                    writer.write("/>");
                /** don't need to pop cdataSectionState because
                 * this element ended so quickly that we didn't get
                 * to push the state.
                 */
            }else{
                if(m_cdataTagOpen)
                    closeCDATA();
                if(shouldIndent())
                    indent(m_elemContext.m_currentElemDepth-1);
                writer.write('<');
                writer.write('/');
                writer.write(name);
                writer.write('>');
            }
        }catch(IOException e){
            throw new SAXException(e);
        }
        if(!m_elemContext.m_startTagOpen&&m_doIndent){
            m_ispreserve=m_preserves.isEmpty()?false:m_preserves.pop();
        }
        m_isprevtext=false;
        // fire off the end element event
        if(m_tracer!=null)
            super.fireEndElem(name);
        m_elemContext=m_elemContext.m_prev;
    }

    protected void indent(int depth) throws IOException{
        if(m_startNewLine)
            outputLineSep();
        /** For m_indentAmount > 0 this extra test might be slower
         * but Xalan's default value is 0, so this extra test
         * will run faster in that situation.
         */
        if(m_indentAmount>0)
            printSpace(depth*m_indentAmount);
    }

    protected final void outputLineSep() throws IOException{
        m_writer.write(m_lineSep,0,m_lineSepLen);
    }

    private void printSpace(int n) throws IOException{
        final Writer writer=m_writer;
        for(int i=0;i<n;i++){
            writer.write(' ');
        }
    }

    public void characters(final char chars[],final int start,final int length)
            throws SAXException{
        // It does not make sense to continue with rest of the method if the number of
        // characters to read from array is 0.
        // Section 7.6.1 of XSLT 1.0 (http://www.w3.org/TR/xslt#value-of) suggest no text node
        // is created if string is empty.
        if(length==0||(m_inEntityRef&&!m_expandDTDEntities))
            return;
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }else if(m_needToCallStartDocument){
            startDocumentInternal();
        }
        if(m_cdataStartCalled||m_elemContext.m_isCdataSection){
            /** either due to startCDATA() being called or due to
             * cdata-section-elements atribute, we need this as cdata
             */
            cdata(chars,start,length);
            return;
        }
        if(m_cdataTagOpen)
            closeCDATA();
        // the check with _escaping is a bit of a hack for XLSTC
        if(m_disableOutputEscapingStates.peekOrFalse()||(!m_escaping)){
            charactersRaw(chars,start,length);
            // time to fire off characters generation event
            if(m_tracer!=null)
                super.fireCharEvent(chars,start,length);
            return;
        }
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }
        try{
            int i;
            char ch1;
            int startClean;
            // skip any leading whitspace
            // don't go off the end and use a hand inlined version
            // of isWhitespace(ch)
            final int end=start+length;
            int lastDirty=start-1; // last character that needed processing
            for(i=start;
                ((i<end)
                        &&((ch1=chars[i])==0x20
                        ||(ch1==0xA&&m_lineSepUse)
                        ||ch1==0xD
                        ||ch1==0x09));
                i++){
                /**
                 * We are processing leading whitespace, but are doing the same
                 * processing for dirty characters here as for non-whitespace.
                 *
                 */
                if(!m_charInfo.isTextASCIIClean(ch1)){
                    lastDirty=processDirty(chars,end,i,ch1,lastDirty,true);
                    i=lastDirty;
                }
            }
            /** If there is some non-whitespace, mark that we may need
             * to preserve this. This is only important if we have indentation on.
             */
            if(i<end)
                m_ispreserve=true;
//            int lengthClean;    // number of clean characters in a row
//            final boolean[] isAsciiClean = m_charInfo.getASCIIClean();
            final boolean isXML10=XMLVERSION10.equals(getVersion());
            // we've skipped the leading whitespace, now deal with the rest
            for(;i<end;i++){
                {
                    // A tight loop to skip over common clean chars
                    // This tight loop makes it easier for the JIT
                    // to optimize.
                    char ch2;
                    while(i<end
                            &&((ch2=chars[i])<127)
                            &&m_charInfo.isTextASCIIClean(ch2))
                        i++;
                    if(i==end)
                        break;
                }
                final char ch=chars[i];
                /**  The check for isCharacterInC0orC1Ranger and
                 *  isNELorLSEPCharacter has been added
                 *  to support Control Characters in XML 1.1
                 */
                if(!isCharacterInC0orC1Range(ch)&&
                        (isXML10||!isNELorLSEPCharacter(ch))&&
                        (escapingNotNeeded(ch)&&(!m_charInfo.isSpecialTextChar(ch)))
                        ||('"'==ch)){
                    ; // a character needing no special processing
                }else{
                    lastDirty=processDirty(chars,end,i,ch,lastDirty,true);
                    i=lastDirty;
                }
            }
            // we've reached the end. Any clean characters at the
            // end of the array than need to be written out?
            startClean=lastDirty+1;
            if(i>startClean){
                int lengthClean=i-startClean;
                m_writer.write(chars,startClean,lengthClean);
            }
            // For indentation purposes, mark that we've just writen text out
            m_isprevtext=true;
        }catch(IOException e){
            throw new SAXException(e);
        }
        // time to fire off characters generation event
        if(m_tracer!=null)
            super.fireCharEvent(chars,start,length);
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        if(0==length)
            return;
        characters(ch,start,length);
    }

    public void skippedEntity(String name) throws SAXException{ // TODO: Should handle
    }

    public void processAttributes(Writer writer,int nAttrs) throws IOException, SAXException{
        /** real SAX attributes are not passed in, so process the
         * attributes that were collected after the startElement call.
         * _attribVector is a "cheap" list for Stream serializer output
         * accumulated over a series of calls to attribute(name,value)
         */
        String encoding=getEncoding();
        for(int i=0;i<nAttrs;i++){
            // elementAt is JDK 1.1.8
            final String name=m_attributes.getQName(i);
            final String value=m_attributes.getValue(i);
            writer.write(' ');
            writer.write(name);
            writer.write("=\"");
            writeAttrString(writer,value,encoding);
            writer.write('\"');
        }
    }

    public void writeAttrString(
            Writer writer,
            String string,
            String encoding)
            throws IOException{
        final int len=string.length();
        if(len>m_attrBuff.length){
            m_attrBuff=new char[len*2+1];
        }
        string.getChars(0,len,m_attrBuff,0);
        final char[] stringChars=m_attrBuff;
        for(int i=0;i<len;){
            char ch=stringChars[i];
            if(escapingNotNeeded(ch)&&(!m_charInfo.isSpecialAttrChar(ch))){
                writer.write(ch);
                i++;
            }else{ // I guess the parser doesn't normalize cr/lf in attributes. -sb
//                if ((CharInfo.S_CARRIAGERETURN == ch)
//                    && ((i + 1) < len)
//                    && (CharInfo.S_LINEFEED == stringChars[i + 1]))
//                {
//                    i++;
//                    ch = CharInfo.S_LINEFEED;
//                }
                i=accumDefaultEscape(writer,ch,i,stringChars,len,false,true);
            }
        }
    }

    protected boolean escapingNotNeeded(char ch){
        final boolean ret;
        if(ch<127){
            // This is the old/fast code here, but is this
            // correct for all encodings?
            if(ch>=0x20||(0x0A==ch||0x0D==ch||0x09==ch))
                ret=true;
            else
                ret=false;
        }else{
            ret=m_encodingInfo.isInEncoding(ch);
        }
        return ret;
    }

    protected int accumDefaultEscape(
            Writer writer,
            char ch,
            int i,
            char[] chars,
            int len,
            boolean fromTextNode,
            boolean escLF)
            throws IOException{
        int pos=accumDefaultEntity(writer,ch,i,chars,len,fromTextNode,escLF);
        if(i==pos){
            if(Encodings.isHighUTF16Surrogate(ch)){
                // Should be the UTF-16 low surrogate of the hig/low pair.
                char next;
                // Unicode code point formed from the high/low pair.
                int codePoint=0;
                if(i+1>=len){
                    throw new IOException(
                            Utils.messages.createMessage(
                                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                                    new Object[]{Integer.toHexString(ch)}));
                    //"Invalid UTF-16 surrogate detected: "
                    //+Integer.toHexString(ch)+ " ?");
                }else{
                    next=chars[++i];
                    if(!(Encodings.isLowUTF16Surrogate(next)))
                        throw new IOException(
                                Utils.messages.createMessage(
                                        MsgKey
                                                .ER_INVALID_UTF16_SURROGATE,
                                        new Object[]{
                                                Integer.toHexString(ch)
                                                        +" "
                                                        +Integer.toHexString(next)}));
                    //"Invalid UTF-16 surrogate detected: "
                    //+Integer.toHexString(ch)+" "+Integer.toHexString(next));
                    codePoint=Encodings.toCodePoint(ch,next);
                }
                writer.write("&#");
                writer.write(Integer.toString(codePoint));
                writer.write(';');
                pos+=2; // count the two characters that went into writing out this entity
            }else{
                /**  This if check is added to support control characters in XML 1.1.
                 *  If a character is a Control Character within C0 and C1 range, it is desirable
                 *  to write it out as Numeric Character Reference(NCR) regardless of XML Version
                 *  being used for output document.
                 */
                if(isCharacterInC0orC1Range(ch)||
                        (XMLVERSION11.equals(getVersion())&&isNELorLSEPCharacter(ch))){
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }else if((!escapingNotNeeded(ch)||
                        ((fromTextNode&&m_charInfo.isSpecialTextChar(ch))
                                ||(!fromTextNode&&m_charInfo.isSpecialAttrChar(ch))))
                        &&m_elemContext.m_currentElemDepth>0){
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }else{
                    writer.write(ch);
                }
                pos++;  // count the single character that was processed
            }
        }
        return pos;
    }

    protected int accumDefaultEntity(
            Writer writer,
            char ch,
            int i,
            char[] chars,
            int len,
            boolean fromTextNode,
            boolean escLF)
            throws IOException{
        if(!escLF&&CharInfo.S_LINEFEED==ch){
            writer.write(m_lineSep,0,m_lineSepLen);
        }else{
            // if this is text node character and a special one of those,
            // or if this is a character from attribute value and a special one of those
            if((fromTextNode&&m_charInfo.isSpecialTextChar(ch))||(!fromTextNode&&m_charInfo.isSpecialAttrChar(ch))){
                String outputStringForChar=m_charInfo.getOutputStringForChar(ch);
                if(null!=outputStringForChar){
                    writer.write(outputStringForChar);
                }else
                    return i;
            }else
                return i;
        }
        return i+1;
    }

    private static boolean isCharacterInC0orC1Range(char ch){
        if(ch==0x09||ch==0x0A||ch==0x0D)
            return false;
        else
            return (ch>=0x7F&&ch<=0x9F)||(ch>=0x01&&ch<=0x1F);
    }

    private static boolean isNELorLSEPCharacter(char ch){
        return (ch==0x85||ch==0x2028);
    }

    protected boolean shouldIndent(){
        return m_doIndent&&(!m_ispreserve&&!m_isprevtext)&&(m_elemContext.m_currentElemDepth>0||m_isStandalone);
    }

    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        setDoctypeSystem(systemId);
        setDoctypePublic(publicId);
        m_elemContext.m_elementName=name;
        m_inDoctype=true;
    }

    public void endDTD() throws SAXException{
        try{
            // Don't output doctype declaration until startDocumentInternal
            // has been called. Otherwise, it can appear before XML decl.
            if(m_needToCallStartDocument){
                return;
            }
            if(m_needToOutputDocTypeDecl){
                outputDocTypeDecl(m_elemContext.m_elementName,false);
                m_needToOutputDocTypeDecl=false;
            }
            final Writer writer=m_writer;
            if(!m_inDoctype)
                writer.write("]>");
            else{
                writer.write('>');
            }
            writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    public void startEntity(String name) throws SAXException{
        if(name.equals("[dtd]"))
            m_inExternalDTD=true;
        if(!m_expandDTDEntities&&!m_inExternalDTD){
            /** Only leave the entity as-is if
             * we've been told not to expand them
             * and this is not the magic [dtd] name.
             */
            startNonEscaping();
            characters("&"+name+';');
            endNonEscaping();
        }
        m_inEntityRef=true;
    }

    public void startCDATA() throws SAXException{
        m_cdataStartCalled=true;
    }

    public void endCDATA() throws SAXException{
        if(m_cdataTagOpen)
            closeCDATA();
        m_cdataStartCalled=false;
    }

    public void comment(char ch[],int start,int length)
            throws SAXException{
        int start_old=start;
        if(m_inEntityRef)
            return;
        if(m_elemContext.m_startTagOpen){
            closeStartTag();
            m_elemContext.m_startTagOpen=false;
        }else if(m_needToCallStartDocument){
            startDocumentInternal();
            m_needToCallStartDocument=false;
        }
        try{
            if(shouldIndent()&&m_isStandalone)
                indent();
            final int limit=start+length;
            boolean wasDash=false;
            if(m_cdataTagOpen)
                closeCDATA();
            if(shouldIndent()&&!m_isStandalone)
                indent();
            final Writer writer=m_writer;
            writer.write(COMMENT_BEGIN);
            // Detect occurrences of two consecutive dashes, handle as necessary.
            for(int i=start;i<limit;i++){
                if(wasDash&&ch[i]=='-'){
                    writer.write(ch,start,i-start);
                    writer.write(" -");
                    start=i+1;
                }
                wasDash=(ch[i]=='-');
            }
            // if we have some chars in the comment
            if(length>0){
                // Output the remaining characters (if any)
                final int remainingChars=(limit-start);
                if(remainingChars>0)
                    writer.write(ch,start,remainingChars);
                // Protect comment end from a single trailing dash
                if(ch[limit-1]=='-')
                    writer.write(' ');
            }
            writer.write(COMMENT_END);
        }catch(IOException e){
            throw new SAXException(e);
        }
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
        // time to generate comment event
        if(m_tracer!=null)
            super.fireCommentEvent(ch,start_old,length);
    }

    protected void indent() throws IOException{
        indent(m_elemContext.m_currentElemDepth);
    }

    protected void closeStartTag() throws SAXException{
        if(m_elemContext.m_startTagOpen){
            try{
                if(m_tracer!=null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs=m_attributes.getLength();
                if(nAttrs>0){
                    processAttributes(m_writer,nAttrs);
                    // clear attributes object for re-use with next element
                    m_attributes.clear();
                }
                m_writer.write('>');
            }catch(IOException e){
                throw new SAXException(e);
            }
            /** whether Xalan or XSLTC, we have the prefix mappings now, so
             * lets determine if the current element is specified in the cdata-
             * section-elements list.
             */
            if(m_cdataSectionElements!=null)
                m_elemContext.m_isCdataSection=isCdataSection();
            if(m_doIndent){
                m_isprevtext=false;
                m_preserves.push(m_ispreserve);
            }
        }
    }

    protected String ensureAttributesNamespaceIsDeclared(
            String ns,
            String localName,
            String rawName)
            throws SAXException{
        if(ns!=null&&ns.length()>0){
            // extract the prefix in front of the raw name
            int index=0;
            String prefixFromRawName=
                    (index=rawName.indexOf(":"))<0
                            ?""
                            :rawName.substring(0,index);
            if(index>0){
                // we have a prefix, lets see if it maps to a namespace
                String uri=m_prefixMap.lookupNamespace(prefixFromRawName);
                if(uri!=null&&uri.equals(ns)){
                    // the prefix in the raw name is already maps to the given namespace uri
                    // so we don't need to do anything
                    return null;
                }else{
                    // The uri does not map to the prefix in the raw name,
                    // so lets make the mapping.
                    this.startPrefixMapping(prefixFromRawName,ns,false);
                    this.addAttribute(
                            "http://www.w3.org/2000/xmlns/",
                            prefixFromRawName,
                            "xmlns:"+prefixFromRawName,
                            "CDATA",
                            ns,false);
                    return prefixFromRawName;
                }
            }else{
                // we don't have a prefix in the raw name.
                // Does the URI map to a prefix already?
                String prefix=m_prefixMap.lookupPrefix(ns);
                if(prefix==null){
                    // uri is not associated with a prefix,
                    // so lets generate a new prefix to use
                    prefix=m_prefixMap.generateNextPrefix();
                    this.startPrefixMapping(prefix,ns,false);
                    this.addAttribute(
                            "http://www.w3.org/2000/xmlns/",
                            prefix,
                            "xmlns:"+prefix,
                            "CDATA",
                            ns,false);
                }
                return prefix;
            }
        }
        return null;
    }

    void ensurePrefixIsDeclared(String ns,String rawName)
            throws SAXException{
        if(ns!=null&&ns.length()>0){
            int index;
            final boolean no_prefix=((index=rawName.indexOf(":"))<0);
            String prefix=(no_prefix)?"":rawName.substring(0,index);
            if(null!=prefix){
                String foundURI=m_prefixMap.lookupNamespace(prefix);
                if((null==foundURI)||!foundURI.equals(ns)){
                    this.startPrefixMapping(prefix,ns);
                    // Bugzilla1133: Generate attribute as well as namespace event.
                    // SAX does expect both.
                    this.addAttributeAlways(
                            "http://www.w3.org/2000/xmlns/",
                            no_prefix?"xmlns":prefix,  // local name
                            no_prefix?"xmlns":("xmlns:"+prefix), // qname
                            "CDATA",
                            ns,
                            false);
                }
            }
        }
    }

    public void setContentHandler(ContentHandler ch){
        // this method is really only useful in the ToSAXHandler classes but it is
        // in the interface.  If the method defined here is ever called
        // we are probably in trouble.
    }

    public void serialize(Node node) throws IOException{
        try{
            TreeWalker walker=
                    new TreeWalker(this);
            walker.traverse(node);
        }catch(SAXException se){
            throw new WrappedRuntimeException(se);
        }
    }

    public boolean setEscaping(boolean escape){
        final boolean temp=m_escaping;
        m_escaping=escape;
        return temp;
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

    public boolean addAttributeAlways(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean xslAttribute){
        boolean was_added;
        int index;
        //if (uri == null || localName == null || uri.length() == 0)
        index=m_attributes.getIndex(rawName);
        // Don't use 'localName' as it gives incorrect value, rely only on 'rawName'
        /**else {
         index = m_attributes.getIndex(uri, localName);
         }*/
        if(index>=0){
            String old_value=null;
            if(m_tracer!=null){
                old_value=m_attributes.getValue(index);
                if(value.equals(old_value))
                    old_value=null;
            }
            /** We've seen the attribute before.
             * We may have a null uri or localName, but all we really
             * want to re-set is the value anyway.
             */
            m_attributes.setValue(index,value);
            was_added=false;
            if(old_value!=null){
                firePseudoAttributes();
            }
        }else{
            // the attribute doesn't exist yet, create it
            if(xslAttribute){
                /**
                 * This attribute is from an xsl:attribute element so we take some care in
                 * adding it, e.g.
                 *   <elem1  foo:attr1="1" xmlns:foo="uri1">
                 *       <xsl:attribute name="foo:attr2">2</xsl:attribute>
                 *   </elem1>
                 *
                 * We are adding attr1 and attr2 both as attributes of elem1,
                 * and this code is adding attr2 (the xsl:attribute ).
                 * We could have a collision with the prefix like in the example above.
                 */
                // In the example above, is there a prefix like foo ?
                final int colonIndex=rawName.indexOf(':');
                if(colonIndex>0){
                    String prefix=rawName.substring(0,colonIndex);
                    NamespaceMappings.MappingRecord existing_mapping=m_prefixMap.getMappingFromPrefix(prefix);
                    /** Before adding this attribute (foo:attr2),
                     * is the prefix for it (foo) already mapped at the current depth?
                     */
                    if(existing_mapping!=null
                            &&existing_mapping.m_declarationDepth==m_elemContext.m_currentElemDepth
                            &&!existing_mapping.m_uri.equals(uri)){
                        /**
                         * There is an existing mapping of this prefix,
                         * it differs from the one we need,
                         * and unfortunately it is at the current depth so we
                         * can not over-ride it.
                         */
                        /**
                         * Are we lucky enough that an existing other prefix maps to this URI ?
                         */
                        prefix=m_prefixMap.lookupPrefix(uri);
                        if(prefix==null){
                            /** Unfortunately there is no existing prefix that happens to map to ours,
                             * so to avoid a prefix collision we must generated a new prefix to use.
                             * This is OK because the prefix URI mapping
                             * defined in the xsl:attribute is short in scope,
                             * just the xsl:attribute element itself,
                             * and at this point in serialization the body of the
                             * xsl:attribute, if any, is just a String. Right?
                             *   . . . I sure hope so - Brian M.
                             */
                            prefix=m_prefixMap.generateNextPrefix();
                        }
                        rawName=prefix+':'+localName;
                    }
                }
                try{
                    /** This is our last chance to make sure the namespace for this
                     * attribute is declared, especially if we just generated an alternate
                     * prefix to avoid a collision (the new prefix/rawName will go out of scope
                     * soon and be lost ...  last chance here.
                     */
                    String prefixUsed=
                            ensureAttributesNamespaceIsDeclared(
                                    uri,
                                    localName,
                                    rawName);
                }catch(SAXException e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            m_attributes.addAttribute(uri,localName,rawName,type,value);
            was_added=true;
            if(m_tracer!=null){
                firePseudoAttributes();
            }
        }
        return was_added;
    }

    public void setEncoding(String encoding){
        String old=getEncoding();
        super.setEncoding(encoding);
        if(old==null||!old.equals(encoding)){
            // If we have changed the setting of the
            m_encodingInfo=Encodings.getEncodingInfo(encoding);
            if(encoding!=null&&m_encodingInfo.name==null){
                // We tried to get an EncodingInfo for Object for the given
                // encoding, but it came back with an internall null name
                // so the encoding is not supported by the JDK, issue a message.
                String msg=Utils.messages.createMessage(
                        MsgKey.ER_ENCODING_NOT_SUPPORTED,new Object[]{encoding});
                try{
                    // Prepare to issue the warning message
                    Transformer tran=super.getTransformer();
                    if(tran!=null){
                        ErrorListener errHandler=tran.getErrorListener();
                        // Issue the warning message
                        if(null!=errHandler&&m_sourceLocator!=null)
                            errHandler.warning(new TransformerException(msg,m_sourceLocator));
                        else
                            System.out.println(msg);
                    }else
                        System.out.println(msg);
                }catch(Exception e){
                }
            }
        }
        return;
    }

    public int getIndentAmount(){
        return m_indentAmount;
    }

    public void setIndentAmount(int m_indentAmount){
        this.m_indentAmount=m_indentAmount;
    }

    public void setTransformer(Transformer transformer){
        super.setTransformer(transformer);
        if(m_tracer!=null
                &&!(m_writer instanceof SerializerTraceWriter))
            m_writer=new SerializerTraceWriter(m_writer,m_tracer);
    }

    public boolean reset(){
        boolean wasReset=false;
        if(super.reset()){
            resetToStream();
            wasReset=true;
        }
        return wasReset;
    }    private void setCdataSectionElements(String key,Properties props){
        String s=props.getProperty(key);
        if(null!=s){
            // Vector of URI/LocalName pairs
            Vector v=new Vector();
            int l=s.length();
            boolean inCurly=false;
            StringBuffer buf=new StringBuffer();
            // parse through string, breaking on whitespaces.  I do this instead
            // of a tokenizer so I can track whitespace inside of curly brackets,
            // which theoretically shouldn't happen if they contain legal URLs.
            for(int i=0;i<l;i++){
                char c=s.charAt(i);
                if(Character.isWhitespace(c)){
                    if(!inCurly){
                        if(buf.length()>0){
                            addCdataSectionElement(buf.toString(),v);
                            buf.setLength(0);
                        }
                        continue;
                    }
                }else if('{'==c)
                    inCurly=true;
                else if('}'==c)
                    inCurly=false;
                buf.append(c);
            }
            if(buf.length()>0){
                addCdataSectionElement(buf.toString(),v);
                buf.setLength(0);
            }
            // call the official, public method to set the collected names
            setCdataSectionElements(v);
        }
    }

    private void resetToStream(){
        this.m_cdataStartCalled=false;
        /** The stream is being reset. It is one of
         * ToXMLStream, ToHTMLStream ... and this type can't be changed
         * so neither should m_charInfo which is associated with the
         * type of Stream. Just leave m_charInfo as-is for the next re-use.
         *
         */
        // this.m_charInfo = null; // don't set to null
        this.m_disableOutputEscapingStates.clear();
        this.m_escaping=true;
        // Leave m_format alone for now - Brian M.
        // this.m_format = null;
        this.m_inDoctype=false;
        this.m_ispreserve=false;
        this.m_ispreserve=false;
        this.m_isprevtext=false;
        this.m_isUTF8=false; //  ?? used anywhere ??
        this.m_preserves.clear();
        this.m_shouldFlush=true;
        this.m_spaceBeforeClose=false;
        this.m_startNewLine=false;
        this.m_lineSepUse=true;
        // DON'T SET THE WRITER TO NULL, IT MAY BE REUSED !!
        // this.m_writer = null;
        this.m_expandDTDEntities=true;
    }    private void addCdataSectionElement(String URI_and_localName,Vector v){
        StringTokenizer tokenizer=
                new StringTokenizer(URI_and_localName,"{}",false);
        String s1=tokenizer.nextToken();
        String s2=tokenizer.hasMoreTokens()?tokenizer.nextToken():null;
        if(null==s2){
            // add null URI and the local name
            v.addElement(null);
            v.addElement(s1);
        }else{
            // add URI, then local name
            v.addElement(s1);
            v.addElement(s2);
        }
    }

    // Implement DTDHandler
    public void notationDecl(String name,String pubID,String sysID) throws SAXException{
        // TODO Auto-generated method stub
        try{
            DTDprolog();
            m_writer.write("<!NOTATION ");
            m_writer.write(name);
            if(pubID!=null){
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
            }else{
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }    public void setCdataSectionElements(Vector URI_and_localNames){
        m_cdataSectionElements=URI_and_localNames;
    }

    public void unparsedEntityDecl(String name,String pubID,String sysID,String notationName) throws SAXException{
        // TODO Auto-generated method stub
        try{
            DTDprolog();
            m_writer.write("<!ENTITY ");
            m_writer.write(name);
            if(pubID!=null){
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
            }else{
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" NDATA ");
            m_writer.write(notationName);
            m_writer.write(" >");
            m_writer.write(m_lineSep,0,m_lineSepLen);
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setDTDEntityExpansion(boolean expand){
        m_expandDTDEntities=expand;
    }

    protected void firePseudoAttributes(){
        if(m_tracer!=null){
            try{
                // flush out the "<elemName" if not already flushed
                m_writer.flush();
                // make a StringBuffer to write the name="value" pairs to.
                StringBuffer sb=new StringBuffer();
                int nAttrs=m_attributes.getLength();
                if(nAttrs>0){
                    // make a writer that internally appends to the same
                    // StringBuffer
                    Writer writer=
                            new WritertoStringBuffer(sb);
                    processAttributes(writer,nAttrs);
                    // Don't clear the attributes!
                    // We only want to see what would be written out
                    // at this point, we don't want to loose them.
                }
                sb.append('>');  // the potential > after the attributes.
                // convert the StringBuffer to a char array and
                // emit the trace event that these characters "might"
                // be written
                char ch[]=sb.toString().toCharArray();
                m_tracer.fireGenerateEvent(
                        SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                        ch,
                        0,
                        ch.length);
            }catch(IOException ioe){
                // ignore ?
            }catch(SAXException se){
                // ignore ?
            }
        }
    }

    static final class BoolStack{
        private boolean m_values[];
        private int m_allocatedSize;
        private int m_index;

        public BoolStack(){
            this(32);
        }

        public BoolStack(int size){
            m_allocatedSize=size;
            m_values=new boolean[size];
            m_index=-1;
        }

        public final int size(){
            return m_index+1;
        }

        public final void clear(){
            m_index=-1;
        }

        public final boolean push(boolean val){
            if(m_index==m_allocatedSize-1)
                grow();
            return (m_values[++m_index]=val);
        }

        private void grow(){
            m_allocatedSize*=2;
            boolean newVector[]=new boolean[m_allocatedSize];
            System.arraycopy(m_values,0,newVector,0,m_index+1);
            m_values=newVector;
        }

        public final boolean pop(){
            return m_values[m_index--];
        }

        public final boolean popAndTop(){
            m_index--;
            return (m_index>=0)?m_values[m_index]:false;
        }

        public final void setTop(boolean b){
            m_values[m_index]=b;
        }

        public final boolean peek(){
            return m_values[m_index];
        }

        public final boolean peekOrFalse(){
            return (m_index>-1)?m_values[m_index]:false;
        }

        public final boolean peekOrTrue(){
            return (m_index>-1)?m_values[m_index]:true;
        }

        public boolean isEmpty(){
            return (m_index==-1);
        }
    }

    private class WritertoStringBuffer extends Writer{
        final private StringBuffer m_stringbuf;

        WritertoStringBuffer(StringBuffer sb){
            m_stringbuf=sb;
        }

        public void write(int i){
            m_stringbuf.append((char)i);
        }

        public void write(char[] arg0,int arg1,int arg2) throws IOException{
            m_stringbuf.append(arg0,arg1,arg2);
        }

        public void write(String s){
            m_stringbuf.append(s);
        }

        public void flush() throws IOException{
        }

        public void close() throws IOException{
        }
    }






















}
