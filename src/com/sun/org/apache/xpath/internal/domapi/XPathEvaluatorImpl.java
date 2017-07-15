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
 * $Id: XPathEvaluatorImpl.java,v 1.2.4.1 2005/09/10 04:04:07 jeffsuttor Exp $
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
 * $Id: XPathEvaluatorImpl.java,v 1.2.4.1 2005/09/10 04:04:07 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.domapi;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import com.sun.org.apache.xpath.internal.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNSResolver;

import javax.xml.transform.TransformerException;

public final class XPathEvaluatorImpl implements XPathEvaluator{
    private final Document m_doc;

    public XPathEvaluatorImpl(Document doc){
        m_doc=doc;
    }

    public XPathEvaluatorImpl(){
        m_doc=null;
    }

    private class DummyPrefixResolver implements PrefixResolver{
        DummyPrefixResolver(){
        }

        public String getNamespaceForPrefix(String prefix){
            return getNamespaceForPrefix(prefix,null);
        }

        public String getNamespaceForPrefix(String prefix,Node context){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_RESOLVER,null);
            throw new DOMException(DOMException.NAMESPACE_ERR,fmsg);   // Unable to resolve prefix with null prefix resolver.
        }

        public String getBaseIdentifier(){
            return null;
        }

        public boolean handlesNullPrefixes(){
            return false;
        }
    }

    public XPathExpression createExpression(
            String expression,
            XPathNSResolver resolver)
            throws XPathException, DOMException{
        try{
            // If the resolver is null, create a dummy prefix resolver
            XPath xpath=new XPath(expression,null,
                    ((null==resolver)?new DummyPrefixResolver():((PrefixResolver)resolver)),
                    XPath.SELECT);
            return new XPathExpressionImpl(xpath,m_doc);
        }catch(TransformerException e){
            // Need to pass back exception code DOMException.NAMESPACE_ERR also.
            // Error found in DOM Level 3 XPath Test Suite.
            if(e instanceof XPathStylesheetDOM3Exception)
                throw new DOMException(DOMException.NAMESPACE_ERR,e.getMessageAndLocation());
            else
                throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,e.getMessageAndLocation());
        }
    }

    public XPathNSResolver createNSResolver(Node nodeResolver){
        return new XPathNSResolverImpl((nodeResolver.getNodeType()==Node.DOCUMENT_NODE)
                ?((Document)nodeResolver).getDocumentElement():nodeResolver);
    }

    public Object evaluate(
            String expression,
            Node contextNode,
            XPathNSResolver resolver,
            short type,
            Object result)
            throws XPathException, DOMException{
        XPathExpression xpathExpression=createExpression(expression,resolver);
        return xpathExpression.evaluate(contextNode,type,result);
    }
}
