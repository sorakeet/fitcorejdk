/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.utils.resolver;

import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import org.w3c.dom.Attr;

import java.util.HashMap;
import java.util.Map;

public abstract class ResourceResolverSpi{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(ResourceResolverSpi.class.getName());
    @Deprecated
    protected final boolean secureValidation=true;
    protected Map<String,String> properties=null;

    public static String fixURI(String str){
        // handle platform dependent strings
        str=str.replace(java.io.File.separatorChar,'/');
        if(str.length()>=4){
            // str =~ /^\W:\/([^/])/ # to speak perl ;-))
            char ch0=Character.toUpperCase(str.charAt(0));
            char ch1=str.charAt(1);
            char ch2=str.charAt(2);
            char ch3=str.charAt(3);
            boolean isDosFilename=((('A'<=ch0)&&(ch0<='Z'))
                    &&(ch1==':')&&(ch2=='/')
                    &&(ch3!='/'));
            if(isDosFilename&&log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"Found DOS filename: "+str);
            }
        }
        // Windows fix
        if(str.length()>=2){
            char ch1=str.charAt(1);
            if(ch1==':'){
                char ch0=Character.toUpperCase(str.charAt(0));
                if(('A'<=ch0)&&(ch0<='Z')){
                    str="/"+str;
                }
            }
        }
        // done
        return str;
    }

    public XMLSignatureInput engineResolveURI(ResourceResolverContext context)
            throws ResourceResolverException{
        // The default implementation, to preserve backwards compatibility in the
        // test cases, calls the old resolver API.
        return engineResolve(context.attr,context.baseUri);
    }

    @Deprecated
    public XMLSignatureInput engineResolve(Attr uri,String BaseURI)
            throws ResourceResolverException{
        throw new UnsupportedOperationException();
    }

    public void engineSetProperty(String key,String value){
        if(properties==null){
            properties=new HashMap<String,String>();
        }
        properties.put(key,value);
    }

    public String engineGetProperty(String key){
        if(properties==null){
            return null;
        }
        return properties.get(key);
    }

    public void engineAddProperies(Map<String,String> newProperties){
        if(newProperties!=null&&!newProperties.isEmpty()){
            if(properties==null){
                properties=new HashMap<String,String>();
            }
            properties.putAll(newProperties);
        }
    }

    public boolean engineIsThreadSafe(){
        return false;
    }

    public boolean engineCanResolveURI(ResourceResolverContext context){
        // To preserve backward compatibility with existing resolvers that might override the old method,
        // call the old deprecated API.
        return engineCanResolve(context.attr,context.baseUri);
    }

    @Deprecated
    public boolean engineCanResolve(Attr uri,String BaseURI){
        // This method used to be abstract, so any calls to "super" are bogus.
        throw new UnsupportedOperationException();
    }

    public boolean understandsProperty(String propertyToTest){
        String[] understood=this.engineGetPropertyKeys();
        if(understood!=null){
            for(int i=0;i<understood.length;i++){
                if(understood[i].equals(propertyToTest)){
                    return true;
                }
            }
        }
        return false;
    }

    public String[] engineGetPropertyKeys(){
        return new String[0];
    }
}
