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
package com.sun.org.apache.xml.internal.security.algorithms;

import com.sun.org.apache.xml.internal.security.signature.XMLSignatureException;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.EncryptionConstants;
import org.w3c.dom.Document;

import java.security.MessageDigest;
import java.security.NoSuchProviderException;

public class MessageDigestAlgorithm extends Algorithm{
    public static final String ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5=
            Constants.MoreAlgorithmsSpecNS+"md5";
    public static final String ALGO_ID_DIGEST_SHA1=Constants.SignatureSpecNS+"sha1";
    public static final String ALGO_ID_DIGEST_SHA256=
            EncryptionConstants.EncryptionSpecNS+"sha256";
    public static final String ALGO_ID_DIGEST_SHA384=
            Constants.MoreAlgorithmsSpecNS+"sha384";
    public static final String ALGO_ID_DIGEST_SHA512=
            EncryptionConstants.EncryptionSpecNS+"sha512";
    public static final String ALGO_ID_DIGEST_RIPEMD160=
            EncryptionConstants.EncryptionSpecNS+"ripemd160";
    private final MessageDigest algorithm;

    private MessageDigestAlgorithm(Document doc,String algorithmURI)
            throws XMLSignatureException{
        super(doc,algorithmURI);
        algorithm=getDigestInstance(algorithmURI);
    }

    private static MessageDigest getDigestInstance(String algorithmURI) throws XMLSignatureException{
        String algorithmID=JCEMapper.translateURItoJCEID(algorithmURI);
        if(algorithmID==null){
            Object[] exArgs={algorithmURI};
            throw new XMLSignatureException("algorithms.NoSuchMap",exArgs);
        }
        MessageDigest md;
        String provider=JCEMapper.getProviderId();
        try{
            if(provider==null){
                md=MessageDigest.getInstance(algorithmID);
            }else{
                md=MessageDigest.getInstance(algorithmID,provider);
            }
        }catch(java.security.NoSuchAlgorithmException ex){
            Object[] exArgs={algorithmID,ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm",exArgs);
        }catch(NoSuchProviderException ex){
            Object[] exArgs={algorithmID,ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm",exArgs);
        }
        return md;
    }

    public static MessageDigestAlgorithm getInstance(
            Document doc,String algorithmURI
    ) throws XMLSignatureException{
        return new MessageDigestAlgorithm(doc,algorithmURI);
    }

    public static boolean isEqual(byte[] digesta,byte[] digestb){
        return MessageDigest.isEqual(digesta,digestb);
    }

    public MessageDigest getAlgorithm(){
        return algorithm;
    }

    public byte[] digest(){
        return algorithm.digest();
    }

    public byte[] digest(byte input[]){
        return algorithm.digest(input);
    }

    public int digest(byte buf[],int offset,int len) throws java.security.DigestException{
        return algorithm.digest(buf,offset,len);
    }

    public String getJCEAlgorithmString(){
        return algorithm.getAlgorithm();
    }

    public java.security.Provider getJCEProvider(){
        return algorithm.getProvider();
    }

    public int getDigestLength(){
        return algorithm.getDigestLength();
    }

    public void reset(){
        algorithm.reset();
    }

    public void update(byte[] input){
        algorithm.update(input);
    }

    public void update(byte input){
        algorithm.update(input);
    }

    public void update(byte buf[],int offset,int len){
        algorithm.update(buf,offset,len);
    }

    public String getBaseNamespace(){
        return Constants.SignatureSpecNS;
    }

    public String getBaseLocalName(){
        return Constants._TAG_DIGESTMETHOD;
    }
}
