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
package com.sun.org.apache.xml.internal.security.c14n;

import com.sun.org.apache.xml.internal.security.c14n.implementations.*;
import com.sun.org.apache.xml.internal.security.exceptions.AlgorithmAlreadyRegisteredException;
import com.sun.org.apache.xml.internal.security.utils.JavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Canonicalizer{
    public static final String ENCODING="UTF8";
    public static final String XPATH_C14N_WITH_COMMENTS_SINGLE_NODE=
            "(.//. | .//@* | .//namespace::*)";
    public static final String ALGO_ID_C14N_OMIT_COMMENTS=
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String ALGO_ID_C14N_WITH_COMMENTS=
            ALGO_ID_C14N_OMIT_COMMENTS+"#WithComments";
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS=
            "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS=
            ALGO_ID_C14N_EXCL_OMIT_COMMENTS+"WithComments";
    public static final String ALGO_ID_C14N11_OMIT_COMMENTS=
            "http://www.w3.org/2006/12/xml-c14n11";
    public static final String ALGO_ID_C14N11_WITH_COMMENTS=
            ALGO_ID_C14N11_OMIT_COMMENTS+"#WithComments";
    public static final String ALGO_ID_C14N_PHYSICAL=
            "http://santuario.apache.org/c14n/physical";
    private static Map<String,Class<? extends CanonicalizerSpi>> canonicalizerHash=
            new ConcurrentHashMap<String,Class<? extends CanonicalizerSpi>>();
    private final CanonicalizerSpi canonicalizerSpi;

    private Canonicalizer(String algorithmURI) throws InvalidCanonicalizerException{
        try{
            Class<? extends CanonicalizerSpi> implementingClass=
                    canonicalizerHash.get(algorithmURI);
            canonicalizerSpi=implementingClass.newInstance();
            canonicalizerSpi.reset=true;
        }catch(Exception e){
            Object exArgs[]={algorithmURI};
            throw new InvalidCanonicalizerException(
                    "signature.Canonicalizer.UnknownCanonicalizer",exArgs,e
            );
        }
    }

    public static final Canonicalizer getInstance(String algorithmURI)
            throws InvalidCanonicalizerException{
        return new Canonicalizer(algorithmURI);
    }

    @SuppressWarnings("unchecked")
    public static void register(String algorithmURI,String implementingClass)
            throws AlgorithmAlreadyRegisteredException, ClassNotFoundException{
        JavaUtils.checkRegisterPermission();
        // check whether URI is already registered
        Class<? extends CanonicalizerSpi> registeredClass=
                canonicalizerHash.get(algorithmURI);
        if(registeredClass!=null){
            Object exArgs[]={algorithmURI,registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered",exArgs);
        }
        canonicalizerHash.put(
                algorithmURI,(Class<? extends CanonicalizerSpi>)Class.forName(implementingClass)
        );
    }

    public static void register(String algorithmURI,Class<? extends CanonicalizerSpi> implementingClass)
            throws AlgorithmAlreadyRegisteredException, ClassNotFoundException{
        JavaUtils.checkRegisterPermission();
        // check whether URI is already registered
        Class<? extends CanonicalizerSpi> registeredClass=canonicalizerHash.get(algorithmURI);
        if(registeredClass!=null){
            Object exArgs[]={algorithmURI,registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered",exArgs);
        }
        canonicalizerHash.put(algorithmURI,implementingClass);
    }

    public static void registerDefaultAlgorithms(){
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
                Canonicalizer20010315OmitComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS,
                Canonicalizer20010315WithComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                Canonicalizer20010315ExclOmitComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS,
                Canonicalizer20010315ExclWithComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS,
                Canonicalizer11_OmitComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N11_WITH_COMMENTS,
                Canonicalizer11_WithComments.class
        );
        canonicalizerHash.put(
                Canonicalizer.ALGO_ID_C14N_PHYSICAL,
                CanonicalizerPhysical.class
        );
    }

    public final String getURI(){
        return canonicalizerSpi.engineGetURI();
    }

    public boolean getIncludeComments(){
        return canonicalizerSpi.engineGetIncludeComments();
    }

    public byte[] canonicalize(byte[] inputBytes)
            throws javax.xml.parsers.ParserConfigurationException,
            java.io.IOException, org.xml.sax.SAXException, CanonicalizationException{
        InputStream bais=new ByteArrayInputStream(inputBytes);
        InputSource in=new InputSource(bais);
        DocumentBuilderFactory dfactory=DocumentBuilderFactory.newInstance();
        dfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,Boolean.TRUE);
        dfactory.setNamespaceAware(true);
        // needs to validate for ID attribute normalization
        dfactory.setValidating(true);
        DocumentBuilder db=dfactory.newDocumentBuilder();
        /**
         * for some of the test vectors from the specification,
         * there has to be a validating parser for ID attributes, default
         * attribute values, NMTOKENS, etc.
         * Unfortunately, the test vectors do use different DTDs or
         * even no DTD. So Xerces 1.3.1 fires many warnings about using
         * ErrorHandlers.
         *
         * Text from the spec:
         *
         * The input octet stream MUST contain a well-formed XML document,
         * but the input need not be validated. However, the attribute
         * value normalization and entity reference resolution MUST be
         * performed in accordance with the behaviors of a validating
         * XML processor. As well, nodes for default attributes (declared
         * in the ATTLIST with an AttValue but not specified) are created
         * in each element. Thus, the declarations in the document type
         * declaration are used to help create the canonical form, even
         * though the document type declaration is not retained in the
         * canonical form.
         */
        db.setErrorHandler(new com.sun.org.apache.xml.internal.security.utils.IgnoreAllErrorHandler());
        Document document=db.parse(in);
        return this.canonicalizeSubtree(document);
    }

    public byte[] canonicalizeSubtree(Node node) throws CanonicalizationException{
        return canonicalizerSpi.engineCanonicalizeSubTree(node);
    }

    public byte[] canonicalizeSubtree(Node node,String inclusiveNamespaces)
            throws CanonicalizationException{
        return canonicalizerSpi.engineCanonicalizeSubTree(node,inclusiveNamespaces);
    }

    public byte[] canonicalizeXPathNodeSet(NodeList xpathNodeSet)
            throws CanonicalizationException{
        return canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }

    public byte[] canonicalizeXPathNodeSet(
            NodeList xpathNodeSet,String inclusiveNamespaces
    ) throws CanonicalizationException{
        return
                canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet,inclusiveNamespaces);
    }

    public byte[] canonicalizeXPathNodeSet(Set<Node> xpathNodeSet)
            throws CanonicalizationException{
        return canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }

    public byte[] canonicalizeXPathNodeSet(
            Set<Node> xpathNodeSet,String inclusiveNamespaces
    ) throws CanonicalizationException{
        return
                canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet,inclusiveNamespaces);
    }

    public void setWriter(OutputStream os){
        canonicalizerSpi.setWriter(os);
    }

    public String getImplementingCanonicalizerClass(){
        return canonicalizerSpi.getClass().getName();
    }

    public void notReset(){
        canonicalizerSpi.reset=false;
    }
}
