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
package com.sun.org.apache.xml.internal.security.transforms;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.internal.security.exceptions.AlgorithmAlreadyRegisteredException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.internal.security.transforms.implementations.*;
import com.sun.org.apache.xml.internal.security.utils.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Transform extends SignatureElementProxy{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(Transform.class.getName());
    private static Map<String,Class<? extends TransformSpi>> transformSpiHash=
            new ConcurrentHashMap<String,Class<? extends TransformSpi>>();
    private final TransformSpi transformSpi;

    public Transform(Document doc,String algorithmURI) throws InvalidTransformException{
        this(doc,algorithmURI,(NodeList)null);
    }

    public Transform(Document doc,String algorithmURI,NodeList contextNodes)
            throws InvalidTransformException{
        super(doc);
        transformSpi=initializeTransform(algorithmURI,contextNodes);
    }

    private TransformSpi initializeTransform(String algorithmURI,NodeList contextNodes)
            throws InvalidTransformException{
        this.constructionElement.setAttributeNS(null,Constants._ATT_ALGORITHM,algorithmURI);
        Class<? extends TransformSpi> transformSpiClass=transformSpiHash.get(algorithmURI);
        if(transformSpiClass==null){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform",exArgs);
        }
        TransformSpi newTransformSpi=null;
        try{
            newTransformSpi=transformSpiClass.newInstance();
        }catch(InstantiationException ex){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException(
                    "signature.Transform.UnknownTransform",exArgs,ex
            );
        }catch(IllegalAccessException ex){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException(
                    "signature.Transform.UnknownTransform",exArgs,ex
            );
        }
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"Create URI \""+algorithmURI+"\" class \""
                    +newTransformSpi.getClass()+"\"");
            log.log(java.util.logging.Level.FINE,"The NodeList is "+contextNodes);
        }
        // give it to the current document
        if(contextNodes!=null){
            for(int i=0;i<contextNodes.getLength();i++){
                this.constructionElement.appendChild(contextNodes.item(i).cloneNode(true));
            }
        }
        return newTransformSpi;
    }

    public Transform(Document doc,String algorithmURI,Element contextChild)
            throws InvalidTransformException{
        super(doc);
        HelperNodeList contextNodes=null;
        if(contextChild!=null){
            contextNodes=new HelperNodeList();
            XMLUtils.addReturnToElement(doc,contextNodes);
            contextNodes.appendChild(contextChild);
            XMLUtils.addReturnToElement(doc,contextNodes);
        }
        transformSpi=initializeTransform(algorithmURI,contextNodes);
    }

    public Transform(Element element,String BaseURI)
            throws InvalidTransformException, TransformationException, XMLSecurityException{
        super(element,BaseURI);
        // retrieve Algorithm Attribute from ds:Transform
        String algorithmURI=element.getAttributeNS(null,Constants._ATT_ALGORITHM);
        if(algorithmURI==null||algorithmURI.length()==0){
            Object exArgs[]={Constants._ATT_ALGORITHM,Constants._TAG_TRANSFORM};
            throw new TransformationException("xml.WrongContent",exArgs);
        }
        Class<? extends TransformSpi> transformSpiClass=transformSpiHash.get(algorithmURI);
        if(transformSpiClass==null){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException("signature.Transform.UnknownTransform",exArgs);
        }
        try{
            transformSpi=transformSpiClass.newInstance();
        }catch(InstantiationException ex){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException(
                    "signature.Transform.UnknownTransform",exArgs,ex
            );
        }catch(IllegalAccessException ex){
            Object exArgs[]={algorithmURI};
            throw new InvalidTransformException(
                    "signature.Transform.UnknownTransform",exArgs,ex
            );
        }
    }

    @SuppressWarnings("unchecked")
    public static void register(String algorithmURI,String implementingClass)
            throws AlgorithmAlreadyRegisteredException, ClassNotFoundException,
            InvalidTransformException{
        JavaUtils.checkRegisterPermission();
        // are we already registered?
        Class<? extends TransformSpi> transformSpi=transformSpiHash.get(algorithmURI);
        if(transformSpi!=null){
            Object exArgs[]={algorithmURI,transformSpi};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered",exArgs);
        }
        Class<? extends TransformSpi> transformSpiClass=
                (Class<? extends TransformSpi>)
                        ClassLoaderUtils.loadClass(implementingClass,Transform.class);
        transformSpiHash.put(algorithmURI,transformSpiClass);
    }

    public static void register(String algorithmURI,Class<? extends TransformSpi> implementingClass)
            throws AlgorithmAlreadyRegisteredException{
        JavaUtils.checkRegisterPermission();
        // are we already registered?
        Class<? extends TransformSpi> transformSpi=transformSpiHash.get(algorithmURI);
        if(transformSpi!=null){
            Object exArgs[]={algorithmURI,transformSpi};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered",exArgs);
        }
        transformSpiHash.put(algorithmURI,implementingClass);
    }

    public static void registerDefaultAlgorithms(){
        transformSpiHash.put(
                Transforms.TRANSFORM_BASE64_DECODE,TransformBase64Decode.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N_OMIT_COMMENTS,TransformC14N.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N_WITH_COMMENTS,TransformC14NWithComments.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N11_OMIT_COMMENTS,TransformC14N11.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N11_WITH_COMMENTS,TransformC14N11_WithComments.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,TransformC14NExclusive.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS,TransformC14NExclusiveWithComments.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_XPATH,TransformXPath.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_ENVELOPED_SIGNATURE,TransformEnvelopedSignature.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_XSLT,TransformXSLT.class
        );
        transformSpiHash.put(
                Transforms.TRANSFORM_XPATH2FILTER,TransformXPath2Filter.class
        );
    }

    public XMLSignatureInput performTransform(XMLSignatureInput input)
            throws IOException, CanonicalizationException,
            InvalidCanonicalizerException, TransformationException{
        return performTransform(input,null);
    }

    public XMLSignatureInput performTransform(
            XMLSignatureInput input,OutputStream os
    ) throws IOException, CanonicalizationException,
            InvalidCanonicalizerException, TransformationException{
        XMLSignatureInput result=null;
        try{
            result=transformSpi.enginePerformTransform(input,os,this);
        }catch(ParserConfigurationException ex){
            Object exArgs[]={this.getURI(),"ParserConfigurationException"};
            throw new CanonicalizationException(
                    "signature.Transform.ErrorDuringTransform",exArgs,ex);
        }catch(SAXException ex){
            Object exArgs[]={this.getURI(),"SAXException"};
            throw new CanonicalizationException(
                    "signature.Transform.ErrorDuringTransform",exArgs,ex);
        }
        return result;
    }

    public String getURI(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ALGORITHM);
    }

    public String getBaseLocalName(){
        return Constants._TAG_TRANSFORM;
    }
}
