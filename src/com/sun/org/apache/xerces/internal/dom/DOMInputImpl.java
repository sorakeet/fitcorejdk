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
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;
// REVISIT:
// 1. it should be possible to do the following
// DOMInputImpl extends XMLInputSource implements LSInput
// 2. we probably need only the default constructor.  -- el

public class DOMInputImpl implements LSInput{
    //
    // Data
    //
    protected String fPublicId=null;
    protected String fSystemId=null;
    protected String fBaseSystemId=null;
    protected InputStream fByteStream=null;
    protected Reader fCharStream=null;
    protected String fData=null;
    protected String fEncoding=null;
    protected boolean fCertifiedText=false;

    public DOMInputImpl(){
    }

    public DOMInputImpl(String publicId,String systemId,
                        String baseSystemId){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
    } // DOMInputImpl(String,String,String)

    public DOMInputImpl(String publicId,String systemId,
                        String baseSystemId,InputStream byteStream,
                        String encoding){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
        fByteStream=byteStream;
        fEncoding=encoding;
    } // DOMInputImpl(String,String,String,InputStream,String)

    public DOMInputImpl(String publicId,String systemId,
                        String baseSystemId,Reader charStream,
                        String encoding){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
        fCharStream=charStream;
        fEncoding=encoding;
    } // DOMInputImpl(String,String,String,Reader,String)

    public DOMInputImpl(String publicId,String systemId,
                        String baseSystemId,String data,
                        String encoding){
        fPublicId=publicId;
        fSystemId=systemId;
        fBaseSystemId=baseSystemId;
        fData=data;
        fEncoding=encoding;
    } // DOMInputImpl(String,String,String,String,String)

    public Reader getCharacterStream(){
        return fCharStream;
    }

    public void setCharacterStream(Reader characterStream){
        fCharStream=characterStream;
    }

    public InputStream getByteStream(){
        return fByteStream;
    }

    public void setByteStream(InputStream byteStream){
        fByteStream=byteStream;
    }

    public String getStringData(){
        return fData;
    }

    public void setStringData(String stringData){
        fData=stringData;
    }

    public String getSystemId(){
        return fSystemId;
    }

    public void setSystemId(String systemId){
        fSystemId=systemId;
    }

    public String getPublicId(){
        return fPublicId;
    }

    public void setPublicId(String publicId){
        fPublicId=publicId;
    }

    public String getBaseURI(){
        return fBaseSystemId;
    }

    public void setBaseURI(String baseURI){
        fBaseSystemId=baseURI;
    }

    public String getEncoding(){
        return fEncoding;
    }

    public void setEncoding(String encoding){
        fEncoding=encoding;
    }

    public boolean getCertifiedText(){
        return fCertifiedText;
    }

    public void setCertifiedText(boolean certifiedText){
        fCertifiedText=certifiedText;
    }
}// class DOMInputImpl
