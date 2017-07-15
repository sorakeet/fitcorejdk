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
 * $Id: XPathAPI.java,v 1.2.4.1 2005/09/10 18:18:23 jeffsuttor Exp $
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
 * $Id: XPathAPI.java,v 1.2.4.1 2005/09/10 18:18:23 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.transform.TransformerException;

public class XPathAPI{
    public static Node selectSingleNode(Node contextNode,String str)
            throws TransformerException{
        return selectSingleNode(contextNode,str,contextNode);
    }

    public static Node selectSingleNode(
            Node contextNode,String str,Node namespaceNode)
            throws TransformerException{
        // Have the XObject return its result as a NodeSetDTM.
        NodeIterator nl=selectNodeIterator(contextNode,str,namespaceNode);
        // Return the first node, or null
        return nl.nextNode();
    }

    public static NodeIterator selectNodeIterator(
            Node contextNode,String str,Node namespaceNode)
            throws TransformerException{
        // Execute the XPath, and have it return the result
        XObject list=eval(contextNode,str,namespaceNode);
        // Have the XObject return its result as a NodeSetDTM.
        return list.nodeset();
    }

    public static XObject eval(Node contextNode,String str,Node namespaceNode)
            throws TransformerException{
        // Since we don't have a XML Parser involved here, install some default support
        // for things like namespaces, etc.
        // (Changed from: XPathContext xpathSupport = new XPathContext();
        //    because XPathContext is weak in a number of areas... perhaps
        //    XPathContext should be done away with.)
        XPathContext xpathSupport=new XPathContext();
        // Create an object to resolve namespace prefixes.
        // XPath namespaces are resolved from the input context node's document element
        // if it is a root node, or else the current context node (for lack of a better
        // resolution space, given the simplicity of this sample code).
        PrefixResolverDefault prefixResolver=new PrefixResolverDefault(
                (namespaceNode.getNodeType()==Node.DOCUMENT_NODE)
                        ?((Document)namespaceNode).getDocumentElement():namespaceNode);
        // Create the XPath object.
        XPath xpath=new XPath(str,null,prefixResolver,XPath.SELECT,null);
        // Execute the XPath, and have it return the result
        // return xpath.execute(xpathSupport, contextNode, prefixResolver);
        int ctxtNode=xpathSupport.getDTMHandleFromNode(contextNode);
        return xpath.execute(xpathSupport,ctxtNode,prefixResolver);
    }

    public static NodeIterator selectNodeIterator(Node contextNode,String str)
            throws TransformerException{
        return selectNodeIterator(contextNode,str,contextNode);
    }

    public static NodeList selectNodeList(Node contextNode,String str)
            throws TransformerException{
        return selectNodeList(contextNode,str,contextNode);
    }

    public static NodeList selectNodeList(
            Node contextNode,String str,Node namespaceNode)
            throws TransformerException{
        // Execute the XPath, and have it return the result
        XObject list=eval(contextNode,str,namespaceNode);
        // Return a NodeList.
        return list.nodelist();
    }

    public static XObject eval(Node contextNode,String str)
            throws TransformerException{
        return eval(contextNode,str,contextNode);
    }

    public static XObject eval(
            Node contextNode,String str,PrefixResolver prefixResolver)
            throws TransformerException{
        // Since we don't have a XML Parser involved here, install some default support
        // for things like namespaces, etc.
        // (Changed from: XPathContext xpathSupport = new XPathContext();
        //    because XPathContext is weak in a number of areas... perhaps
        //    XPathContext should be done away with.)
        // Create the XPath object.
        XPath xpath=new XPath(str,null,prefixResolver,XPath.SELECT,null);
        // Execute the XPath, and have it return the result
        XPathContext xpathSupport=new XPathContext();
        int ctxtNode=xpathSupport.getDTMHandleFromNode(contextNode);
        return xpath.execute(xpathSupport,ctxtNode,prefixResolver);
    }
}
