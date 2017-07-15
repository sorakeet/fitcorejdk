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
import com.sun.org.apache.xml.internal.security.keys.content.x509.*;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

public class X509Data extends SignatureElementProxy implements KeyInfoContent{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(X509Data.class.getName());

    public X509Data(Document doc){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public X509Data(Element element,String baseURI) throws XMLSecurityException{
        super(element,baseURI);
        Node sibling=this.constructionElement.getFirstChild();
        while(sibling!=null){
            if(sibling.getNodeType()!=Node.ELEMENT_NODE){
                sibling=sibling.getNextSibling();
                continue;
            }
            return;
        }
        /** No Elements found */
        Object exArgs[]={"Elements",Constants._TAG_X509DATA};
        throw new XMLSecurityException("xml.WrongContent",exArgs);
    }

    public void addIssuerSerial(String X509IssuerName,BigInteger X509SerialNumber){
        this.add(new XMLX509IssuerSerial(this.doc,X509IssuerName,X509SerialNumber));
    }

    public void add(XMLX509IssuerSerial xmlX509IssuerSerial){
        this.constructionElement.appendChild(xmlX509IssuerSerial.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addIssuerSerial(String X509IssuerName,String X509SerialNumber){
        this.add(new XMLX509IssuerSerial(this.doc,X509IssuerName,X509SerialNumber));
    }

    public void addIssuerSerial(String X509IssuerName,int X509SerialNumber){
        this.add(new XMLX509IssuerSerial(this.doc,X509IssuerName,X509SerialNumber));
    }

    public void addSKI(byte[] skiBytes){
        this.add(new XMLX509SKI(this.doc,skiBytes));
    }

    public void add(XMLX509SKI xmlX509SKI){
        this.constructionElement.appendChild(xmlX509SKI.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addSKI(X509Certificate x509certificate)
            throws XMLSecurityException{
        this.add(new XMLX509SKI(this.doc,x509certificate));
    }

    public void addSubjectName(String subjectName){
        this.add(new XMLX509SubjectName(this.doc,subjectName));
    }

    public void add(XMLX509SubjectName xmlX509SubjectName){
        this.constructionElement.appendChild(xmlX509SubjectName.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addSubjectName(X509Certificate x509certificate){
        this.add(new XMLX509SubjectName(this.doc,x509certificate));
    }

    public void addCertificate(X509Certificate x509certificate)
            throws XMLSecurityException{
        this.add(new XMLX509Certificate(this.doc,x509certificate));
    }

    public void add(XMLX509Certificate xmlX509Certificate){
        this.constructionElement.appendChild(xmlX509Certificate.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addCertificate(byte[] x509certificateBytes){
        this.add(new XMLX509Certificate(this.doc,x509certificateBytes));
    }

    public void addCRL(byte[] crlBytes){
        this.add(new XMLX509CRL(this.doc,crlBytes));
    }

    public void add(XMLX509CRL xmlX509CRL){
        this.constructionElement.appendChild(xmlX509CRL.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addDigest(X509Certificate x509certificate,String algorithmURI)
            throws XMLSecurityException{
        this.add(new XMLX509Digest(this.doc,x509certificate,algorithmURI));
    }

    public void add(XMLX509Digest xmlX509Digest){
        this.constructionElement.appendChild(xmlX509Digest.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addDigest(byte[] x509certificateDigestBytes,String algorithmURI){
        this.add(new XMLX509Digest(this.doc,x509certificateDigestBytes,algorithmURI));
    }

    public void addUnknownElement(Element element){
        this.constructionElement.appendChild(element);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLX509IssuerSerial itemIssuerSerial(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509ISSUERSERIAL,i);
        if(e!=null){
            return new XMLX509IssuerSerial(e,this.baseURI);
        }
        return null;
    }

    public XMLX509SKI itemSKI(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509SKI,i);
        if(e!=null){
            return new XMLX509SKI(e,this.baseURI);
        }
        return null;
    }

    public XMLX509SubjectName itemSubjectName(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509SUBJECTNAME,i);
        if(e!=null){
            return new XMLX509SubjectName(e,this.baseURI);
        }
        return null;
    }

    public XMLX509Certificate itemCertificate(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509CERTIFICATE,i);
        if(e!=null){
            return new XMLX509Certificate(e,this.baseURI);
        }
        return null;
    }

    public XMLX509CRL itemCRL(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509CRL,i);
        if(e!=null){
            return new XMLX509CRL(e,this.baseURI);
        }
        return null;
    }

    public XMLX509Digest itemDigest(int i) throws XMLSecurityException{
        Element e=
                XMLUtils.selectDs11Node(
                        this.constructionElement.getFirstChild(),Constants._TAG_X509DIGEST,i);
        if(e!=null){
            return new XMLX509Digest(e,this.baseURI);
        }
        return null;
    }

    public Element itemUnknownElement(int i){
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"itemUnknownElement not implemented:"+i);
        }
        return null;
    }

    public boolean containsIssuerSerial(){
        return this.lengthIssuerSerial()>0;
    }

    public int lengthIssuerSerial(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_X509ISSUERSERIAL);
    }

    public boolean containsSKI(){
        return this.lengthSKI()>0;
    }

    public int lengthSKI(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_X509SKI);
    }

    public boolean containsSubjectName(){
        return this.lengthSubjectName()>0;
    }

    public int lengthSubjectName(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_X509SUBJECTNAME);
    }

    public boolean containsCertificate(){
        return this.lengthCertificate()>0;
    }

    public int lengthCertificate(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_X509CERTIFICATE);
    }

    public boolean containsDigest(){
        return this.lengthDigest()>0;
    }

    public int lengthDigest(){
        return this.length(Constants.SignatureSpec11NS,Constants._TAG_X509DIGEST);
    }

    public boolean containsCRL(){
        return this.lengthCRL()>0;
    }

    public int lengthCRL(){
        return this.length(Constants.SignatureSpecNS,Constants._TAG_X509CRL);
    }

    public boolean containsUnknownElement(){
        return this.lengthUnknownElement()>0;
    }

    public int lengthUnknownElement(){
        int result=0;
        Node n=this.constructionElement.getFirstChild();
        while(n!=null){
            if((n.getNodeType()==Node.ELEMENT_NODE)
                    &&!n.getNamespaceURI().equals(Constants.SignatureSpecNS)){
                result++;
            }
            n=n.getNextSibling();
        }
        return result;
    }

    public String getBaseLocalName(){
        return Constants._TAG_X509DATA;
    }
}
