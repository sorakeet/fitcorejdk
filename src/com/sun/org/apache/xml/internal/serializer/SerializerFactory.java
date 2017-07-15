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
 * <p>
 * $Id: SerializerFactory.java,v 1.2.4.1 2005/09/15 08:15:24 suresh_emailid Exp $
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
/**
 * $Id: SerializerFactory.java,v 1.2.4.1 2005/09/15 08:15:24 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import org.xml.sax.ContentHandler;

import javax.xml.transform.OutputKeys;
import java.util.Properties;

public final class SerializerFactory{
    private SerializerFactory(){
    }

    public static Serializer getSerializer(Properties format){
        Serializer ser;
        try{
            String method=format.getProperty(OutputKeys.METHOD);
            if(method==null){
                String msg=Utils.messages.createMessage(
                        MsgKey.ER_FACTORY_PROPERTY_MISSING,
                        new Object[]{OutputKeys.METHOD});
                throw new IllegalArgumentException(msg);
            }
            String className=
                    format.getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);
            if(null==className){
                // Missing Content Handler property, load default using OutputPropertiesFactory
                Properties methodDefaults=
                        OutputPropertiesFactory.getDefaultMethodProperties(method);
                className=
                        methodDefaults.getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);
                if(null==className){
                    String msg=Utils.messages.createMessage(
                            MsgKey.ER_FACTORY_PROPERTY_MISSING,
                            new Object[]{OutputPropertiesFactory.S_KEY_CONTENT_HANDLER});
                    throw new IllegalArgumentException(msg);
                }
            }
            Class cls=ObjectFactory.findProviderClass(className,true);
            // _serializers.put(method, cls);
            Object obj=cls.newInstance();
            if(obj instanceof SerializationHandler){
                // this is one of the supplied serializers
                ser=(Serializer)cls.newInstance();
                ser.setOutputFormat(format);
            }else{
                /**
                 *  This  must be a user defined Serializer.
                 *  It had better implement ContentHandler.
                 */
                if(obj instanceof ContentHandler){
                    /**
                     * The user defined serializer defines ContentHandler,
                     * but we need to wrap it with ToXMLSAXHandler which
                     * will collect SAX-like events and emit true
                     * SAX ContentHandler events to the users handler.
                     */
                    className=SerializerConstants.DEFAULT_SAX_SERIALIZER;
                    cls=ObjectFactory.findProviderClass(className,true);
                    SerializationHandler sh=
                            (SerializationHandler)cls.newInstance();
                    sh.setContentHandler((ContentHandler)obj);
                    sh.setOutputFormat(format);
                    ser=sh;
                }else{
                    // user defined serializer does not implement
                    // ContentHandler, ... very bad
                    throw new Exception(
                            Utils.messages.createMessage(
                                    MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                                    new Object[]{className}));
                }
            }
        }catch(Exception e){
            throw new com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException(e);
        }
        // If we make it to here ser is not null.
        return ser;
    }
}
