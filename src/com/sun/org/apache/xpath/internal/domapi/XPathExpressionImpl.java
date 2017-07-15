/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * $Id: XPathExpressionImpl.java,v 1.2.4.1 2005/09/10 04:06:55 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * $Id: XPathExpressionImpl.java,v 1.2.4.1 2005/09/10 04:06:55 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.domapi;

import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import com.sun.org.apache.xpath.internal.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNamespace;

import javax.xml.transform.TransformerException;

class XPathExpressionImpl implements XPathExpression{
    final private XPath m_xpath;
    final private Document m_doc;

    XPathExpressionImpl(XPath xpath,Document doc){
        m_xpath=xpath;
        m_doc=doc;
    }

    public Object evaluate(
            Node contextNode,
            short type,
            Object result)
            throws XPathException, DOMException{
        // If the XPathEvaluator was determined by "casting" the document
        if(m_doc!=null){
            // Check that the context node is owned by the same document
            if((contextNode!=m_doc)&&(!contextNode.getOwnerDocument().equals(m_doc))){
                String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_WRONG_DOCUMENT,null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,fmsg);
            }
            // Check that the context node is an acceptable node type
            short nodeType=contextNode.getNodeType();
            if((nodeType!=Document.DOCUMENT_NODE)&&
                    (nodeType!=Document.ELEMENT_NODE)&&
                    (nodeType!=Document.ATTRIBUTE_NODE)&&
                    (nodeType!=Document.TEXT_NODE)&&
                    (nodeType!=Document.CDATA_SECTION_NODE)&&
                    (nodeType!=Document.COMMENT_NODE)&&
                    (nodeType!=Document.PROCESSING_INSTRUCTION_NODE)&&
                    (nodeType!=XPathNamespace.XPATH_NAMESPACE_NODE)){
                String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_WRONG_NODETYPE,null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,fmsg);
            }
        }
        //
        // If the type is not a supported type, throw an exception and be
        // done with it!
        if(!XPathResultImpl.isValidType(type)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INVALID_XPATH_TYPE,new Object[]{new Integer(type)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg); // Invalid XPath type argument: {0}
        }
        // Cache xpath context?
        XPathContext xpathSupport=new XPathContext();
        // if m_document is not null, build the DTM from the document
        if(null!=m_doc){
            xpathSupport.getDTMHandleFromNode(m_doc);
        }
        XObject xobj=null;
        try{
            xobj=m_xpath.execute(xpathSupport,contextNode,null);
        }catch(TransformerException te){
            // What should we do here?
            throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,te.getMessageAndLocation());
        }
        // Create a new XPathResult object
        // Reuse result object passed in?
        // The constructor will check the compatibility of type and xobj and
        // throw an exception if they are not compatible.
        return new XPathResultImpl(type,xobj,contextNode,m_xpath);
    }
}
