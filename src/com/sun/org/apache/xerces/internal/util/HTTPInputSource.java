/**
 * Copyright (c) 2007, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class HTTPInputSource extends XMLInputSource{
    //
    // Data
    //
    protected boolean fFollowRedirects=true;
    protected Map<String,String> fHTTPRequestProperties=new HashMap<>();
    //
    // Constructors
    //

    public HTTPInputSource(String publicId,String systemId,String baseSystemId){
        super(publicId,systemId,baseSystemId);
    } // <init>(String,String,String)

    public HTTPInputSource(XMLResourceIdentifier resourceIdentifier){
        super(resourceIdentifier);
    } // <init>(XMLResourceIdentifier)

    public HTTPInputSource(String publicId,String systemId,
                           String baseSystemId,InputStream byteStream,String encoding){
        super(publicId,systemId,baseSystemId,byteStream,encoding);
    } // <init>(String,String,String,InputStream,String)

    public HTTPInputSource(String publicId,String systemId,
                           String baseSystemId,Reader charStream,String encoding){
        super(publicId,systemId,baseSystemId,charStream,encoding);
    } // <init>(String,String,String,Reader,String)
    //
    // Public methods
    //

    public boolean getFollowHTTPRedirects(){
        return fFollowRedirects;
    } // getFollowHTTPRedirects():boolean

    public void setFollowHTTPRedirects(boolean followRedirects){
        fFollowRedirects=followRedirects;
    } // setFollowHTTPRedirects(boolean)

    public String getHTTPRequestProperty(String key){
        return fHTTPRequestProperties.get(key);
    } // getHTTPRequestProperty(String):String

    public Iterator<Map.Entry<String,String>> getHTTPRequestProperties(){
        return fHTTPRequestProperties.entrySet().iterator();
    } // getHTTPRequestProperties():Iterator

    public void setHTTPRequestProperty(String key,String value){
        if(value!=null){
            fHTTPRequestProperties.put(key,value);
        }else{
            fHTTPRequestProperties.remove(key);
        }
    } // setHTTPRequestProperty(String,String)
} // class HTTPInputSource
