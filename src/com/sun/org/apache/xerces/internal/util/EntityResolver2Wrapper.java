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

import com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver;
import com.sun.org.apache.xerces.internal.impl.XMLEntityDescription;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class EntityResolver2Wrapper
        implements ExternalSubsetResolver{
    //
    // Data
    //
    protected EntityResolver2 fEntityResolver;
    //
    // Constructors
    //

    public EntityResolver2Wrapper(){
    }

    public EntityResolver2Wrapper(EntityResolver2 entityResolver){
        setEntityResolver(entityResolver);
    } // <init>(EntityResolver2)
    //
    // Public methods
    //

    public EntityResolver2 getEntityResolver(){
        return fEntityResolver;
    } // getEntityResolver():EntityResolver2

    public void setEntityResolver(EntityResolver2 entityResolver){
        fEntityResolver=entityResolver;
    } // setEntityResolver(EntityResolver2)
    //
    // ExternalSubsetResolver methods
    //

    public XMLInputSource getExternalSubset(XMLDTDDescription grammarDescription)
            throws XNIException, IOException{
        if(fEntityResolver!=null){
            String name=grammarDescription.getRootName();
            String baseURI=grammarDescription.getBaseSystemId();
            // Resolve using EntityResolver2
            try{
                InputSource inputSource=fEntityResolver.getExternalSubset(name,baseURI);
                return (inputSource!=null)?createXMLInputSource(inputSource,baseURI):null;
            }
            // error resolving external subset
            catch(SAXException e){
                Exception ex=e.getException();
                if(ex==null){
                    ex=e;
                }
                throw new XNIException(ex);
            }
        }
        // unable to resolve external subset
        return null;
    } // getExternalSubset(XMLDTDDescription):XMLInputSource
    //
    // XMLEntityResolver methods
    //

    private XMLInputSource createXMLInputSource(InputSource source,String baseURI){
        String publicId=source.getPublicId();
        String systemId=source.getSystemId();
        String baseSystemId=baseURI;
        InputStream byteStream=source.getByteStream();
        Reader charStream=source.getCharacterStream();
        String encoding=source.getEncoding();
        XMLInputSource xmlInputSource=
                new XMLInputSource(publicId,systemId,baseSystemId);
        xmlInputSource.setByteStream(byteStream);
        xmlInputSource.setCharacterStream(charStream);
        xmlInputSource.setEncoding(encoding);
        return xmlInputSource;
    } // createXMLInputSource(InputSource,String):XMLInputSource

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException{
        if(fEntityResolver!=null){
            String pubId=resourceIdentifier.getPublicId();
            String sysId=resourceIdentifier.getLiteralSystemId();
            String baseURI=resourceIdentifier.getBaseSystemId();
            String name=null;
            if(resourceIdentifier instanceof XMLDTDDescription){
                name="[dtd]";
            }else if(resourceIdentifier instanceof XMLEntityDescription){
                name=((XMLEntityDescription)resourceIdentifier).getEntityName();
            }
            // When both pubId and sysId are null, the user's entity resolver
            // can do nothing about it. We'd better not bother calling it.
            // This happens when the resourceIdentifier is a GrammarDescription,
            // which describes a schema grammar of some namespace, but without
            // any schema location hint. -Sg
            if(pubId==null&&sysId==null){
                return null;
            }
            // Resolve using EntityResolver2
            try{
                InputSource inputSource=
                        fEntityResolver.resolveEntity(name,pubId,baseURI,sysId);
                return (inputSource!=null)?createXMLInputSource(inputSource,baseURI):null;
            }
            // error resolving entity
            catch(SAXException e){
                Exception ex=e.getException();
                if(ex==null){
                    ex=e;
                }
                throw new XNIException(ex);
            }
        }
        // unable to resolve entity
        return null;
    } // resolveEntity(XMLResourceIdentifier):XMLInputSource
} // class EntityResolver2Wrapper
