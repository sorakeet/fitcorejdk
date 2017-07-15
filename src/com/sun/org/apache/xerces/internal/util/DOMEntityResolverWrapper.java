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
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class DOMEntityResolverWrapper
        implements XMLEntityResolver{
    //
    // Data
    //
    private static final String XML_TYPE="http://www.w3.org/TR/REC-xml";
    private static final String XSD_TYPE="http://www.w3.org/2001/XMLSchema";
    protected LSResourceResolver fEntityResolver;
    //
    // Constructors
    //

    public DOMEntityResolverWrapper(){
    }

    public DOMEntityResolverWrapper(LSResourceResolver entityResolver){
        setEntityResolver(entityResolver);
    } // LSResourceResolver
    //
    // Public methods
    //

    public LSResourceResolver getEntityResolver(){
        return fEntityResolver;
    } // getEntityResolver():LSResourceResolver

    public void setEntityResolver(LSResourceResolver entityResolver){
        fEntityResolver=entityResolver;
    } // setEntityResolver(LSResourceResolver)
    //
    // XMLEntityResolver methods
    //

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException{
        // resolve entity using DOM entity resolver
        if(fEntityResolver!=null){
            // For entity resolution the type of the resource would be  XML TYPE
            // DOM L3 LS spec mention only the XML 1.0 recommendation right now
            LSInput inputSource=
                    resourceIdentifier==null
                            ?fEntityResolver.resolveResource(
                            null,
                            null,
                            null,
                            null,
                            null)
                            :fEntityResolver.resolveResource(
                            getType(resourceIdentifier),
                            resourceIdentifier.getNamespace(),
                            resourceIdentifier.getPublicId(),
                            resourceIdentifier.getLiteralSystemId(),
                            resourceIdentifier.getBaseSystemId());
            if(inputSource!=null){
                String publicId=inputSource.getPublicId();
                String systemId=inputSource.getSystemId();
                String baseSystemId=inputSource.getBaseURI();
                InputStream byteStream=inputSource.getByteStream();
                Reader charStream=inputSource.getCharacterStream();
                String encoding=inputSource.getEncoding();
                String data=inputSource.getStringData();
                /**
                 * An LSParser looks at inputs specified in LSInput in
                 * the following order: characterStream, byteStream,
                 * stringData, systemId, publicId.
                 */
                XMLInputSource xmlInputSource=
                        new XMLInputSource(publicId,systemId,baseSystemId);
                if(charStream!=null){
                    xmlInputSource.setCharacterStream(charStream);
                }else if(byteStream!=null){
                    xmlInputSource.setByteStream((InputStream)byteStream);
                }else if(data!=null&&data.length()!=0){
                    xmlInputSource.setCharacterStream(new StringReader(data));
                }
                xmlInputSource.setEncoding(encoding);
                return xmlInputSource;
            }
        }
        // unable to resolve entity
        return null;
    } // resolveEntity(String,String,String):XMLInputSource

    private String getType(XMLResourceIdentifier resourceIdentifier){
        if(resourceIdentifier instanceof XMLGrammarDescription){
            XMLGrammarDescription desc=(XMLGrammarDescription)resourceIdentifier;
            if(XMLGrammarDescription.XML_SCHEMA.equals(desc.getGrammarType())){
                return XSD_TYPE;
            }
        }
        return XML_TYPE;
    } // getType(XMLResourceIdentifier):String
} // DOMEntityResolverWrapper
