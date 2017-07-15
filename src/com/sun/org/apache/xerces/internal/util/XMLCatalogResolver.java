/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004,2005 The Apache Software Foundation.
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
/**
 * Copyright 2004,2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class XMLCatalogResolver
        implements XMLEntityResolver, EntityResolver2, LSResourceResolver{
    private CatalogManager fResolverCatalogManager=null;
    private Catalog fCatalog=null;
    private String[] fCatalogsList=null;
    private boolean fCatalogsChanged=true;
    private boolean fPreferPublic=true;
    private boolean fUseLiteralSystemId=true;

    public XMLCatalogResolver(){
        this(null,true);
    }

    public XMLCatalogResolver(String[] catalogs,boolean preferPublic){
        init(catalogs,preferPublic);
    }

    private void init(String[] catalogs,boolean preferPublic){
        fCatalogsList=(catalogs!=null)?(String[])catalogs.clone():null;
        fPreferPublic=preferPublic;
        fResolverCatalogManager=new CatalogManager();
        fResolverCatalogManager.setAllowOasisXMLCatalogPI(false);
        fResolverCatalogManager.setCatalogClassName("com.sun.org.apache.xml.internal.resolver.Catalog");
        fResolverCatalogManager.setCatalogFiles("");
        fResolverCatalogManager.setIgnoreMissingProperties(true);
        fResolverCatalogManager.setPreferPublic(fPreferPublic);
        fResolverCatalogManager.setRelativeCatalogs(false);
        fResolverCatalogManager.setUseStaticCatalog(false);
        fResolverCatalogManager.setVerbosity(0);
    }

    public XMLCatalogResolver(String[] catalogs){
        this(catalogs,true);
    }

    public final synchronized String[] getCatalogList(){
        return (fCatalogsList!=null)
                ?(String[])fCatalogsList.clone():null;
    }

    public final synchronized void setCatalogList(String[] catalogs){
        fCatalogsChanged=true;
        fCatalogsList=(catalogs!=null)
                ?(String[])catalogs.clone():null;
    }

    public final synchronized void clear(){
        fCatalog=null;
    }

    public final boolean getPreferPublic(){
        return fPreferPublic;
    }

    public final void setPreferPublic(boolean preferPublic){
        fPreferPublic=preferPublic;
        fResolverCatalogManager.setPreferPublic(preferPublic);
    }

    public final boolean getUseLiteralSystemId(){
        return fUseLiteralSystemId;
    }

    public final void setUseLiteralSystemId(boolean useLiteralSystemId){
        fUseLiteralSystemId=useLiteralSystemId;
    }

    public InputSource resolveEntity(String publicId,String systemId)
            throws SAXException, IOException{
        String resolvedId=null;
        if(publicId!=null&&systemId!=null){
            resolvedId=resolvePublic(publicId,systemId);
        }else if(systemId!=null){
            resolvedId=resolveSystem(systemId);
        }
        if(resolvedId!=null){
            InputSource source=new InputSource(resolvedId);
            source.setPublicId(publicId);
            return source;
        }
        return null;
    }

    public InputSource getExternalSubset(String name,String baseURI)
            throws SAXException, IOException{
        return null;
    }

    public InputSource resolveEntity(String name,String publicId,
                                     String baseURI,String systemId) throws SAXException, IOException{
        String resolvedId=null;
        if(!getUseLiteralSystemId()&&baseURI!=null){
            // Attempt to resolve the system identifier against the base URI.
            try{
                URI uri=new URI(new URI(baseURI),systemId);
                systemId=uri.toString();
            }
            // Ignore the exception. Fallback to the literal system identifier.
            catch(URI.MalformedURIException ex){
            }
        }
        if(publicId!=null&&systemId!=null){
            resolvedId=resolvePublic(publicId,systemId);
        }else if(systemId!=null){
            resolvedId=resolveSystem(systemId);
        }
        if(resolvedId!=null){
            InputSource source=new InputSource(resolvedId);
            source.setPublicId(publicId);
            return source;
        }
        return null;
    }

    public LSInput resolveResource(String type,String namespaceURI,
                                   String publicId,String systemId,String baseURI){
        String resolvedId=null;
        try{
            // The namespace is useful for resolving namespace aware
            // grammars such as XML schema. Let it take precedence over
            // the external identifier if one exists.
            if(namespaceURI!=null){
                resolvedId=resolveURI(namespaceURI);
            }
            if(!getUseLiteralSystemId()&&baseURI!=null){
                // Attempt to resolve the system identifier against the base URI.
                try{
                    URI uri=new URI(new URI(baseURI),systemId);
                    systemId=uri.toString();
                }
                // Ignore the exception. Fallback to the literal system identifier.
                catch(URI.MalformedURIException ex){
                }
            }
            // Resolve against an external identifier if one exists. This
            // is useful for resolving DTD external subsets and other
            // external entities. For XML schemas if there was no namespace
            // mapping we might be able to resolve a system identifier
            // specified as a location hint.
            if(resolvedId==null){
                if(publicId!=null&&systemId!=null){
                    resolvedId=resolvePublic(publicId,systemId);
                }else if(systemId!=null){
                    resolvedId=resolveSystem(systemId);
                }
            }
        }
        // Ignore IOException. It cannot be thrown from this method.
        catch(IOException ex){
        }
        if(resolvedId!=null){
            return new DOMInputImpl(publicId,resolvedId,baseURI);
        }
        return null;
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException{
        String resolvedId=resolveIdentifier(resourceIdentifier);
        if(resolvedId!=null){
            return new XMLInputSource(resourceIdentifier.getPublicId(),
                    resolvedId,
                    resourceIdentifier.getBaseSystemId());
        }
        return null;
    }

    public String resolveIdentifier(XMLResourceIdentifier resourceIdentifier)
            throws IOException, XNIException{
        String resolvedId=null;
        // The namespace is useful for resolving namespace aware
        // grammars such as XML schema. Let it take precedence over
        // the external identifier if one exists.
        String namespace=resourceIdentifier.getNamespace();
        if(namespace!=null){
            resolvedId=resolveURI(namespace);
        }
        // Resolve against an external identifier if one exists. This
        // is useful for resolving DTD external subsets and other
        // external entities. For XML schemas if there was no namespace
        // mapping we might be able to resolve a system identifier
        // specified as a location hint.
        if(resolvedId==null){
            String publicId=resourceIdentifier.getPublicId();
            String systemId=getUseLiteralSystemId()
                    ?resourceIdentifier.getLiteralSystemId()
                    :resourceIdentifier.getExpandedSystemId();
            if(publicId!=null&&systemId!=null){
                resolvedId=resolvePublic(publicId,systemId);
            }else if(systemId!=null){
                resolvedId=resolveSystem(systemId);
            }
        }
        return resolvedId;
    }

    public final synchronized String resolveSystem(String systemId)
            throws IOException{
        if(fCatalogsChanged){
            parseCatalogs();
            fCatalogsChanged=false;
        }
        return (fCatalog!=null)
                ?fCatalog.resolveSystem(systemId):null;
    }

    public final synchronized String resolvePublic(String publicId,String systemId)
            throws IOException{
        if(fCatalogsChanged){
            parseCatalogs();
            fCatalogsChanged=false;
        }
        return (fCatalog!=null)
                ?fCatalog.resolvePublic(publicId,systemId):null;
    }

    public final synchronized String resolveURI(String uri)
            throws IOException{
        if(fCatalogsChanged){
            parseCatalogs();
            fCatalogsChanged=false;
        }
        return (fCatalog!=null)
                ?fCatalog.resolveURI(uri):null;
    }

    private void parseCatalogs() throws IOException{
        if(fCatalogsList!=null){
            fCatalog=new Catalog(fResolverCatalogManager);
            attachReaderToCatalog(fCatalog);
            for(int i=0;i<fCatalogsList.length;++i){
                String catalog=fCatalogsList[i];
                if(catalog!=null&&catalog.length()>0){
                    fCatalog.parseCatalog(catalog);
                }
            }
        }else{
            fCatalog=null;
        }
    }

    private void attachReaderToCatalog(Catalog catalog){
        SAXParserFactory spf=new SAXParserFactoryImpl();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXCatalogReader saxReader=new SAXCatalogReader(spf);
        saxReader.setCatalogParser(OASISXMLCatalogReader.namespaceName,"catalog",
                "com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader");
        catalog.addReader("application/xml",saxReader);
    }
}
