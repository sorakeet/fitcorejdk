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

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.transforms.Transforms;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.I18n;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolver;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class Manifest extends SignatureElementProxy{
    public static final int MAXIMUM_REFERENCE_COUNT=30;
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(Manifest.class.getName());
    private List<Reference> references;
    private Element[] referencesEl;
    private boolean verificationResults[]=null;
    private Map<String,String> resolverProperties=null;
    private List<ResourceResolver> perManifestResolvers=null;
    private boolean secureValidation;

    public Manifest(Document doc){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.references=new ArrayList<Reference>();
    }

    public Manifest(Element element,String baseURI) throws XMLSecurityException{
        this(element,baseURI,false);
    }

    public Manifest(
            Element element,String baseURI,boolean secureValidation
    ) throws XMLSecurityException{
        super(element,baseURI);
        Attr attr=element.getAttributeNodeNS(null,"Id");
        if(attr!=null){
            element.setIdAttributeNode(attr,true);
        }
        this.secureValidation=secureValidation;
        // check out Reference children
        this.referencesEl=
                XMLUtils.selectDsNodes(
                        this.constructionElement.getFirstChild(),Constants._TAG_REFERENCE
                );
        int le=this.referencesEl.length;
        if(le==0){
            // At least one Reference must be present. Bad.
            Object exArgs[]={Constants._TAG_REFERENCE,Constants._TAG_MANIFEST};
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    I18n.translate("xml.WrongContent",exArgs));
        }
        if(secureValidation&&le>MAXIMUM_REFERENCE_COUNT){
            Object exArgs[]={le,MAXIMUM_REFERENCE_COUNT};
            throw new XMLSecurityException("signature.tooManyReferences",exArgs);
        }
        // create List
        this.references=new ArrayList<Reference>(le);
        for(int i=0;i<le;i++){
            Element refElem=referencesEl[i];
            Attr refAttr=refElem.getAttributeNodeNS(null,"Id");
            if(refAttr!=null){
                refElem.setIdAttributeNode(refAttr,true);
            }
            this.references.add(null);
        }
    }

    public void addDocument(
            String baseURI,String referenceURI,Transforms transforms,
            String digestURI,String referenceId,String referenceType
    ) throws XMLSignatureException{
        // the this.doc is handed implicitly by the this.getOwnerDocument()
        Reference ref=
                new Reference(this.doc,baseURI,referenceURI,this,transforms,digestURI);
        if(referenceId!=null){
            ref.setId(referenceId);
        }
        if(referenceType!=null){
            ref.setType(referenceType);
        }
        // add Reference object to our cache vector
        this.references.add(ref);
        // add the Element of the Reference object to the Manifest/SignedInfo
        this.constructionElement.appendChild(ref.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void generateDigestValues()
            throws XMLSignatureException, ReferenceNotInitializedException{
        for(int i=0;i<this.getLength();i++){
            // update the cached Reference object, the Element content is automatically updated
            Reference currentRef=this.references.get(i);
            currentRef.generateDigestValue();
        }
    }

    public int getLength(){
        return this.references.size();
    }

    public String getId(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ID);
    }

    public void setId(String Id){
        if(Id!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_ID,Id);
            this.constructionElement.setIdAttributeNS(null,Constants._ATT_ID,true);
        }
    }

    public boolean getVerificationResult(int index) throws XMLSecurityException{
        if((index<0)||(index>this.getLength()-1)){
            Object exArgs[]={Integer.toString(index),Integer.toString(this.getLength())};
            Exception e=
                    new IndexOutOfBoundsException(
                            I18n.translate("signature.Verification.IndexOutOfBounds",exArgs)
                    );
            throw new XMLSecurityException("generic.EmptyMessage",e);
        }
        if(this.verificationResults==null){
            try{
                this.verifyReferences();
            }catch(Exception ex){
                throw new XMLSecurityException("generic.EmptyMessage",ex);
            }
        }
        return this.verificationResults[index];
    }

    public boolean verifyReferences()
            throws MissingResourceFailureException, XMLSecurityException{
        return this.verifyReferences(false);
    }

    public boolean verifyReferences(boolean followManifests)
            throws MissingResourceFailureException, XMLSecurityException{
        if(referencesEl==null){
            this.referencesEl=
                    XMLUtils.selectDsNodes(
                            this.constructionElement.getFirstChild(),Constants._TAG_REFERENCE
                    );
        }
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"verify "+referencesEl.length+" References");
            log.log(java.util.logging.Level.FINE,"I am "+(followManifests
                    ?"":"not")+" requested to follow nested Manifests");
        }
        if(referencesEl.length==0){
            throw new XMLSecurityException("empty");
        }
        if(secureValidation&&referencesEl.length>MAXIMUM_REFERENCE_COUNT){
            Object exArgs[]={referencesEl.length,MAXIMUM_REFERENCE_COUNT};
            throw new XMLSecurityException("signature.tooManyReferences",exArgs);
        }
        this.verificationResults=new boolean[referencesEl.length];
        boolean verify=true;
        for(int i=0;i<this.referencesEl.length;i++){
            Reference currentRef=
                    new Reference(referencesEl[i],this.baseURI,this,secureValidation);
            this.references.set(i,currentRef);
            // if only one item does not verify, the whole verification fails
            try{
                boolean currentRefVerified=currentRef.verify();
                this.setVerificationResult(i,currentRefVerified);
                if(!currentRefVerified){
                    verify=false;
                }
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,"The Reference has Type "+currentRef.getType());
                }
                // was verification successful till now and do we want to verify the Manifest?
                if(verify&&followManifests&&currentRef.typeIsReferenceToManifest()){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,"We have to follow a nested Manifest");
                    }
                    try{
                        XMLSignatureInput signedManifestNodes=
                                currentRef.dereferenceURIandPerformTransforms(null);
                        Set<Node> nl=signedManifestNodes.getNodeSet();
                        Manifest referencedManifest=null;
                        Iterator<Node> nlIterator=nl.iterator();
                        findManifest:
                        while(nlIterator.hasNext()){
                            Node n=nlIterator.next();
                            if((n.getNodeType()==Node.ELEMENT_NODE)
                                    &&((Element)n).getNamespaceURI().equals(Constants.SignatureSpecNS)
                                    &&((Element)n).getLocalName().equals(Constants._TAG_MANIFEST)
                                    ){
                                try{
                                    referencedManifest=
                                            new Manifest(
                                                    (Element)n,signedManifestNodes.getSourceURI(),secureValidation
                                            );
                                    break findManifest;
                                }catch(XMLSecurityException ex){
                                    if(log.isLoggable(java.util.logging.Level.FINE)){
                                        log.log(java.util.logging.Level.FINE,ex.getMessage(),ex);
                                    }
                                    // Hm, seems not to be a ds:Manifest
                                }
                            }
                        }
                        if(referencedManifest==null){
                            // The Reference stated that it points to a ds:Manifest
                            // but we did not find a ds:Manifest in the signed area
                            throw new MissingResourceFailureException("empty",currentRef);
                        }
                        referencedManifest.perManifestResolvers=this.perManifestResolvers;
                        referencedManifest.resolverProperties=this.resolverProperties;
                        boolean referencedManifestValid=
                                referencedManifest.verifyReferences(followManifests);
                        if(!referencedManifestValid){
                            verify=false;
                            log.log(java.util.logging.Level.WARNING,"The nested Manifest was invalid (bad)");
                        }else{
                            if(log.isLoggable(java.util.logging.Level.FINE)){
                                log.log(java.util.logging.Level.FINE,"The nested Manifest was valid (good)");
                            }
                        }
                    }catch(IOException ex){
                        throw new ReferenceNotInitializedException("empty",ex);
                    }catch(ParserConfigurationException ex){
                        throw new ReferenceNotInitializedException("empty",ex);
                    }catch(SAXException ex){
                        throw new ReferenceNotInitializedException("empty",ex);
                    }
                }
            }catch(ReferenceNotInitializedException ex){
                Object exArgs[]={currentRef.getURI()};
                throw new MissingResourceFailureException(
                        "signature.Verification.Reference.NoInput",exArgs,ex,currentRef
                );
            }
        }
        return verify;
    }

    private void setVerificationResult(int index,boolean verify){
        if(this.verificationResults==null){
            this.verificationResults=new boolean[this.getLength()];
        }
        this.verificationResults[index]=verify;
    }

    public void addResourceResolver(ResourceResolver resolver){
        if(resolver==null){
            return;
        }
        if(perManifestResolvers==null){
            perManifestResolvers=new ArrayList<ResourceResolver>();
        }
        this.perManifestResolvers.add(resolver);
    }

    public void addResourceResolver(ResourceResolverSpi resolverSpi){
        if(resolverSpi==null){
            return;
        }
        if(perManifestResolvers==null){
            perManifestResolvers=new ArrayList<ResourceResolver>();
        }
        perManifestResolvers.add(new ResourceResolver(resolverSpi));
    }

    public List<ResourceResolver> getPerManifestResolvers(){
        return perManifestResolvers;
    }

    public Map<String,String> getResolverProperties(){
        return resolverProperties;
    }

    public void setResolverProperty(String key,String value){
        if(resolverProperties==null){
            resolverProperties=new HashMap<String,String>(10);
        }
        this.resolverProperties.put(key,value);
    }

    public String getResolverProperty(String key){
        return this.resolverProperties.get(key);
    }

    public byte[] getSignedContentItem(int i) throws XMLSignatureException{
        try{
            return this.getReferencedContentAfterTransformsItem(i).getBytes();
        }catch(IOException ex){
            throw new XMLSignatureException("empty",ex);
        }catch(CanonicalizationException ex){
            throw new XMLSignatureException("empty",ex);
        }catch(InvalidCanonicalizerException ex){
            throw new XMLSignatureException("empty",ex);
        }catch(XMLSecurityException ex){
            throw new XMLSignatureException("empty",ex);
        }
    }

    public XMLSignatureInput getReferencedContentAfterTransformsItem(int i)
            throws XMLSecurityException{
        return this.item(i).getContentsAfterTransformation();
    }

    public Reference item(int i) throws XMLSecurityException{
        if(this.references.get(i)==null){
            // not yet constructed, so _we_ have to
            Reference ref=
                    new Reference(referencesEl[i],this.baseURI,this,secureValidation);
            this.references.set(i,ref);
        }
        return this.references.get(i);
    }

    public XMLSignatureInput getReferencedContentBeforeTransformsItem(int i)
            throws XMLSecurityException{
        return this.item(i).getContentsBeforeTransformation();
    }

    public int getSignedContentLength(){
        return this.getLength();
    }

    public String getBaseLocalName(){
        return Constants._TAG_MANIFEST;
    }
}
