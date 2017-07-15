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
package com.sun.org.apache.xml.internal.security.keys.content;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class DEREncodedKeyValue extends Signature11ElementProxy implements KeyInfoContent{
    private static final String supportedKeyTypes[]={"RSA","DSA","EC"};

    public DEREncodedKeyValue(Element element,String BaseURI) throws XMLSecurityException{
        super(element,BaseURI);
    }

    public DEREncodedKeyValue(Document doc,PublicKey publicKey) throws XMLSecurityException{
        super(doc);
        this.addBase64Text(getEncodedDER(publicKey));
    }

    protected byte[] getEncodedDER(PublicKey publicKey) throws XMLSecurityException{
        try{
            KeyFactory keyFactory=KeyFactory.getInstance(publicKey.getAlgorithm());
            X509EncodedKeySpec keySpec=keyFactory.getKeySpec(publicKey,X509EncodedKeySpec.class);
            return keySpec.getEncoded();
        }catch(NoSuchAlgorithmException e){
            Object exArgs[]={publicKey.getAlgorithm(),publicKey.getFormat(),publicKey.getClass().getName()};
            throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedPublicKey",exArgs,e);
        }catch(InvalidKeySpecException e){
            Object exArgs[]={publicKey.getAlgorithm(),publicKey.getFormat(),publicKey.getClass().getName()};
            throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedPublicKey",exArgs,e);
        }
    }

    public DEREncodedKeyValue(Document doc,byte[] encodedKey){
        super(doc);
        this.addBase64Text(encodedKey);
    }

    public String getId(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ID);
    }

    public void setId(String id){
        if(id!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_ID,id);
            this.constructionElement.setIdAttributeNS(null,Constants._ATT_ID,true);
        }else{
            this.constructionElement.removeAttributeNS(null,Constants._ATT_ID);
        }
    }

    public String getBaseLocalName(){
        return Constants._TAG_DERENCODEDKEYVALUE;
    }

    public PublicKey getPublicKey() throws XMLSecurityException{
        byte[] encodedKey=getBytesFromTextChild();
        // Iterate over the supported key types until one produces a public key.
        for(String keyType : supportedKeyTypes){
            try{
                KeyFactory keyFactory=KeyFactory.getInstance(keyType);
                X509EncodedKeySpec keySpec=new X509EncodedKeySpec(encodedKey);
                PublicKey publicKey=keyFactory.generatePublic(keySpec);
                if(publicKey!=null){
                    return publicKey;
                }
            }catch(NoSuchAlgorithmException e){
                // Do nothing, try the next type
            }catch(InvalidKeySpecException e){
                // Do nothing, try the next type
            }
        }
        throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedEncodedKey");
    }
}
