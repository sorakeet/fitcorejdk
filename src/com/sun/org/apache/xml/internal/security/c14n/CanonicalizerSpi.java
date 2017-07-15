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

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Set;

public abstract class CanonicalizerSpi{
    protected boolean reset=false;

    public byte[] engineCanonicalize(byte[] inputBytes)
            throws javax.xml.parsers.ParserConfigurationException, java.io.IOException,
            org.xml.sax.SAXException, CanonicalizationException{
        java.io.InputStream bais=new ByteArrayInputStream(inputBytes);
        InputSource in=new InputSource(bais);
        DocumentBuilderFactory dfactory=DocumentBuilderFactory.newInstance();
        dfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,Boolean.TRUE);
        // needs to validate for ID attribute normalization
        dfactory.setNamespaceAware(true);
        DocumentBuilder db=dfactory.newDocumentBuilder();
        Document document=db.parse(in);
        return this.engineCanonicalizeSubTree(document);
    }

    public abstract byte[] engineCanonicalizeSubTree(Node rootNode)
            throws CanonicalizationException;

    public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet)
            throws CanonicalizationException{
        return this.engineCanonicalizeXPathNodeSet(
                XMLUtils.convertNodelistToSet(xpathNodeSet)
        );
    }

    public abstract byte[] engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet)
            throws CanonicalizationException;

    public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet,String inclusiveNamespaces)
            throws CanonicalizationException{
        return this.engineCanonicalizeXPathNodeSet(
                XMLUtils.convertNodelistToSet(xpathNodeSet),inclusiveNamespaces
        );
    }

    public abstract byte[] engineCanonicalizeXPathNodeSet(
            Set<Node> xpathNodeSet,String inclusiveNamespaces
    ) throws CanonicalizationException;

    public abstract String engineGetURI();

    public abstract boolean engineGetIncludeComments();

    public abstract byte[] engineCanonicalizeSubTree(Node rootNode,String inclusiveNamespaces)
            throws CanonicalizationException;

    public abstract void setWriter(OutputStream os);
}
