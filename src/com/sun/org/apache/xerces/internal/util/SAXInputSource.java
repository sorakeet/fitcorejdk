/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.Reader;

public final class SAXInputSource extends XMLInputSource{
    private XMLReader fXMLReader;
    private InputSource fInputSource;

    public SAXInputSource(){
        this(null);
    }

    public SAXInputSource(InputSource inputSource){
        this(null,inputSource);
    }

    public SAXInputSource(XMLReader reader,InputSource inputSource){
        super(inputSource!=null?inputSource.getPublicId():null,
                inputSource!=null?inputSource.getSystemId():null,null);
        if(inputSource!=null){
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        }
        fInputSource=inputSource;
        fXMLReader=reader;
    }

    public XMLReader getXMLReader(){
        return fXMLReader;
    }

    public void setXMLReader(XMLReader reader){
        fXMLReader=reader;
    }

    public InputSource getInputSource(){
        return fInputSource;
    }

    public void setInputSource(InputSource inputSource){
        if(inputSource!=null){
            setPublicId(inputSource.getPublicId());
            setSystemId(inputSource.getSystemId());
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        }else{
            setPublicId(null);
            setSystemId(null);
            setByteStream(null);
            setCharacterStream(null);
            setEncoding(null);
        }
        fInputSource=inputSource;
    }

    public void setPublicId(String publicId){
        super.setPublicId(publicId);
        if(fInputSource==null){
            fInputSource=new InputSource();
        }
        fInputSource.setPublicId(publicId);
    } // setPublicId(String)

    public void setSystemId(String systemId){
        super.setSystemId(systemId);
        if(fInputSource==null){
            fInputSource=new InputSource();
        }
        fInputSource.setSystemId(systemId);
    } // setSystemId(String)

    public void setByteStream(InputStream byteStream){
        super.setByteStream(byteStream);
        if(fInputSource==null){
            fInputSource=new InputSource();
        }
        fInputSource.setByteStream(byteStream);
    } // setByteStream(InputStream)

    public void setCharacterStream(Reader charStream){
        super.setCharacterStream(charStream);
        if(fInputSource==null){
            fInputSource=new InputSource();
        }
        fInputSource.setCharacterStream(charStream);
    } // setCharacterStream(Reader)

    public void setEncoding(String encoding){
        super.setEncoding(encoding);
        if(fInputSource==null){
            fInputSource=new InputSource();
        }
        fInputSource.setEncoding(encoding);
    } // setEncoding(String)
} // SAXInputSource
