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
// CatalogResolver.java - A SAX EntityResolver/JAXP URI Resolver
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
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CatalogResolver implements EntityResolver, URIResolver{
    public boolean namespaceAware=true;
    public boolean validating=false;
    private Catalog catalog=null;
    private CatalogManager catalogManager=CatalogManager.getStaticManager();

    public CatalogResolver(){
        initializeCatalogs(false);
    }

    private void initializeCatalogs(boolean privateCatalog){
        catalog=catalogManager.getCatalog();
    }

    public CatalogResolver(boolean privateCatalog){
        initializeCatalogs(privateCatalog);
    }

    public CatalogResolver(CatalogManager manager){
        catalogManager=manager;
        initializeCatalogs(!catalogManager.getUseStaticCatalog());
    }

    public Catalog getCatalog(){
        return catalog;
    }

    public InputSource resolveEntity(String publicId,String systemId){
        String resolved=getResolvedEntity(publicId,systemId);
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
        }
        return null;
    }

    public String getResolvedEntity(String publicId,String systemId){
        String resolved=null;
        if(catalog==null){
            catalogManager.debug.message(1,"Catalog resolution attempted with null catalog; ignored");
            return null;
        }
        if(systemId!=null){
            try{
                resolved=catalog.resolveSystem(systemId);
            }catch(MalformedURLException me){
                catalogManager.debug.message(1,"Malformed URL exception trying to resolve",
                        publicId);
                resolved=null;
            }catch(IOException ie){
                catalogManager.debug.message(1,"I/O exception trying to resolve",publicId);
                resolved=null;
            }
        }
        if(resolved==null){
            if(publicId!=null){
                try{
                    resolved=catalog.resolvePublic(publicId,systemId);
                }catch(MalformedURLException me){
                    catalogManager.debug.message(1,"Malformed URL exception trying to resolve",
                            publicId);
                }catch(IOException ie){
                    catalogManager.debug.message(1,"I/O exception trying to resolve",publicId);
                }
            }
            if(resolved!=null){
                catalogManager.debug.message(2,"Resolved public",publicId,resolved);
            }
        }else{
            catalogManager.debug.message(2,"Resolved system",systemId,resolved);
        }
        return resolved;
    }

    public Source resolve(String href,String base)
            throws TransformerException{
        String uri=href;
        String fragment=null;
        int hashPos=href.indexOf("#");
        if(hashPos>=0){
            uri=href.substring(0,hashPos);
            fragment=href.substring(hashPos+1);
        }
        String result=null;
        try{
            result=catalog.resolveURI(href);
        }catch(Exception e){
            // nop;
        }
        if(result==null){
            try{
                URL url=null;
                if(base==null){
                    url=new URL(uri);
                    result=url.toString();
                }else{
                    URL baseURL=new URL(base);
                    url=(href.length()==0?baseURL:new URL(baseURL,uri));
                    result=url.toString();
                }
            }catch(MalformedURLException mue){
                // try to make an absolute URI from the current base
                String absBase=makeAbsolute(base);
                if(!absBase.equals(base)){
                    // don't bother if the absBase isn't different!
                    return resolve(href,absBase);
                }else{
                    throw new TransformerException("Malformed URL "
                            +href+"(base "+base+")",
                            mue);
                }
            }
        }
        catalogManager.debug.message(2,"Resolved URI",href,result);
        SAXSource source=new SAXSource();
        source.setInputSource(new InputSource(result));
        setEntityResolver(source);
        return source;
    }

    private void setEntityResolver(SAXSource source) throws TransformerException{
        XMLReader reader=source.getXMLReader();
        if(reader==null){
            SAXParserFactory spFactory=catalogManager.useServicesMechanism()?
                    SAXParserFactory.newInstance():new SAXParserFactoryImpl();
            spFactory.setNamespaceAware(true);
            try{
                reader=spFactory.newSAXParser().getXMLReader();
            }catch(ParserConfigurationException ex){
                throw new TransformerException(ex);
            }catch(SAXException ex){
                throw new TransformerException(ex);
            }
        }
        reader.setEntityResolver(this);
        source.setXMLReader(reader);
    }

    private String makeAbsolute(String uri){
        if(uri==null){
            uri="";
        }
        try{
            URL url=new URL(uri);
            return url.toString();
        }catch(MalformedURLException mue){
            try{
                URL fileURL=FileURL.makeURL(uri);
                return fileURL.toString();
            }catch(MalformedURLException mue2){
                // bail
                return uri;
            }
        }
    }
}
