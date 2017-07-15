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
package com.sun.org.apache.xml.internal.security.keys.storage;

import com.sun.org.apache.xml.internal.security.keys.storage.implementations.KeyStoreResolver;
import com.sun.org.apache.xml.internal.security.keys.storage.implementations.SingleCertificateResolver;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class StorageResolver{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(StorageResolver.class.getName());
    private List<StorageResolverSpi> storageResolvers=null;

    public StorageResolver(){
    }

    public StorageResolver(StorageResolverSpi resolver){
        this.add(resolver);
    }

    public void add(StorageResolverSpi resolver){
        if(storageResolvers==null){
            storageResolvers=new ArrayList<StorageResolverSpi>();
        }
        this.storageResolvers.add(resolver);
    }

    public StorageResolver(KeyStore keyStore){
        this.add(keyStore);
    }

    public void add(KeyStore keyStore){
        try{
            this.add(new KeyStoreResolver(keyStore));
        }catch(StorageResolverException ex){
            log.log(java.util.logging.Level.SEVERE,"Could not add KeyStore because of: ",ex);
        }
    }

    public StorageResolver(X509Certificate x509certificate){
        this.add(x509certificate);
    }

    public void add(X509Certificate x509certificate){
        this.add(new SingleCertificateResolver(x509certificate));
    }

    public Iterator<Certificate> getIterator(){
        return new StorageResolverIterator(this.storageResolvers.iterator());
    }

    static class StorageResolverIterator implements Iterator<Certificate>{
        Iterator<StorageResolverSpi> resolvers=null;
        Iterator<Certificate> currentResolver=null;

        public StorageResolverIterator(Iterator<StorageResolverSpi> resolvers){
            this.resolvers=resolvers;
            currentResolver=findNextResolver();
        }

        // Find the next storage with at least one element and return its Iterator
        private Iterator<Certificate> findNextResolver(){
            while(resolvers.hasNext()){
                StorageResolverSpi resolverSpi=resolvers.next();
                Iterator<Certificate> iter=resolverSpi.getIterator();
                if(iter.hasNext()){
                    return iter;
                }
            }
            return null;
        }        public boolean hasNext(){
            if(currentResolver==null){
                return false;
            }
            if(currentResolver.hasNext()){
                return true;
            }
            currentResolver=findNextResolver();
            return (currentResolver!=null);
        }

        public Certificate next(){
            if(hasNext()){
                return currentResolver.next();
            }
            throw new NoSuchElementException();
        }

        public void remove(){
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }


    }
}
