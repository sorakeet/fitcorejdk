/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
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
// SAXCatalogReader.java - Read XML Catalog files
package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogException;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import org.xml.sax.*;
import sun.reflect.misc.ReflectUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class SAXCatalogReader implements CatalogReader, ContentHandler, DocumentHandler{
    protected SAXParserFactory parserFactory=null;
    protected String parserClass=null;
    protected Map<String,String> namespaceMap=new HashMap<>();
    protected Debug debug=CatalogManager.getStaticManager().debug;
    private SAXCatalogParser saxParser=null;
    private boolean abandonHope=false;
    private Catalog catalog;

    public SAXCatalogReader(){
        parserFactory=null;
        parserClass=null;
    }

    public SAXCatalogReader(SAXParserFactory parserFactory){
        this.parserFactory=parserFactory;
    }

    public SAXCatalogReader(String parserClass){
        this.parserClass=parserClass;
    }

    public SAXParserFactory getParserFactory(){
        return parserFactory;
    }

    public void setParserFactory(SAXParserFactory parserFactory){
        this.parserFactory=parserFactory;
    }

    public String getParserClass(){
        return parserClass;
    }

    public void setParserClass(String parserClass){
        this.parserClass=parserClass;
    }

    public void setCatalogParser(String namespaceURI,
                                 String rootElement,
                                 String parserClass){
        if(namespaceURI==null){
            namespaceMap.put(rootElement,parserClass);
        }else{
            namespaceMap.put("{"+namespaceURI+"}"+rootElement,parserClass);
        }
    }

    public void readCatalog(Catalog catalog,String fileUrl)
            throws MalformedURLException, IOException,
            CatalogException{
        URL url=null;
        try{
            url=new URL(fileUrl);
        }catch(MalformedURLException e){
            url=new URL("file:///"+fileUrl);
        }
        debug=catalog.getCatalogManager().debug;
        try{
            URLConnection urlCon=url.openConnection();
            readCatalog(catalog,urlCon.getInputStream());
        }catch(FileNotFoundException e){
            catalog.getCatalogManager().debug.message(1,"Failed to load catalog, file not found",
                    url.toString());
        }
    }

    public void readCatalog(Catalog catalog,InputStream is)
            throws IOException, CatalogException{
        // Create an instance of the parser
        if(parserFactory==null&&parserClass==null){
            debug.message(1,"Cannot read SAX catalog without a parser");
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }
        debug=catalog.getCatalogManager().debug;
        EntityResolver bResolver=catalog.getCatalogManager().getBootstrapResolver();
        this.catalog=catalog;
        try{
            if(parserFactory!=null){
                SAXParser parser=parserFactory.newSAXParser();
                SAXParserHandler spHandler=new SAXParserHandler();
                spHandler.setContentHandler(this);
                if(bResolver!=null){
                    spHandler.setEntityResolver(bResolver);
                }
                parser.parse(new InputSource(is),spHandler);
            }else{
                Parser parser=(Parser)ReflectUtil.forName(parserClass).newInstance();
                parser.setDocumentHandler(this);
                if(bResolver!=null){
                    parser.setEntityResolver(bResolver);
                }
                parser.parse(new InputSource(is));
            }
        }catch(ClassNotFoundException cnfe){
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(IllegalAccessException iae){
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(InstantiationException ie){
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(ParserConfigurationException pce){
            throw new CatalogException(CatalogException.UNKNOWN_FORMAT);
        }catch(SAXException se){
            Exception e=se.getException();
            // FIXME: there must be a better way
            UnknownHostException uhe=new UnknownHostException();
            FileNotFoundException fnfe=new FileNotFoundException();
            if(e!=null){
                if(e.getClass()==uhe.getClass()){
                    throw new CatalogException(CatalogException.PARSE_FAILED,
                            e.toString());
                }else if(e.getClass()==fnfe.getClass()){
                    throw new CatalogException(CatalogException.PARSE_FAILED,
                            e.toString());
                }
            }
            throw new CatalogException(se);
        }
    }

    public void setDocumentLocator(Locator locator){
        if(saxParser!=null){
            saxParser.setDocumentLocator(locator);
        }
    }
    // ----------------------------------------------------------------------
    // Implement the SAX ContentHandler interface

    public void startDocument() throws SAXException{
        saxParser=null;
        abandonHope=false;
        return;
    }

    public void endDocument() throws SAXException{
        if(saxParser!=null){
            saxParser.endDocument();
        }
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        if(saxParser!=null){
            saxParser.startPrefixMapping(prefix,uri);
        }
    }

    public void endPrefixMapping(String prefix)
            throws SAXException{
        if(saxParser!=null){
            saxParser.endPrefixMapping(prefix);
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException{
        if(abandonHope){
            return;
        }
        if(saxParser==null){
            String saxParserClass=getCatalogParser(namespaceURI,
                    localName);
            if(saxParserClass==null){
                abandonHope=true;
                if(namespaceURI==null){
                    debug.message(2,"No Catalog parser for "+localName);
                }else{
                    debug.message(2,"No Catalog parser for "
                            +"{"+namespaceURI+"}"
                            +localName);
                }
                return;
            }
            try{
                saxParser=(SAXCatalogParser)
                        ReflectUtil.forName(saxParserClass).newInstance();
                saxParser.setCatalog(catalog);
                saxParser.startDocument();
                saxParser.startElement(namespaceURI,localName,qName,atts);
            }catch(ClassNotFoundException cnfe){
                saxParser=null;
                abandonHope=true;
                debug.message(2,cnfe.toString());
            }catch(InstantiationException ie){
                saxParser=null;
                abandonHope=true;
                debug.message(2,ie.toString());
            }catch(IllegalAccessException iae){
                saxParser=null;
                abandonHope=true;
                debug.message(2,iae.toString());
            }catch(ClassCastException cce){
                saxParser=null;
                abandonHope=true;
                debug.message(2,cce.toString());
            }
        }else{
            saxParser.startElement(namespaceURI,localName,qName,atts);
        }
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName) throws SAXException{
        if(saxParser!=null){
            saxParser.endElement(namespaceURI,localName,qName);
        }
    }

    public void characters(char ch[],int start,int length)
            throws SAXException{
        if(saxParser!=null){
            saxParser.characters(ch,start,length);
        }
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        if(saxParser!=null){
            saxParser.ignorableWhitespace(ch,start,length);
        }
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(saxParser!=null){
            saxParser.processingInstruction(target,data);
        }
    }

    public void skippedEntity(String name)
            throws SAXException{
        if(saxParser!=null){
            saxParser.skippedEntity(name);
        }
    }

    public void startElement(String name,
                             AttributeList atts)
            throws SAXException{
        if(abandonHope){
            return;
        }
        if(saxParser==null){
            String prefix="";
            if(name.indexOf(':')>0){
                prefix=name.substring(0,name.indexOf(':'));
            }
            String localName=name;
            if(localName.indexOf(':')>0){
                localName=localName.substring(localName.indexOf(':')+1);
            }
            String namespaceURI=null;
            if(prefix.equals("")){
                namespaceURI=atts.getValue("xmlns");
            }else{
                namespaceURI=atts.getValue("xmlns:"+prefix);
            }
            String saxParserClass=getCatalogParser(namespaceURI,
                    localName);
            if(saxParserClass==null){
                abandonHope=true;
                if(namespaceURI==null){
                    debug.message(2,"No Catalog parser for "+name);
                }else{
                    debug.message(2,"No Catalog parser for "
                            +"{"+namespaceURI+"}"
                            +name);
                }
                return;
            }
            try{
                saxParser=(SAXCatalogParser)
                        ReflectUtil.forName(saxParserClass).newInstance();
                saxParser.setCatalog(catalog);
                saxParser.startDocument();
                saxParser.startElement(name,atts);
            }catch(ClassNotFoundException cnfe){
                saxParser=null;
                abandonHope=true;
                debug.message(2,cnfe.toString());
            }catch(InstantiationException ie){
                saxParser=null;
                abandonHope=true;
                debug.message(2,ie.toString());
            }catch(IllegalAccessException iae){
                saxParser=null;
                abandonHope=true;
                debug.message(2,iae.toString());
            }catch(ClassCastException cce){
                saxParser=null;
                abandonHope=true;
                debug.message(2,cce.toString());
            }
        }else{
            saxParser.startElement(name,atts);
        }
    }

    public String getCatalogParser(String namespaceURI,
                                   String rootElement){
        if(namespaceURI==null){
            return namespaceMap.get(rootElement);
        }else{
            return namespaceMap.get("{"+namespaceURI+"}"+rootElement);
        }
    }

    public void endElement(String name) throws SAXException{
        if(saxParser!=null){
            saxParser.endElement(name);
        }
    }
}
