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
package com.sun.org.apache.xml.internal.security.signature;

import com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm;
import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.transforms.Transforms;
import com.sun.org.apache.xml.internal.security.utils.*;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolver;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.*;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public final class XMLSignature extends SignatureElementProxy{
    public static final String ALGO_ID_MAC_HMAC_SHA1=
            Constants.SignatureSpecNS+"hmac-sha1";
    public static final String ALGO_ID_SIGNATURE_DSA=
            Constants.SignatureSpecNS+"dsa-sha1";
    public static final String ALGO_ID_SIGNATURE_DSA_SHA256=
            Constants.SignatureSpec11NS+"dsa-sha256";
    public static final String ALGO_ID_SIGNATURE_RSA=
            Constants.SignatureSpecNS+"rsa-sha1";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA1=
            Constants.SignatureSpecNS+"rsa-sha1";
    public static final String ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5=
            Constants.MoreAlgorithmsSpecNS+"rsa-md5";
    public static final String ALGO_ID_SIGNATURE_RSA_RIPEMD160=
            Constants.MoreAlgorithmsSpecNS+"rsa-ripemd160";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA256=
            Constants.MoreAlgorithmsSpecNS+"rsa-sha256";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA384=
            Constants.MoreAlgorithmsSpecNS+"rsa-sha384";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA512=
            Constants.MoreAlgorithmsSpecNS+"rsa-sha512";
    public static final String ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5=
            Constants.MoreAlgorithmsSpecNS+"hmac-md5";
    public static final String ALGO_ID_MAC_HMAC_RIPEMD160=
            Constants.MoreAlgorithmsSpecNS+"hmac-ripemd160";
    public static final String ALGO_ID_MAC_HMAC_SHA256=
            Constants.MoreAlgorithmsSpecNS+"hmac-sha256";
    public static final String ALGO_ID_MAC_HMAC_SHA384=
            Constants.MoreAlgorithmsSpecNS+"hmac-sha384";
    public static final String ALGO_ID_MAC_HMAC_SHA512=
            Constants.MoreAlgorithmsSpecNS+"hmac-sha512";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA1=
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA256=
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA384=
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA512=
            "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
    private static final int MODE_SIGN=0;
    private static final int MODE_VERIFY=1;
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(XMLSignature.class.getName());
    private SignedInfo signedInfo;
    private KeyInfo keyInfo;
    private boolean followManifestsDuringValidation=false;
    private Element signatureValueElement;
    private int state=MODE_SIGN;

    public XMLSignature(Document doc,String baseURI,String signatureMethodURI)
            throws XMLSecurityException{
        this(doc,baseURI,signatureMethodURI,0,Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
    }

    public XMLSignature(
            Document doc,
            String baseURI,
            String signatureMethodURI,
            int hmacOutputLength,
            String canonicalizationMethodURI
    ) throws XMLSecurityException{
        super(doc);
        String xmlnsDsPrefix=getDefaultPrefix(Constants.SignatureSpecNS);
        if(xmlnsDsPrefix==null||xmlnsDsPrefix.length()==0){
            this.constructionElement.setAttributeNS(
                    Constants.NamespaceSpecNS,"xmlns",Constants.SignatureSpecNS
            );
        }else{
            this.constructionElement.setAttributeNS(
                    Constants.NamespaceSpecNS,"xmlns:"+xmlnsDsPrefix,Constants.SignatureSpecNS
            );
        }
        XMLUtils.addReturnToElement(this.constructionElement);
        this.baseURI=baseURI;
        this.signedInfo=
                new SignedInfo(
                        this.doc,signatureMethodURI,hmacOutputLength,canonicalizationMethodURI
                );
        this.constructionElement.appendChild(this.signedInfo.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
        // create an empty SignatureValue; this is filled by setSignatureValueElement
        signatureValueElement=
                XMLUtils.createElementInSignatureSpace(this.doc,Constants._TAG_SIGNATUREVALUE);
        this.constructionElement.appendChild(signatureValueElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLSignature(Document doc,String baseURI,String signatureMethodURI,
                        int hmacOutputLength) throws XMLSecurityException{
        this(
                doc,baseURI,signatureMethodURI,hmacOutputLength,
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
        );
    }

    public XMLSignature(
            Document doc,
            String baseURI,
            String signatureMethodURI,
            String canonicalizationMethodURI
    ) throws XMLSecurityException{
        this(doc,baseURI,signatureMethodURI,0,canonicalizationMethodURI);
    }

    public XMLSignature(
            Document doc,
            String baseURI,
            Element SignatureMethodElem,
            Element CanonicalizationMethodElem
    ) throws XMLSecurityException{
        super(doc);
        String xmlnsDsPrefix=getDefaultPrefix(Constants.SignatureSpecNS);
        if(xmlnsDsPrefix==null||xmlnsDsPrefix.length()==0){
            this.constructionElement.setAttributeNS(
                    Constants.NamespaceSpecNS,"xmlns",Constants.SignatureSpecNS
            );
        }else{
            this.constructionElement.setAttributeNS(
                    Constants.NamespaceSpecNS,"xmlns:"+xmlnsDsPrefix,Constants.SignatureSpecNS
            );
        }
        XMLUtils.addReturnToElement(this.constructionElement);
        this.baseURI=baseURI;
        this.signedInfo=
                new SignedInfo(this.doc,SignatureMethodElem,CanonicalizationMethodElem);
        this.constructionElement.appendChild(this.signedInfo.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
        // create an empty SignatureValue; this is filled by setSignatureValueElement
        signatureValueElement=
                XMLUtils.createElementInSignatureSpace(this.doc,Constants._TAG_SIGNATUREVALUE);
        this.constructionElement.appendChild(signatureValueElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLSignature(Element element,String baseURI)
            throws XMLSignatureException, XMLSecurityException{
        this(element,baseURI,false);
    }

    public XMLSignature(Element element,String baseURI,boolean secureValidation)
            throws XMLSignatureException, XMLSecurityException{
        super(element,baseURI);
        // check out SignedInfo child
        Element signedInfoElem=XMLUtils.getNextElement(element.getFirstChild());
        // check to see if it is there
        if(signedInfoElem==null){
            Object exArgs[]={Constants._TAG_SIGNEDINFO,Constants._TAG_SIGNATURE};
            throw new XMLSignatureException("xml.WrongContent",exArgs);
        }
        // create a SignedInfo object from that element
        this.signedInfo=new SignedInfo(signedInfoElem,baseURI,secureValidation);
        // get signedInfoElem again in case it has changed
        signedInfoElem=XMLUtils.getNextElement(element.getFirstChild());
        // check out SignatureValue child
        this.signatureValueElement=
                XMLUtils.getNextElement(signedInfoElem.getNextSibling());
        // check to see if it exists
        if(signatureValueElement==null){
            Object exArgs[]={Constants._TAG_SIGNATUREVALUE,Constants._TAG_SIGNATURE};
            throw new XMLSignatureException("xml.WrongContent",exArgs);
        }
        Attr signatureValueAttr=signatureValueElement.getAttributeNodeNS(null,"Id");
        if(signatureValueAttr!=null){
            signatureValueElement.setIdAttributeNode(signatureValueAttr,true);
        }
        // <element ref="ds:KeyInfo" minOccurs="0"/>
        Element keyInfoElem=
                XMLUtils.getNextElement(signatureValueElement.getNextSibling());
        // If it exists use it, but it's not mandatory
        if(keyInfoElem!=null
                &&keyInfoElem.getNamespaceURI().equals(Constants.SignatureSpecNS)
                &&keyInfoElem.getLocalName().equals(Constants._TAG_KEYINFO)){
            this.keyInfo=new KeyInfo(keyInfoElem,baseURI);
            this.keyInfo.setSecureValidation(secureValidation);
        }
        // <element ref="ds:Object" minOccurs="0" maxOccurs="unbounded"/>
        Element objectElem=
                XMLUtils.getNextElement(signatureValueElement.getNextSibling());
        while(objectElem!=null){
            Attr objectAttr=objectElem.getAttributeNodeNS(null,"Id");
            if(objectAttr!=null){
                objectElem.setIdAttributeNode(objectAttr,true);
            }
            NodeList nodes=objectElem.getChildNodes();
            int length=nodes.getLength();
            // Register Ids of the Object child elements
            for(int i=0;i<length;i++){
                Node child=nodes.item(i);
                if(child.getNodeType()==Node.ELEMENT_NODE){
                    Element childElem=(Element)child;
                    String tag=childElem.getLocalName();
                    if(tag.equals("Manifest")){
                        new Manifest(childElem,baseURI);
                    }else if(tag.equals("SignatureProperties")){
                        new SignatureProperties(childElem,baseURI);
                    }
                }
            }
            objectElem=XMLUtils.getNextElement(objectElem.getNextSibling());
        }
        this.state=MODE_VERIFY;
    }

    public String getId(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ID);
    }

    public void setId(String id){
        if(id!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_ID,id);
            this.constructionElement.setIdAttributeNS(null,Constants._ATT_ID,true);
        }
    }

    public void appendObject(ObjectContainer object) throws XMLSignatureException{
        //try {
        //if (this.state != MODE_SIGN) {
        // throw new XMLSignatureException(
        //  "signature.operationOnlyBeforeSign");
        //}
        this.constructionElement.appendChild(object.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
        //} catch (XMLSecurityException ex) {
        // throw new XMLSignatureException("empty", ex);
        //}
    }

    public ObjectContainer getObjectItem(int i){
        Element objElem=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_OBJECT,i
                );
        try{
            return new ObjectContainer(objElem,this.baseURI);
        }catch(XMLSecurityException ex){
            return null;
        }
    }

    public int getObjectLength(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_OBJECT);
    }

    public void sign(Key signingKey) throws XMLSignatureException{
        if(signingKey instanceof PublicKey){
            throw new IllegalArgumentException(
                    I18n.translate("algorithms.operationOnlyVerification")
            );
        }
        try{
            //Create a SignatureAlgorithm object
            SignedInfo si=this.getSignedInfo();
            SignatureAlgorithm sa=si.getSignatureAlgorithm();
            OutputStream so=null;
            try{
                // initialize SignatureAlgorithm for signing
                sa.initSign(signingKey);
                // generate digest values for all References in this SignedInfo
                si.generateDigestValues();
                so=new UnsyncBufferedOutputStream(new SignerOutputStream(sa));
                // get the canonicalized bytes from SignedInfo
                si.signInOctetStream(so);
            }catch(XMLSecurityException ex){
                throw ex;
            }finally{
                if(so!=null){
                    try{
                        so.close();
                    }catch(IOException ex){
                        if(log.isLoggable(java.util.logging.Level.FINE)){
                            log.log(java.util.logging.Level.FINE,ex.getMessage(),ex);
                        }
                    }
                }
            }
            // set them on the SignatureValue element
            this.setSignatureValueElement(sa.sign());
        }catch(XMLSignatureException ex){
            throw ex;
        }catch(CanonicalizationException ex){
            throw new XMLSignatureException("empty",ex);
        }catch(InvalidCanonicalizerException ex){
            throw new XMLSignatureException("empty",ex);
        }catch(XMLSecurityException ex){
            throw new XMLSignatureException("empty",ex);
        }
    }

    public SignedInfo getSignedInfo(){
        return this.signedInfo;
    }

    private void setSignatureValueElement(byte[] bytes){
        while(signatureValueElement.hasChildNodes()){
            signatureValueElement.removeChild(signatureValueElement.getFirstChild());
        }
        String base64codedValue=Base64.encode(bytes);
        if(base64codedValue.length()>76&&!XMLUtils.ignoreLineBreaks()){
            base64codedValue="\n"+base64codedValue+"\n";
        }
        Text t=this.doc.createTextNode(base64codedValue);
        signatureValueElement.appendChild(t);
    }

    public void addResourceResolver(ResourceResolver resolver){
        this.getSignedInfo().addResourceResolver(resolver);
    }

    public void addResourceResolver(ResourceResolverSpi resolver){
        this.getSignedInfo().addResourceResolver(resolver);
    }

    public boolean checkSignatureValue(X509Certificate cert)
            throws XMLSignatureException{
        // see if cert is null
        if(cert!=null){
            // check the values with the public key from the cert
            return this.checkSignatureValue(cert.getPublicKey());
        }
        Object exArgs[]={"Didn't get a certificate"};
        throw new XMLSignatureException("empty",exArgs);
    }

    public boolean checkSignatureValue(Key pk) throws XMLSignatureException{
        //COMMENT: pk suggests it can only be a public key?
        //check to see if the key is not null
        if(pk==null){
            Object exArgs[]={"Didn't get a key"};
            throw new XMLSignatureException("empty",exArgs);
        }
        // all references inside the signedinfo need to be dereferenced and
        // digested again to see if the outcome matches the stored value in the
        // SignedInfo.
        // If followManifestsDuringValidation is true it will do the same for
        // References inside a Manifest.
        try{
            SignedInfo si=this.getSignedInfo();
            //create a SignatureAlgorithms from the SignatureMethod inside
            //SignedInfo. This is used to validate the signature.
            SignatureAlgorithm sa=si.getSignatureAlgorithm();
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,"signatureMethodURI = "+sa.getAlgorithmURI());
                log.log(java.util.logging.Level.FINE,"jceSigAlgorithm    = "+sa.getJCEAlgorithmString());
                log.log(java.util.logging.Level.FINE,"jceSigProvider     = "+sa.getJCEProviderName());
                log.log(java.util.logging.Level.FINE,"PublicKey = "+pk);
            }
            byte sigBytes[]=null;
            try{
                sa.initVerify(pk);
                // Get the canonicalized (normalized) SignedInfo
                SignerOutputStream so=new SignerOutputStream(sa);
                OutputStream bos=new UnsyncBufferedOutputStream(so);
                si.signInOctetStream(bos);
                bos.close();
                // retrieve the byte[] from the stored signature
                sigBytes=this.getSignatureValue();
            }catch(IOException ex){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,ex.getMessage(),ex);
                }
                // Impossible...
            }catch(XMLSecurityException ex){
                throw ex;
            }
            // have SignatureAlgorithm sign the input bytes and compare them to
            // the bytes that were stored in the signature.
            if(!sa.verify(sigBytes)){
                log.log(java.util.logging.Level.WARNING,"Signature verification failed.");
                return false;
            }
            return si.verify(this.followManifestsDuringValidation);
        }catch(XMLSignatureException ex){
            throw ex;
        }catch(XMLSecurityException ex){
            throw new XMLSignatureException("empty",ex);
        }
    }

    public byte[] getSignatureValue() throws XMLSignatureException{
        try{
            return Base64.decode(signatureValueElement);
        }catch(Base64DecodingException ex){
            throw new XMLSignatureException("empty",ex);
        }
    }

    public void addDocument(
            String referenceURI,
            Transforms trans,
            String digestURI,
            String referenceId,
            String referenceType
    ) throws XMLSignatureException{
        this.signedInfo.addDocument(
                this.baseURI,referenceURI,trans,digestURI,referenceId,referenceType
        );
    }

    public void addDocument(
            String referenceURI,
            Transforms trans,
            String digestURI
    ) throws XMLSignatureException{
        this.signedInfo.addDocument(this.baseURI,referenceURI,trans,digestURI,null,null);
    }

    public void addDocument(String referenceURI,Transforms trans)
            throws XMLSignatureException{
        this.signedInfo.addDocument(
                this.baseURI,referenceURI,trans,Constants.ALGO_ID_DIGEST_SHA1,null,null
        );
    }

    public void addDocument(String referenceURI) throws XMLSignatureException{
        this.signedInfo.addDocument(
                this.baseURI,referenceURI,null,Constants.ALGO_ID_DIGEST_SHA1,null,null
        );
    }

    public void addKeyInfo(X509Certificate cert) throws XMLSecurityException{
        X509Data x509data=new X509Data(this.doc);
        x509data.addCertificate(cert);
        this.getKeyInfo().add(x509data);
    }

    public KeyInfo getKeyInfo(){
        // check to see if we are signing and if we have to create a keyinfo
        if(this.state==MODE_SIGN&&this.keyInfo==null){
            // create the KeyInfo
            this.keyInfo=new KeyInfo(this.doc);
            // get the Element from KeyInfo
            Element keyInfoElement=this.keyInfo.getElement();
            Element firstObject=
                    XMLUtils.selectDsNode(
                            this.constructionElement.getFirstChild(),Constants._TAG_OBJECT,0
                    );
            if(firstObject!=null){
                // add it before the object
                this.constructionElement.insertBefore(keyInfoElement,firstObject);
                XMLUtils.addReturnBeforeChild(this.constructionElement,firstObject);
            }else{
                // add it as the last element to the signature
                this.constructionElement.appendChild(keyInfoElement);
                XMLUtils.addReturnToElement(this.constructionElement);
            }
        }
        return this.keyInfo;
    }

    public void addKeyInfo(PublicKey pk){
        this.getKeyInfo().add(pk);
    }

    public SecretKey createSecretKey(byte[] secretKeyBytes){
        return this.getSignedInfo().createSecretKey(secretKeyBytes);
    }

    public void setFollowNestedManifests(boolean followManifests){
        this.followManifestsDuringValidation=followManifests;
    }

    public String getBaseLocalName(){
        return Constants._TAG_SIGNATURE;
    }
}
