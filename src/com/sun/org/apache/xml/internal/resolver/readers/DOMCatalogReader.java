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
// DOMCatalogReader.java - Read XML Catalog files
package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogException;
import com.sun.org.apache.xml.internal.resolver.helpers.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import sun.reflect.misc.ReflectUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class DOMCatalogReader implements CatalogReader{
    protected Map<String,String> namespaceMap=new HashMap<>();

    public DOMCatalogReader(){
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
            throws MalformedURLException, IOException, CatalogException{
        URL url=new URL(fileUrl);
        URLConnection urlCon=url.openConnection();
        readCatalog(catalog,urlCon.getInputStream());
    }

    public void readCatalog(Catalog catalog,InputStream is)
            throws IOException, CatalogException{
        DocumentBuilderFactory factory=null;
        DocumentBuilder builder=null;
        factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        try{
            builder=factory.newDocumentBuilder();
        }catch(ParserConfigurationException pce){
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }
        Document doc=null;
        try{
            doc=builder.parse(is);
        }catch(SAXException se){
            throw new CatalogException(CatalogException.UNKNOWN_FORMAT);
        }
        Element root=doc.getDocumentElement();
        String namespaceURI=Namespaces.getNamespaceURI(root);
        String localName=Namespaces.getLocalName(root);
        String domParserClass=getCatalogParser(namespaceURI,
                localName);
        if(domParserClass==null){
            if(namespaceURI==null){
                catalog.getCatalogManager().debug.message(1,"No Catalog parser for "
                        +localName);
            }else{
                catalog.getCatalogManager().debug.message(1,"No Catalog parser for "
                        +"{"+namespaceURI+"}"
                        +localName);
            }
            return;
        }
        DOMCatalogParser domParser=null;
        try{
            domParser=(DOMCatalogParser)ReflectUtil.forName(domParserClass).newInstance();
        }catch(ClassNotFoundException cnfe){
            catalog.getCatalogManager().debug.message(1,"Cannot load XML Catalog Parser class",domParserClass);
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(InstantiationException ie){
            catalog.getCatalogManager().debug.message(1,"Cannot instantiate XML Catalog Parser class",domParserClass);
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(IllegalAccessException iae){
            catalog.getCatalogManager().debug.message(1,"Cannot access XML Catalog Parser class",domParserClass);
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }catch(ClassCastException cce){
            catalog.getCatalogManager().debug.message(1,"Cannot cast XML Catalog Parser class",domParserClass);
            throw new CatalogException(CatalogException.UNPARSEABLE);
        }
        Node node=root.getFirstChild();
        while(node!=null){
            domParser.parseCatalogEntry(catalog,node);
            node=node.getNextSibling();
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
}
