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
package com.sun.org.apache.xml.internal.security.keys.content.x509;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;

public class XMLX509Digest extends Signature11ElementProxy implements XMLX509DataContent{
    public XMLX509Digest(Element element,String BaseURI) throws XMLSecurityException{
        super(element,BaseURI);
    }

    public XMLX509Digest(Document doc,byte[] digestBytes,String algorithmURI){
        super(doc);
        this.addBase64Text(digestBytes);
        this.constructionElement.setAttributeNS(null,Constants._ATT_ALGORITHM,algorithmURI);
    }

    public XMLX509Digest(Document doc,X509Certificate x509certificate,String algorithmURI) throws XMLSecurityException{
        super(doc);
        this.addBase64Text(getDigestBytesFromCert(x509certificate,algorithmURI));
        this.constructionElement.setAttributeNS(null,Constants._ATT_ALGORITHM,algorithmURI);
    }

    public static byte[] getDigestBytesFromCert(X509Certificate cert,String algorithmURI) throws XMLSecurityException{
        String jcaDigestAlgorithm=JCEMapper.translateURItoJCEID(algorithmURI);
        if(jcaDigestAlgorithm==null){
            Object exArgs[]={algorithmURI};
            throw new XMLSecurityException("XMLX509Digest.UnknownDigestAlgorithm",exArgs);
        }
        try{
            MessageDigest md=MessageDigest.getInstance(jcaDigestAlgorithm);
            return md.digest(cert.getEncoded());
        }catch(Exception e){
            Object exArgs[]={jcaDigestAlgorithm};
            throw new XMLSecurityException("XMLX509Digest.FailedDigest",exArgs);
        }
    }

    public String getAlgorithm(){
        return this.getAlgorithmAttr().getNodeValue();
    }

    public Attr getAlgorithmAttr(){
        return this.constructionElement.getAttributeNodeNS(null,Constants._ATT_ALGORITHM);
    }

    public byte[] getDigestBytes() throws XMLSecurityException{
        return this.getBytesFromTextChild();
    }

    public String getBaseLocalName(){
        return Constants._TAG_X509DIGEST;
    }
}
