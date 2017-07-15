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
 * $Id: ExsltDynamic.java,v 1.1.2.1 2005/08/01 02:08:51 jeffsuttor Exp $
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
 * $Id: ExsltDynamic.java,v 1.1.2.1 2005/08/01 02:08:51 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xalan.internal.res.XSLTErrorResources;
import com.sun.org.apache.xpath.internal.NodeSet;
import com.sun.org.apache.xpath.internal.NodeSetDTM;
import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.w3c.dom.*;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

public class ExsltDynamic extends ExsltBase{
    public static final String EXSL_URI="http://exslt.org/common";

    public static double max(ExpressionContext myContext,NodeList nl,String expr)
            throws SAXNotSupportedException{
        XPathContext xctxt=null;
        if(myContext instanceof XPathContext.XPathExpressionContext)
            xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
        else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext}));
        if(expr==null||expr.length()==0)
            return Double.NaN;
        NodeSetDTM contextNodes=new NodeSetDTM(nl,xctxt);
        xctxt.pushContextNodeList(contextNodes);
        double maxValue=-Double.MAX_VALUE;
        for(int i=0;i<contextNodes.getLength();i++){
            int contextNode=contextNodes.item(i);
            xctxt.pushCurrentNode(contextNode);
            double result=0;
            try{
                XPath dynamicXPath=new XPath(expr,xctxt.getSAXLocator(),
                        xctxt.getNamespaceContext(),
                        XPath.SELECT);
                result=dynamicXPath.execute(xctxt,contextNode,xctxt.getNamespaceContext()).num();
            }catch(TransformerException e){
                xctxt.popCurrentNode();
                xctxt.popContextNodeList();
                return Double.NaN;
            }
            xctxt.popCurrentNode();
            if(result>maxValue)
                maxValue=result;
        }
        xctxt.popContextNodeList();
        return maxValue;
    }

    public static double min(ExpressionContext myContext,NodeList nl,String expr)
            throws SAXNotSupportedException{
        XPathContext xctxt=null;
        if(myContext instanceof XPathContext.XPathExpressionContext)
            xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
        else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext}));
        if(expr==null||expr.length()==0)
            return Double.NaN;
        NodeSetDTM contextNodes=new NodeSetDTM(nl,xctxt);
        xctxt.pushContextNodeList(contextNodes);
        double minValue=Double.MAX_VALUE;
        for(int i=0;i<nl.getLength();i++){
            int contextNode=contextNodes.item(i);
            xctxt.pushCurrentNode(contextNode);
            double result=0;
            try{
                XPath dynamicXPath=new XPath(expr,xctxt.getSAXLocator(),
                        xctxt.getNamespaceContext(),
                        XPath.SELECT);
                result=dynamicXPath.execute(xctxt,contextNode,xctxt.getNamespaceContext()).num();
            }catch(TransformerException e){
                xctxt.popCurrentNode();
                xctxt.popContextNodeList();
                return Double.NaN;
            }
            xctxt.popCurrentNode();
            if(result<minValue)
                minValue=result;
        }
        xctxt.popContextNodeList();
        return minValue;
    }

    public static double sum(ExpressionContext myContext,NodeList nl,String expr)
            throws SAXNotSupportedException{
        XPathContext xctxt=null;
        if(myContext instanceof XPathContext.XPathExpressionContext)
            xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
        else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext}));
        if(expr==null||expr.length()==0)
            return Double.NaN;
        NodeSetDTM contextNodes=new NodeSetDTM(nl,xctxt);
        xctxt.pushContextNodeList(contextNodes);
        double sum=0;
        for(int i=0;i<nl.getLength();i++){
            int contextNode=contextNodes.item(i);
            xctxt.pushCurrentNode(contextNode);
            double result=0;
            try{
                XPath dynamicXPath=new XPath(expr,xctxt.getSAXLocator(),
                        xctxt.getNamespaceContext(),
                        XPath.SELECT);
                result=dynamicXPath.execute(xctxt,contextNode,xctxt.getNamespaceContext()).num();
            }catch(TransformerException e){
                xctxt.popCurrentNode();
                xctxt.popContextNodeList();
                return Double.NaN;
            }
            xctxt.popCurrentNode();
            sum=sum+result;
        }
        xctxt.popContextNodeList();
        return sum;
    }

    public static NodeList map(ExpressionContext myContext,NodeList nl,String expr)
            throws SAXNotSupportedException{
        XPathContext xctxt=null;
        Document lDoc=null;
        if(myContext instanceof XPathContext.XPathExpressionContext)
            xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
        else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext}));
        if(expr==null||expr.length()==0)
            return new NodeSet();
        NodeSetDTM contextNodes=new NodeSetDTM(nl,xctxt);
        xctxt.pushContextNodeList(contextNodes);
        NodeSet resultSet=new NodeSet();
        resultSet.setShouldCacheNodes(true);
        for(int i=0;i<nl.getLength();i++){
            int contextNode=contextNodes.item(i);
            xctxt.pushCurrentNode(contextNode);
            XObject object=null;
            try{
                XPath dynamicXPath=new XPath(expr,xctxt.getSAXLocator(),
                        xctxt.getNamespaceContext(),
                        XPath.SELECT);
                object=dynamicXPath.execute(xctxt,contextNode,xctxt.getNamespaceContext());
                if(object instanceof XNodeSet){
                    NodeList nodelist=null;
                    nodelist=((XNodeSet)object).nodelist();
                    for(int k=0;k<nodelist.getLength();k++){
                        Node n=nodelist.item(k);
                        if(!resultSet.contains(n))
                            resultSet.addNode(n);
                    }
                }else{
                    if(lDoc==null){
                        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        DocumentBuilder db=dbf.newDocumentBuilder();
                        lDoc=db.newDocument();
                    }
                    Element element=null;
                    if(object instanceof XNumber)
                        element=lDoc.createElementNS(EXSL_URI,"exsl:number");
                    else if(object instanceof XBoolean)
                        element=lDoc.createElementNS(EXSL_URI,"exsl:boolean");
                    else
                        element=lDoc.createElementNS(EXSL_URI,"exsl:string");
                    Text textNode=lDoc.createTextNode(object.str());
                    element.appendChild(textNode);
                    resultSet.addNode(element);
                }
            }catch(Exception e){
                xctxt.popCurrentNode();
                xctxt.popContextNodeList();
                return new NodeSet();
            }
            xctxt.popCurrentNode();
        }
        xctxt.popContextNodeList();
        return resultSet;
    }

    public static XObject evaluate(ExpressionContext myContext,String xpathExpr)
            throws SAXNotSupportedException{
        if(myContext instanceof XPathContext.XPathExpressionContext){
            XPathContext xctxt=null;
            try{
                xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
                XPath dynamicXPath=new XPath(xpathExpr,xctxt.getSAXLocator(),
                        xctxt.getNamespaceContext(),
                        XPath.SELECT);
                return dynamicXPath.execute(xctxt,myContext.getContextNode(),
                        xctxt.getNamespaceContext());
            }catch(TransformerException e){
                return new XNodeSet(xctxt.getDTMManager());
            }
        }else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext})); //"Invalid context passed to evaluate "
    }

    public static NodeList closure(ExpressionContext myContext,NodeList nl,String expr)
            throws SAXNotSupportedException{
        XPathContext xctxt=null;
        if(myContext instanceof XPathContext.XPathExpressionContext)
            xctxt=((XPathContext.XPathExpressionContext)myContext).getXPathContext();
        else
            throw new SAXNotSupportedException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_CONTEXT_PASSED,new Object[]{myContext}));
        if(expr==null||expr.length()==0)
            return new NodeSet();
        NodeSet closureSet=new NodeSet();
        closureSet.setShouldCacheNodes(true);
        NodeList iterationList=nl;
        do{
            NodeSet iterationSet=new NodeSet();
            NodeSetDTM contextNodes=new NodeSetDTM(iterationList,xctxt);
            xctxt.pushContextNodeList(contextNodes);
            for(int i=0;i<iterationList.getLength();i++){
                int contextNode=contextNodes.item(i);
                xctxt.pushCurrentNode(contextNode);
                XObject object=null;
                try{
                    XPath dynamicXPath=new XPath(expr,xctxt.getSAXLocator(),
                            xctxt.getNamespaceContext(),
                            XPath.SELECT);
                    object=dynamicXPath.execute(xctxt,contextNode,xctxt.getNamespaceContext());
                    if(object instanceof XNodeSet){
                        NodeList nodelist=null;
                        nodelist=((XNodeSet)object).nodelist();
                        for(int k=0;k<nodelist.getLength();k++){
                            Node n=nodelist.item(k);
                            if(!iterationSet.contains(n))
                                iterationSet.addNode(n);
                        }
                    }else{
                        xctxt.popCurrentNode();
                        xctxt.popContextNodeList();
                        return new NodeSet();
                    }
                }catch(TransformerException e){
                    xctxt.popCurrentNode();
                    xctxt.popContextNodeList();
                    return new NodeSet();
                }
                xctxt.popCurrentNode();
            }
            xctxt.popContextNodeList();
            iterationList=iterationSet;
            for(int i=0;i<iterationList.getLength();i++){
                Node n=iterationList.item(i);
                if(!closureSet.contains(n))
                    closureSet.addNode(n);
            }
        }while(iterationList.getLength()>0);
        return closureSet;
    }
}
