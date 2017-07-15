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
package com.sun.org.apache.xml.internal.security.keys.storage.implementations;

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolverException;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolverSpi;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class KeyStoreResolver extends StorageResolverSpi{
    private KeyStore keyStore=null;

    public KeyStoreResolver(KeyStore keyStore) throws StorageResolverException{
        this.keyStore=keyStore;
        // Do a quick check on the keystore
        try{
            keyStore.aliases();
        }catch(KeyStoreException ex){
            throw new StorageResolverException("generic.EmptyMessage",ex);
        }
    }

    public Iterator<Certificate> getIterator(){
        return new KeyStoreIterator(this.keyStore);
    }

    static class KeyStoreIterator implements Iterator<Certificate>{
        KeyStore keyStore=null;
        Enumeration<String> aliases=null;
        Certificate nextCert=null;

        public KeyStoreIterator(KeyStore keyStore){
            try{
                this.keyStore=keyStore;
                this.aliases=this.keyStore.aliases();
            }catch(KeyStoreException ex){
                // empty Enumeration
                this.aliases=new Enumeration<String>(){
                    public boolean hasMoreElements(){
                        return false;
                    }

                    public String nextElement(){
                        return null;
                    }
                };
            }
        }

        public boolean hasNext(){
            if(nextCert==null){
                nextCert=findNextCert();
            }
            return (nextCert!=null);
        }

        public Certificate next(){
            if(nextCert==null){
                // maybe caller did not call hasNext()
                nextCert=findNextCert();
                if(nextCert==null){
                    throw new NoSuchElementException();
                }
            }
            Certificate ret=nextCert;
            nextCert=null;
            return ret;
        }

        public void remove(){
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }

        // Find the next entry that contains a certificate and return it.
        // In particular, this skips over entries containing symmetric keys.
        private Certificate findNextCert(){
            while(this.aliases.hasMoreElements()){
                String alias=this.aliases.nextElement();
                try{
                    Certificate cert=this.keyStore.getCertificate(alias);
                    if(cert!=null){
                        return cert;
                    }
                }catch(KeyStoreException ex){
                    return null;
                }
            }
            return null;
        }
    }
}
