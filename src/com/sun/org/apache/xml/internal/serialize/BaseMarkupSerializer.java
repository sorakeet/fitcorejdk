/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Sep 14, 2000:
//  Fixed comments to preserve whitespaces and add a line break
//  when indenting. Reported by Gervase Markham <gerv@gerv.net>
// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>
// Sep 13, 2000:
//   CR in character data will print as &#0D;
// Aug 25, 2000:
//   Fixed processing instruction printing inside element content
//   to not escape content. Reported by Mikael Staldal
//   <d96-mst@d.kth.se>
// Aug 25, 2000:
//   Added ability to omit comments.
//   Contributed by Anupam Bagchi <abagchi@jtcsv.com>
// Aug 26, 2000:
//   Fixed bug in newline handling when preserving spaces.
//   Contributed by Mike Dusseault <mdusseault@home.com>
// Aug 29, 2000:
//   Fixed state.unescaped not being set to false when
//   entering element state.
//   Reported by Lowell Vaughn <lvaughn@agillion.com>
package com.sun.org.apache.xml.internal.serialize;

import com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import com.sun.org.apache.xerces.internal.dom.DOMLocatorImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import org.w3c.dom.*;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public abstract class BaseMarkupSerializer
        implements ContentHandler, DocumentHandler, LexicalHandler,
        DTDHandler, DeclHandler, DOMSerializer, Serializer{
    protected final DOMErrorImpl fDOMError=new DOMErrorImpl();
    protected final StringBuffer fStrBuffer=new StringBuffer(40);
    // DOM L3 implementation
    protected short features=0xFFFFFFFF;
    protected DOMErrorHandler fDOMErrorHandler;
    protected LSSerializerFilter fDOMFilter;
    protected EncodingInfo _encodingInfo;
    protected boolean _started;
    protected Map<String,String> _prefixes;
    protected String _docTypePublicId;
    protected String _docTypeSystemId;
    protected OutputFormat _format;
    protected Printer _printer;
    protected boolean _indenting;
    protected Node fCurrentNode=null;
    private ElementState[] _elementStates;
    private int _elementStateCount;
    private Vector _preRoot;
    private boolean _prepared;
    private Writer _writer;
    private OutputStream _output;
    //--------------------------------//
    // Constructor and initialization //
    //--------------------------------//

    protected BaseMarkupSerializer(OutputFormat format){
        int i;
        _elementStates=new ElementState[10];
        for(i=0;i<_elementStates.length;++i)
            _elementStates[i]=new ElementState();
        _format=format;
    }

    public void setOutputByteStream(OutputStream output){
        if(output==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "ArgumentIsNull",new Object[]{"output"});
            throw new NullPointerException(msg);
        }
        _output=output;
        _writer=null;
        reset();
    }

    public void setOutputCharStream(Writer writer){
        if(writer==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "ArgumentIsNull",new Object[]{"writer"});
            throw new NullPointerException(msg);
        }
        _writer=writer;
        _output=null;
        reset();
    }

    public void setOutputFormat(OutputFormat format){
        if(format==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "ArgumentIsNull",new Object[]{"format"});
            throw new NullPointerException(msg);
        }
        _format=format;
        reset();
    }

    public DocumentHandler asDocumentHandler()
            throws IOException{
        prepare();
        return this;
    }

    public ContentHandler asContentHandler()
            throws IOException{
        prepare();
        return this;
    }

    public DOMSerializer asDOMSerializer()
            throws IOException{
        prepare();
        return this;
    }

    protected void prepare()
            throws IOException{
        if(_prepared)
            return;
        if(_writer==null&&_output==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "NoWriterSupplied",null);
            throw new IOException(msg);
        }
        // If the output stream has been set, use it to construct
        // the writer. It is possible that the serializer has been
        // reused with the same output stream and different encoding.
        _encodingInfo=_format.getEncodingInfo();
        if(_output!=null){
            _writer=_encodingInfo.getWriter(_output);
        }
        if(_format.getIndenting()){
            _indenting=true;
            _printer=new IndentPrinter(_writer,_format);
        }else{
            _indenting=false;
            _printer=new Printer(_writer,_format);
        }
        ElementState state;
        _elementStateCount=0;
        state=_elementStates[0];
        state.namespaceURI=null;
        state.localName=null;
        state.rawName=null;
        state.preserveSpace=_format.getPreserveSpace();
        state.empty=true;
        state.afterElement=false;
        state.afterComment=false;
        state.doCData=state.inCData=false;
        state.prefixes=null;
        _docTypePublicId=_format.getDoctypePublic();
        _docTypeSystemId=_format.getDoctypeSystem();
        _started=false;
        _prepared=true;
    }

    public boolean reset(){
        if(_elementStateCount>1){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,
                    "ResetInMiddle",null);
            throw new IllegalStateException(msg);
        }
        _prepared=false;
        fCurrentNode=null;
        fStrBuffer.setLength(0);
        return true;
    }
    //----------------------------------//
    // DOM document serializing methods //
    //----------------------------------//

    public void serialize(Element elem)
            throws IOException{
        reset();
        prepare();
        serializeNode(elem);
        _printer.flush();
        if(_printer.getException()!=null)
            throw _printer.getException();
    }

    public void serialize(Document doc)
            throws IOException{
        reset();
        prepare();
        serializeNode(doc);
        serializePreRoot();
        _printer.flush();
        if(_printer.getException()!=null)
            throw _printer.getException();
    }

    public void serialize(DocumentFragment frag)
            throws IOException{
        reset();
        prepare();
        serializeNode(frag);
        _printer.flush();
        if(_printer.getException()!=null)
            throw _printer.getException();
    }

    public void serialize(Node node) throws IOException{
        reset();
        prepare();
        serializeNode(node);
        //Print any PIs and Comments which appeared in 'node'
        serializePreRoot();
        _printer.flush();
        if(_printer.getException()!=null)
            throw _printer.getException();
    }
    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//

    public void startNonEscaping(){
        ElementState state;
        state=getElementState();
        state.unescaped=true;
    }

    public void endNonEscaping(){
        ElementState state;
        state=getElementState();
        state.unescaped=false;
    }

    public void startPreserving(){
        ElementState state;
        state=getElementState();
        state.preserveSpace=true;
    }

    public void endPreserving(){
        ElementState state;
        state=getElementState();
        state.preserveSpace=false;
    }

    public void setDocumentLocator(Locator locator){
        // Nothing to do
    }

    public void startDocument()
            throws SAXException{
        try{
            prepare();
        }catch(IOException except){
            throw new SAXException(except.toString());
        }
        // Nothing to do here. All the magic happens in startDocument(String)
    }

    public void endDocument()
            throws SAXException{
        try{
            // Print all the elements accumulated outside of
            // the root element.
            serializePreRoot();
            // Flush the output, this is necessary for fStrBuffered output.
            _printer.flush();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        if(_prefixes==null)
            _prefixes=new HashMap<>();
        _prefixes.put(uri,prefix==null?"":prefix);
    }

    public void endPrefixMapping(String prefix)
            throws SAXException{
    }

    public void characters(char[] chars,int start,int length)
            throws SAXException{
        ElementState state;
        try{
            state=content();
            // Check if text should be print as CDATA section or unescaped
            // based on elements listed in the output format (the element
            // state) or whether we are inside a CDATA section or entity.
            if(state.inCData||state.doCData){
                int saveIndent;
                // Print a CDATA section. The text is not escaped, but ']]>'
                // appearing in the code must be identified and dealt with.
                // The contents of a text node is considered space preserving.
                if(!state.inCData){
                    _printer.printText("<![CDATA[");
                    state.inCData=true;
                }
                saveIndent=_printer.getNextIndent();
                _printer.setNextIndent(0);
                char ch;
                final int end=start+length;
                for(int index=start;index<end;++index){
                    ch=chars[index];
                    if(ch==']'&&index+2<end&&
                            chars[index+1]==']'&&chars[index+2]=='>'){
                        _printer.printText("]]]]><![CDATA[>");
                        index+=2;
                        continue;
                    }
                    if(!XMLChar.isValid(ch)){
                        // check if it is surrogate
                        if(++index<end){
                            surrogates(ch,chars[index]);
                        }else{
                            fatalError("The character '"+(char)ch+"' is an invalid XML character");
                        }
                        continue;
                    }else{
                        if((ch>=' '&&_encodingInfo.isPrintable((char)ch)&&ch!=0xF7)||
                                ch=='\n'||ch=='\r'||ch=='\t'){
                            _printer.printText((char)ch);
                        }else{
                            // The character is not printable -- split CDATA section
                            _printer.printText("]]>&#x");
                            _printer.printText(Integer.toHexString(ch));
                            _printer.printText(";<![CDATA[");
                        }
                    }
                }
                _printer.setNextIndent(saveIndent);
            }else{
                int saveIndent;
                if(state.preserveSpace){
                    // If preserving space then hold of indentation so no
                    // excessive spaces are printed at line breaks, escape
                    // the text content without replacing spaces and print
                    // the text breaking only at line breaks.
                    saveIndent=_printer.getNextIndent();
                    _printer.setNextIndent(0);
                    printText(chars,start,length,true,state.unescaped);
                    _printer.setNextIndent(saveIndent);
                }else{
                    printText(chars,start,length,false,state.unescaped);
                }
            }
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void ignorableWhitespace(char[] chars,int start,int length)
            throws SAXException{
        int i;
        try{
            content();
            // Print ignorable whitespaces only when indenting, after
            // all they are indentation. Cancel the indentation to
            // not indent twice.
            if(_indenting){
                _printer.setThisIndent(0);
                for(i=start;length-->0;++i)
                    _printer.printText(chars[i]);
            }
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public final void processingInstruction(String target,String code)
            throws SAXException{
        try{
            processingInstructionIO(target,code);
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void processingInstructionIO(String target,String code)
            throws IOException{
        int index;
        ElementState state;
        state=content();
        // Create the processing instruction textual representation.
        // Make sure we don't have '?>' inside either target or code.
        index=target.indexOf("?>");
        if(index>=0)
            fStrBuffer.append("<?").append(target.substring(0,index));
        else
            fStrBuffer.append("<?").append(target);
        if(code!=null){
            fStrBuffer.append(' ');
            index=code.indexOf("?>");
            if(index>=0)
                fStrBuffer.append(code.substring(0,index));
            else
                fStrBuffer.append(code);
        }
        fStrBuffer.append("?>");
        // If before the root element (or after it), do not print
        // the PI directly but place it in the pre-root vector.
        if(isDocumentState()){
            if(_preRoot==null)
                _preRoot=new Vector();
            _preRoot.addElement(fStrBuffer.toString());
        }else{
            _printer.indent();
            printText(fStrBuffer.toString(),true,true);
            _printer.unindent();
            if(_indenting)
                state.afterElement=true;
        }
        fStrBuffer.setLength(0);
    }

    protected void printText(String text,boolean preserveSpace,boolean unescaped)
            throws IOException{
        int index;
        char ch;
        if(preserveSpace){
            // Preserving spaces: the text must print exactly as it is,
            // without breaking when spaces appear in the text and without
            // consolidating spaces. If a line terminator is used, a line
            // break will occur.
            for(index=0;index<text.length();++index){
                ch=text.charAt(index);
                if(ch=='\n'||ch=='\r'||unescaped)
                    _printer.printText(ch);
                else
                    printEscaped(ch);
            }
        }else{
            // Not preserving spaces: print one part at a time, and
            // use spaces between parts to break them into different
            // lines. Spaces at beginning of line will be stripped
            // by printing mechanism. Line terminator is treated
            // no different than other text part.
            for(index=0;index<text.length();++index){
                ch=text.charAt(index);
                if(ch==' '||ch=='\f'||ch=='\t'||ch=='\n'||ch=='\r')
                    _printer.printSpace();
                else if(unescaped)
                    _printer.printText(ch);
                else
                    printEscaped(ch);
            }
        }
    }

    protected void printEscaped(int ch)
            throws IOException{
        String charRef;
        // If there is a suitable entity reference for this
        // character, print it. The list of available entity
        // references is almost but not identical between
        // XML and HTML.
        charRef=getEntityRef(ch);
        if(charRef!=null){
            _printer.printText('&');
            _printer.printText(charRef);
            _printer.printText(';');
        }else if((ch>=' '&&_encodingInfo.isPrintable((char)ch)&&ch!=0xF7)||
                ch=='\n'||ch=='\r'||ch=='\t'){
            // Non printables are below ASCII space but not tab or line
            // terminator, ASCII delete, or above a certain Unicode threshold.
            if(ch<0x10000){
                _printer.printText((char)ch);
            }else{
                _printer.printText((char)(((ch-0x10000)>>10)+0xd800));
                _printer.printText((char)(((ch-0x10000)&0x3ff)+0xdc00));
            }
        }else{
            printHex(ch);
        }
    }

    protected abstract String getEntityRef(int ch);

    final void printHex(int ch) throws IOException{
        _printer.printText("&#x");
        _printer.printText(Integer.toHexString(ch));
        _printer.printText(';');
    }
    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//

    public void skippedEntity(String name)
            throws SAXException{
        try{
            endCDATA();
            content();
            _printer.printText('&');
            _printer.printText(name);
            _printer.printText(';');
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    protected ElementState content()
            throws IOException{
        ElementState state;
        state=getElementState();
        if(!isDocumentState()){
            // Need to close CData section first
            if(state.inCData&&!state.doCData){
                _printer.printText("]]>");
                state.inCData=false;
            }
            // If this is the first content in the element,
            // change the state to not-empty and close the
            // opening element tag.
            if(state.empty){
                _printer.printText('>');
                state.empty=false;
            }
            // Except for one content type, all of them
            // are not last element. That one content
            // type will take care of itself.
            state.afterElement=false;
            // Except for one content type, all of them
            // are not last comment. That one content
            // type will take care of itself.
            state.afterComment=false;
        }
        return state;
    }

    protected ElementState getElementState(){
        return _elementStates[_elementStateCount];
    }
    //------------------------------------------//
    // SAX DTD/Decl handler serializing methods //
    //------------------------------------------//

    protected boolean isDocumentState(){
        return _elementStateCount==0;
    }

    protected void serializePreRoot()
            throws IOException{
        int i;
        if(_preRoot!=null){
            for(i=0;i<_preRoot.size();++i){
                printText((String)_preRoot.elementAt(i),true,true);
                if(_indenting)
                    _printer.breakLine();
            }
            _preRoot.removeAllElements();
        }
    }

    public final void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        try{
            _printer.enterDTD();
            _docTypePublicId=publicId;
            _docTypeSystemId=systemId;
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void endDTD(){
        // Nothing to do here, all the magic occurs in startDocument(String).
    }

    public void startEntity(String name){
        // ???
    }

    public void endEntity(String name){
        // ???
    }

    public void startCDATA(){
        ElementState state;
        state=getElementState();
        state.doCData=true;
    }

    public void endCDATA(){
        ElementState state;
        state=getElementState();
        state.doCData=false;
    }
    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//

    public void comment(char[] chars,int start,int length)
            throws SAXException{
        try{
            comment(new String(chars,start,length));
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void comment(String text)
            throws IOException{
        int index;
        ElementState state;
        if(_format.getOmitComments())
            return;
        state=content();
        // Create the processing comment textual representation.
        // Make sure we don't have '-->' inside the comment.
        index=text.indexOf("-->");
        if(index>=0)
            fStrBuffer.append("<!--").append(text.substring(0,index)).append("-->");
        else
            fStrBuffer.append("<!--").append(text).append("-->");
        // If before the root element (or after it), do not print
        // the comment directly but place it in the pre-root vector.
        if(isDocumentState()){
            if(_preRoot==null)
                _preRoot=new Vector();
            _preRoot.addElement(fStrBuffer.toString());
        }else{
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if(_indenting&&!state.preserveSpace)
                _printer.breakLine();
            _printer.indent();
            printText(fStrBuffer.toString(),true,true);
            _printer.unindent();
            if(_indenting)
                state.afterElement=true;
        }
        fStrBuffer.setLength(0);
        state.afterComment=true;
        state.afterElement=false;
    }

    public void elementDecl(String name,String model)
            throws SAXException{
        try{
            _printer.enterDTD();
            _printer.printText("<!ELEMENT ");
            _printer.printText(name);
            _printer.printText(' ');
            _printer.printText(model);
            _printer.printText('>');
            if(_indenting)
                _printer.breakLine();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void attributeDecl(String eName,String aName,String type,
                              String valueDefault,String value)
            throws SAXException{
        try{
            _printer.enterDTD();
            _printer.printText("<!ATTLIST ");
            _printer.printText(eName);
            _printer.printText(' ');
            _printer.printText(aName);
            _printer.printText(' ');
            _printer.printText(type);
            if(valueDefault!=null){
                _printer.printText(' ');
                _printer.printText(valueDefault);
            }
            if(value!=null){
                _printer.printText(" \"");
                printEscaped(value);
                _printer.printText('"');
            }
            _printer.printText('>');
            if(_indenting)
                _printer.breakLine();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
        try{
            _printer.enterDTD();
            _printer.printText("<!ENTITY ");
            _printer.printText(name);
            _printer.printText(" \"");
            printEscaped(value);
            _printer.printText("\">");
            if(_indenting)
                _printer.breakLine();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void externalEntityDecl(String name,String publicId,String systemId)
            throws SAXException{
        try{
            _printer.enterDTD();
            unparsedEntityDecl(name,publicId,systemId,null);
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    protected void printEscaped(String source)
            throws IOException{
        for(int i=0;i<source.length();++i){
            int ch=source.charAt(i);
            if((ch&0xfc00)==0xd800&&i+1<source.length()){
                int lowch=source.charAt(i+1);
                if((lowch&0xfc00)==0xdc00){
                    ch=0x10000+((ch-0xd800)<<10)+lowch-0xdc00;
                    i++;
                }
            }
            printEscaped(ch);
        }
    }

    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        try{
            _printer.enterDTD();
            if(publicId!=null){
                _printer.printText("<!NOTATION ");
                _printer.printText(name);
                _printer.printText(" PUBLIC ");
                printDoctypeURL(publicId);
                if(systemId!=null){
                    _printer.printText(' ');
                    printDoctypeURL(systemId);
                }
            }else{
                _printer.printText("<!NOTATION ");
                _printer.printText(name);
                _printer.printText(" SYSTEM ");
                printDoctypeURL(systemId);
            }
            _printer.printText('>');
            if(_indenting)
                _printer.breakLine();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }
    //---------------------------------------------//
    // Text pretty printing and formatting methods //
    //---------------------------------------------//

    public void unparsedEntityDecl(String name,String publicId,
                                   String systemId,String notationName)
            throws SAXException{
        try{
            _printer.enterDTD();
            if(publicId==null){
                _printer.printText("<!ENTITY ");
                _printer.printText(name);
                _printer.printText(" SYSTEM ");
                printDoctypeURL(systemId);
            }else{
                _printer.printText("<!ENTITY ");
                _printer.printText(name);
                _printer.printText(" PUBLIC ");
                printDoctypeURL(publicId);
                _printer.printText(' ');
                printDoctypeURL(systemId);
            }
            if(notationName!=null){
                _printer.printText(" NDATA ");
                _printer.printText(notationName);
            }
            _printer.printText('>');
            if(_indenting)
                _printer.breakLine();
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    protected void printDoctypeURL(String url)
            throws IOException{
        int i;
        _printer.printText('"');
        for(i=0;i<url.length();++i){
            if(url.charAt(i)=='"'||url.charAt(i)<0x20||url.charAt(i)>0x7F){
                _printer.printText('%');
                _printer.printText(Integer.toHexString(url.charAt(i)));
            }else
                _printer.printText(url.charAt(i));
        }
        _printer.printText('"');
    }

    protected void serializeNode(Node node)
            throws IOException{
        fCurrentNode=node;
        // Based on the node type call the suitable SAX handler.
        // Only comments entities and documents which are not
        // handled by SAX are serialized directly.
        switch(node.getNodeType()){
            case Node.TEXT_NODE:{
                String text;
                text=node.getNodeValue();
                if(text!=null){
                    if(fDOMFilter!=null&&
                            (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_TEXT)!=0){
                        short code=fDOMFilter.acceptNode(node);
                        switch(code){
                            case NodeFilter.FILTER_REJECT:
                            case NodeFilter.FILTER_SKIP:{
                                break;
                            }
                            default:{
                                characters(text);
                            }
                        }
                    }else if(!_indenting||getElementState().preserveSpace
                            ||(text.replace('\n',' ').trim().length()!=0))
                        characters(text);
                }
                break;
            }
            case Node.CDATA_SECTION_NODE:{
                String text=node.getNodeValue();
                if((features&DOMSerializerImpl.CDATA)!=0){
                    if(text!=null){
                        if(fDOMFilter!=null
                                &&(fDOMFilter.getWhatToShow()
                                &NodeFilter.SHOW_CDATA_SECTION)
                                !=0){
                            short code=fDOMFilter.acceptNode(node);
                            switch(code){
                                case NodeFilter.FILTER_REJECT:
                                case NodeFilter.FILTER_SKIP:{
                                    // skip the CDATA node
                                    return;
                                }
                                default:{
                                    //fall through..
                                }
                            }
                        }
                        startCDATA();
                        characters(text);
                        endCDATA();
                    }
                }else{
                    // transform into a text node
                    characters(text);
                }
                break;
            }
            case Node.COMMENT_NODE:{
                String text;
                if(!_format.getOmitComments()){
                    text=node.getNodeValue();
                    if(text!=null){
                        if(fDOMFilter!=null&&
                                (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_COMMENT)!=0){
                            short code=fDOMFilter.acceptNode(node);
                            switch(code){
                                case NodeFilter.FILTER_REJECT:
                                case NodeFilter.FILTER_SKIP:{
                                    // skip the comment node
                                    return;
                                }
                                default:{
                                    // fall through
                                }
                            }
                        }
                        comment(text);
                    }
                }
                break;
            }
            case Node.ENTITY_REFERENCE_NODE:{
                Node child;
                endCDATA();
                content();
                if(((features&DOMSerializerImpl.ENTITIES)!=0)
                        ||(node.getFirstChild()==null)){
                    if(fDOMFilter!=null&&
                            (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_ENTITY_REFERENCE)!=0){
                        short code=fDOMFilter.acceptNode(node);
                        switch(code){
                            case NodeFilter.FILTER_REJECT:{
                                return; // remove the node
                            }
                            case NodeFilter.FILTER_SKIP:{
                                child=node.getFirstChild();
                                while(child!=null){
                                    serializeNode(child);
                                    child=child.getNextSibling();
                                }
                                return;
                            }
                            default:{
                                // fall through
                            }
                        }
                    }
                    checkUnboundNamespacePrefixedNode(node);
                    _printer.printText("&");
                    _printer.printText(node.getNodeName());
                    _printer.printText(";");
                }else{
                    child=node.getFirstChild();
                    while(child!=null){
                        serializeNode(child);
                        child=child.getNextSibling();
                    }
                }
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE:{
                if(fDOMFilter!=null&&
                        (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_PROCESSING_INSTRUCTION)!=0){
                    short code=fDOMFilter.acceptNode(node);
                    switch(code){
                        case NodeFilter.FILTER_REJECT:
                        case NodeFilter.FILTER_SKIP:{
                            return;  // skip this node
                        }
                        default:{ // fall through
                        }
                    }
                }
                processingInstructionIO(node.getNodeName(),node.getNodeValue());
                break;
            }
            case Node.ELEMENT_NODE:{
                if(fDOMFilter!=null&&
                        (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_ELEMENT)!=0){
                    short code=fDOMFilter.acceptNode(node);
                    switch(code){
                        case NodeFilter.FILTER_REJECT:{
                            return;
                        }
                        case NodeFilter.FILTER_SKIP:{
                            Node child=node.getFirstChild();
                            while(child!=null){
                                serializeNode(child);
                                child=child.getNextSibling();
                            }
                            return;  // skip this node
                        }
                        default:{ // fall through
                        }
                    }
                }
                serializeElement((Element)node);
                break;
            }
            case Node.DOCUMENT_NODE:{
                DocumentType docType;
                DOMImplementation domImpl;
                NamedNodeMap map;
                Entity entity;
                Notation notation;
                int i;
                serializeDocument();
                // If there is a document type, use the SAX events to
                // serialize it.
                docType=((Document)node).getDoctype();
                if(docType!=null){
                    // DOM Level 2 (or higher)
                    domImpl=((Document)node).getImplementation();
                    try{
                        String internal;
                        _printer.enterDTD();
                        _docTypePublicId=docType.getPublicId();
                        _docTypeSystemId=docType.getSystemId();
                        internal=docType.getInternalSubset();
                        if(internal!=null&&internal.length()>0)
                            _printer.printText(internal);
                        endDTD();
                    }
                    // DOM Level 1 -- does implementation have methods?
                    catch(NoSuchMethodError nsme){
                        Class docTypeClass=docType.getClass();
                        String docTypePublicId=null;
                        String docTypeSystemId=null;
                        try{
                            java.lang.reflect.Method getPublicId=docTypeClass.getMethod("getPublicId",(Class[])null);
                            if(getPublicId.getReturnType().equals(String.class)){
                                docTypePublicId=(String)getPublicId.invoke(docType,(Object[])null);
                            }
                        }catch(Exception e){
                            // ignore
                        }
                        try{
                            java.lang.reflect.Method getSystemId=docTypeClass.getMethod("getSystemId",(Class[])null);
                            if(getSystemId.getReturnType().equals(String.class)){
                                docTypeSystemId=(String)getSystemId.invoke(docType,(Object[])null);
                            }
                        }catch(Exception e){
                            // ignore
                        }
                        _printer.enterDTD();
                        _docTypePublicId=docTypePublicId;
                        _docTypeSystemId=docTypeSystemId;
                        endDTD();
                    }
                    serializeDTD(docType.getName());
                }
                _started=true;
                // !! Fall through
            }
            case Node.DOCUMENT_FRAGMENT_NODE:{
                Node child;
                // By definition this will happen if the node is a document,
                // document fragment, etc. Just serialize its contents. It will
                // work well for other nodes that we do not know how to serialize.
                child=node.getFirstChild();
                while(child!=null){
                    serializeNode(child);
                    child=child.getNextSibling();
                }
                break;
            }
            default:
                break;
        }
    }

    protected void serializeDocument() throws IOException{
        int i;
        String dtd=_printer.leaveDTD();
        if(!_started){
            if(!_format.getOmitXMLDeclaration()){
                StringBuffer buffer;
                // Serialize the document declaration appreaing at the head
                // of very XML document (unless asked not to).
                buffer=new StringBuffer("<?xml version=\"");
                if(_format.getVersion()!=null)
                    buffer.append(_format.getVersion());
                else
                    buffer.append("1.0");
                buffer.append('"');
                String format_encoding=_format.getEncoding();
                if(format_encoding!=null){
                    buffer.append(" encoding=\"");
                    buffer.append(format_encoding);
                    buffer.append('"');
                }
                if(_format.getStandalone()&&_docTypeSystemId==null&&
                        _docTypePublicId==null)
                    buffer.append(" standalone=\"yes\"");
                buffer.append("?>");
                _printer.printText(buffer);
                _printer.breakLine();
            }
        }
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }

    protected void serializeDTD(String name) throws IOException{
        String dtd=_printer.leaveDTD();
        if(!_format.getOmitDocumentType()){
            if(_docTypeSystemId!=null){
                // System identifier must be specified to print DOCTYPE.
                // If public identifier is specified print 'PUBLIC
                // <public> <system>', if not, print 'SYSTEM <system>'.
                _printer.printText("<!DOCTYPE ");
                _printer.printText(name);
                if(_docTypePublicId!=null){
                    _printer.printText(" PUBLIC ");
                    printDoctypeURL(_docTypePublicId);
                    if(_indenting){
                        _printer.breakLine();
                        for(int i=0;i<18+name.length();++i)
                            _printer.printText(" ");
                    }else
                        _printer.printText(" ");
                    printDoctypeURL(_docTypeSystemId);
                }else{
                    _printer.printText(" SYSTEM ");
                    printDoctypeURL(_docTypeSystemId);
                }
                // If we accumulated any DTD contents while printing.
                // this would be the place to print it.
                if(dtd!=null&&dtd.length()>0){
                    _printer.printText(" [");
                    printText(dtd,true,true);
                    _printer.printText(']');
                }
                _printer.printText(">");
                _printer.breakLine();
            }else if(dtd!=null&&dtd.length()>0){
                _printer.printText("<!DOCTYPE ");
                _printer.printText(name);
                _printer.printText(" [");
                printText(dtd,true,true);
                _printer.printText("]>");
                _printer.breakLine();
            }
        }
    }

    protected void characters(String text)
            throws IOException{
        ElementState state;
        state=content();
        // Check if text should be print as CDATA section or unescaped
        // based on elements listed in the output format (the element
        // state) or whether we are inside a CDATA section or entity.
        if(state.inCData||state.doCData){
            int index;
            int saveIndent;
            // Print a CDATA section. The text is not escaped, but ']]>'
            // appearing in the code must be identified and dealt with.
            // The contents of a text node is considered space preserving.
            if(!state.inCData){
                _printer.printText("<![CDATA[");
                state.inCData=true;
            }
            saveIndent=_printer.getNextIndent();
            _printer.setNextIndent(0);
            printCDATAText(text);
            _printer.setNextIndent(saveIndent);
        }else{
            int saveIndent;
            if(state.preserveSpace){
                // If preserving space then hold of indentation so no
                // excessive spaces are printed at line breaks, escape
                // the text content without replacing spaces and print
                // the text breaking only at line breaks.
                saveIndent=_printer.getNextIndent();
                _printer.setNextIndent(0);
                printText(text,true,state.unescaped);
                _printer.setNextIndent(saveIndent);
            }else{
                printText(text,false,state.unescaped);
            }
        }
    }

    protected abstract void serializeElement(Element elem)
            throws IOException;

    protected void printCDATAText(String text) throws IOException{
        int length=text.length();
        char ch;
        for(int index=0;index<length;++index){
            ch=text.charAt(index);
            if(ch==']'
                    &&index+2<length
                    &&text.charAt(index+1)==']'
                    &&text.charAt(index+2)=='>'){ // check for ']]>'
                if(fDOMErrorHandler!=null){
                    // REVISIT: this means that if DOM Error handler is not registered we don't report any
                    // fatal errors and might serialize not wellformed document
                    if((features&DOMSerializerImpl.SPLITCDATA)==0){
                        String msg=DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.SERIALIZER_DOMAIN,
                                "EndingCDATA",
                                null);
                        if((features&DOMSerializerImpl.WELLFORMED)!=0){
                            // issue fatal error
                            modifyDOMError(msg,DOMError.SEVERITY_FATAL_ERROR,"wf-invalid-character",fCurrentNode);
                            fDOMErrorHandler.handleError(fDOMError);
                            throw new LSException(LSException.SERIALIZE_ERR,msg);
                        }else{
                            // issue error
                            modifyDOMError(msg,DOMError.SEVERITY_ERROR,"cdata-section-not-splitted",fCurrentNode);
                            if(!fDOMErrorHandler.handleError(fDOMError)){
                                throw new LSException(LSException.SERIALIZE_ERR,msg);
                            }
                        }
                    }else{
                        // issue warning
                        String msg=
                                DOMMessageFormatter.formatMessage(
                                        DOMMessageFormatter.SERIALIZER_DOMAIN,
                                        "SplittingCDATA",
                                        null);
                        modifyDOMError(
                                msg,
                                DOMError.SEVERITY_WARNING,
                                null,fCurrentNode);
                        fDOMErrorHandler.handleError(fDOMError);
                    }
                }
                // split CDATA section
                _printer.printText("]]]]><![CDATA[>");
                index+=2;
                continue;
            }
            if(!XMLChar.isValid(ch)){
                // check if it is surrogate
                if(++index<length){
                    surrogates(ch,text.charAt(index));
                }else{
                    fatalError("The character '"+(char)ch+"' is an invalid XML character");
                }
                continue;
            }else{
                if((ch>=' '&&_encodingInfo.isPrintable((char)ch)&&ch!=0xF7)||
                        ch=='\n'||ch=='\r'||ch=='\t'){
                    _printer.printText((char)ch);
                }else{
                    // The character is not printable -- split CDATA section
                    _printer.printText("]]>&#x");
                    _printer.printText(Integer.toHexString(ch));
                    _printer.printText(";<![CDATA[");
                }
            }
        }
    }
    //--------------------------------//
    // Element state handling methods //
    //--------------------------------//

    protected void surrogates(int high,int low) throws IOException{
        if(XMLChar.isHighSurrogate(high)){
            if(!XMLChar.isLowSurrogate(low)){
                //Invalid XML
                fatalError("The character '"+(char)low+"' is an invalid XML character");
            }else{
                int supplemental=XMLChar.supplemental((char)high,(char)low);
                if(!XMLChar.isValid(supplemental)){
                    //Invalid XML
                    fatalError("The character '"+(char)supplemental+"' is an invalid XML character");
                }else{
                    if(content().inCData){
                        _printer.printText("]]>&#x");
                        _printer.printText(Integer.toHexString(supplemental));
                        _printer.printText(";<![CDATA[");
                    }else{
                        printHex(supplemental);
                    }
                }
            }
        }else{
            fatalError("The character '"+(char)high+"' is an invalid XML character");
        }
    }

    protected void printText(char[] chars,int start,int length,
                             boolean preserveSpace,boolean unescaped)
            throws IOException{
        int index;
        char ch;
        if(preserveSpace){
            // Preserving spaces: the text must print exactly as it is,
            // without breaking when spaces appear in the text and without
            // consolidating spaces. If a line terminator is used, a line
            // break will occur.
            while(length-->0){
                ch=chars[start];
                ++start;
                if(ch=='\n'||ch=='\r'||unescaped)
                    _printer.printText(ch);
                else
                    printEscaped(ch);
            }
        }else{
            // Not preserving spaces: print one part at a time, and
            // use spaces between parts to break them into different
            // lines. Spaces at beginning of line will be stripped
            // by printing mechanism. Line terminator is treated
            // no different than other text part.
            while(length-->0){
                ch=chars[start];
                ++start;
                if(ch==' '||ch=='\f'||ch=='\t'||ch=='\n'||ch=='\r')
                    _printer.printSpace();
                else if(unescaped)
                    _printer.printText(ch);
                else
                    printEscaped(ch);
            }
        }
    }

    protected ElementState enterElementState(String namespaceURI,String localName,
                                             String rawName,boolean preserveSpace){
        ElementState state;
        if(_elementStateCount+1==_elementStates.length){
            ElementState[] newStates;
            // Need to create a larger array of states. This does not happen
            // often, unless the document is really deep.
            newStates=new ElementState[_elementStates.length+10];
            for(int i=0;i<_elementStates.length;++i)
                newStates[i]=_elementStates[i];
            for(int i=_elementStates.length;i<newStates.length;++i)
                newStates[i]=new ElementState();
            _elementStates=newStates;
        }
        ++_elementStateCount;
        state=_elementStates[_elementStateCount];
        state.namespaceURI=namespaceURI;
        state.localName=localName;
        state.rawName=rawName;
        state.preserveSpace=preserveSpace;
        state.empty=true;
        state.afterElement=false;
        state.afterComment=false;
        state.doCData=state.inCData=false;
        state.unescaped=false;
        state.prefixes=_prefixes;
        _prefixes=null;
        return state;
    }

    protected ElementState leaveElementState(){
        if(_elementStateCount>0){
            /**Corrected by David Blondeau (blondeau@intalio.com)*/
            _prefixes=null;
            //_prefixes = _elementStates[ _elementStateCount ].prefixes;
            --_elementStateCount;
            return _elementStates[_elementStateCount];
        }else{
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN,"Internal",null);
            throw new IllegalStateException(msg);
        }
    }

    protected String getPrefix(String namespaceURI){
        String prefix;
        if(_prefixes!=null){
            prefix=_prefixes.get(namespaceURI);
            if(prefix!=null)
                return prefix;
        }
        if(_elementStateCount==0)
            return null;
        else{
            for(int i=_elementStateCount;i>0;--i){
                if(_elementStates[i].prefixes!=null){
                    prefix=(String)_elementStates[i].prefixes.get(namespaceURI);
                    if(prefix!=null)
                        return prefix;
                }
            }
        }
        return null;
    }

    protected DOMError modifyDOMError(String message,short severity,String type,Node node){
        fDOMError.reset();
        fDOMError.fMessage=message;
        fDOMError.fType=type;
        fDOMError.fSeverity=severity;
        fDOMError.fLocator=new DOMLocatorImpl(-1,-1,-1,node,null);
        return fDOMError;
    }

    protected void fatalError(String message) throws IOException{
        if(fDOMErrorHandler!=null){
            modifyDOMError(message,DOMError.SEVERITY_FATAL_ERROR,null,fCurrentNode);
            fDOMErrorHandler.handleError(fDOMError);
        }else{
            throw new IOException(message);
        }
    }

    protected void checkUnboundNamespacePrefixedNode(Node node) throws IOException{
    }
}
