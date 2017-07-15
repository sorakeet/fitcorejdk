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
package com.sun.org.apache.xml.internal.security.encryption;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm;
import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverException;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations.EncryptedKeyResolver;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureException;
import com.sun.org.apache.xml.internal.security.transforms.InvalidTransformException;
import com.sun.org.apache.xml.internal.security.transforms.TransformationException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.org.apache.xml.internal.security.utils.*;
import org.w3c.dom.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.*;

public class XMLCipher{
    public static final String TRIPLEDES=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES;
    public static final String AES_128=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
    public static final String AES_256=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256;
    public static final String AES_192=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192;
    public static final String AES_128_GCM=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM;
    public static final String AES_192_GCM=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192_GCM;
    public static final String AES_256_GCM=
            EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM;
    public static final String RSA_v1dot5=
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
    public static final String RSA_OAEP=
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;
    public static final String RSA_OAEP_11=
            EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP_11;
    public static final String DIFFIE_HELLMAN=
            EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH;
    public static final String TRIPLEDES_KeyWrap=
            EncryptionConstants.ALGO_ID_KEYWRAP_TRIPLEDES;
    public static final String AES_128_KeyWrap=
            EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
    public static final String AES_256_KeyWrap=
            EncryptionConstants.ALGO_ID_KEYWRAP_AES256;
    public static final String AES_192_KeyWrap=
            EncryptionConstants.ALGO_ID_KEYWRAP_AES192;
    public static final String SHA1=
            Constants.ALGO_ID_DIGEST_SHA1;
    public static final String SHA256=
            MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
    public static final String SHA512=
            MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512;
    public static final String RIPEMD_160=
            MessageDigestAlgorithm.ALGO_ID_DIGEST_RIPEMD160;
    public static final String XML_DSIG=
            Constants.SignatureSpecNS;
    public static final String N14C_XML=
            Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String N14C_XML_WITH_COMMENTS=
            Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String EXCL_XML_N14C=
            Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String EXCL_XML_N14C_WITH_COMMENTS=
            Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
    public static final String PHYSICAL_XML_N14C=
            Canonicalizer.ALGO_ID_C14N_PHYSICAL;
    public static final String BASE64_ENCODING=
            com.sun.org.apache.xml.internal.security.transforms.Transforms.TRANSFORM_BASE64_DECODE;
    public static final int ENCRYPT_MODE=Cipher.ENCRYPT_MODE;
    public static final int DECRYPT_MODE=Cipher.DECRYPT_MODE;
    public static final int UNWRAP_MODE=Cipher.UNWRAP_MODE;
    public static final int WRAP_MODE=Cipher.WRAP_MODE;
    private static final String ENC_ALGORITHMS=TRIPLEDES+"\n"+
            AES_128+"\n"+AES_256+"\n"+AES_192+"\n"+RSA_v1dot5+"\n"+
            RSA_OAEP+"\n"+RSA_OAEP_11+"\n"+TRIPLEDES_KeyWrap+"\n"+
            AES_128_KeyWrap+"\n"+AES_256_KeyWrap+"\n"+AES_192_KeyWrap+"\n"+
            AES_128_GCM+"\n"+AES_192_GCM+"\n"+AES_256_GCM+"\n";
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(XMLCipher.class.getName());
    private Cipher contextCipher;
    private int cipherMode=Integer.MIN_VALUE;
    private String algorithm=null;
    private String requestedJCEProvider=null;
    private Canonicalizer canon;
    private Document contextDocument;
    private Factory factory;
    private Serializer serializer;
    private Key key;
    private Key kek;
    // The EncryptedKey being built (part of a WRAP operation) or read
    // (part of an UNWRAP operation)
    private EncryptedKey ek;
    // The EncryptedData being built (part of a WRAP operation) or read
    // (part of an UNWRAP operation)
    private EncryptedData ed;
    private SecureRandom random;
    private boolean secureValidation;
    private String digestAlg;
    private List<KeyResolverSpi> internalKeyResolvers;

    private XMLCipher(
            String transformation,
            String provider,
            String canonAlg,
            String digestMethod
    ) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Constructing XMLCipher...");
        }
        factory=new Factory();
        algorithm=transformation;
        requestedJCEProvider=provider;
        digestAlg=digestMethod;
        // Create a canonicalizer - used when serializing DOM to octets
        // prior to encryption (and for the reverse)
        try{
            if(canonAlg==null){
                // The default is to preserve the physical representation.
                this.canon=Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_PHYSICAL);
            }else{
                this.canon=Canonicalizer.getInstance(canonAlg);
            }
        }catch(InvalidCanonicalizerException ice){
            throw new XMLEncryptionException("empty",ice);
        }
        if(serializer==null){
            serializer=new DocumentSerializer();
        }
        serializer.setCanonicalizer(this.canon);
        if(transformation!=null){
            contextCipher=constructCipher(transformation,digestMethod);
        }
    }

    private Cipher constructCipher(String algorithm,String digestAlgorithm) throws XMLEncryptionException{
        String jceAlgorithm=JCEMapper.translateURItoJCEID(algorithm);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"JCE Algorithm = "+jceAlgorithm);
        }
        Cipher c;
        try{
            if(requestedJCEProvider==null){
                c=Cipher.getInstance(jceAlgorithm);
            }else{
                c=Cipher.getInstance(jceAlgorithm,requestedJCEProvider);
            }
        }catch(NoSuchAlgorithmException nsae){
            // Check to see if an RSA OAEP MGF-1 with SHA-1 algorithm was requested
            // Some JDKs don't support RSA/ECB/OAEPPadding
            if(XMLCipher.RSA_OAEP.equals(algorithm)
                    &&(digestAlgorithm==null
                    ||MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1.equals(digestAlgorithm))){
                try{
                    if(requestedJCEProvider==null){
                        c=Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
                    }else{
                        c=Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding",requestedJCEProvider);
                    }
                }catch(Exception ex){
                    throw new XMLEncryptionException("empty",ex);
                }
            }else{
                throw new XMLEncryptionException("empty",nsae);
            }
        }catch(NoSuchProviderException nspre){
            throw new XMLEncryptionException("empty",nspre);
        }catch(NoSuchPaddingException nspae){
            throw new XMLEncryptionException("empty",nspae);
        }
        return c;
    }

    public static XMLCipher getInstance(String transformation) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,null,null,null);
    }

    private static void validateTransformation(String transformation){
        if(null==transformation){
            throw new NullPointerException("Transformation unexpectedly null...");
        }
        if(!isValidEncryptionAlgorithm(transformation)){
            log.log(java.util.logging.Level.WARNING,"Algorithm non-standard, expected one of "+ENC_ALGORITHMS);
        }
    }

    private static boolean isValidEncryptionAlgorithm(String algorithm){
        return (
                algorithm.equals(TRIPLEDES)||
                        algorithm.equals(AES_128)||
                        algorithm.equals(AES_256)||
                        algorithm.equals(AES_192)||
                        algorithm.equals(AES_128_GCM)||
                        algorithm.equals(AES_192_GCM)||
                        algorithm.equals(AES_256_GCM)||
                        algorithm.equals(RSA_v1dot5)||
                        algorithm.equals(RSA_OAEP)||
                        algorithm.equals(RSA_OAEP_11)||
                        algorithm.equals(TRIPLEDES_KeyWrap)||
                        algorithm.equals(AES_128_KeyWrap)||
                        algorithm.equals(AES_256_KeyWrap)||
                        algorithm.equals(AES_192_KeyWrap)
        );
    }

    public static XMLCipher getInstance(String transformation,String canon)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation and c14n algorithm");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,null,canon,null);
    }

    public static XMLCipher getInstance(String transformation,String canon,String digestMethod)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation and c14n algorithm");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,null,canon,digestMethod);
    }

    public static XMLCipher getProviderInstance(String transformation,String provider)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation and provider");
        }
        if(null==provider){
            throw new NullPointerException("Provider unexpectedly null..");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,provider,null,null);
    }

    public static XMLCipher getProviderInstance(
            String transformation,String provider,String canon
    ) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation, provider and c14n algorithm");
        }
        if(null==provider){
            throw new NullPointerException("Provider unexpectedly null..");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,provider,canon,null);
    }

    public static XMLCipher getProviderInstance(
            String transformation,String provider,String canon,String digestMethod
    ) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with transformation, provider and c14n algorithm");
        }
        if(null==provider){
            throw new NullPointerException("Provider unexpectedly null..");
        }
        validateTransformation(transformation);
        return new XMLCipher(transformation,provider,canon,digestMethod);
    }

    public static XMLCipher getInstance() throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with no arguments");
        }
        return new XMLCipher(null,null,null,null);
    }

    public static XMLCipher getProviderInstance(String provider) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Getting XMLCipher with provider");
        }
        return new XMLCipher(null,provider,null,null);
    }

    private static void removeContent(Node node){
        while(node.hasChildNodes()){
            node.removeChild(node.getFirstChild());
        }
    }

    public Serializer getSerializer(){
        return serializer;
    }

    public void setSerializer(Serializer serializer){
        this.serializer=serializer;
        serializer.setCanonicalizer(this.canon);
    }

    public void init(int opmode,Key key) throws XMLEncryptionException{
        // sanity checks
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Initializing XMLCipher...");
        }
        ek=null;
        ed=null;
        switch(opmode){
            case ENCRYPT_MODE:
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"opmode = ENCRYPT_MODE");
                }
                ed=createEncryptedData(CipherData.VALUE_TYPE,"NO VALUE YET");
                break;
            case DECRYPT_MODE:
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"opmode = DECRYPT_MODE");
                }
                break;
            case WRAP_MODE:
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"opmode = WRAP_MODE");
                }
                ek=createEncryptedKey(CipherData.VALUE_TYPE,"NO VALUE YET");
                break;
            case UNWRAP_MODE:
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"opmode = UNWRAP_MODE");
                }
                break;
            default:
                log.log(java.util.logging.Level.SEVERE,"Mode unexpectedly invalid");
                throw new XMLEncryptionException("Invalid mode in init");
        }
        cipherMode=opmode;
        this.key=key;
    }

    public EncryptedData createEncryptedData(int type,String value) throws XMLEncryptionException{
        EncryptedData result=null;
        CipherData data=null;
        switch(type){
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference=factory.newCipherReference(value);
                data=factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result=factory.newEncryptedData(data);
                break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue=factory.newCipherValue(value);
                data=factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result=factory.newEncryptedData(data);
        }
        return result;
    }

    public EncryptedKey createEncryptedKey(int type,String value) throws XMLEncryptionException{
        EncryptedKey result=null;
        CipherData data=null;
        switch(type){
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference=factory.newCipherReference(value);
                data=factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result=factory.newEncryptedKey(data);
                break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue=factory.newCipherValue(value);
                data=factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result=factory.newEncryptedKey(data);
        }
        return result;
    }

    public void setSecureValidation(boolean secureValidation){
        this.secureValidation=secureValidation;
    }

    public void registerInternalKeyResolver(KeyResolverSpi keyResolver){
        if(internalKeyResolvers==null){
            internalKeyResolvers=new ArrayList<KeyResolverSpi>();
        }
        internalKeyResolvers.add(keyResolver);
    }

    public EncryptedData getEncryptedData(){
        // Sanity checks
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Returning EncryptedData");
        }
        return ed;
    }

    public EncryptedKey getEncryptedKey(){
        // Sanity checks
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Returning EncryptedKey");
        }
        return ek;
    }

    public void setKEK(Key kek){
        this.kek=kek;
    }

    public Element martial(EncryptedData encryptedData){
        return factory.toElement(encryptedData);
    }

    public Element martial(Document context,EncryptedData encryptedData){
        contextDocument=context;
        return factory.toElement(encryptedData);
    }

    public Element martial(EncryptedKey encryptedKey){
        return factory.toElement(encryptedKey);
    }

    public Element martial(Document context,EncryptedKey encryptedKey){
        contextDocument=context;
        return factory.toElement(encryptedKey);
    }

    public Element martial(ReferenceList referenceList){
        return factory.toElement(referenceList);
    }

    public Element martial(Document context,ReferenceList referenceList){
        contextDocument=context;
        return factory.toElement(referenceList);
    }

    private Document encryptElementContent(Element element) throws /** XMLEncryption */Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypting element content...");
        }
        if(null==element){
            log.log(java.util.logging.Level.SEVERE,"Element unexpectedly null...");
        }
        if(cipherMode!=ENCRYPT_MODE&&log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if(algorithm==null){
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        encryptData(contextDocument,element,true);
        Element encryptedElement=factory.toElement(ed);
        removeContent(element);
        element.appendChild(encryptedElement);
        return contextDocument;
    }

    public Document doFinal(Document context,Document source) throws /** XMLEncryption */Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Processing source document...");
        }
        if(null==context){
            log.log(java.util.logging.Level.SEVERE,"Context document unexpectedly null...");
        }
        if(null==source){
            log.log(java.util.logging.Level.SEVERE,"Source document unexpectedly null...");
        }
        contextDocument=context;
        Document result=null;
        switch(cipherMode){
            case DECRYPT_MODE:
                result=decryptElement(source.getDocumentElement());
                break;
            case ENCRYPT_MODE:
                result=encryptElement(source.getDocumentElement());
                break;
            case UNWRAP_MODE:
            case WRAP_MODE:
                break;
            default:
                throw new XMLEncryptionException("empty",new IllegalStateException());
        }
        return result;
    }

    private Document encryptElement(Element element) throws Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypting element...");
        }
        if(null==element){
            log.log(java.util.logging.Level.SEVERE,"Element unexpectedly null...");
        }
        if(cipherMode!=ENCRYPT_MODE&&log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if(algorithm==null){
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        encryptData(contextDocument,element,false);
        Element encryptedElement=factory.toElement(ed);
        Node sourceParent=element.getParentNode();
        sourceParent.replaceChild(encryptedElement,element);
        return contextDocument;
    }

    public EncryptedData encryptData(
            Document context,Element element,boolean contentMode
    ) throws /** XMLEncryption */Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypting element...");
        }
        if(null==context){
            log.log(java.util.logging.Level.SEVERE,"Context document unexpectedly null...");
        }
        if(null==element){
            log.log(java.util.logging.Level.SEVERE,"Element unexpectedly null...");
        }
        if(cipherMode!=ENCRYPT_MODE&&log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if(contentMode){
            return encryptData(context,element,EncryptionConstants.TYPE_CONTENT,null);
        }else{
            return encryptData(context,element,EncryptionConstants.TYPE_ELEMENT,null);
        }
    }

    private EncryptedData encryptData(
            Document context,Element element,String type,InputStream serializedData
    ) throws /** XMLEncryption */Exception{
        contextDocument=context;
        if(algorithm==null){
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        byte[] serializedOctets=null;
        if(serializedData==null){
            if(type.equals(EncryptionConstants.TYPE_CONTENT)){
                NodeList children=element.getChildNodes();
                if(null!=children){
                    serializedOctets=serializer.serializeToByteArray(children);
                }else{
                    Object exArgs[]={"Element has no content."};
                    throw new XMLEncryptionException("empty",exArgs);
                }
            }else{
                serializedOctets=serializer.serializeToByteArray(element);
            }
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"Serialized octets:\n"+new String(serializedOctets,"UTF-8"));
            }
        }
        byte[] encryptedBytes=null;
        // Now create the working cipher if none was created already
        Cipher c;
        if(contextCipher==null){
            c=constructCipher(algorithm,null);
        }else{
            c=contextCipher;
        }
        // Now perform the encryption
        try{
            // The Spec mandates a 96-bit IV for GCM algorithms
            if(AES_128_GCM.equals(algorithm)||AES_192_GCM.equals(algorithm)
                    ||AES_256_GCM.equals(algorithm)){
                if(random==null){
                    random=SecureRandom.getInstance("SHA1PRNG");
                }
                byte[] temp=new byte[12];
                random.nextBytes(temp);
                IvParameterSpec paramSpec=new IvParameterSpec(temp);
                c.init(cipherMode,key,paramSpec);
            }else{
                c.init(cipherMode,key);
            }
        }catch(InvalidKeyException ike){
            throw new XMLEncryptionException("empty",ike);
        }catch(NoSuchAlgorithmException ex){
            throw new XMLEncryptionException("empty",ex);
        }
        try{
            if(serializedData!=null){
                int numBytes;
                byte[] buf=new byte[8192];
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                while((numBytes=serializedData.read(buf))!=-1){
                    byte[] data=c.update(buf,0,numBytes);
                    baos.write(data);
                }
                baos.write(c.doFinal());
                encryptedBytes=baos.toByteArray();
            }else{
                encryptedBytes=c.doFinal(serializedOctets);
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Expected cipher.outputSize = "+
                            Integer.toString(c.getOutputSize(serializedOctets.length)));
                }
            }
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"Actual cipher.outputSize = "
                        +Integer.toString(encryptedBytes.length));
            }
        }catch(IllegalStateException ise){
            throw new XMLEncryptionException("empty",ise);
        }catch(IllegalBlockSizeException ibse){
            throw new XMLEncryptionException("empty",ibse);
        }catch(BadPaddingException bpe){
            throw new XMLEncryptionException("empty",bpe);
        }catch(UnsupportedEncodingException uee){
            throw new XMLEncryptionException("empty",uee);
        }
        // Now build up to a properly XML Encryption encoded octet stream
        // IvParameterSpec iv;
        byte[] iv=c.getIV();
        byte[] finalEncryptedBytes=new byte[iv.length+encryptedBytes.length];
        System.arraycopy(iv,0,finalEncryptedBytes,0,iv.length);
        System.arraycopy(encryptedBytes,0,finalEncryptedBytes,iv.length,encryptedBytes.length);
        String base64EncodedEncryptedOctets=Base64.encode(finalEncryptedBytes);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypted octets:\n"+base64EncodedEncryptedOctets);
            log.log(java.util.logging.Level.FINE,"Encrypted octets length = "+base64EncodedEncryptedOctets.length());
        }
        try{
            CipherData cd=ed.getCipherData();
            CipherValue cv=cd.getCipherValue();
            // cv.setValue(base64EncodedEncryptedOctets.getBytes());
            cv.setValue(base64EncodedEncryptedOctets);
            if(type!=null){
                ed.setType(new URI(type).toString());
            }
            EncryptionMethod method=
                    factory.newEncryptionMethod(new URI(algorithm).toString());
            method.setDigestAlgorithm(digestAlg);
            ed.setEncryptionMethod(method);
        }catch(URISyntaxException ex){
            throw new XMLEncryptionException("empty",ex);
        }
        return ed;
    }

    private Document decryptElement(Element element) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Decrypting element...");
        }
        if(cipherMode!=DECRYPT_MODE){
            log.log(java.util.logging.Level.SEVERE,"XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        byte[] octets=decryptToByteArray(element);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Decrypted octets:\n"+new String(octets));
        }
        Node sourceParent=element.getParentNode();
        Node decryptedNode=serializer.deserialize(octets,sourceParent);
        // The de-serialiser returns a node whose children we need to take on.
        if(sourceParent!=null&&Node.DOCUMENT_NODE==sourceParent.getNodeType()){
            // If this is a content decryption, this may have problems
            contextDocument.removeChild(contextDocument.getDocumentElement());
            contextDocument.appendChild(decryptedNode);
        }else if(sourceParent!=null){
            sourceParent.replaceChild(decryptedNode,element);
        }
        return contextDocument;
    }

    public byte[] decryptToByteArray(Element element) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Decrypting to ByteArray...");
        }
        if(cipherMode!=DECRYPT_MODE){
            log.log(java.util.logging.Level.SEVERE,"XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        EncryptedData encryptedData=factory.newEncryptedData(element);
        if(key==null){
            KeyInfo ki=encryptedData.getKeyInfo();
            if(ki!=null){
                try{
                    // Add an EncryptedKey resolver
                    String encMethodAlgorithm=encryptedData.getEncryptionMethod().getAlgorithm();
                    EncryptedKeyResolver resolver=new EncryptedKeyResolver(encMethodAlgorithm,kek);
                    if(internalKeyResolvers!=null){
                        int size=internalKeyResolvers.size();
                        for(int i=0;i<size;i++){
                            resolver.registerInternalKeyResolver(internalKeyResolvers.get(i));
                        }
                    }
                    ki.registerInternalKeyResolver(resolver);
                    ki.setSecureValidation(secureValidation);
                    key=ki.getSecretKey();
                }catch(KeyResolverException kre){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,kre.getMessage(),kre);
                    }
                }
            }
            if(key==null){
                log.log(java.util.logging.Level.SEVERE,
                        "XMLCipher::decryptElement called without a key and unable to resolve"
                );
                throw new XMLEncryptionException("encryption.nokey");
            }
        }
        // Obtain the encrypted octets
        XMLCipherInput cipherInput=new XMLCipherInput(encryptedData);
        cipherInput.setSecureValidation(secureValidation);
        byte[] encryptedBytes=cipherInput.getBytes();
        // Now create the working cipher
        String jceAlgorithm=
                JCEMapper.translateURItoJCEID(encryptedData.getEncryptionMethod().getAlgorithm());
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"JCE Algorithm = "+jceAlgorithm);
        }
        Cipher c;
        try{
            if(requestedJCEProvider==null){
                c=Cipher.getInstance(jceAlgorithm);
            }else{
                c=Cipher.getInstance(jceAlgorithm,requestedJCEProvider);
            }
        }catch(NoSuchAlgorithmException nsae){
            throw new XMLEncryptionException("empty",nsae);
        }catch(NoSuchProviderException nspre){
            throw new XMLEncryptionException("empty",nspre);
        }catch(NoSuchPaddingException nspae){
            throw new XMLEncryptionException("empty",nspae);
        }
        // Calculate the IV length and copy out
        // For now, we only work with Block ciphers, so this will work.
        // This should probably be put into the JCE mapper.
        int ivLen=c.getBlockSize();
        String alg=encryptedData.getEncryptionMethod().getAlgorithm();
        if(AES_128_GCM.equals(alg)||AES_192_GCM.equals(alg)||AES_256_GCM.equals(alg)){
            ivLen=12;
        }
        byte[] ivBytes=new byte[ivLen];
        // You may be able to pass the entire piece in to IvParameterSpec
        // and it will only take the first x bytes, but no way to be certain
        // that this will work for every JCE provider, so lets copy the
        // necessary bytes into a dedicated array.
        System.arraycopy(encryptedBytes,0,ivBytes,0,ivLen);
        IvParameterSpec iv=new IvParameterSpec(ivBytes);
        try{
            c.init(cipherMode,key,iv);
        }catch(InvalidKeyException ike){
            throw new XMLEncryptionException("empty",ike);
        }catch(InvalidAlgorithmParameterException iape){
            throw new XMLEncryptionException("empty",iape);
        }
        try{
            return c.doFinal(encryptedBytes,ivLen,encryptedBytes.length-ivLen);
        }catch(IllegalBlockSizeException ibse){
            throw new XMLEncryptionException("empty",ibse);
        }catch(BadPaddingException bpe){
            throw new XMLEncryptionException("empty",bpe);
        }
    }

    public Document doFinal(Document context,Element element) throws /** XMLEncryption */Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Processing source element...");
        }
        if(null==context){
            log.log(java.util.logging.Level.SEVERE,"Context document unexpectedly null...");
        }
        if(null==element){
            log.log(java.util.logging.Level.SEVERE,"Source element unexpectedly null...");
        }
        contextDocument=context;
        Document result=null;
        switch(cipherMode){
            case DECRYPT_MODE:
                result=decryptElement(element);
                break;
            case ENCRYPT_MODE:
                result=encryptElement(element);
                break;
            case UNWRAP_MODE:
            case WRAP_MODE:
                break;
            default:
                throw new XMLEncryptionException("empty",new IllegalStateException());
        }
        return result;
    }

    public Document doFinal(Document context,Element element,boolean content)
            throws /** XMLEncryption*/Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Processing source element...");
        }
        if(null==context){
            log.log(java.util.logging.Level.SEVERE,"Context document unexpectedly null...");
        }
        if(null==element){
            log.log(java.util.logging.Level.SEVERE,"Source element unexpectedly null...");
        }
        contextDocument=context;
        Document result=null;
        switch(cipherMode){
            case DECRYPT_MODE:
                if(content){
                    result=decryptElementContent(element);
                }else{
                    result=decryptElement(element);
                }
                break;
            case ENCRYPT_MODE:
                if(content){
                    result=encryptElementContent(element);
                }else{
                    result=encryptElement(element);
                }
                break;
            case UNWRAP_MODE:
            case WRAP_MODE:
                break;
            default:
                throw new XMLEncryptionException("empty",new IllegalStateException());
        }
        return result;
    }

    public EncryptedData encryptData(Document context,Element element) throws
            /** XMLEncryption */Exception{
        return encryptData(context,element,false);
    }

    public EncryptedData encryptData(
            Document context,String type,InputStream serializedData
    ) throws Exception{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypting element...");
        }
        if(null==context){
            log.log(java.util.logging.Level.SEVERE,"Context document unexpectedly null...");
        }
        if(null==serializedData){
            log.log(java.util.logging.Level.SEVERE,"Serialized data unexpectedly null...");
        }
        if(cipherMode!=ENCRYPT_MODE&&log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        return encryptData(context,null,type,serializedData);
    }

    public EncryptedData loadEncryptedData(Document context,Element element)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Loading encrypted element...");
        }
        if(null==context){
            throw new NullPointerException("Context document unexpectedly null...");
        }
        if(null==element){
            throw new NullPointerException("Element unexpectedly null...");
        }
        if(cipherMode!=DECRYPT_MODE){
            throw new XMLEncryptionException("XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        contextDocument=context;
        ed=factory.newEncryptedData(element);
        return ed;
    }

    public EncryptedKey loadEncryptedKey(Element element) throws XMLEncryptionException{
        return loadEncryptedKey(element.getOwnerDocument(),element);
    }

    public EncryptedKey loadEncryptedKey(Document context,Element element)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Loading encrypted key...");
        }
        if(null==context){
            throw new NullPointerException("Context document unexpectedly null...");
        }
        if(null==element){
            throw new NullPointerException("Element unexpectedly null...");
        }
        if(cipherMode!=UNWRAP_MODE&&cipherMode!=DECRYPT_MODE){
            throw new XMLEncryptionException(
                    "XMLCipher unexpectedly not in UNWRAP_MODE or DECRYPT_MODE..."
            );
        }
        contextDocument=context;
        ek=factory.newEncryptedKey(element);
        return ek;
    }

    public EncryptedKey encryptKey(Document doc,Key key) throws XMLEncryptionException{
        return encryptKey(doc,key,null,null);
    }

    public EncryptedKey encryptKey(
            Document doc,
            Key key,
            String mgfAlgorithm,
            byte[] oaepParams
    ) throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypting key ...");
        }
        if(null==key){
            log.log(java.util.logging.Level.SEVERE,"Key unexpectedly null...");
        }
        if(cipherMode!=WRAP_MODE){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in WRAP_MODE...");
        }
        if(algorithm==null){
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        contextDocument=doc;
        byte[] encryptedBytes=null;
        Cipher c;
        if(contextCipher==null){
            // Now create the working cipher
            c=constructCipher(algorithm,null);
        }else{
            c=contextCipher;
        }
        // Now perform the encryption
        try{
            // Should internally generate an IV
            // todo - allow user to set an IV
            OAEPParameterSpec oaepParameters=
                    constructOAEPParameters(
                            algorithm,digestAlg,mgfAlgorithm,oaepParams
                    );
            if(oaepParameters==null){
                c.init(Cipher.WRAP_MODE,this.key);
            }else{
                c.init(Cipher.WRAP_MODE,this.key,oaepParameters);
            }
            encryptedBytes=c.wrap(key);
        }catch(InvalidKeyException ike){
            throw new XMLEncryptionException("empty",ike);
        }catch(IllegalBlockSizeException ibse){
            throw new XMLEncryptionException("empty",ibse);
        }catch(InvalidAlgorithmParameterException e){
            throw new XMLEncryptionException("empty",e);
        }
        String base64EncodedEncryptedOctets=Base64.encode(encryptedBytes);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Encrypted key octets:\n"+base64EncodedEncryptedOctets);
            log.log(java.util.logging.Level.FINE,"Encrypted key octets length = "+base64EncodedEncryptedOctets.length());
        }
        CipherValue cv=ek.getCipherData().getCipherValue();
        cv.setValue(base64EncodedEncryptedOctets);
        try{
            EncryptionMethod method=factory.newEncryptionMethod(new URI(algorithm).toString());
            method.setDigestAlgorithm(digestAlg);
            method.setMGFAlgorithm(mgfAlgorithm);
            method.setOAEPparams(oaepParams);
            ek.setEncryptionMethod(method);
        }catch(URISyntaxException ex){
            throw new XMLEncryptionException("empty",ex);
        }
        return ek;
    }

    private OAEPParameterSpec constructOAEPParameters(
            String encryptionAlgorithm,
            String digestAlgorithm,
            String mgfAlgorithm,
            byte[] oaepParams
    ){
        if(XMLCipher.RSA_OAEP.equals(encryptionAlgorithm)
                ||XMLCipher.RSA_OAEP_11.equals(encryptionAlgorithm)){
            String jceDigestAlgorithm="SHA-1";
            if(digestAlgorithm!=null){
                jceDigestAlgorithm=JCEMapper.translateURItoJCEID(digestAlgorithm);
            }
            PSource.PSpecified pSource=PSource.PSpecified.DEFAULT;
            if(oaepParams!=null){
                pSource=new PSource.PSpecified(oaepParams);
            }
            MGF1ParameterSpec mgfParameterSpec=new MGF1ParameterSpec("SHA-1");
            if(XMLCipher.RSA_OAEP_11.equals(encryptionAlgorithm)){
                if(EncryptionConstants.MGF1_SHA256.equals(mgfAlgorithm)){
                    mgfParameterSpec=new MGF1ParameterSpec("SHA-256");
                }else if(EncryptionConstants.MGF1_SHA384.equals(mgfAlgorithm)){
                    mgfParameterSpec=new MGF1ParameterSpec("SHA-384");
                }else if(EncryptionConstants.MGF1_SHA512.equals(mgfAlgorithm)){
                    mgfParameterSpec=new MGF1ParameterSpec("SHA-512");
                }
            }
            return new OAEPParameterSpec(jceDigestAlgorithm,"MGF1",mgfParameterSpec,pSource);
        }
        return null;
    }

    public Key decryptKey(EncryptedKey encryptedKey) throws XMLEncryptionException{
        return decryptKey(encryptedKey,ed.getEncryptionMethod().getAlgorithm());
    }

    public Key decryptKey(EncryptedKey encryptedKey,String algorithm)
            throws XMLEncryptionException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Decrypting key from previously loaded EncryptedKey...");
        }
        if(cipherMode!=UNWRAP_MODE&&log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"XMLCipher unexpectedly not in UNWRAP_MODE...");
        }
        if(algorithm==null){
            throw new XMLEncryptionException("Cannot decrypt a key without knowing the algorithm");
        }
        if(key==null){
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"Trying to find a KEK via key resolvers");
            }
            KeyInfo ki=encryptedKey.getKeyInfo();
            if(ki!=null){
                ki.setSecureValidation(secureValidation);
                try{
                    String keyWrapAlg=encryptedKey.getEncryptionMethod().getAlgorithm();
                    String keyType=JCEMapper.getJCEKeyAlgorithmFromURI(keyWrapAlg);
                    if("RSA".equals(keyType)){
                        key=ki.getPrivateKey();
                    }else{
                        key=ki.getSecretKey();
                    }
                }catch(Exception e){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,e.getMessage(),e);
                    }
                }
            }
            if(key==null){
                log.log(java.util.logging.Level.SEVERE,"XMLCipher::decryptKey called without a KEK and cannot resolve");
                throw new XMLEncryptionException("Unable to decrypt without a KEK");
            }
        }
        // Obtain the encrypted octets
        XMLCipherInput cipherInput=new XMLCipherInput(encryptedKey);
        cipherInput.setSecureValidation(secureValidation);
        byte[] encryptedBytes=cipherInput.getBytes();
        String jceKeyAlgorithm=JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"JCE Key Algorithm: "+jceKeyAlgorithm);
        }
        Cipher c;
        if(contextCipher==null){
            // Now create the working cipher
            c=
                    constructCipher(
                            encryptedKey.getEncryptionMethod().getAlgorithm(),
                            encryptedKey.getEncryptionMethod().getDigestAlgorithm()
                    );
        }else{
            c=contextCipher;
        }
        Key ret;
        try{
            EncryptionMethod encMethod=encryptedKey.getEncryptionMethod();
            OAEPParameterSpec oaepParameters=
                    constructOAEPParameters(
                            encMethod.getAlgorithm(),encMethod.getDigestAlgorithm(),
                            encMethod.getMGFAlgorithm(),encMethod.getOAEPparams()
                    );
            if(oaepParameters==null){
                c.init(Cipher.UNWRAP_MODE,key);
            }else{
                c.init(Cipher.UNWRAP_MODE,key,oaepParameters);
            }
            ret=c.unwrap(encryptedBytes,jceKeyAlgorithm,Cipher.SECRET_KEY);
        }catch(InvalidKeyException ike){
            throw new XMLEncryptionException("empty",ike);
        }catch(NoSuchAlgorithmException nsae){
            throw new XMLEncryptionException("empty",nsae);
        }catch(InvalidAlgorithmParameterException e){
            throw new XMLEncryptionException("empty",e);
        }
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Decryption of key type "+algorithm+" OK");
        }
        return ret;
    }

    private Document decryptElementContent(Element element) throws XMLEncryptionException{
        Element e=
                (Element)element.getElementsByTagNameNS(
                        EncryptionConstants.EncryptionSpecNS,
                        EncryptionConstants._TAG_ENCRYPTEDDATA
                ).item(0);
        if(null==e){
            throw new XMLEncryptionException("No EncryptedData child element.");
        }
        return decryptElement(e);
    }

    public AgreementMethod createAgreementMethod(String algorithm){
        return factory.newAgreementMethod(algorithm);
    }

    public CipherData createCipherData(int type){
        return factory.newCipherData(type);
    }

    public CipherReference createCipherReference(String uri){
        return factory.newCipherReference(uri);
    }

    public CipherValue createCipherValue(String value){
        return factory.newCipherValue(value);
    }

    public EncryptionMethod createEncryptionMethod(String algorithm){
        return factory.newEncryptionMethod(algorithm);
    }

    public EncryptionProperties createEncryptionProperties(){
        return factory.newEncryptionProperties();
    }

    public EncryptionProperty createEncryptionProperty(){
        return factory.newEncryptionProperty();
    }

    public ReferenceList createReferenceList(int type){
        return factory.newReferenceList(type);
    }

    public Transforms createTransforms(){
        return factory.newTransforms();
    }

    public Transforms createTransforms(Document doc){
        return factory.newTransforms(doc);
    }

    private class Factory{
        AgreementMethod newAgreementMethod(String algorithm){
            return new AgreementMethodImpl(algorithm);
        }

        CipherReference newCipherReference(String uri){
            return new CipherReferenceImpl(uri);
        }

        ReferenceList newReferenceList(int type){
            return new ReferenceListImpl(type);
        }

        Transforms newTransforms(){
            return new TransformsImpl();
        }

        Transforms newTransforms(Document doc){
            return new TransformsImpl(doc);
        }

        EncryptedData newEncryptedData(Element element) throws XMLEncryptionException{
            EncryptedData result=null;
            NodeList dataElements=
                    element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,EncryptionConstants._TAG_CIPHERDATA);
            // Need to get the last CipherData found, as earlier ones will
            // be for elements in the KeyInfo lists
            Element dataElement=
                    (Element)dataElements.item(dataElements.getLength()-1);
            CipherData data=newCipherData(dataElement);
            result=newEncryptedData(data);
            result.setId(element.getAttributeNS(null,EncryptionConstants._ATT_ID));
            result.setType(element.getAttributeNS(null,EncryptionConstants._ATT_TYPE));
            result.setMimeType(element.getAttributeNS(null,EncryptionConstants._ATT_MIMETYPE));
            result.setEncoding(element.getAttributeNS(null,Constants._ATT_ENCODING));
            Element encryptionMethodElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if(null!=encryptionMethodElement){
                result.setEncryptionMethod(newEncryptionMethod(encryptionMethodElement));
            }
            // BFL 16/7/03 - simple implementation
            // TODO: Work out how to handle relative URI
            Element keyInfoElement=
                    (Element)element.getElementsByTagNameNS(
                            Constants.SignatureSpecNS,Constants._TAG_KEYINFO).item(0);
            if(null!=keyInfoElement){
                KeyInfo ki=newKeyInfo(keyInfoElement);
                result.setKeyInfo(ki);
            }
            // TODO: Implement
            Element encryptionPropertiesElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if(null!=encryptionPropertiesElement){
                result.setEncryptionProperties(
                        newEncryptionProperties(encryptionPropertiesElement)
                );
            }
            return result;
        }

        EncryptedData newEncryptedData(CipherData data){
            return new EncryptedDataImpl(data);
        }

        CipherData newCipherData(Element element) throws XMLEncryptionException{
            if(null==element){
                throw new NullPointerException("element is null");
            }
            int type=0;
            Element e=null;
            if(element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERVALUE).getLength()>0
                    ){
                type=CipherData.VALUE_TYPE;
                e=(Element)element.getElementsByTagNameNS(
                        EncryptionConstants.EncryptionSpecNS,
                        EncryptionConstants._TAG_CIPHERVALUE).item(0);
            }else if(element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERREFERENCE).getLength()>0){
                type=CipherData.REFERENCE_TYPE;
                e=(Element)element.getElementsByTagNameNS(
                        EncryptionConstants.EncryptionSpecNS,
                        EncryptionConstants._TAG_CIPHERREFERENCE).item(0);
            }
            CipherData result=newCipherData(type);
            if(type==CipherData.VALUE_TYPE){
                result.setCipherValue(newCipherValue(e));
            }else if(type==CipherData.REFERENCE_TYPE){
                result.setCipherReference(newCipherReference(e));
            }
            return result;
        }

        CipherData newCipherData(int type){
            return new CipherDataImpl(type);
        }

        CipherReference newCipherReference(Element element) throws XMLEncryptionException{
            Attr uriAttr=
                    element.getAttributeNodeNS(null,EncryptionConstants._ATT_URI);
            CipherReference result=new CipherReferenceImpl(uriAttr);
            // Find any Transforms
            NodeList transformsElements=
                    element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,EncryptionConstants._TAG_TRANSFORMS);
            Element transformsElement=(Element)transformsElements.item(0);
            if(transformsElement!=null){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"Creating a DSIG based Transforms element");
                }
                try{
                    result.setTransforms(new TransformsImpl(transformsElement));
                }catch(XMLSignatureException xse){
                    throw new XMLEncryptionException("empty",xse);
                }catch(InvalidTransformException ite){
                    throw new XMLEncryptionException("empty",ite);
                }catch(XMLSecurityException xse){
                    throw new XMLEncryptionException("empty",xse);
                }
            }
            return result;
        }

        CipherValue newCipherValue(Element element){
            String value=XMLUtils.getFullTextChildrenFromElement(element);
            return newCipherValue(value);
        }

        CipherValue newCipherValue(String value){
            return new CipherValueImpl(value);
        }

        KeyInfo newKeyInfo(Element element) throws XMLEncryptionException{
            try{
                KeyInfo ki=new KeyInfo(element,null);
                ki.setSecureValidation(secureValidation);
                if(internalKeyResolvers!=null){
                    int size=internalKeyResolvers.size();
                    for(int i=0;i<size;i++){
                        ki.registerInternalKeyResolver(internalKeyResolvers.get(i));
                    }
                }
                return ki;
            }catch(XMLSecurityException xse){
                throw new XMLEncryptionException("Error loading Key Info",xse);
            }
        }

        EncryptionMethod newEncryptionMethod(Element element){
            String encAlgorithm=element.getAttributeNS(null,EncryptionConstants._ATT_ALGORITHM);
            EncryptionMethod result=newEncryptionMethod(encAlgorithm);
            Element keySizeElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_KEYSIZE).item(0);
            if(null!=keySizeElement){
                result.setKeySize(
                        Integer.valueOf(
                                keySizeElement.getFirstChild().getNodeValue()).intValue());
            }
            Element oaepParamsElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_OAEPPARAMS).item(0);
            if(null!=oaepParamsElement){
                try{
                    String oaepParams=oaepParamsElement.getFirstChild().getNodeValue();
                    result.setOAEPparams(Base64.decode(oaepParams.getBytes("UTF-8")));
                }catch(UnsupportedEncodingException e){
                    throw new RuntimeException("UTF-8 not supported",e);
                }catch(Base64DecodingException e){
                    throw new RuntimeException("BASE-64 decoding error",e);
                }
            }
            Element digestElement=
                    (Element)element.getElementsByTagNameNS(
                            Constants.SignatureSpecNS,Constants._TAG_DIGESTMETHOD).item(0);
            if(digestElement!=null){
                String digestAlgorithm=digestElement.getAttributeNS(null,"Algorithm");
                result.setDigestAlgorithm(digestAlgorithm);
            }
            Element mgfElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpec11NS,EncryptionConstants._TAG_MGF).item(0);
            if(mgfElement!=null&&!XMLCipher.RSA_OAEP.equals(algorithm)){
                String mgfAlgorithm=mgfElement.getAttributeNS(null,"Algorithm");
                result.setMGFAlgorithm(mgfAlgorithm);
            }
            // TODO: Make this mess work
            // <any namespace='##other' minOccurs='0' maxOccurs='unbounded'/>
            return result;
        }

        EncryptionMethod newEncryptionMethod(String algorithm){
            return new EncryptionMethodImpl(algorithm);
        }

        EncryptionProperties newEncryptionProperties(Element element){
            EncryptionProperties result=newEncryptionProperties();
            result.setId(element.getAttributeNS(null,EncryptionConstants._ATT_ID));
            NodeList encryptionPropertyList=
                    element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTIONPROPERTY);
            for(int i=0;i<encryptionPropertyList.getLength();i++){
                Node n=encryptionPropertyList.item(i);
                if(null!=n){
                    result.addEncryptionProperty(newEncryptionProperty((Element)n));
                }
            }
            return result;
        }

        EncryptionProperties newEncryptionProperties(){
            return new EncryptionPropertiesImpl();
        }

        EncryptionProperty newEncryptionProperty(Element element){
            EncryptionProperty result=newEncryptionProperty();
            result.setTarget(element.getAttributeNS(null,EncryptionConstants._ATT_TARGET));
            result.setId(element.getAttributeNS(null,EncryptionConstants._ATT_ID));
            // TODO: Make this lot work...
            // <anyAttribute namespace="http://www.w3.org/XML/1998/namespace"/>
            // TODO: Make this work...
            // <any namespace='##other' processContents='lax'/>
            return result;
        }

        EncryptionProperty newEncryptionProperty(){
            return new EncryptionPropertyImpl();
        }

        EncryptedKey newEncryptedKey(Element element) throws XMLEncryptionException{
            EncryptedKey result=null;
            NodeList dataElements=
                    element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,EncryptionConstants._TAG_CIPHERDATA);
            Element dataElement=
                    (Element)dataElements.item(dataElements.getLength()-1);
            CipherData data=newCipherData(dataElement);
            result=newEncryptedKey(data);
            result.setId(element.getAttributeNS(null,EncryptionConstants._ATT_ID));
            result.setType(element.getAttributeNS(null,EncryptionConstants._ATT_TYPE));
            result.setMimeType(element.getAttributeNS(null,EncryptionConstants._ATT_MIMETYPE));
            result.setEncoding(element.getAttributeNS(null,Constants._ATT_ENCODING));
            result.setRecipient(element.getAttributeNS(null,EncryptionConstants._ATT_RECIPIENT));
            Element encryptionMethodElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if(null!=encryptionMethodElement){
                result.setEncryptionMethod(newEncryptionMethod(encryptionMethodElement));
            }
            Element keyInfoElement=
                    (Element)element.getElementsByTagNameNS(
                            Constants.SignatureSpecNS,Constants._TAG_KEYINFO).item(0);
            if(null!=keyInfoElement){
                KeyInfo ki=newKeyInfo(keyInfoElement);
                result.setKeyInfo(ki);
            }
            // TODO: Implement
            Element encryptionPropertiesElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if(null!=encryptionPropertiesElement){
                result.setEncryptionProperties(
                        newEncryptionProperties(encryptionPropertiesElement)
                );
            }
            Element referenceListElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_REFERENCELIST).item(0);
            if(null!=referenceListElement){
                result.setReferenceList(newReferenceList(referenceListElement));
            }
            Element carriedNameElement=
                    (Element)element.getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_CARRIEDKEYNAME).item(0);
            if(null!=carriedNameElement){
                result.setCarriedName(carriedNameElement.getFirstChild().getNodeValue());
            }
            return result;
        }

        EncryptedKey newEncryptedKey(CipherData data){
            return new EncryptedKeyImpl(data);
        }

        ReferenceList newReferenceList(Element element){
            int type=0;
            if(null!=element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_DATAREFERENCE).item(0)){
                type=ReferenceList.DATA_REFERENCE;
            }else if(null!=element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_KEYREFERENCE).item(0)){
                type=ReferenceList.KEY_REFERENCE;
            }
            ReferenceList result=new ReferenceListImpl(type);
            NodeList list=null;
            switch(type){
                case ReferenceList.DATA_REFERENCE:
                    list=
                            element.getElementsByTagNameNS(
                                    EncryptionConstants.EncryptionSpecNS,
                                    EncryptionConstants._TAG_DATAREFERENCE);
                    for(int i=0;i<list.getLength();i++){
                        String uri=((Element)list.item(i)).getAttribute("URI");
                        result.add(result.newDataReference(uri));
                    }
                    break;
                case ReferenceList.KEY_REFERENCE:
                    list=
                            element.getElementsByTagNameNS(
                                    EncryptionConstants.EncryptionSpecNS,
                                    EncryptionConstants._TAG_KEYREFERENCE);
                    for(int i=0;i<list.getLength();i++){
                        String uri=((Element)list.item(i)).getAttribute("URI");
                        result.add(result.newKeyReference(uri));
                    }
            }
            return result;
        }

        Element toElement(EncryptedData encryptedData){
            return ((EncryptedDataImpl)encryptedData).toElement();
        }

        Element toElement(EncryptedKey encryptedKey){
            return ((EncryptedKeyImpl)encryptedKey).toElement();
        }

        Element toElement(ReferenceList referenceList){
            return ((ReferenceListImpl)referenceList).toElement();
        }

        private class AgreementMethodImpl implements AgreementMethod{
            private byte[] kaNonce=null;
            private List<Element> agreementMethodInformation=null;
            private KeyInfo originatorKeyInfo=null;
            private KeyInfo recipientKeyInfo=null;
            private String algorithmURI=null;

            public AgreementMethodImpl(String algorithm){
                agreementMethodInformation=new LinkedList<Element>();
                URI tmpAlgorithm=null;
                try{
                    tmpAlgorithm=new URI(algorithm);
                }catch(URISyntaxException ex){
                    throw (IllegalArgumentException)
                            new IllegalArgumentException().initCause(ex);
                }
                algorithmURI=tmpAlgorithm.toString();
            }

            public byte[] getKANonce(){
                return kaNonce;
            }

            public void setKANonce(byte[] kanonce){
                kaNonce=kanonce;
            }

            public Iterator<Element> getAgreementMethodInformation(){
                return agreementMethodInformation.iterator();
            }

            public void addAgreementMethodInformation(Element info){
                agreementMethodInformation.add(info);
            }

            public void revoveAgreementMethodInformation(Element info){
                agreementMethodInformation.remove(info);
            }

            public KeyInfo getOriginatorKeyInfo(){
                return originatorKeyInfo;
            }

            public void setOriginatorKeyInfo(KeyInfo keyInfo){
                originatorKeyInfo=keyInfo;
            }

            public KeyInfo getRecipientKeyInfo(){
                return recipientKeyInfo;
            }

            public void setRecipientKeyInfo(KeyInfo keyInfo){
                recipientKeyInfo=keyInfo;
            }

            public String getAlgorithm(){
                return algorithmURI;
            }
        }

        private class CipherDataImpl implements CipherData{
            private static final String valueMessage=
                    "Data type is reference type.";
            private static final String referenceMessage=
                    "Data type is value type.";
            private CipherValue cipherValue=null;
            private CipherReference cipherReference=null;
            private int cipherType=Integer.MIN_VALUE;

            public CipherDataImpl(int type){
                cipherType=type;
            }

            public int getDataType(){
                return cipherType;
            }

            public CipherValue getCipherValue(){
                return cipherValue;
            }

            public void setCipherValue(CipherValue value) throws XMLEncryptionException{
                if(cipherType==REFERENCE_TYPE){
                    throw new XMLEncryptionException(
                            "empty",new UnsupportedOperationException(valueMessage)
                    );
                }
                cipherValue=value;
            }

            public CipherReference getCipherReference(){
                return cipherReference;
            }

            public void setCipherReference(CipherReference reference) throws
                    XMLEncryptionException{
                if(cipherType==VALUE_TYPE){
                    throw new XMLEncryptionException(
                            "empty",new UnsupportedOperationException(referenceMessage)
                    );
                }
                cipherReference=reference;
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_CIPHERDATA
                        );
                if(cipherType==VALUE_TYPE){
                    result.appendChild(((CipherValueImpl)cipherValue).toElement());
                }else if(cipherType==REFERENCE_TYPE){
                    result.appendChild(((CipherReferenceImpl)cipherReference).toElement());
                }
                return result;
            }
        }

        private class CipherReferenceImpl implements CipherReference{
            private String referenceURI=null;
            private Transforms referenceTransforms=null;
            private Attr referenceNode=null;

            public CipherReferenceImpl(String uri){
                /** Don't check validity of URI as may be "" */
                referenceURI=uri;
                referenceNode=null;
            }

            public CipherReferenceImpl(Attr uri){
                referenceURI=uri.getNodeValue();
                referenceNode=uri;
            }

            public String getURI(){
                return referenceURI;
            }

            public Attr getURIAsAttr(){
                return referenceNode;
            }

            public Transforms getTransforms(){
                return referenceTransforms;
            }

            public void setTransforms(Transforms transforms){
                referenceTransforms=transforms;
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_CIPHERREFERENCE
                        );
                result.setAttributeNS(null,EncryptionConstants._ATT_URI,referenceURI);
                if(null!=referenceTransforms){
                    result.appendChild(((TransformsImpl)referenceTransforms).toElement());
                }
                return result;
            }
        }

        private class CipherValueImpl implements CipherValue{
            private String cipherValue=null;

            public CipherValueImpl(String value){
                cipherValue=value;
            }

            public String getValue(){
                return cipherValue;
            }

            public void setValue(String value){
                cipherValue=value;
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_CIPHERVALUE
                        );
                result.appendChild(contextDocument.createTextNode(cipherValue));
                return result;
            }
        }

        private class EncryptedDataImpl extends EncryptedTypeImpl implements EncryptedData{
            public EncryptedDataImpl(CipherData data){
                super(data);
            }

            Element toElement(){
                Element result=
                        ElementProxy.createElementForFamily(
                                contextDocument,EncryptionConstants.EncryptionSpecNS,
                                EncryptionConstants._TAG_ENCRYPTEDDATA
                        );
                if(null!=super.getId()){
                    result.setAttributeNS(null,EncryptionConstants._ATT_ID,super.getId());
                }
                if(null!=super.getType()){
                    result.setAttributeNS(null,EncryptionConstants._ATT_TYPE,super.getType());
                }
                if(null!=super.getMimeType()){
                    result.setAttributeNS(
                            null,EncryptionConstants._ATT_MIMETYPE,super.getMimeType()
                    );
                }
                if(null!=super.getEncoding()){
                    result.setAttributeNS(
                            null,EncryptionConstants._ATT_ENCODING,super.getEncoding()
                    );
                }
                if(null!=super.getEncryptionMethod()){
                    result.appendChild(
                            ((EncryptionMethodImpl)super.getEncryptionMethod()).toElement()
                    );
                }
                if(null!=super.getKeyInfo()){
                    result.appendChild(super.getKeyInfo().getElement().cloneNode(true));
                }
                result.appendChild(((CipherDataImpl)super.getCipherData()).toElement());
                if(null!=super.getEncryptionProperties()){
                    result.appendChild(((EncryptionPropertiesImpl)
                            super.getEncryptionProperties()).toElement());
                }
                return result;
            }
        }

        private class EncryptedKeyImpl extends EncryptedTypeImpl implements EncryptedKey{
            private String keyRecipient=null;
            private ReferenceList referenceList=null;
            private String carriedName=null;

            public EncryptedKeyImpl(CipherData data){
                super(data);
            }

            Element toElement(){
                Element result=
                        ElementProxy.createElementForFamily(
                                contextDocument,EncryptionConstants.EncryptionSpecNS,
                                EncryptionConstants._TAG_ENCRYPTEDKEY
                        );
                if(null!=super.getId()){
                    result.setAttributeNS(null,EncryptionConstants._ATT_ID,super.getId());
                }
                if(null!=super.getType()){
                    result.setAttributeNS(null,EncryptionConstants._ATT_TYPE,super.getType());
                }
                if(null!=super.getMimeType()){
                    result.setAttributeNS(
                            null,EncryptionConstants._ATT_MIMETYPE,super.getMimeType()
                    );
                }
                if(null!=super.getEncoding()){
                    result.setAttributeNS(null,Constants._ATT_ENCODING,super.getEncoding());
                }
                if(null!=getRecipient()){
                    result.setAttributeNS(
                            null,EncryptionConstants._ATT_RECIPIENT,getRecipient()
                    );
                }
                if(null!=super.getEncryptionMethod()){
                    result.appendChild(((EncryptionMethodImpl)
                            super.getEncryptionMethod()).toElement());
                }
                if(null!=super.getKeyInfo()){
                    result.appendChild(super.getKeyInfo().getElement().cloneNode(true));
                }
                result.appendChild(((CipherDataImpl)super.getCipherData()).toElement());
                if(null!=super.getEncryptionProperties()){
                    result.appendChild(((EncryptionPropertiesImpl)
                            super.getEncryptionProperties()).toElement());
                }
                if(referenceList!=null&&!referenceList.isEmpty()){
                    result.appendChild(((ReferenceListImpl)getReferenceList()).toElement());
                }
                if(null!=carriedName){
                    Element element=
                            ElementProxy.createElementForFamily(
                                    contextDocument,
                                    EncryptionConstants.EncryptionSpecNS,
                                    EncryptionConstants._TAG_CARRIEDKEYNAME
                            );
                    Node node=contextDocument.createTextNode(carriedName);
                    element.appendChild(node);
                    result.appendChild(element);
                }
                return result;
            }

            public String getRecipient(){
                return keyRecipient;
            }

            public void setRecipient(String recipient){
                keyRecipient=recipient;
            }

            public ReferenceList getReferenceList(){
                return referenceList;
            }

            public void setReferenceList(ReferenceList list){
                referenceList=list;
            }

            public String getCarriedName(){
                return carriedName;
            }

            public void setCarriedName(String name){
                carriedName=name;
            }
        }

        private abstract class EncryptedTypeImpl{
            private String id=null;
            private String type=null;
            private String mimeType=null;
            private String encoding=null;
            private EncryptionMethod encryptionMethod=null;
            private KeyInfo keyInfo=null;
            private CipherData cipherData=null;
            private EncryptionProperties encryptionProperties=null;

            protected EncryptedTypeImpl(CipherData data){
                cipherData=data;
            }

            public String getId(){
                return id;
            }

            public void setId(String id){
                this.id=id;
            }

            public String getType(){
                return type;
            }

            public void setType(String type){
                if(type==null||type.length()==0){
                    this.type=null;
                }else{
                    URI tmpType=null;
                    try{
                        tmpType=new URI(type);
                    }catch(URISyntaxException ex){
                        throw (IllegalArgumentException)
                                new IllegalArgumentException().initCause(ex);
                    }
                    this.type=tmpType.toString();
                }
            }

            public String getMimeType(){
                return mimeType;
            }

            public void setMimeType(String type){
                mimeType=type;
            }

            public String getEncoding(){
                return encoding;
            }

            public void setEncoding(String encoding){
                if(encoding==null||encoding.length()==0){
                    this.encoding=null;
                }else{
                    URI tmpEncoding=null;
                    try{
                        tmpEncoding=new URI(encoding);
                    }catch(URISyntaxException ex){
                        throw (IllegalArgumentException)
                                new IllegalArgumentException().initCause(ex);
                    }
                    this.encoding=tmpEncoding.toString();
                }
            }

            public EncryptionMethod getEncryptionMethod(){
                return encryptionMethod;
            }

            public void setEncryptionMethod(EncryptionMethod method){
                encryptionMethod=method;
            }

            public KeyInfo getKeyInfo(){
                return keyInfo;
            }

            public void setKeyInfo(KeyInfo info){
                keyInfo=info;
            }

            public CipherData getCipherData(){
                return cipherData;
            }

            public EncryptionProperties getEncryptionProperties(){
                return encryptionProperties;
            }

            public void setEncryptionProperties(EncryptionProperties properties){
                encryptionProperties=properties;
            }
        }

        private class EncryptionMethodImpl implements EncryptionMethod{
            private String algorithm=null;
            private int keySize=Integer.MIN_VALUE;
            private byte[] oaepParams=null;
            private List<Element> encryptionMethodInformation=null;
            private String digestAlgorithm=null;
            private String mgfAlgorithm=null;

            public EncryptionMethodImpl(String algorithm){
                URI tmpAlgorithm=null;
                try{
                    tmpAlgorithm=new URI(algorithm);
                }catch(URISyntaxException ex){
                    throw (IllegalArgumentException)
                            new IllegalArgumentException().initCause(ex);
                }
                this.algorithm=tmpAlgorithm.toString();
                encryptionMethodInformation=new LinkedList<Element>();
            }

            public String getAlgorithm(){
                return algorithm;
            }

            public int getKeySize(){
                return keySize;
            }

            public void setKeySize(int size){
                keySize=size;
            }

            public byte[] getOAEPparams(){
                return oaepParams;
            }

            public void setOAEPparams(byte[] params){
                oaepParams=params;
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_ENCRYPTIONMETHOD
                        );
                result.setAttributeNS(null,EncryptionConstants._ATT_ALGORITHM,algorithm);
                if(keySize>0){
                    result.appendChild(
                            XMLUtils.createElementInEncryptionSpace(
                                    contextDocument,EncryptionConstants._TAG_KEYSIZE
                            ).appendChild(contextDocument.createTextNode(String.valueOf(keySize))));
                }
                if(null!=oaepParams){
                    Element oaepElement=
                            XMLUtils.createElementInEncryptionSpace(
                                    contextDocument,EncryptionConstants._TAG_OAEPPARAMS
                            );
                    oaepElement.appendChild(contextDocument.createTextNode(Base64.encode(oaepParams)));
                    result.appendChild(oaepElement);
                }
                if(digestAlgorithm!=null){
                    Element digestElement=
                            XMLUtils.createElementInSignatureSpace(contextDocument,Constants._TAG_DIGESTMETHOD);
                    digestElement.setAttributeNS(null,"Algorithm",digestAlgorithm);
                    result.appendChild(digestElement);
                }
                if(mgfAlgorithm!=null){
                    Element mgfElement=
                            XMLUtils.createElementInEncryption11Space(
                                    contextDocument,EncryptionConstants._TAG_MGF
                            );
                    mgfElement.setAttributeNS(null,"Algorithm",mgfAlgorithm);
                    mgfElement.setAttributeNS(
                            Constants.NamespaceSpecNS,
                            "xmlns:"+ElementProxy.getDefaultPrefix(EncryptionConstants.EncryptionSpec11NS),
                            EncryptionConstants.EncryptionSpec11NS
                    );
                    result.appendChild(mgfElement);
                }
                Iterator<Element> itr=encryptionMethodInformation.iterator();
                while(itr.hasNext()){
                    result.appendChild(itr.next());
                }
                return result;
            }            public void setDigestAlgorithm(String digestAlgorithm){
                this.digestAlgorithm=digestAlgorithm;
            }

            public String getDigestAlgorithm(){
                return digestAlgorithm;
            }

            public void setMGFAlgorithm(String mgfAlgorithm){
                this.mgfAlgorithm=mgfAlgorithm;
            }

            public String getMGFAlgorithm(){
                return mgfAlgorithm;
            }

            public Iterator<Element> getEncryptionMethodInformation(){
                return encryptionMethodInformation.iterator();
            }

            public void addEncryptionMethodInformation(Element info){
                encryptionMethodInformation.add(info);
            }

            public void removeEncryptionMethodInformation(Element info){
                encryptionMethodInformation.remove(info);
            }


        }

        private class EncryptionPropertiesImpl implements EncryptionProperties{
            private String id=null;
            private List<EncryptionProperty> encryptionProperties=null;

            public EncryptionPropertiesImpl(){
                encryptionProperties=new LinkedList<EncryptionProperty>();
            }

            public String getId(){
                return id;
            }

            public void setId(String id){
                this.id=id;
            }

            public Iterator<EncryptionProperty> getEncryptionProperties(){
                return encryptionProperties.iterator();
            }

            public void addEncryptionProperty(EncryptionProperty property){
                encryptionProperties.add(property);
            }

            public void removeEncryptionProperty(EncryptionProperty property){
                encryptionProperties.remove(property);
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_ENCRYPTIONPROPERTIES
                        );
                if(null!=id){
                    result.setAttributeNS(null,EncryptionConstants._ATT_ID,id);
                }
                Iterator<EncryptionProperty> itr=getEncryptionProperties();
                while(itr.hasNext()){
                    result.appendChild(((EncryptionPropertyImpl)itr.next()).toElement());
                }
                return result;
            }
        }

        private class EncryptionPropertyImpl implements EncryptionProperty{
            private String target=null;
            private String id=null;
            private Map<String,String> attributeMap=new HashMap<String,String>();
            private List<Element> encryptionInformation=null;

            public EncryptionPropertyImpl(){
                encryptionInformation=new LinkedList<Element>();
            }

            public String getTarget(){
                return target;
            }

            public void setTarget(String target){
                if(target==null||target.length()==0){
                    this.target=null;
                }else if(target.startsWith("#")){
                    /**
                     * This is a same document URI reference. Do not parse,
                     * because it has no scheme.
                     */
                    this.target=target;
                }else{
                    URI tmpTarget=null;
                    try{
                        tmpTarget=new URI(target);
                    }catch(URISyntaxException ex){
                        throw (IllegalArgumentException)
                                new IllegalArgumentException().initCause(ex);
                    }
                    this.target=tmpTarget.toString();
                }
            }

            public String getId(){
                return id;
            }

            public void setId(String id){
                this.id=id;
            }

            public String getAttribute(String attribute){
                return attributeMap.get(attribute);
            }

            public void setAttribute(String attribute,String value){
                attributeMap.put(attribute,value);
            }

            public Iterator<Element> getEncryptionInformation(){
                return encryptionInformation.iterator();
            }

            public void addEncryptionInformation(Element info){
                encryptionInformation.add(info);
            }

            public void removeEncryptionInformation(Element info){
                encryptionInformation.remove(info);
            }

            Element toElement(){
                Element result=
                        XMLUtils.createElementInEncryptionSpace(
                                contextDocument,EncryptionConstants._TAG_ENCRYPTIONPROPERTY
                        );
                if(null!=target){
                    result.setAttributeNS(null,EncryptionConstants._ATT_TARGET,target);
                }
                if(null!=id){
                    result.setAttributeNS(null,EncryptionConstants._ATT_ID,id);
                }
                // TODO: figure out the anyAttribyte stuff...
                // TODO: figure out the any stuff...
                return result;
            }
        }

        private class TransformsImpl extends com.sun.org.apache.xml.internal.security.transforms.Transforms
                implements Transforms{
            public TransformsImpl(){
                super(contextDocument);
            }

            public TransformsImpl(Document doc){
                if(doc==null){
                    throw new RuntimeException("Document is null");
                }
                this.doc=doc;
                this.constructionElement=
                        createElementForFamilyLocal(
                                this.doc,this.getBaseNamespace(),this.getBaseLocalName()
                        );
            }

            // Over-ride the namespace
            public String getBaseNamespace(){
                return EncryptionConstants.EncryptionSpecNS;
            }

            public TransformsImpl(Element element)
                    throws XMLSignatureException, InvalidTransformException,
                    XMLSecurityException, TransformationException{
                super(element,"");
            }

            public Element toElement(){
                if(doc==null){
                    doc=contextDocument;
                }
                return getElement();
            }

            public com.sun.org.apache.xml.internal.security.transforms.Transforms getDSTransforms(){
                return this;
            }
        }

        private class ReferenceListImpl implements ReferenceList{
            private Class<?> sentry;
            private List<Reference> references;

            public ReferenceListImpl(int type){
                if(type==ReferenceList.DATA_REFERENCE){
                    sentry=DataReference.class;
                }else if(type==ReferenceList.KEY_REFERENCE){
                    sentry=KeyReference.class;
                }else{
                    throw new IllegalArgumentException();
                }
                references=new LinkedList<Reference>();
            }

            public void add(Reference reference){
                if(!reference.getClass().equals(sentry)){
                    throw new IllegalArgumentException();
                }
                references.add(reference);
            }

            public void remove(Reference reference){
                if(!reference.getClass().equals(sentry)){
                    throw new IllegalArgumentException();
                }
                references.remove(reference);
            }

            public int size(){
                return references.size();
            }

            public boolean isEmpty(){
                return references.isEmpty();
            }

            public Iterator<Reference> getReferences(){
                return references.iterator();
            }

            public Reference newDataReference(String uri){
                return new DataReference(uri);
            }

            public Reference newKeyReference(String uri){
                return new KeyReference(uri);
            }

            Element toElement(){
                Element result=
                        ElementProxy.createElementForFamily(
                                contextDocument,
                                EncryptionConstants.EncryptionSpecNS,
                                EncryptionConstants._TAG_REFERENCELIST
                        );
                Iterator<Reference> eachReference=references.iterator();
                while(eachReference.hasNext()){
                    Reference reference=eachReference.next();
                    result.appendChild(((ReferenceImpl)reference).toElement());
                }
                return result;
            }

            private abstract class ReferenceImpl implements Reference{
                private String uri;
                private List<Element> referenceInformation;

                ReferenceImpl(String uri){
                    this.uri=uri;
                    referenceInformation=new LinkedList<Element>();
                }

                public Element toElement(){
                    String tagName=getType();
                    Element result=
                            ElementProxy.createElementForFamily(
                                    contextDocument,
                                    EncryptionConstants.EncryptionSpecNS,
                                    tagName
                            );
                    result.setAttribute(EncryptionConstants._ATT_URI,uri);
                    // TODO: Need to martial referenceInformation
                    // Figure out how to make this work..
                    // <any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
                    return result;
                }

                public abstract String getType();

                public String getURI(){
                    return uri;
                }

                public void setURI(String uri){
                    this.uri=uri;
                }

                public Iterator<Element> getElementRetrievalInformation(){
                    return referenceInformation.iterator();
                }

                public void addElementRetrievalInformation(Element node){
                    referenceInformation.add(node);
                }

                public void removeElementRetrievalInformation(Element node){
                    referenceInformation.remove(node);
                }
            }

            private class DataReference extends ReferenceImpl{
                DataReference(String uri){
                    super(uri);
                }

                public String getType(){
                    return EncryptionConstants._TAG_DATAREFERENCE;
                }
            }

            private class KeyReference extends ReferenceImpl{
                KeyReference(String uri){
                    super(uri);
                }

                public String getType(){
                    return EncryptionConstants._TAG_KEYREFERENCE;
                }
            }
        }
    }
}
