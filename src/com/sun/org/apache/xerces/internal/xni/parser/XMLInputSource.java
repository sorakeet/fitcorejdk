/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;

import java.io.InputStream;
import java.io.Reader;

public class XMLInputSource{
    //
    // Data
    //
    protected String fPublicId;
    protected String fSystemId;
    protected String fBaseSystemId;
    protected InputStream fByteStream;
    protected Reader fCharStream;
    protected String fEncoding;
    //
    // Constructors
    //

    public XMLInputSource(String publicId,String systemId,
                          String baseSystemId){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
    } // <init>(String,String,String)

    public XMLInputSource(XMLResourceIdentifier resourceIdentifier){
        fPublicId=resourceIdentifier.getPublicId();
        fSystemId=resourceIdentifier.getLiteralSystemId();
        fBaseSystemId=resourceIdentifier.getBaseSystemId();
    } // <init>(XMLResourceIdentifier)

    public XMLInputSource(String publicId,String systemId,
                          String baseSystemId,InputStream byteStream,
                          String encoding){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
        fByteStream=byteStream;
        fEncoding=encoding;
    } // <init>(String,String,String,InputStream,String)

    public XMLInputSource(String publicId,String systemId,
                          String baseSystemId,Reader charStream,
                          String encoding){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
        fCharStream=charStream;
        fEncoding=encoding;
    } // <init>(String,String,String,Reader,String)
    //
    // Public methods
    //

    public String getPublicId(){
        return fPublicId;
    } // getPublicId():String

    public void setPublicId(String publicId){
        fPublicId=publicId;
    } // setPublicId(String)

    public String getSystemId(){
        return fSystemId;
    } // getSystemId():String

    public void setSystemId(String systemId){
        fSystemId=systemId;
    } // setSystemId(String)

    public String getBaseSystemId(){
        return fBaseSystemId;
    } // getBaseSystemId():String

    public void setBaseSystemId(String baseSystemId){
        fBaseSystemId=baseSystemId;
    } // setBaseSystemId(String)

    public InputStream getByteStream(){
        return fByteStream;
    } // getByteStream():InputStream

    public void setByteStream(InputStream byteStream){
        fByteStream=byteStream;
    } // setByteStream(InputSource)

    public Reader getCharacterStream(){
        return fCharStream;
    } // getCharacterStream():Reader

    public void setCharacterStream(Reader charStream){
        fCharStream=charStream;
    } // setCharacterStream(Reader)

    public String getEncoding(){
        return fEncoding;
    } // getEncoding():String

    public void setEncoding(String encoding){
        fEncoding=encoding;
    } // setEncoding(String)
} // class XMLInputSource
