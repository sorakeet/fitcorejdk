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
package com.sun.org.apache.xml.internal.security.c14n.implementations;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CanonicalizerPhysical extends CanonicalizerBase{
    private final SortedSet<Attr> result=new TreeSet<Attr>(COMPARE);

    public CanonicalizerPhysical(){
        super(true);
    }

    @Override
    protected void handleParent(Element e,NameSpaceSymbTable ns){
        // nothing to do
    }

    @Override
    protected Iterator<Attr> handleAttributes(Element element,NameSpaceSymbTable ns)
            throws CanonicalizationException{
        /** $todo$ well, should we throw UnsupportedOperationException ? */
        throw new CanonicalizationException("c14n.Canonicalizer.UnsupportedOperation");
    }

    @Override
    protected Iterator<Attr> handleAttributesSubtree(Element element,NameSpaceSymbTable ns)
            throws CanonicalizationException{
        if(!element.hasAttributes()){
            return null;
        }
        // result will contain all the attrs declared directly on that element
        final SortedSet<Attr> result=this.result;
        result.clear();
        if(element.hasAttributes()){
            NamedNodeMap attrs=element.getAttributes();
            int attrsLength=attrs.getLength();
            for(int i=0;i<attrsLength;i++){
                Attr attribute=(Attr)attrs.item(i);
                result.add(attribute);
            }
        }
        return result.iterator();
    }

    protected void circumventBugIfNeeded(XMLSignatureInput input)
            throws CanonicalizationException, ParserConfigurationException, IOException, SAXException{
        // nothing to do
    }

    @Override
    protected void outputPItoWriter(ProcessingInstruction currentPI,
                                    OutputStream writer,int position) throws IOException{
        // Processing Instructions before or after the document element are not treated specially
        super.outputPItoWriter(currentPI,writer,NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT);
    }

    @Override
    protected void outputCommentToWriter(Comment currentComment,
                                         OutputStream writer,int position) throws IOException{
        // Comments before or after the document element are not treated specially
        super.outputCommentToWriter(currentComment,writer,NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT);
    }

    public final String engineGetURI(){
        return Canonicalizer.ALGO_ID_C14N_PHYSICAL;
    }

    public final boolean engineGetIncludeComments(){
        return true;
    }

    public byte[] engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet,String inclusiveNamespaces)
            throws CanonicalizationException{
        /** $todo$ well, should we throw UnsupportedOperationException ? */
        throw new CanonicalizationException("c14n.Canonicalizer.UnsupportedOperation");
    }

    public byte[] engineCanonicalizeSubTree(Node rootNode,String inclusiveNamespaces)
            throws CanonicalizationException{
        /** $todo$ well, should we throw UnsupportedOperationException ? */
        throw new CanonicalizationException("c14n.Canonicalizer.UnsupportedOperation");
    }
}
