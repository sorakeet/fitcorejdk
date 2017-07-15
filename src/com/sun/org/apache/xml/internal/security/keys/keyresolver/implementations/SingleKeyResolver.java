/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations;

import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverException;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Element;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class SingleKeyResolver extends KeyResolverSpi{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(SingleKeyResolver.class.getName());
    private String keyName;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey secretKey;

    public SingleKeyResolver(String keyName,PublicKey publicKey){
        this.keyName=keyName;
        this.publicKey=publicKey;
    }

    public SingleKeyResolver(String keyName,PrivateKey privateKey){
        this.keyName=keyName;
        this.privateKey=privateKey;
    }

    public SingleKeyResolver(String keyName,SecretKey secretKey){
        this.keyName=keyName;
        this.secretKey=secretKey;
    }

    public boolean engineCanResolve(Element element,String baseURI,StorageResolver storage){
        return XMLUtils.elementIsInSignatureSpace(element,Constants._TAG_KEYNAME);
    }

    public PublicKey engineLookupAndResolvePublicKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Can I resolve "+element.getTagName()+"?");
        }
        if(publicKey!=null
                &&XMLUtils.elementIsInSignatureSpace(element,Constants._TAG_KEYNAME)){
            String name=element.getFirstChild().getNodeValue();
            if(keyName.equals(name)){
                return publicKey;
            }
        }
        log.log(java.util.logging.Level.FINE,"I can't");
        return null;
    }

    public X509Certificate engineLookupResolveX509Certificate(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        return null;
    }

    public SecretKey engineResolveSecretKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Can I resolve "+element.getTagName()+"?");
        }
        if(secretKey!=null
                &&XMLUtils.elementIsInSignatureSpace(element,Constants._TAG_KEYNAME)){
            String name=element.getFirstChild().getNodeValue();
            if(keyName.equals(name)){
                return secretKey;
            }
        }
        log.log(java.util.logging.Level.FINE,"I can't");
        return null;
    }

    public PrivateKey engineLookupAndResolvePrivateKey(
            Element element,String baseURI,StorageResolver storage
    ) throws KeyResolverException{
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Can I resolve "+element.getTagName()+"?");
        }
        if(privateKey!=null
                &&XMLUtils.elementIsInSignatureSpace(element,Constants._TAG_KEYNAME)){
            String name=element.getFirstChild().getNodeValue();
            if(keyName.equals(name)){
                return privateKey;
            }
        }
        log.log(java.util.logging.Level.FINE,"I can't");
        return null;
    }
}
