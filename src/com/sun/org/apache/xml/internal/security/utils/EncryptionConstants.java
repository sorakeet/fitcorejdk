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
package com.sun.org.apache.xml.internal.security.utils;

public class EncryptionConstants{
    // Attributes that exist in XML Signature in the same way
    public static final String _ATT_ALGORITHM=Constants._ATT_ALGORITHM;
    public static final String _ATT_ID=Constants._ATT_ID;
    public static final String _ATT_TARGET=Constants._ATT_TARGET;
    public static final String _ATT_TYPE=Constants._ATT_TYPE;
    public static final String _ATT_URI=Constants._ATT_URI;
    // Attributes new in XML Encryption
    public static final String _ATT_ENCODING="Encoding";
    public static final String _ATT_RECIPIENT="Recipient";
    public static final String _ATT_MIMETYPE="MimeType";
    public static final String _TAG_CARRIEDKEYNAME="CarriedKeyName";
    public static final String _TAG_CIPHERDATA="CipherData";
    public static final String _TAG_CIPHERREFERENCE="CipherReference";
    public static final String _TAG_CIPHERVALUE="CipherValue";
    public static final String _TAG_DATAREFERENCE="DataReference";
    public static final String _TAG_ENCRYPTEDDATA="EncryptedData";
    public static final String _TAG_ENCRYPTEDKEY="EncryptedKey";
    public static final String _TAG_ENCRYPTIONMETHOD="EncryptionMethod";
    public static final String _TAG_ENCRYPTIONPROPERTIES="EncryptionProperties";
    public static final String _TAG_ENCRYPTIONPROPERTY="EncryptionProperty";
    public static final String _TAG_KEYREFERENCE="KeyReference";
    public static final String _TAG_KEYSIZE="KeySize";
    public static final String _TAG_OAEPPARAMS="OAEPparams";
    public static final String _TAG_MGF="MGF";
    public static final String _TAG_REFERENCELIST="ReferenceList";
    public static final String _TAG_TRANSFORMS="Transforms";
    public static final String _TAG_AGREEMENTMETHOD="AgreementMethod";
    public static final String _TAG_KA_NONCE="KA-Nonce";
    public static final String _TAG_ORIGINATORKEYINFO="OriginatorKeyInfo";
    public static final String _TAG_RECIPIENTKEYINFO="RecipientKeyInfo";
    public static final String ENCRYPTIONSPECIFICATION_URL=
            "http://www.w3.org/TR/2001/WD-xmlenc-core-20010626/";
    public static final String EncryptionSpecNS=
            "http://www.w3.org/2001/04/xmlenc#";
    public static final String EncryptionSpec11NS=
            "http://www.w3.org/2009/xmlenc11#";
    public static final String TYPE_CONTENT=EncryptionSpecNS+"Content";
    public static final String TYPE_ELEMENT=EncryptionSpecNS+"Element";
    public static final String TYPE_MEDIATYPE=
            "http://www.isi.edu/in-notes/iana/assignments/media-types/";
    public static final String ALGO_ID_BLOCKCIPHER_TRIPLEDES=
            EncryptionConstants.EncryptionSpecNS+"tripledes-cbc";
    public static final String ALGO_ID_BLOCKCIPHER_AES128=
            EncryptionConstants.EncryptionSpecNS+"aes128-cbc";
    public static final String ALGO_ID_BLOCKCIPHER_AES256=
            EncryptionConstants.EncryptionSpecNS+"aes256-cbc";
    public static final String ALGO_ID_BLOCKCIPHER_AES192=
            EncryptionConstants.EncryptionSpecNS+"aes192-cbc";
    public static final String ALGO_ID_BLOCKCIPHER_AES128_GCM=
            "http://www.w3.org/2009/xmlenc11#aes128-gcm";
    public static final String ALGO_ID_BLOCKCIPHER_AES192_GCM=
            "http://www.w3.org/2009/xmlenc11#aes192-gcm";
    public static final String ALGO_ID_BLOCKCIPHER_AES256_GCM=
            "http://www.w3.org/2009/xmlenc11#aes256-gcm";
    public static final String ALGO_ID_KEYTRANSPORT_RSA15=
            EncryptionConstants.EncryptionSpecNS+"rsa-1_5";
    public static final String ALGO_ID_KEYTRANSPORT_RSAOAEP=
            EncryptionConstants.EncryptionSpecNS+"rsa-oaep-mgf1p";
    public static final String ALGO_ID_KEYTRANSPORT_RSAOAEP_11=
            EncryptionConstants.EncryptionSpec11NS+"rsa-oaep";
    public static final String ALGO_ID_KEYAGREEMENT_DH=
            EncryptionConstants.EncryptionSpecNS+"dh";
    public static final String ALGO_ID_KEYWRAP_TRIPLEDES=
            EncryptionConstants.EncryptionSpecNS+"kw-tripledes";
    public static final String ALGO_ID_KEYWRAP_AES128=
            EncryptionConstants.EncryptionSpecNS+"kw-aes128";
    public static final String ALGO_ID_KEYWRAP_AES256=
            EncryptionConstants.EncryptionSpecNS+"kw-aes256";
    public static final String ALGO_ID_KEYWRAP_AES192=
            EncryptionConstants.EncryptionSpecNS+"kw-aes192";
    public static final String ALGO_ID_AUTHENTICATION_XMLSIGNATURE=
            "http://www.w3.org/TR/2001/CR-xmldsig-core-20010419/";
    public static final String ALGO_ID_C14N_WITHCOMMENTS=
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public static final String ALGO_ID_C14N_OMITCOMMENTS=
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String ALGO_ID_ENCODING_BASE64=
            "http://www.w3.org/2000/09/xmldsig#base64";
    public static final String MGF1_SHA1=
            EncryptionConstants.EncryptionSpec11NS+"mgf1sha1";
    public static final String MGF1_SHA224=
            EncryptionConstants.EncryptionSpec11NS+"mgf1sha224";
    public static final String MGF1_SHA256=
            EncryptionConstants.EncryptionSpec11NS+"mgf1sha256";
    public static final String MGF1_SHA384=
            EncryptionConstants.EncryptionSpec11NS+"mgf1sha384";
    public static final String MGF1_SHA512=
            EncryptionConstants.EncryptionSpec11NS+"mgf1sha512";

    private EncryptionConstants(){
        // we don't allow instantiation
    }
}
