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
 * $Id: ToHTMLStream.java,v 1.2.4.1 2005/09/15 08:15:26 suresh_emailid Exp $
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
 * $Id: ToHTMLStream.java,v 1.2.4.1 2005/09/15 08:15:26 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public final class ToHTMLStream extends ToStream{
    static final Trie m_elementFlags=new Trie();
    private static final CharInfo m_htmlcharInfo=
//        new CharInfo(CharInfo.HTML_ENTITIES_RESOURCE);
            CharInfo.getCharInfoInternal(CharInfo.HTML_ENTITIES_RESOURCE,Method.HTML);
    static private final ElemDesc m_dummy=new ElemDesc(0|ElemDesc.BLOCK);

    static{
        initTagReference(m_elementFlags);
    }

    protected boolean m_inDTD=false;
    private boolean m_inBlockElem=false;
    private boolean m_specialEscapeURLs=true;
    private boolean m_omitMetaTag=false;
    private Trie m_htmlInfo=new Trie(m_elementFlags);

    public ToHTMLStream(){
        super();
        m_charInfo=m_htmlcharInfo;
        // initialize namespaces
        m_prefixMap=new NamespaceMappings();
    }

    static void initTagReference(Trie m_elementFlags){
        // HTML 4.0 loose DTD
        m_elementFlags.put("BASEFONT",new ElemDesc(0|ElemDesc.EMPTY));
        m_elementFlags.put(
                "FRAME",
                new ElemDesc(0|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put("FRAMESET",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("NOFRAMES",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put(
                "ISINDEX",
                new ElemDesc(0|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put(
                "APPLET",
                new ElemDesc(0|ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put("CENTER",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("DIR",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("MENU",new ElemDesc(0|ElemDesc.BLOCK));
        // HTML 4.0 strict DTD
        m_elementFlags.put("TT",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("I",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("B",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("BIG",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("SMALL",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("EM",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("STRONG",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("DFN",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("CODE",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("SAMP",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("KBD",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("VAR",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("CITE",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("ABBR",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put("ACRONYM",new ElemDesc(0|ElemDesc.PHRASE));
        m_elementFlags.put(
                "SUP",
                new ElemDesc(0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL));
        m_elementFlags.put(
                "SUB",
                new ElemDesc(0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL));
        m_elementFlags.put(
                "SPAN",
                new ElemDesc(0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL));
        m_elementFlags.put(
                "BDO",
                new ElemDesc(0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL));
        m_elementFlags.put(
                "BR",
                new ElemDesc(
                        0
                                |ElemDesc.SPECIAL
                                |ElemDesc.ASPECIAL
                                |ElemDesc.EMPTY
                                |ElemDesc.BLOCK));
        m_elementFlags.put("BODY",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put(
                "ADDRESS",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
                "DIV",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("A",new ElemDesc(0|ElemDesc.SPECIAL));
        m_elementFlags.put(
                "MAP",
                new ElemDesc(
                        0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL|ElemDesc.BLOCK));
        m_elementFlags.put(
                "AREA",
                new ElemDesc(0|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put(
                "LINK",
                new ElemDesc(
                        0|ElemDesc.HEADMISC|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put(
                "IMG",
                new ElemDesc(
                        0
                                |ElemDesc.SPECIAL
                                |ElemDesc.ASPECIAL
                                |ElemDesc.EMPTY
                                |ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put(
                "OBJECT",
                new ElemDesc(
                        0
                                |ElemDesc.SPECIAL
                                |ElemDesc.ASPECIAL
                                |ElemDesc.HEADMISC
                                |ElemDesc.WHITESPACESENSITIVE));
        m_elementFlags.put("PARAM",new ElemDesc(0|ElemDesc.EMPTY));
        m_elementFlags.put(
                "HR",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET
                                |ElemDesc.EMPTY));
        m_elementFlags.put(
                "P",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put(
                "H1",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "H2",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "H3",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "H4",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "H5",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "H6",
                new ElemDesc(0|ElemDesc.HEAD|ElemDesc.BLOCK));
        m_elementFlags.put(
                "PRE",
                new ElemDesc(0|ElemDesc.PREFORMATTED|ElemDesc.BLOCK));
        m_elementFlags.put(
                "Q",
                new ElemDesc(0|ElemDesc.SPECIAL|ElemDesc.ASPECIAL));
        m_elementFlags.put(
                "BLOCKQUOTE",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("INS",new ElemDesc(0));
        m_elementFlags.put("DEL",new ElemDesc(0));
        m_elementFlags.put(
                "DL",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("DT",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("DD",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put(
                "OL",
                new ElemDesc(0|ElemDesc.LIST|ElemDesc.BLOCK));
        m_elementFlags.put(
                "UL",
                new ElemDesc(0|ElemDesc.LIST|ElemDesc.BLOCK));
        m_elementFlags.put("LI",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("FORM",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("LABEL",new ElemDesc(0|ElemDesc.FORMCTRL));
        m_elementFlags.put(
                "INPUT",
                new ElemDesc(
                        0|ElemDesc.FORMCTRL|ElemDesc.INLINELABEL|ElemDesc.EMPTY));
        m_elementFlags.put(
                "SELECT",
                new ElemDesc(0|ElemDesc.FORMCTRL|ElemDesc.INLINELABEL));
        m_elementFlags.put("OPTGROUP",new ElemDesc(0));
        m_elementFlags.put("OPTION",new ElemDesc(0));
        m_elementFlags.put(
                "TEXTAREA",
                new ElemDesc(0|ElemDesc.FORMCTRL|ElemDesc.INLINELABEL));
        m_elementFlags.put(
                "FIELDSET",
                new ElemDesc(0|ElemDesc.BLOCK|ElemDesc.BLOCKFORM));
        m_elementFlags.put("LEGEND",new ElemDesc(0));
        m_elementFlags.put(
                "BUTTON",
                new ElemDesc(0|ElemDesc.FORMCTRL|ElemDesc.INLINELABEL));
        m_elementFlags.put(
                "TABLE",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("CAPTION",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("THEAD",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("TFOOT",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("TBODY",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("COLGROUP",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put(
                "COL",
                new ElemDesc(0|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put("TR",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put("TH",new ElemDesc(0));
        m_elementFlags.put("TD",new ElemDesc(0));
        m_elementFlags.put(
                "HEAD",
                new ElemDesc(0|ElemDesc.BLOCK|ElemDesc.HEADELEM));
        m_elementFlags.put("TITLE",new ElemDesc(0|ElemDesc.BLOCK));
        m_elementFlags.put(
                "BASE",
                new ElemDesc(0|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put(
                "META",
                new ElemDesc(
                        0|ElemDesc.HEADMISC|ElemDesc.EMPTY|ElemDesc.BLOCK));
        m_elementFlags.put(
                "STYLE",
                new ElemDesc(
                        0|ElemDesc.HEADMISC|ElemDesc.RAW|ElemDesc.BLOCK));
        m_elementFlags.put(
                "SCRIPT",
                new ElemDesc(
                        0
                                |ElemDesc.SPECIAL
                                |ElemDesc.ASPECIAL
                                |ElemDesc.HEADMISC
                                |ElemDesc.RAW));
        m_elementFlags.put(
                "NOSCRIPT",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        m_elementFlags.put("HTML",new ElemDesc(0|ElemDesc.BLOCK));
        // From "John Ky" <hand@syd.speednet.com.au
        // Transitional Document Type Definition ()
        // file:///C:/Documents%20and%20Settings/sboag.BOAG600E/My%20Documents/html/sgml/loosedtd.html#basefont
        m_elementFlags.put("FONT",new ElemDesc(0|ElemDesc.FONTSTYLE));
        // file:///C:/Documents%20and%20Settings/sboag.BOAG600E/My%20Documents/html/present/graphics.html#edef-STRIKE
        m_elementFlags.put("S",new ElemDesc(0|ElemDesc.FONTSTYLE));
        m_elementFlags.put("STRIKE",new ElemDesc(0|ElemDesc.FONTSTYLE));
        // file:///C:/Documents%20and%20Settings/sboag.BOAG600E/My%20Documents/html/present/graphics.html#edef-U
        m_elementFlags.put("U",new ElemDesc(0|ElemDesc.FONTSTYLE));
        // From "John Ky" <hand@syd.speednet.com.au
        m_elementFlags.put("NOBR",new ElemDesc(0|ElemDesc.FONTSTYLE));
        // HTML 4.0, section 16.5
        m_elementFlags.put(
                "IFRAME",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        // Netscape 4 extension
        m_elementFlags.put(
                "LAYER",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        // Netscape 4 extension
        m_elementFlags.put(
                "ILAYER",
                new ElemDesc(
                        0
                                |ElemDesc.BLOCK
                                |ElemDesc.BLOCKFORM
                                |ElemDesc.BLOCKFORMFIELDSET));
        // NOW FOR ATTRIBUTE INFORMATION . . .
        ElemDesc elemDesc;
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("A");
        elemDesc.setAttr("HREF",ElemDesc.ATTRURL);
        elemDesc.setAttr("NAME",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("AREA");
        elemDesc.setAttr("HREF",ElemDesc.ATTRURL);
        elemDesc.setAttr("NOHREF",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("BASE");
        elemDesc.setAttr("HREF",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("BUTTON");
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("BLOCKQUOTE");
        elemDesc.setAttr("CITE",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("DEL");
        elemDesc.setAttr("CITE",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("DIR");
        elemDesc.setAttr("COMPACT",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("DIV");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL); // Netscape 4 extension
        elemDesc.setAttr("NOWRAP",ElemDesc.ATTREMPTY); // Internet-Explorer extension
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("DL");
        elemDesc.setAttr("COMPACT",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("FORM");
        elemDesc.setAttr("ACTION",ElemDesc.ATTRURL);
        // ----------------------------------------------
        // Attribution to: "Voytenko, Dimitry" <DVoytenko@SECTORBASE.COM>
        elemDesc=(ElemDesc)m_elementFlags.get("FRAME");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC",ElemDesc.ATTRURL);
        elemDesc.setAttr("NORESIZE",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("HEAD");
        elemDesc.setAttr("PROFILE",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("HR");
        elemDesc.setAttr("NOSHADE",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        // HTML 4.0, section 16.5
        elemDesc=(ElemDesc)m_elementFlags.get("IFRAME");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC",ElemDesc.ATTRURL);
        // ----------------------------------------------
        // Netscape 4 extension
        elemDesc=(ElemDesc)m_elementFlags.get("ILAYER");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("IMG");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        elemDesc.setAttr("LONGDESC",ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP",ElemDesc.ATTRURL);
        elemDesc.setAttr("ISMAP",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("INPUT");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP",ElemDesc.ATTRURL);
        elemDesc.setAttr("CHECKED",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("ISMAP",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("READONLY",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("INS");
        elemDesc.setAttr("CITE",ElemDesc.ATTRURL);
        // ----------------------------------------------
        // Netscape 4 extension
        elemDesc=(ElemDesc)m_elementFlags.get("LAYER");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("LINK");
        elemDesc.setAttr("HREF",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("MENU");
        elemDesc.setAttr("COMPACT",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("OBJECT");
        elemDesc.setAttr("CLASSID",ElemDesc.ATTRURL);
        elemDesc.setAttr("CODEBASE",ElemDesc.ATTRURL);
        elemDesc.setAttr("DATA",ElemDesc.ATTRURL);
        elemDesc.setAttr("ARCHIVE",ElemDesc.ATTRURL);
        elemDesc.setAttr("USEMAP",ElemDesc.ATTRURL);
        elemDesc.setAttr("DECLARE",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("OL");
        elemDesc.setAttr("COMPACT",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("OPTGROUP");
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("OPTION");
        elemDesc.setAttr("SELECTED",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("Q");
        elemDesc.setAttr("CITE",ElemDesc.ATTRURL);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("SCRIPT");
        elemDesc.setAttr("SRC",ElemDesc.ATTRURL);
        elemDesc.setAttr("FOR",ElemDesc.ATTRURL);
        elemDesc.setAttr("DEFER",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("SELECT");
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("MULTIPLE",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("TABLE");
        elemDesc.setAttr("NOWRAP",ElemDesc.ATTREMPTY); // Internet-Explorer extension
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("TD");
        elemDesc.setAttr("NOWRAP",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("TEXTAREA");
        elemDesc.setAttr("DISABLED",ElemDesc.ATTREMPTY);
        elemDesc.setAttr("READONLY",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("TH");
        elemDesc.setAttr("NOWRAP",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        // The nowrap attribute of a tr element is both
        // a Netscape and Internet-Explorer extension
        elemDesc=(ElemDesc)m_elementFlags.get("TR");
        elemDesc.setAttr("NOWRAP",ElemDesc.ATTREMPTY);
        // ----------------------------------------------
        elemDesc=(ElemDesc)m_elementFlags.get("UL");
        elemDesc.setAttr("COMPACT",ElemDesc.ATTREMPTY);
    }

    public static final ElemDesc getElemDesc(String name){
        /** this method used to return m_dummy  when name was null
         * but now it doesn't check and and requires non-null name.
         */
        Object obj=m_elementFlags.get(name);
        if(null!=obj)
            return (ElemDesc)obj;
        return m_dummy;
    }

    private final boolean getSpecialEscapeURLs(){
        return m_specialEscapeURLs;
    }

    public void setSpecialEscapeURLs(boolean bool){
        m_specialEscapeURLs=bool;
    }

    private final boolean getOmitMetaTag(){
        return m_omitMetaTag;
    }

    public void setOmitMetaTag(boolean bool){
        m_omitMetaTag=bool;
    }

    public final void endDocument() throws SAXException{
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
        // Process any pending starDocument and startElement first.
        flushPending();
        // Use a fairly nasty hack to tell if the next node is supposed to be
        // unescaped text.
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
                //writer.write("<?" + target);
                writer.write("<?");
                writer.write(target);
                if(data.length()>0&&!Character.isSpaceChar(data.charAt(0)))
                    writer.write(' ');
                //writer.write(data + ">"); // different from XML
                writer.write(data); // different from XML
                writer.write('>'); // different from XML
                // Always output a newline char if not inside of an
                // element. The whitespace is not significant in that
                // case.
                if(m_elemContext.m_currentElemDepth<=0)
                    outputLineSep();
                m_startNewLine=true;
            }catch(IOException e){
                throw new SAXException(e);
            }
        }
        // now generate the PI event
        if(m_tracer!=null)
            super.fireEscapingEvent(target,data);
    }
    //    private String m_currentElementName = null;

    private boolean isASCIIDigit(char c){
        return (c>='0'&&c<='9');
    }

    private boolean isHHSign(String str){
        boolean sign=true;
        try{
            char r=(char)Integer.parseInt(str,16);
        }catch(NumberFormatException e){
            sign=false;
        }
        return sign;
    }

    protected synchronized void init(OutputStream output,Properties format)
            throws UnsupportedEncodingException{
        if(null==format){
            format=OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
        }
        super.init(output,format,false);
    }

    public void namespaceAfterStartElement(String prefix,String uri)
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

    public final void entityReference(String name)
            throws SAXException{
        try{
            final java.io.Writer writer=m_writer;
            writer.write('&');
            writer.write(name);
            writer.write(';');
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    protected void startDocumentInternal() throws SAXException{
        super.startDocumentInternal();
        m_needToCallStartDocument=false;
        m_needToOutputDocTypeDecl=true;
        m_startNewLine=false;
        setOmitXMLDeclaration(true);
        if(true==m_needToOutputDocTypeDecl){
            String doctypeSystem=getDoctypeSystem();
            String doctypePublic=getDoctypePublic();
            if((null!=doctypeSystem)||(null!=doctypePublic)){
                final java.io.Writer writer=m_writer;
                try{
                    writer.write("<!DOCTYPE html");
                    if(null!=doctypePublic){
                        writer.write(" PUBLIC \"");
                        writer.write(doctypePublic);
                        writer.write('"');
                    }
                    if(null!=doctypeSystem){
                        if(null==doctypePublic)
                            writer.write(" SYSTEM \"");
                        else
                            writer.write(" \"");
                        writer.write(doctypeSystem);
                        writer.write('"');
                    }
                    writer.write('>');
                    outputLineSep();
                }catch(IOException e){
                    throw new SAXException(e);
                }
            }
        }
        m_needToOutputDocTypeDecl=false;
    }

    public void elementDecl(String name,String model) throws SAXException{
        // The internal DTD subset is not serialized by the ToHTMLStream serializer
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
        // The internal DTD subset is not serialized by the ToHTMLStream serializer
    }

    public void setOutputFormat(Properties format){
        m_specialEscapeURLs=
                OutputPropertyUtils.getBooleanProperty(
                        OutputPropertiesFactory.S_USE_URL_ESCAPING,
                        format);
        m_omitMetaTag=
                OutputPropertyUtils.getBooleanProperty(
                        OutputPropertiesFactory.S_OMIT_META_TAG,
                        format);
        super.setOutputFormat(format);
    }

    public void setOutputStream(OutputStream output){
        try{
            Properties format;
            if(null==m_format)
                format=OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
            else
                format=m_format;
            init(output,format,true);
        }catch(UnsupportedEncodingException uee){
            // Should have been warned in init, I guess...
        }
    }

    public void attributeDecl(
            String eName,
            String aName,
            String type,
            String valueDefault,
            String value)
            throws SAXException{
        // The internal DTD subset is not serialized by the ToHTMLStream serializer
    }

    public void externalEntityDecl(
            String name,
            String publicId,
            String systemId)
            throws SAXException{
        // The internal DTD subset is not serialized by the ToHTMLStream serializer
    }

    public final void cdata(char ch[],int start,int length)
            throws SAXException{
        if((null!=m_elemContext.m_elementName)
                &&(m_elemContext.m_elementName.equalsIgnoreCase("SCRIPT")
                ||m_elemContext.m_elementName.equalsIgnoreCase("STYLE"))){
            try{
                if(m_elemContext.m_startTagOpen){
                    closeStartTag();
                    m_elemContext.m_startTagOpen=false;
                }
                m_ispreserve=true;
                if(shouldIndent())
                    indent();
                // writer.write(ch, start, length);
                writeNormalizedChars(ch,start,length,true,m_lineSepUse);
            }catch(IOException ioe){
                throw new SAXException(
                        Utils.messages.createMessage(
                                MsgKey.ER_OIERROR,
                                null),
                        ioe);
                //"IO error", ioe);
            }
        }else{
            super.cdata(ch,start,length);
        }
    }

    public final void characters(char chars[],int start,int length)
            throws SAXException{
        if(m_elemContext.m_isRaw){
            try{
                if(m_elemContext.m_startTagOpen){
                    closeStartTag();
                    m_elemContext.m_startTagOpen=false;
                }
                m_ispreserve=true;
//              With m_ispreserve just set true it looks like shouldIndent()
//              will always return false, so drop any possible indentation.
//              if (shouldIndent())
//                  indent();
                // writer.write("<![CDATA[");
                // writer.write(chars, start, length);
                writeNormalizedChars(chars,start,length,false,m_lineSepUse);
                // writer.write("]]>");
                // time to generate characters event
                if(m_tracer!=null)
                    super.fireCharEvent(chars,start,length);
                return;
            }catch(IOException ioe){
                throw new SAXException(
                        Utils.messages.createMessage(
                                MsgKey.ER_OIERROR,
                                null),
                        ioe);
                //"IO error", ioe);
            }
        }else{
            super.characters(chars,start,length);
        }
    }

    public void startElement(
            String namespaceURI,
            String localName,
            String name,
            Attributes atts)
            throws SAXException{
        ElemContext elemContext=m_elemContext;
        // clean up any pending things first
        if(elemContext.m_startTagOpen){
            closeStartTag();
            elemContext.m_startTagOpen=false;
        }else if(m_cdataTagOpen){
            closeCDATA();
            m_cdataTagOpen=false;
        }else if(m_needToCallStartDocument){
            startDocumentInternal();
            m_needToCallStartDocument=false;
        }
        // if this element has a namespace then treat it like XML
        if(null!=namespaceURI&&namespaceURI.length()>0){
            super.startElement(namespaceURI,localName,name,atts);
            return;
        }
        try{
            // getElemDesc2(name) is faster than getElemDesc(name)
            ElemDesc elemDesc=getElemDesc2(name);
            int elemFlags=elemDesc.getFlags();
            // deal with indentation issues first
            if(m_doIndent){
                boolean isBlockElement=(elemFlags&ElemDesc.BLOCK)!=0;
                if(m_ispreserve)
                    m_ispreserve=false;
                else if(
                        (null!=elemContext.m_elementName)
                                &&(!m_inBlockElem
                                ||isBlockElement) /** && !isWhiteSpaceSensitive */
                        ){
                    m_startNewLine=true;
                    indent();
                }
                m_inBlockElem=!isBlockElement;
            }
            // save any attributes for later processing
            if(atts!=null)
                addAttributes(atts);
            m_isprevtext=false;
            final java.io.Writer writer=m_writer;
            writer.write('<');
            writer.write(name);
            if(m_tracer!=null)
                firePseudoAttributes();
            if((elemFlags&ElemDesc.EMPTY)!=0){
                // an optimization for elements which are expected
                // to be empty.
                m_elemContext=elemContext.push();
                /** XSLTC sometimes calls namespaceAfterStartElement()
                 * so we need to remember the name
                 */
                m_elemContext.m_elementName=name;
                m_elemContext.m_elementDesc=elemDesc;
                return;
            }else{
                elemContext=elemContext.push(namespaceURI,localName,name);
                m_elemContext=elemContext;
                elemContext.m_elementDesc=elemDesc;
                elemContext.m_isRaw=(elemFlags&ElemDesc.RAW)!=0;
            }
            if((elemFlags&ElemDesc.HEADELEM)!=0){
                // This is the <HEAD> element, do some special processing
                closeStartTag();
                elemContext.m_startTagOpen=false;
                if(!m_omitMetaTag){
                    if(m_doIndent)
                        indent();
                    writer.write(
                            "<META http-equiv=\"Content-Type\" content=\"text/html; charset=");
                    String encoding=getEncoding();
                    String encode=Encodings.getMimeEncoding(encoding);
                    writer.write(encode);
                    writer.write("\">");
                }
            }
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    private ElemDesc getElemDesc2(String name){
        Object obj=m_htmlInfo.get2(name);
        if(null!=obj)
            return (ElemDesc)obj;
        return m_dummy;
    }

    public void processAttributes(java.io.Writer writer,int nAttrs)
            throws IOException, SAXException{
        /**
         * process the collected attributes
         */
        for(int i=0;i<nAttrs;i++){
            processAttribute(
                    writer,
                    m_attributes.getQName(i),
                    m_attributes.getValue(i),
                    m_elemContext.m_elementDesc);
        }
    }

    protected void processAttribute(
            java.io.Writer writer,
            String name,
            String value,
            ElemDesc elemDesc)
            throws IOException{
        writer.write(' ');
        if(((value.length()==0)||value.equalsIgnoreCase(name))
                &&elemDesc!=null
                &&elemDesc.isAttrFlagSet(name,ElemDesc.ATTREMPTY)){
            writer.write(name);
        }else{
            // %REVIEW% %OPT%
            // Two calls to single-char write may NOT
            // be more efficient than one to string-write...
            writer.write(name);
            writer.write("=\"");
            if(elemDesc!=null
                    &&elemDesc.isAttrFlagSet(name,ElemDesc.ATTRURL))
                writeAttrURI(writer,value,m_specialEscapeURLs);
            else
                writeAttrString(writer,value,this.getEncoding());
            writer.write('"');
        }
    }

    public void writeAttrURI(
            final java.io.Writer writer,String string,boolean doURLEscaping)
            throws IOException{
        // http://www.ietf.org/rfc/rfc2396.txt says:
        // A URI is always in an "escaped" form, since escaping or unescaping a
        // completed URI might change its semantics.  Normally, the only time
        // escape encodings can safely be made is when the URI is being created
        // from its component parts; each component may have its own set of
        // characters that are reserved, so only the mechanism responsible for
        // generating or interpreting that component can determine whether or
        // not escaping a character will change its semantics. Likewise, a URI
        // must be separated into its components before the escaped characters
        // within those components can be safely decoded.
        //
        // ...So we do our best to do limited escaping of the URL, without
        // causing damage.  If the URL is already properly escaped, in theory, this
        // function should not change the string value.
        final int end=string.length();
        if(end>m_attrBuff.length){
            m_attrBuff=new char[end*2+1];
        }
        string.getChars(0,end,m_attrBuff,0);
        final char[] chars=m_attrBuff;
        int cleanStart=0;
        int cleanLength=0;
        char ch=0;
        for(int i=0;i<end;i++){
            ch=chars[i];
            if((ch<32)||(ch>126)){
                if(cleanLength>0){
                    writer.write(chars,cleanStart,cleanLength);
                    cleanLength=0;
                }
                if(doURLEscaping){
                    // Encode UTF16 to UTF8.
                    // Reference is Unicode, A Primer, by Tony Graham.
                    // Page 92.
                    // Note that Kay doesn't escape 0x20...
                    //  if(ch == 0x20) // Not sure about this... -sb
                    //  {
                    //    writer.write(ch);
                    //  }
                    //  else
                    if(ch<=0x7F){
                        writer.write('%');
                        writer.write(makeHHString(ch));
                    }else if(ch<=0x7FF){
                        // Clear low 6 bits before rotate, put high 4 bits in low byte,
                        // and set two high bits.
                        int high=(ch>>6)|0xC0;
                        int low=(ch&0x3F)|0x80;
                        // First 6 bits, + high bit
                        writer.write('%');
                        writer.write(makeHHString(high));
                        writer.write('%');
                        writer.write(makeHHString(low));
                    }else if(Encodings.isHighUTF16Surrogate(ch)) // high surrogate
                    {
                        // I'm sure this can be done in 3 instructions, but I choose
                        // to try and do it exactly like it is done in the book, at least
                        // until we are sure this is totally clean.  I don't think performance
                        // is a big issue with this particular function, though I could be
                        // wrong.  Also, the stuff below clearly does more masking than
                        // it needs to do.
                        // Clear high 6 bits.
                        int highSurrogate=((int)ch)&0x03FF;
                        // Middle 4 bits (wwww) + 1
                        // "Note that the value of wwww from the high surrogate bit pattern
                        // is incremented to make the uuuuu bit pattern in the scalar value
                        // so the surrogate pair don't address the BMP."
                        int wwww=((highSurrogate&0x03C0)>>6);
                        int uuuuu=wwww+1;
                        // next 4 bits
                        int zzzz=(highSurrogate&0x003C)>>2;
                        // low 2 bits
                        int yyyyyy=((highSurrogate&0x0003)<<4)&0x30;
                        // Get low surrogate character.
                        ch=chars[++i];
                        // Clear high 6 bits.
                        int lowSurrogate=((int)ch)&0x03FF;
                        // put the middle 4 bits into the bottom of yyyyyy (byte 3)
                        yyyyyy=yyyyyy|((lowSurrogate&0x03C0)>>6);
                        // bottom 6 bits.
                        int xxxxxx=(lowSurrogate&0x003F);
                        int byte1=0xF0|(uuuuu>>2); // top 3 bits of uuuuu
                        int byte2=
                                0x80|(((uuuuu&0x03)<<4)&0x30)|zzzz;
                        int byte3=0x80|yyyyyy;
                        int byte4=0x80|xxxxxx;
                        writer.write('%');
                        writer.write(makeHHString(byte1));
                        writer.write('%');
                        writer.write(makeHHString(byte2));
                        writer.write('%');
                        writer.write(makeHHString(byte3));
                        writer.write('%');
                        writer.write(makeHHString(byte4));
                    }else{
                        int high=(ch>>12)|0xE0; // top 4 bits
                        int middle=((ch&0x0FC0)>>6)|0x80;
                        // middle 6 bits
                        int low=(ch&0x3F)|0x80;
                        // First 6 bits, + high bit
                        writer.write('%');
                        writer.write(makeHHString(high));
                        writer.write('%');
                        writer.write(makeHHString(middle));
                        writer.write('%');
                        writer.write(makeHHString(low));
                    }
                }else if(escapingNotNeeded(ch)){
                    writer.write(ch);
                }else{
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                // In this character range we have first written out any previously accumulated
                // "clean" characters, then processed the current more complicated character,
                // which may have incremented "i".
                // We now we reset the next possible clean character.
                cleanStart=i+1;
            }
            // Since http://www.ietf.org/rfc/rfc2396.txt refers to the URI grammar as
            // not allowing quotes in the URI proper syntax, nor in the fragment
            // identifier, we believe that it's OK to double escape quotes.
            else if(ch=='"'){
                // If the character is a '%' number number, try to avoid double-escaping.
                // There is a question if this is legal behavior.
                // Dmitri Ilyin: to check if '%' number number is invalid. It must be checked if %xx is a sign, that would be encoded
                // The encoded signes are in Hex form. So %xx my be in form %3C that is "<" sign. I will try to change here a little.
                //        if( ((i+2) < len) && isASCIIDigit(stringArray[i+1]) && isASCIIDigit(stringArray[i+2]) )
                // We are no longer escaping '%'
                if(cleanLength>0){
                    writer.write(chars,cleanStart,cleanLength);
                    cleanLength=0;
                }
                // Mike Kay encodes this as &#34;, so he may know something I don't?
                if(doURLEscaping)
                    writer.write("%22");
                else
                    writer.write("&quot;"); // we have to escape this, I guess.
                // We have written out any clean characters, then the escaped '%' and now we
                // We now we reset the next possible clean character.
                cleanStart=i+1;
            }else if(ch=='&'){
                // HTML 4.01 reads, "Authors should use "&amp;" (ASCII decimal 38)
                // instead of "&" to avoid confusion with the beginning of a character
                // reference (entity reference open delimiter).
                if(cleanLength>0){
                    writer.write(chars,cleanStart,cleanLength);
                    cleanLength=0;
                }
                writer.write("&amp;");
                cleanStart=i+1;
            }else{
                // no processing for this character, just count how
                // many characters in a row that we have that need no processing
                cleanLength++;
            }
        }
        // are there any clean characters at the end of the array
        // that we haven't processed yet?
        if(cleanLength>1){
            // if the whole string can be written out as-is do so
            // otherwise write out the clean chars at the end of the
            // array
            if(cleanStart==0)
                writer.write(string);
            else
                writer.write(chars,cleanStart,cleanLength);
        }else if(cleanLength==1){
            // a little optimization for 1 clean character
            // (we could have let the previous if(...) handle them all)
            writer.write(ch);
        }
    }

    private static String makeHHString(int i){
        String s=Integer.toHexString(i).toUpperCase();
        if(s.length()==1){
            s="0"+s;
        }
        return s;
    }

    public void writeAttrString(
            final java.io.Writer writer,String string,String encoding)
            throws IOException{
        final int end=string.length();
        if(end>m_attrBuff.length){
            m_attrBuff=new char[end*2+1];
        }
        string.getChars(0,end,m_attrBuff,0);
        final char[] chars=m_attrBuff;
        int cleanStart=0;
        int cleanLength=0;
        char ch=0;
        for(int i=0;i<end;i++){
            ch=chars[i];
            // System.out.println("SPECIALSSIZE: "+SPECIALSSIZE);
            // System.out.println("ch: "+(int)ch);
            // System.out.println("m_maxCharacter: "+(int)m_maxCharacter);
            // System.out.println("m_attrCharsMap[ch]: "+(int)m_attrCharsMap[ch]);
            if(escapingNotNeeded(ch)&&(!m_charInfo.isSpecialAttrChar(ch))){
                cleanLength++;
            }else if('<'==ch||'>'==ch){
                cleanLength++; // no escaping in this case, as specified in 15.2
            }else if(
                    ('&'==ch)&&((i+1)<end)&&('{'==chars[i+1])){
                cleanLength++; // no escaping in this case, as specified in 15.2
            }else{
                if(cleanLength>0){
                    writer.write(chars,cleanStart,cleanLength);
                    cleanLength=0;
                }
                int pos=accumDefaultEntity(writer,ch,i,chars,end,false,true);
                if(i!=pos){
                    i=pos-1;
                }else{
                    if(Encodings.isHighUTF16Surrogate(ch)){
                        writeUTF16Surrogate(ch,chars,i,end);
                        i++; // two input characters processed
                        // this increments by one and the for()
                        // loop itself increments by another one.
                    }
                    // The next is kind of a hack to keep from escaping in the case
                    // of Shift_JIS and the like.
                    /**
                     else if ((ch < m_maxCharacter) && (m_maxCharacter == 0xFFFF)
                     && (ch != 160))
                     {
                     writer.write(ch);  // no escaping in this case
                     }
                     else
                     */
                    String outputStringForChar=m_charInfo.getOutputStringForChar(ch);
                    if(null!=outputStringForChar){
                        writer.write(outputStringForChar);
                    }else if(escapingNotNeeded(ch)){
                        writer.write(ch); // no escaping in this case
                    }else{
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                    }
                }
                cleanStart=i+1;
            }
        } // end of for()
        // are there any clean characters at the end of the array
        // that we haven't processed yet?
        if(cleanLength>1){
            // if the whole string can be written out as-is do so
            // otherwise write out the clean chars at the end of the
            // array
            if(cleanStart==0)
                writer.write(string);
            else
                writer.write(chars,cleanStart,cleanLength);
        }else if(cleanLength==1){
            // a little optimization for 1 clean character
            // (we could have let the previous if(...) handle them all)
            writer.write(ch);
        }
    }

    public final void endElement(
            final String namespaceURI,
            final String localName,
            final String name)
            throws SAXException{
        // deal with any pending issues
        if(m_cdataTagOpen)
            closeCDATA();
        // if the element has a namespace, treat it like XML, not HTML
        if(null!=namespaceURI&&namespaceURI.length()>0){
            super.endElement(namespaceURI,localName,name);
            return;
        }
        try{
            ElemContext elemContext=m_elemContext;
            final ElemDesc elemDesc=elemContext.m_elementDesc;
            final int elemFlags=elemDesc.getFlags();
            final boolean elemEmpty=(elemFlags&ElemDesc.EMPTY)!=0;
            // deal with any indentation issues
            if(m_doIndent){
                final boolean isBlockElement=(elemFlags&ElemDesc.BLOCK)!=0;
                boolean shouldIndent=false;
                if(m_ispreserve){
                    m_ispreserve=false;
                }else if(m_doIndent&&(!m_inBlockElem||isBlockElement)){
                    m_startNewLine=true;
                    shouldIndent=true;
                }
                if(!elemContext.m_startTagOpen&&shouldIndent)
                    indent(elemContext.m_currentElemDepth-1);
                m_inBlockElem=!isBlockElement;
            }
            final java.io.Writer writer=m_writer;
            if(!elemContext.m_startTagOpen){
                writer.write("</");
                writer.write(name);
                writer.write('>');
            }else{
                // the start-tag open when this method was called,
                // so we need to process it now.
                if(m_tracer!=null)
                    super.fireStartElem(name);
                // the starting tag was still open when we received this endElement() call
                // so we need to process any gathered attributes NOW, before they go away.
                int nAttrs=m_attributes.getLength();
                if(nAttrs>0){
                    processAttributes(m_writer,nAttrs);
                    // clear attributes object for re-use with next element
                    m_attributes.clear();
                }
                if(!elemEmpty){
                    // As per Dave/Paul recommendation 12/06/2000
                    // if (shouldIndent)
                    // writer.write('>');
                    //  indent(m_currentIndent);
                    writer.write("></");
                    writer.write(name);
                    writer.write('>');
                }else{
                    writer.write('>');
                }
            }
            // clean up because the element has ended
            if((elemFlags&ElemDesc.WHITESPACESENSITIVE)!=0)
                m_ispreserve=true;
            m_isprevtext=false;
            // fire off the end element event
            if(m_tracer!=null)
                super.fireEndElem(name);
            // OPTIMIZE-EMPTY
            if(elemEmpty){
                // a quick exit if the HTML element had no children.
                // This block of code can be removed if the corresponding block of code
                // in startElement() also labeled with "OPTIMIZE-EMPTY" is also removed
                m_elemContext=elemContext.m_prev;
                return;
            }
            // some more clean because the element has ended.
            if(!elemContext.m_startTagOpen){
                if(m_doIndent&&!m_preserves.isEmpty())
                    m_preserves.pop();
            }
            m_elemContext=elemContext.m_prev;
//            m_isRawStack.pop();
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    public final void endElement(String elemName) throws SAXException{
        endElement(null,null,elemName);
    }

    public void comment(char ch[],int start,int length)
            throws SAXException{
        // The internal DTD subset is not serialized by the ToHTMLStream serializer
        if(m_inDTD)
            return;
        super.comment(ch,start,length);
    }

    public void endDTD() throws SAXException{
        m_inDTD=false;
        /** for ToHTMLStream the DOCTYPE is entirely output in the
         * startDocumentInternal() method, so don't do anything here
         */
    }

    protected void closeStartTag() throws SAXException{
        try{
            // finish processing attributes, time to fire off the start element event
            if(m_tracer!=null)
                super.fireStartElem(m_elemContext.m_elementName);
            int nAttrs=m_attributes.getLength();
            if(nAttrs>0){
                processAttributes(m_writer,nAttrs);
                // clear attributes object for re-use with next element
                m_attributes.clear();
            }
            m_writer.write('>');
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
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        m_inDTD=true;
        super.startDTD(name,publicId,systemId);
    }

    public boolean reset(){
        boolean ret=super.reset();
        if(!ret)
            return false;
        initToHTMLStream();
        return true;
    }

    private void initToHTMLStream(){
//        m_elementDesc = null;
        m_inBlockElem=false;
        m_inDTD=false;
//        m_isRawStack.clear();
        m_omitMetaTag=false;
        m_specialEscapeURLs=true;
    }

    public void addUniqueAttribute(String name,String value,int flags)
            throws SAXException{
        try{
            final java.io.Writer writer=m_writer;
            if((flags&NO_BAD_CHARS)>0&&m_htmlcharInfo.onlyQuotAmpLtGt){
                // "flags" has indicated that the characters
                // '>'  '<'   '&'  and '"' are not in the value and
                // m_htmlcharInfo has recorded that there are no other
                // entities in the range 0 to 127 so we write out the
                // value directly
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                writer.write(value);
                writer.write('"');
            }else if(
                    (flags&HTML_ATTREMPTY)>0
                            &&(value.length()==0||value.equalsIgnoreCase(name))){
                writer.write(' ');
                writer.write(name);
            }else{
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                if((flags&HTML_ATTRURL)>0){
                    writeAttrURI(writer,value,m_specialEscapeURLs);
                }else{
                    writeAttrString(writer,value,this.getEncoding());
                }
                writer.write('"');
            }
        }catch(IOException e){
            throw new SAXException(e);
        }
    }

    static class Trie{
        public static final int ALPHA_SIZE=128;
        final Node m_Root;
        private final boolean m_lowerCaseOnly;
        private char[] m_charBuffer=new char[0];

        public Trie(){
            m_Root=new Node();
            m_lowerCaseOnly=false;
        }

        public Trie(boolean lowerCaseOnly){
            m_Root=new Node();
            m_lowerCaseOnly=lowerCaseOnly;
        }

        public Trie(Trie existingTrie){
            // copy some fields from the existing Trie into this one.
            m_Root=existingTrie.m_Root;
            m_lowerCaseOnly=existingTrie.m_lowerCaseOnly;
            // get a buffer just big enough to hold the longest key in the table.
            int max=existingTrie.getLongestKeyLength();
            m_charBuffer=new char[max];
        }

        public Object put(String key,Object value){
            final int len=key.length();
            if(len>m_charBuffer.length){
                // make the biggest buffer ever needed in get(String)
                m_charBuffer=new char[len];
            }
            Node node=m_Root;
            for(int i=0;i<len;i++){
                Node nextNode=
                        node.m_nextChar[Character.toLowerCase(key.charAt(i))];
                if(nextNode!=null){
                    node=nextNode;
                }else{
                    for(;i<len;i++){
                        Node newNode=new Node();
                        if(m_lowerCaseOnly){
                            // put this value into the tree only with a lower case key
                            node.m_nextChar[Character.toLowerCase(
                                    key.charAt(i))]=
                                    newNode;
                        }else{
                            // put this value into the tree with a case insensitive key
                            node.m_nextChar[Character.toUpperCase(
                                    key.charAt(i))]=
                                    newNode;
                            node.m_nextChar[Character.toLowerCase(
                                    key.charAt(i))]=
                                    newNode;
                        }
                        node=newNode;
                    }
                    break;
                }
            }
            Object ret=node.m_Value;
            node.m_Value=value;
            return ret;
        }

        public Object get(final String key){
            final int len=key.length();
            /** If the name is too long, we won't find it, this also keeps us
             * from overflowing m_charBuffer
             */
            if(m_charBuffer.length<len)
                return null;
            Node node=m_Root;
            switch(len) // optimize the look up based on the number of chars
            {
                // case 0 looks silly, but the generated bytecode runs
                // faster for lookup of elements of length 2 with this in
                // and a fair bit faster.  Don't know why.
                case 0:{
                    return null;
                }
                case 1:{
                    final char ch=key.charAt(0);
                    if(ch<ALPHA_SIZE){
                        node=node.m_nextChar[ch];
                        if(node!=null)
                            return node.m_Value;
                    }
                    return null;
                }
                //                comment out case 2 because the default is faster
                //                case 2 :
                //                    {
                //                        final char ch0 = key.charAt(0);
                //                        final char ch1 = key.charAt(1);
                //                        if (ch0 < ALPHA_SIZE && ch1 < ALPHA_SIZE)
                //                        {
                //                            node = node.m_nextChar[ch0];
                //                            if (node != null)
                //                            {
                //
                //                                if (ch1 < ALPHA_SIZE)
                //                                {
                //                                    node = node.m_nextChar[ch1];
                //                                    if (node != null)
                //                                        return node.m_Value;
                //                                }
                //                            }
                //                        }
                //                        return null;
                //                   }
                default:{
                    for(int i=0;i<len;i++){
                        // A thread-safe way to loop over the characters
                        final char ch=key.charAt(i);
                        if(ALPHA_SIZE<=ch){
                            // the key is not 7-bit ASCII so we won't find it here
                            return null;
                        }
                        node=node.m_nextChar[ch];
                        if(node==null)
                            return null;
                    }
                    return node.m_Value;
                }
            }
        }

        public Object get2(final String key){
            final int len=key.length();
            /** If the name is too long, we won't find it, this also keeps us
             * from overflowing m_charBuffer
             */
            if(m_charBuffer.length<len)
                return null;
            Node node=m_Root;
            switch(len) // optimize the look up based on the number of chars
            {
                // case 0 looks silly, but the generated bytecode runs
                // faster for lookup of elements of length 2 with this in
                // and a fair bit faster.  Don't know why.
                case 0:{
                    return null;
                }
                case 1:{
                    final char ch=key.charAt(0);
                    if(ch<ALPHA_SIZE){
                        node=node.m_nextChar[ch];
                        if(node!=null)
                            return node.m_Value;
                    }
                    return null;
                }
                default:{
                    /** Copy string into array. This is not thread-safe because
                     * it modifies the contents of m_charBuffer. If multiple
                     * threads were to use this Trie they all would be
                     * using this same array (not good). So this
                     * method is not thread-safe, but it is faster because
                     * converting to a char[] and looping over elements of
                     * the array is faster than a String's charAt(i).
                     */
                    key.getChars(0,len,m_charBuffer,0);
                    for(int i=0;i<len;i++){
                        final char ch=m_charBuffer[i];
                        if(ALPHA_SIZE<=ch){
                            // the key is not 7-bit ASCII so we won't find it here
                            return null;
                        }
                        node=node.m_nextChar[ch];
                        if(node==null)
                            return null;
                    }
                    return node.m_Value;
                }
            }
        }

        public int getLongestKeyLength(){
            return m_charBuffer.length;
        }

        private class Node{
            final Node m_nextChar[];
            Object m_Value;
            Node(){
                m_nextChar=new Node[ALPHA_SIZE];
                m_Value=null;
            }
        }
    }
}
