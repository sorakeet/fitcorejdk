/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.org.apache.xml.internal.serialize;

import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class SerializerFactory{
    public static final String FactoriesProperty="com.sun.org.apache.xml.internal.serialize.factories";
    private static final Map<String,SerializerFactory> _factories=Collections.synchronizedMap(new HashMap());

    static{
        SerializerFactory factory;
        String list;
        StringTokenizer token;
        String className;
        // The default factories are always registered first,
        // any factory specified in the properties file and supporting
        // the same method will override the default factory.
        factory=new SerializerFactoryImpl(Method.XML);
        registerSerializerFactory(factory);
        factory=new SerializerFactoryImpl(Method.HTML);
        registerSerializerFactory(factory);
        factory=new SerializerFactoryImpl(Method.XHTML);
        registerSerializerFactory(factory);
        factory=new SerializerFactoryImpl(Method.TEXT);
        registerSerializerFactory(factory);
        list=SecuritySupport.getSystemProperty(FactoriesProperty);
        if(list!=null){
            token=new StringTokenizer(list," ;,:");
            while(token.hasMoreTokens()){
                className=token.nextToken();
                try{
                    factory=(SerializerFactory)ObjectFactory.newInstance(className,true);
                    if(_factories.containsKey(factory.getSupportedMethod()))
                        _factories.put(factory.getSupportedMethod(),factory);
                }catch(Exception except){
                }
            }
        }
    }

    public static void registerSerializerFactory(SerializerFactory factory){
        String method;
        synchronized(_factories){
            method=factory.getSupportedMethod();
            _factories.put(method,factory);
        }
    }

    public static SerializerFactory getSerializerFactory(String method){
        return _factories.get(method);
    }

    protected abstract String getSupportedMethod();

    public abstract Serializer makeSerializer(OutputFormat format);

    public abstract Serializer makeSerializer(Writer writer,
                                              OutputFormat format);

    public abstract Serializer makeSerializer(OutputStream output,
                                              OutputFormat format)
            throws UnsupportedEncodingException;
}
