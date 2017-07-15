/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
 */
// ResolvingParser.java - An interface for reading catalog files
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xml.internal.resolver.tools;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class ResolvingParser
        implements Parser, DTDHandler, DocumentHandler, EntityResolver{
    public static boolean namespaceAware=true;
    public static boolean validating=false;
    public static boolean suppressExplanation=false;
    private SAXParser saxParser=null;
    private Parser parser=null;
    private DocumentHandler documentHandler=null;
    private DTDHandler dtdHandler=null;
    private CatalogManager catalogManager=CatalogManager.getStaticManager();
    private CatalogResolver catalogResolver=null;
    private CatalogResolver piCatalogResolver=null;
    private boolean allowXMLCatalogPI=false;
    private boolean oasisXMLCatalogPI=false;
    private URL baseURL=null;

    public ResolvingParser(){
        initParser();
    }

    private void initParser(){
        catalogResolver=new CatalogResolver(catalogManager);
        SAXParserFactory spf=catalogManager.useServicesMechanism()?
                SAXParserFactory.newInstance():new SAXParserFactoryImpl();
        spf.setNamespaceAware(namespaceAware);
        spf.setValidating(validating);
        try{
            saxParser=spf.newSAXParser();
            parser=saxParser.getParser();
            documentHandler=null;
            dtdHandler=null;
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public ResolvingParser(CatalogManager manager){
        catalogManager=manager;
        initParser();
    }

    public Catalog getCatalog(){
        return catalogResolver.getCatalog();
    }

    public void setLocale(Locale locale) throws SAXException{
        parser.setLocale(locale);
    }

    public void setEntityResolver(EntityResolver resolver){
        // nop
    }

    public void setDTDHandler(DTDHandler handler){
        dtdHandler=handler;
    }

    public void setDocumentHandler(DocumentHandler handler){
        documentHandler=handler;
    }

    public void setErrorHandler(ErrorHandler handler){
        parser.setErrorHandler(handler);
    }

    public void parse(InputSource input)
            throws IOException,
            SAXException{
        setupParse(input.getSystemId());
        try{
            parser.parse(input);
        }catch(InternalError ie){
            explain(input.getSystemId());
            throw ie;
        }
    }

    public void parse(String systemId)
            throws IOException,
            SAXException{
        setupParse(systemId);
        try{
            parser.parse(systemId);
        }catch(InternalError ie){
            explain(systemId);
            throw ie;
        }
    }

    private void setupParse(String systemId){
        allowXMLCatalogPI=true;
        parser.setEntityResolver(this);
        parser.setDocumentHandler(this);
        parser.setDTDHandler(this);
        URL cwd=null;
        try{
            cwd=FileURL.makeURL("basename");
        }catch(MalformedURLException mue){
            cwd=null;
        }
        try{
            baseURL=new URL(systemId);
        }catch(MalformedURLException mue){
            if(cwd!=null){
                try{
                    baseURL=new URL(cwd,systemId);
                }catch(MalformedURLException mue2){
                    // give up
                    baseURL=null;
                }
            }else{
                // give up
                baseURL=null;
            }
        }
    }

    private void explain(String systemId){
        if(!suppressExplanation){
            System.out.println("Parser probably encountered bad URI in "+systemId);
            System.out.println("For example, replace '/some/uri' with 'file:/some/uri'.");
        }
    }

    public void setDocumentLocator(Locator locator){
        if(documentHandler!=null){
            documentHandler.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException{
        if(documentHandler!=null){
            documentHandler.startDocument();
        }
    }

    public void endDocument() throws SAXException{
        if(documentHandler!=null){
            documentHandler.endDocument();
        }
    }

    public void startElement(String name,AttributeList atts)
            throws SAXException{
        allowXMLCatalogPI=false;
        if(documentHandler!=null){
            documentHandler.startElement(name,atts);
        }
    }

    public void endElement(String name) throws SAXException{
        if(documentHandler!=null){
            documentHandler.endElement(name);
        }
    }

    public void characters(char[] ch,int start,int length)
            throws SAXException{
        if(documentHandler!=null){
            documentHandler.characters(ch,start,length);
        }
    }

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
        if(documentHandler!=null){
            documentHandler.ignorableWhitespace(ch,start,length);
        }
    }

    public void processingInstruction(String target,String pidata)
            throws SAXException{
        if(target.equals("oasis-xml-catalog")){
            URL catalog=null;
            String data=pidata;
            int pos=data.indexOf("catalog=");
            if(pos>=0){
                data=data.substring(pos+8);
                if(data.length()>1){
                    String quote=data.substring(0,1);
                    data=data.substring(1);
                    pos=data.indexOf(quote);
                    if(pos>=0){
                        data=data.substring(0,pos);
                        try{
                            if(baseURL!=null){
                                catalog=new URL(baseURL,data);
                            }else{
                                catalog=new URL(data);
                            }
                        }catch(MalformedURLException mue){
                            // nevermind
                        }
                    }
                }
            }
            if(allowXMLCatalogPI){
                if(catalogManager.getAllowOasisXMLCatalogPI()){
                    catalogManager.debug.message(4,"oasis-xml-catalog PI",pidata);
                    if(catalog!=null){
                        try{
                            catalogManager.debug.message(4,"oasis-xml-catalog",catalog.toString());
                            oasisXMLCatalogPI=true;
                            if(piCatalogResolver==null){
                                piCatalogResolver=new CatalogResolver(true);
                            }
                            piCatalogResolver.getCatalog().parseCatalog(catalog.toString());
                        }catch(Exception e){
                            catalogManager.debug.message(3,"Exception parsing oasis-xml-catalog: "
                                    +catalog.toString());
                        }
                    }else{
                        catalogManager.debug.message(3,"PI oasis-xml-catalog unparseable: "+pidata);
                    }
                }else{
                    catalogManager.debug.message(4,"PI oasis-xml-catalog ignored: "+pidata);
                }
            }else{
                catalogManager.debug.message(3,"PI oasis-xml-catalog occurred in an invalid place: "
                        +pidata);
            }
        }else{
            if(documentHandler!=null){
                documentHandler.processingInstruction(target,pidata);
            }
        }
    }

    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        allowXMLCatalogPI=false;
        if(dtdHandler!=null){
            dtdHandler.notationDecl(name,publicId,systemId);
        }
    }

    public void unparsedEntityDecl(String name,
                                   String publicId,
                                   String systemId,
                                   String notationName)
            throws SAXException{
        allowXMLCatalogPI=false;
        if(dtdHandler!=null){
            dtdHandler.unparsedEntityDecl(name,publicId,systemId,notationName);
        }
    }

    public InputSource resolveEntity(String publicId,String systemId){
        allowXMLCatalogPI=false;
        String resolved=catalogResolver.getResolvedEntity(publicId,systemId);
        if(resolved==null&&piCatalogResolver!=null){
            resolved=piCatalogResolver.getResolvedEntity(publicId,systemId);
        }
        if(resolved!=null){
            try{
                InputSource iSource=new InputSource(resolved);
                iSource.setPublicId(publicId);
                // Ideally this method would not attempt to open the
                // InputStream, but there is a bug (in Xerces, at least)
                // that causes the parser to mistakenly open the wrong
                // system identifier if the returned InputSource does
                // not have a byteStream.
                //
                // It could be argued that we still shouldn't do this here,
                // but since the purpose of calling the entityResolver is
                // almost certainly to open the input stream, it seems to
                // do little harm.
                //
                URL url=new URL(resolved);
                InputStream iStream=url.openStream();
                iSource.setByteStream(iStream);
                return iSource;
            }catch(Exception e){
                catalogManager.debug.message(1,"Failed to create InputSource",resolved);
                return null;
            }
        }else{
            return null;
        }
    }
}
