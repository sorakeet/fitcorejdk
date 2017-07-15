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
 * $Id: StylesheetPIHandler.java,v 1.2.4.1 2005/09/15 08:15:57 suresh_emailid Exp $
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
 * $Id: StylesheetPIHandler.java,v 1.2.4.1 2005/09/15 08:15:57 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.util.StringTokenizer;
import java.util.Vector;

public class StylesheetPIHandler extends DefaultHandler{
    String m_baseID;
    String m_media;
    String m_title;
    String m_charset;
    Vector m_stylesheets=new Vector();
    // Add code to use a URIResolver. Patch from Dmitri Ilyin.
    URIResolver m_uriResolver;

    public StylesheetPIHandler(String baseID,String media,String title,
                               String charset){
        m_baseID=baseID;
        m_media=media;
        m_title=title;
        m_charset=charset;
    }

    public URIResolver getURIResolver(){
        return m_uriResolver;
    }

    public void setURIResolver(URIResolver resolver){
        m_uriResolver=resolver;
    }

    public Source getAssociatedStylesheet(){
        int sz=m_stylesheets.size();
        if(sz>0){
            Source source=(Source)m_stylesheets.elementAt(sz-1);
            return source;
        }else
            return null;
    }

    public void startElement(
            String namespaceURI,String localName,String qName,Attributes atts)
            throws org.xml.sax.SAXException{
        throw new StopParseException();
    }

    public void processingInstruction(String target,String data)
            throws org.xml.sax.SAXException{
        if(target.equals("xml-stylesheet")){
            String href=null;  // CDATA #REQUIRED
            String type=null;  // CDATA #REQUIRED
            String title=null;  // CDATA #IMPLIED
            String media=null;  // CDATA #IMPLIED
            String charset=null;  // CDATA #IMPLIED
            boolean alternate=false;  // (yes|no) "no"
            StringTokenizer tokenizer=new StringTokenizer(data," \t=\n",true);
            boolean lookedAhead=false;
            Source source=null;
            String token="";
            while(tokenizer.hasMoreTokens()){
                if(!lookedAhead)
                    token=tokenizer.nextToken();
                else
                    lookedAhead=false;
                if(tokenizer.hasMoreTokens()&&
                        (token.equals(" ")||token.equals("\t")||token.equals("=")))
                    continue;
                String name=token;
                if(name.equals("type")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    type=token.substring(1,token.length()-1);
                }else if(name.equals("href")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    href=token;
                    if(tokenizer.hasMoreTokens()){
                        token=tokenizer.nextToken();
                        // If the href value has parameters to be passed to a
                        // servlet(something like "foobar?id=12..."),
                        // we want to make sure we get them added to
                        // the href value. Without this check, we would move on
                        // to try to process another attribute and that would be
                        // wrong.
                        // We need to set lookedAhead here to flag that we
                        // already have the next token.
                        while(token.equals("=")&&tokenizer.hasMoreTokens()){
                            href=href+token+tokenizer.nextToken();
                            if(tokenizer.hasMoreTokens()){
                                token=tokenizer.nextToken();
                                lookedAhead=true;
                            }else{
                                break;
                            }
                        }
                    }
                    href=href.substring(1,href.length()-1);
                    try{
                        // Add code to use a URIResolver. Patch from Dmitri Ilyin.
                        if(m_uriResolver!=null){
                            source=m_uriResolver.resolve(href,m_baseID);
                        }else{
                            href=SystemIDResolver.getAbsoluteURI(href,m_baseID);
                            source=new SAXSource(new InputSource(href));
                        }
                    }catch(TransformerException te){
                        throw new org.xml.sax.SAXException(te);
                    }
                }else if(name.equals("title")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    title=token.substring(1,token.length()-1);
                }else if(name.equals("media")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    media=token.substring(1,token.length()-1);
                }else if(name.equals("charset")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    charset=token.substring(1,token.length()-1);
                }else if(name.equals("alternate")){
                    token=tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens()&&
                            (token.equals(" ")||token.equals("\t")||token.equals("=")))
                        token=tokenizer.nextToken();
                    alternate=token.substring(1,token.length()
                            -1).equals("yes");
                }
            }
            if((null!=type)
                    &&(type.equals("text/xsl")||type.equals("text/xml")||type.equals("application/xml+xslt"))
                    &&(null!=href)){
                if(null!=m_media){
                    if(null!=media){
                        if(!media.equals(m_media))
                            return;
                    }else
                        return;
                }
                if(null!=m_charset){
                    if(null!=charset){
                        if(!charset.equals(m_charset))
                            return;
                    }else
                        return;
                }
                if(null!=m_title){
                    if(null!=title){
                        if(!title.equals(m_title))
                            return;
                    }else
                        return;
                }
                m_stylesheets.addElement(source);
            }
        }
    }

    public String getBaseId(){
        return m_baseID;
    }

    public void setBaseId(String baseId){
        m_baseID=baseId;
    }
}
