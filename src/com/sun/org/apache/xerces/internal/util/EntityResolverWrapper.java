/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class EntityResolverWrapper
        implements XMLEntityResolver{
    //
    // Data
    //
    protected EntityResolver fEntityResolver;
    //
    // Constructors
    //

    public EntityResolverWrapper(){
    }

    public EntityResolverWrapper(EntityResolver entityResolver){
        setEntityResolver(entityResolver);
    } // <init>(EntityResolver)
    //
    // Public methods
    //

    public EntityResolver getEntityResolver(){
        return fEntityResolver;
    } // getEntityResolver():EntityResolver

    public void setEntityResolver(EntityResolver entityResolver){
        fEntityResolver=entityResolver;
    } // setEntityResolver(EntityResolver)
    //
    // XMLEntityResolver methods
    //

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException{
        // When both pubId and sysId are null, the user's entity resolver
        // can do nothing about it. We'd better not bother calling it.
        // This happens when the resourceIdentifier is a GrammarDescription,
        // which describes a schema grammar of some namespace, but without
        // any schema location hint. -Sg
        String pubId=resourceIdentifier.getPublicId();
        String sysId=resourceIdentifier.getExpandedSystemId();
        if(pubId==null&&sysId==null)
            return null;
        // resolve entity using SAX entity resolver
        if(fEntityResolver!=null&&resourceIdentifier!=null){
            try{
                InputSource inputSource=fEntityResolver.resolveEntity(pubId,sysId);
                if(inputSource!=null){
                    String publicId=inputSource.getPublicId();
                    String systemId=inputSource.getSystemId();
                    String baseSystemId=resourceIdentifier.getBaseSystemId();
                    InputStream byteStream=inputSource.getByteStream();
                    Reader charStream=inputSource.getCharacterStream();
                    String encoding=inputSource.getEncoding();
                    XMLInputSource xmlInputSource=
                            new XMLInputSource(publicId,systemId,baseSystemId);
                    xmlInputSource.setByteStream(byteStream);
                    xmlInputSource.setCharacterStream(charStream);
                    xmlInputSource.setEncoding(encoding);
                    return xmlInputSource;
                }
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
    } // resolveEntity(String,String,String):XMLInputSource
}
