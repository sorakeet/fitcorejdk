/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: DOM2Helper.java,v 1.2.4.1 2005/09/15 08:15:37 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: DOM2Helper.java,v 1.2.4.1 2005/09/15 08:15:37 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class DOM2Helper extends DOMHelper{
    private Document m_doc;

    public DOM2Helper(){
    }

    public static boolean isNodeAfter(Node node1,Node node2){
        // Assume first that the nodes are DTM nodes, since discovering node
        // order is massivly faster for the DTM.
        if(node1 instanceof DOMOrder&&node2 instanceof DOMOrder){
            int index1=((DOMOrder)node1).getUid();
            int index2=((DOMOrder)node2).getUid();
            return index1<=index2;
        }else{
            // isNodeAfter will return true if node is after countedNode
            // in document order. The base isNodeAfter is sloooow (relatively).
            return DOMHelper.isNodeAfter(node1,node2);
        }
    }

    public static Node getParentOfNode(Node node){
        Node parent=node.getParentNode();
        if(parent==null&&(Node.ATTRIBUTE_NODE==node.getNodeType()))
            parent=((Attr)node).getOwnerElement();
        return parent;
    }

    public void checkNode(Node node) throws TransformerException{
        // if(!(node instanceof com.sun.org.apache.xerces.internal.dom.NodeImpl))
        //  throw new TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XERCES_CANNOT_HANDLE_NODES, new Object[]{((Object)node).getClass()})); //"DOM2Helper can not handle nodes of type"
        //+((Object)node).getClass());
    }

    public boolean supportsSAX(){
        return true;
    }

    public Document getDocument(){
        return m_doc;
    }

    public void setDocument(Document doc){
        m_doc=doc;
    }

    public void parse(InputSource source) throws TransformerException{
        try{
            // I guess I should use JAXP factory here... when it's legal.
            // com.sun.org.apache.xerces.internal.parsers.DOMParser parser
            //  = new com.sun.org.apache.xerces.internal.parsers.DOMParser();
            DocumentBuilderFactory builderFactory=
                    DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(true);
            DocumentBuilder parser=builderFactory.newDocumentBuilder();
            /**
             // domParser.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", getShouldExpandEntityRefs()? false : true);
             if(m_useDOM2getNamespaceURI)
             {
             parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", true);
             parser.setFeature("http://xml.org/sax/features/namespaces", true);
             }
             else
             {
             parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
             }

             parser.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
             */
            parser.setErrorHandler(
                    new DefaultErrorHandler());
            // if(null != m_entityResolver)
            // {
            // System.out.println("Setting the entity resolver.");
            //  parser.setEntityResolver(m_entityResolver);
            // }
            setDocument(parser.parse(source));
        }catch(org.xml.sax.SAXException se){
            throw new TransformerException(se);
        }catch(ParserConfigurationException pce){
            throw new TransformerException(pce);
        }catch(IOException ioe){
            throw new TransformerException(ioe);
        }
        // setDocument(((com.sun.org.apache.xerces.internal.parsers.DOMParser)parser).getDocument());
    }

    public String getNamespaceOfNode(Node n){
        return n.getNamespaceURI();
    }

    public String getLocalNameOfNode(Node n){
        String name=n.getLocalName();
        return (null==name)?super.getLocalNameOfNode(n):name;
    }

    public Element getElementByID(String id,Document doc){
        return doc.getElementById(id);
    }
    /** Field m_useDOM2getNamespaceURI is a compile-time flag which
     *  gates some of the parser options used to build a DOM -- but
     * that code is commented out at this time and nobody else
     * references it, so I've commented this out as well. */
    //private boolean m_useDOM2getNamespaceURI = false;
}
