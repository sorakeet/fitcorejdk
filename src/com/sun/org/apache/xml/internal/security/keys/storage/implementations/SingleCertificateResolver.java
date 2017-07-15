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

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolverSpi;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleCertificateResolver extends StorageResolverSpi{
    private X509Certificate certificate=null;

    public SingleCertificateResolver(X509Certificate x509cert){
        this.certificate=x509cert;
    }

    public Iterator<Certificate> getIterator(){
        return new InternalIterator(this.certificate);
    }

    static class InternalIterator implements Iterator<Certificate>{
        boolean alreadyReturned=false;
        X509Certificate certificate=null;

        public InternalIterator(X509Certificate x509cert){
            this.certificate=x509cert;
        }

        public boolean hasNext(){
            return !this.alreadyReturned;
        }

        public Certificate next(){
            if(this.alreadyReturned){
                throw new NoSuchElementException();
            }
            this.alreadyReturned=true;
            return this.certificate;
        }

        public void remove(){
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }
    }
}
