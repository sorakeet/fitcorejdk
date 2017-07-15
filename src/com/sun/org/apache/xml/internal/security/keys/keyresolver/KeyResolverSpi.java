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
package com.sun.org.apache.xml.internal.security.keys.keyresolver;

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import org.w3c.dom.Element;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public abstract class KeyResolverSpi{
    protected java.util.Map<String,String> properties=null;
    protected boolean globalResolver=false;
    protected boolean secureValidation;

    public void setSecureValidation(boolean secureValidation){
        this.secureValidation=secureValidation;
    }

    public boolean engineCanResolve(Element element,String baseURI,StorageResolver storage){
        throw new UnsupportedOperationException();
    }

    public PublicKey engineResolvePublicKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        throw new UnsupportedOperationException();
    }

    ;

    public PublicKey engineLookupAndResolvePublicKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        KeyResolverSpi tmp=cloneIfNeeded();
        if(!tmp.engineCanResolve(element,baseURI,storage)){
            return null;
        }
        return tmp.engineResolvePublicKey(element,baseURI,storage);
    }

    private KeyResolverSpi cloneIfNeeded() throws KeyResolverException{
        KeyResolverSpi tmp=this;
        if(globalResolver){
            try{
                tmp=getClass().newInstance();
            }catch(InstantiationException e){
                throw new KeyResolverException("",e);
            }catch(IllegalAccessException e){
                throw new KeyResolverException("",e);
            }
        }
        return tmp;
    }

    public X509Certificate engineResolveX509Certificate(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        throw new UnsupportedOperationException();
    }

    ;

    public X509Certificate engineLookupResolveX509Certificate(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        KeyResolverSpi tmp=cloneIfNeeded();
        if(!tmp.engineCanResolve(element,baseURI,storage)){
            return null;
        }
        return tmp.engineResolveX509Certificate(element,baseURI,storage);
    }

    public SecretKey engineResolveSecretKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        throw new UnsupportedOperationException();
    }

    ;

    public SecretKey engineLookupAndResolveSecretKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        KeyResolverSpi tmp=cloneIfNeeded();
        if(!tmp.engineCanResolve(element,baseURI,storage)){
            return null;
        }
        return tmp.engineResolveSecretKey(element,baseURI,storage);
    }

    public PrivateKey engineLookupAndResolvePrivateKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        // This method was added later, it has no equivalent
        // engineResolvePrivateKey() in the old API.
        // We cannot throw UnsupportedOperationException because
        // KeyResolverSpi implementations who don't know about
        // this method would stop the search too early.
        return null;
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

    public boolean understandsProperty(String propertyToTest){
        if(properties==null){
            return false;
        }
        return properties.get(propertyToTest)!=null;
    }

    public void setGlobalResolver(boolean globalResolver){
        this.globalResolver=globalResolver;
    }
}
