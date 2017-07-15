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
 * $Id: SourceTreeManager.java,v 1.2.4.1 2005/09/10 18:14:09 jeffsuttor Exp $
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
 * $Id: SourceTreeManager.java,v 1.2.4.1 2005/09/10 18:14:09 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Vector;

public class SourceTreeManager{
    URIResolver m_uriResolver;
    private Vector m_sourceTree=new Vector();

    public static XMLReader getXMLReader(Source inputSource,SourceLocator locator)
            throws TransformerException{
        try{
            XMLReader reader=(inputSource instanceof SAXSource)
                    ?((SAXSource)inputSource).getXMLReader():null;
            if(null==reader){
                try{
                    javax.xml.parsers.SAXParserFactory factory=
                            javax.xml.parsers.SAXParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    javax.xml.parsers.SAXParser jaxpParser=
                            factory.newSAXParser();
                    reader=jaxpParser.getXMLReader();
                }catch(javax.xml.parsers.ParserConfigurationException ex){
                    throw new org.xml.sax.SAXException(ex);
                }catch(javax.xml.parsers.FactoryConfigurationError ex1){
                    throw new org.xml.sax.SAXException(ex1.toString());
                }catch(NoSuchMethodError ex2){
                }catch(AbstractMethodError ame){
                }
                if(null==reader)
                    reader=XMLReaderFactory.createXMLReader();
            }
            try{
                reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                        true);
            }catch(org.xml.sax.SAXException se){
                // What can we do?
                // TODO: User diagnostics.
            }
            return reader;
        }catch(org.xml.sax.SAXException se){
            throw new TransformerException(se.getMessage(),locator,se);
        }
    }

    public void reset(){
        m_sourceTree=new Vector();
    }

    public URIResolver getURIResolver(){
        return m_uriResolver;
    }

    public void setURIResolver(URIResolver resolver){
        m_uriResolver=resolver;
    }

    public String findURIFromDoc(int owner){
        int n=m_sourceTree.size();
        for(int i=0;i<n;i++){
            SourceTree sTree=(SourceTree)m_sourceTree.elementAt(i);
            if(owner==sTree.m_root)
                return sTree.m_url;
        }
        return null;
    }

    public void removeDocumentFromCache(int n){
        if(DTM.NULL==n)
            return;
        for(int i=m_sourceTree.size()-1;i>=0;--i){
            SourceTree st=(SourceTree)m_sourceTree.elementAt(i);
            if(st!=null&&st.m_root==n){
                m_sourceTree.removeElementAt(i);
                return;
            }
        }
    }

    public int getSourceTree(
            String base,String urlString,SourceLocator locator,XPathContext xctxt)
            throws TransformerException{
        // System.out.println("getSourceTree");
        try{
            Source source=this.resolveURI(base,urlString,locator);
            // System.out.println("getSourceTree - base: "+base+", urlString: "+urlString+", source: "+source.getSystemId());
            return getSourceTree(source,locator,xctxt);
        }catch(IOException ioe){
            throw new TransformerException(ioe.getMessage(),locator,ioe);
        }
        /** catch (TransformerException te)
         {
         throw new TransformerException(te.getMessage(), locator, te);
         }*/
    }

    public Source resolveURI(
            String base,String urlString,SourceLocator locator)
            throws TransformerException, IOException{
        Source source=null;
        if(null!=m_uriResolver){
            source=m_uriResolver.resolve(urlString,base);
        }
        if(null==source){
            String uri=SystemIDResolver.getAbsoluteURI(urlString,base);
            source=new StreamSource(uri);
        }
        return source;
    }

    public int getSourceTree(Source source,SourceLocator locator,XPathContext xctxt)
            throws TransformerException{
        int n=getNode(source);
        if(DTM.NULL!=n)
            return n;
        n=parseToNode(source,locator,xctxt);
        if(DTM.NULL!=n)
            putDocumentInCache(n,source);
        return n;
    }

    public void putDocumentInCache(int n,Source source){
        int cachedNode=getNode(source);
        if(DTM.NULL!=cachedNode){
            if(!(cachedNode==n))
                throw new RuntimeException(
                        "Programmer's Error!  "
                                +"putDocumentInCache found reparse of doc: "
                                +source.getSystemId());
            return;
        }
        if(null!=source.getSystemId()){
            m_sourceTree.addElement(new SourceTree(n,source.getSystemId()));
        }
    }

    public int getNode(Source source){
//    if (source instanceof DOMSource)
//      return ((DOMSource) source).getNode();
        // TODO: Not sure if the BaseID is really the same thing as the ID.
        String url=source.getSystemId();
        if(null==url)
            return DTM.NULL;
        int n=m_sourceTree.size();
        // System.out.println("getNode: "+n);
        for(int i=0;i<n;i++){
            SourceTree sTree=(SourceTree)m_sourceTree.elementAt(i);
            // System.out.println("getNode -         url: "+url);
            // System.out.println("getNode - sTree.m_url: "+sTree.m_url);
            if(url.equals(sTree.m_url))
                return sTree.m_root;
        }
        // System.out.println("getNode - returning: "+node);
        return DTM.NULL;
    }

    public int parseToNode(Source source,SourceLocator locator,XPathContext xctxt)
            throws TransformerException{
        try{
            Object xowner=xctxt.getOwnerObject();
            DTM dtm;
            if(null!=xowner&&xowner instanceof com.sun.org.apache.xml.internal.dtm.DTMWSFilter){
                dtm=xctxt.getDTM(source,false,
                        (com.sun.org.apache.xml.internal.dtm.DTMWSFilter)xowner,false,true);
            }else{
                dtm=xctxt.getDTM(source,false,null,false,true);
            }
            return dtm.getDocument();
        }catch(Exception e){
            //e.printStackTrace();
            throw new TransformerException(e.getMessage(),locator,e);
        }
    }
}
