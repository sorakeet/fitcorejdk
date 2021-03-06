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
package com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509Certificate;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverException;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Element;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class X509CertificateResolver extends KeyResolverSpi{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(X509CertificateResolver.class.getName());

    public PublicKey engineLookupAndResolvePublicKey(
            Element element,String BaseURI,StorageResolver storage
    ) throws KeyResolverException{
        X509Certificate cert=
                this.engineLookupResolveX509Certificate(element,BaseURI,storage);
        if(cert!=null){
            return cert.getPublicKey();
        }
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(
            Element element,String BaseURI,StorageResolver storage
    ) throws KeyResolverException{
        try{
            Element[] els=
                    XMLUtils.selectDsNodes(element.getFirstChild(),Constants._TAG_X509CERTIFICATE);
            if((els==null)||(els.length==0)){
                Element el=
                        XMLUtils.selectDsNode(element.getFirstChild(),Constants._TAG_X509DATA,0);
                if(el!=null){
                    return engineLookupResolveX509Certificate(el,BaseURI,storage);
                }
                return null;
            }
            // populate Object array
            for(int i=0;i<els.length;i++){
                XMLX509Certificate xmlCert=new XMLX509Certificate(els[i],BaseURI);
                X509Certificate cert=xmlCert.getX509Certificate();
                if(cert!=null){
                    return cert;
                }
            }
            return null;
        }catch(XMLSecurityException ex){
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"XMLSecurityException",ex);
            }
            throw new KeyResolverException("generic.EmptyMessage",ex);
        }
    }

    public javax.crypto.SecretKey engineLookupAndResolveSecretKey(
            Element element,String BaseURI,StorageResolver storage
    ){
        return null;
    }
}
