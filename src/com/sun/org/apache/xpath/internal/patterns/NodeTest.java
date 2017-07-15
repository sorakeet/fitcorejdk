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
 * $Id: NodeTest.java,v 1.2.4.2 2005/09/15 00:21:14 jeffsuttor Exp $
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
 * $Id: NodeTest.java,v 1.2.4.2 2005/09/15 00:21:14 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.patterns;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xpath.internal.*;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class NodeTest extends Expression{
    public static final String WILD="*";
    public static final String SUPPORTS_PRE_STRIPPING=
            "http://xml.apache.org/xpath/features/whitespace-pre-stripping";
    public static final int SHOW_BYFUNCTION=0x00010000;
    public static final XNumber SCORE_NODETEST=
            new XNumber(XPath.MATCH_SCORE_NODETEST);
    public static final XNumber SCORE_NSWILD=
            new XNumber(XPath.MATCH_SCORE_NSWILD);
    public static final XNumber SCORE_QNAME=
            new XNumber(XPath.MATCH_SCORE_QNAME);
    public static final XNumber SCORE_OTHER=
            new XNumber(XPath.MATCH_SCORE_OTHER);
    public static final XNumber SCORE_NONE=
            new XNumber(XPath.MATCH_SCORE_NONE);
    static final long serialVersionUID=-5736721866747906182L;
    protected int m_whatToShow;
    protected String m_name;
    String m_namespace;
    XNumber m_score;
    private boolean m_isTotallyWild;
    public NodeTest(int whatToShow,String namespace,String name){
        initNodeTest(whatToShow,namespace,name);
    }

    public void initNodeTest(int whatToShow,String namespace,String name){
        m_whatToShow=whatToShow;
        m_namespace=namespace;
        m_name=name;
        calcScore();
    }

    protected void calcScore(){
        if((m_namespace==null)&&(m_name==null))
            m_score=SCORE_NODETEST;
        else if(((m_namespace==WILD)||(m_namespace==null))
                &&(m_name==WILD))
            m_score=SCORE_NODETEST;
        else if((m_namespace!=WILD)&&(m_name==WILD))
            m_score=SCORE_NSWILD;
        else
            m_score=SCORE_QNAME;
        m_isTotallyWild=(m_namespace==null&&m_name==WILD);
    }
    public NodeTest(int whatToShow){
        initNodeTest(whatToShow);
    }

    public void initNodeTest(int whatToShow){
        m_whatToShow=whatToShow;
        calcScore();
    }

    public NodeTest(){
    }

    public static int getNodeTypeTest(int whatToShow){
        // %REVIEW% Is there a better way?
        if(0!=(whatToShow&DTMFilter.SHOW_ELEMENT))
            return DTM.ELEMENT_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_ATTRIBUTE))
            return DTM.ATTRIBUTE_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_TEXT))
            return DTM.TEXT_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT))
            return DTM.DOCUMENT_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT_FRAGMENT))
            return DTM.DOCUMENT_FRAGMENT_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_NAMESPACE))
            return DTM.NAMESPACE_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_COMMENT))
            return DTM.COMMENT_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_PROCESSING_INSTRUCTION))
            return DTM.PROCESSING_INSTRUCTION_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT_TYPE))
            return DTM.DOCUMENT_TYPE_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_ENTITY))
            return DTM.ENTITY_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_ENTITY_REFERENCE))
            return DTM.ENTITY_REFERENCE_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_NOTATION))
            return DTM.NOTATION_NODE;
        if(0!=(whatToShow&DTMFilter.SHOW_CDATA_SECTION))
            return DTM.CDATA_SECTION_NODE;
        return 0;
    }

    public static void debugWhatToShow(int whatToShow){
        java.util.Vector v=new java.util.Vector();
        if(0!=(whatToShow&DTMFilter.SHOW_ATTRIBUTE))
            v.addElement("SHOW_ATTRIBUTE");
        if(0!=(whatToShow&DTMFilter.SHOW_NAMESPACE))
            v.addElement("SHOW_NAMESPACE");
        if(0!=(whatToShow&DTMFilter.SHOW_CDATA_SECTION))
            v.addElement("SHOW_CDATA_SECTION");
        if(0!=(whatToShow&DTMFilter.SHOW_COMMENT))
            v.addElement("SHOW_COMMENT");
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT))
            v.addElement("SHOW_DOCUMENT");
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT_FRAGMENT))
            v.addElement("SHOW_DOCUMENT_FRAGMENT");
        if(0!=(whatToShow&DTMFilter.SHOW_DOCUMENT_TYPE))
            v.addElement("SHOW_DOCUMENT_TYPE");
        if(0!=(whatToShow&DTMFilter.SHOW_ELEMENT))
            v.addElement("SHOW_ELEMENT");
        if(0!=(whatToShow&DTMFilter.SHOW_ENTITY))
            v.addElement("SHOW_ENTITY");
        if(0!=(whatToShow&DTMFilter.SHOW_ENTITY_REFERENCE))
            v.addElement("SHOW_ENTITY_REFERENCE");
        if(0!=(whatToShow&DTMFilter.SHOW_NOTATION))
            v.addElement("SHOW_NOTATION");
        if(0!=(whatToShow&DTMFilter.SHOW_PROCESSING_INSTRUCTION))
            v.addElement("SHOW_PROCESSING_INSTRUCTION");
        if(0!=(whatToShow&DTMFilter.SHOW_TEXT))
            v.addElement("SHOW_TEXT");
        int n=v.size();
        for(int i=0;i<n;i++){
            if(i>0)
                System.out.print(" | ");
            System.out.print(v.elementAt(i));
        }
        if(0==n)
            System.out.print("empty whatToShow: "+whatToShow);
        System.out.println();
    }    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        NodeTest nt=(NodeTest)expr;
        if(null!=nt.m_name){
            if(null==m_name)
                return false;
            else if(!nt.m_name.equals(m_name))
                return false;
        }else if(null!=m_name)
            return false;
        if(null!=nt.m_namespace){
            if(null==m_namespace)
                return false;
            else if(!nt.m_namespace.equals(m_namespace))
                return false;
        }else if(null!=m_namespace)
            return false;
        if(m_whatToShow!=nt.m_whatToShow)
            return false;
        if(m_isTotallyWild!=nt.m_isTotallyWild)
            return false;
        return true;
    }

    public int getWhatToShow(){
        return m_whatToShow;
    }

    public void setWhatToShow(int what){
        m_whatToShow=what;
    }

    public String getNamespace(){
        return m_namespace;
    }

    public void setNamespace(String ns){
        m_namespace=ns;
    }

    public String getLocalName(){
        return (null==m_name)?"":m_name;
    }

    public void setLocalName(String name){
        m_name=name;
    }

    public XNumber getStaticScore(){
        return m_score;
    }

    public void setStaticScore(XNumber score){
        m_score=score;
    }

    public double getDefaultScore(){
        return m_score.num();
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        assertion(false,"callVisitors should not be called for this object!!!");
    }

    private static final boolean subPartMatch(String p,String t){
        // boolean b = (p == t) || ((null != p) && ((t == WILD) || p.equals(t)));
        // System.out.println("subPartMatch - p: "+p+", t: "+t+", result: "+b);
        return (p==t)||((null!=p)&&((t==WILD)||p.equals(t)));
    }

    private static final boolean subPartMatchNS(String p,String t){
        return (p==t)
                ||((null!=p)
                &&((p.length()>0)
                ?((t==WILD)||p.equals(t)):null==t));
    }

    public XObject execute(XPathContext xctxt,int context)
            throws javax.xml.transform.TransformerException{
        DTM dtm=xctxt.getDTM(context);
        short nodeType=dtm.getNodeType(context);
        if(m_whatToShow==DTMFilter.SHOW_ALL)
            return m_score;
        int nodeBit=(m_whatToShow&(0x00000001<<(nodeType-1)));
        switch(nodeBit){
            case DTMFilter.SHOW_DOCUMENT_FRAGMENT:
            case DTMFilter.SHOW_DOCUMENT:
                return SCORE_OTHER;
            case DTMFilter.SHOW_COMMENT:
                return m_score;
            case DTMFilter.SHOW_CDATA_SECTION:
            case DTMFilter.SHOW_TEXT:
                // was:
                // return (!xctxt.getDOMHelper().shouldStripSourceNode(context))
                //       ? m_score : SCORE_NONE;
                return m_score;
            case DTMFilter.SHOW_PROCESSING_INSTRUCTION:
                return subPartMatch(dtm.getNodeName(context),m_name)
                        ?m_score:SCORE_NONE;
            // From the draft: "Two expanded names are equal if they
            // have the same local part, and either both have no URI or
            // both have the same URI."
            // "A node test * is true for any node of the principal node type.
            // For example, child::* will select all element children of the
            // context node, and attribute::* will select all attributes of
            // the context node."
            // "A node test can have the form NCName:*. In this case, the prefix
            // is expanded in the same way as with a QName using the context
            // namespace declarations. The node test will be true for any node
            // of the principal type whose expanded name has the URI to which
            // the prefix expands, regardless of the local part of the name."
            case DTMFilter.SHOW_NAMESPACE:{
                String ns=dtm.getLocalName(context);
                return (subPartMatch(ns,m_name))?m_score:SCORE_NONE;
            }
            case DTMFilter.SHOW_ATTRIBUTE:
            case DTMFilter.SHOW_ELEMENT:{
                return (m_isTotallyWild||(subPartMatchNS(dtm.getNamespaceURI(context),m_namespace)&&subPartMatch(dtm.getLocalName(context),m_name)))
                        ?m_score:SCORE_NONE;
            }
            default:
                return SCORE_NONE;
        }  // end switch(testType)
    }

    public XObject execute(XPathContext xctxt,int context,
                           DTM dtm,int expType)
            throws javax.xml.transform.TransformerException{
        if(m_whatToShow==DTMFilter.SHOW_ALL)
            return m_score;
        int nodeBit=(m_whatToShow&(0x00000001
                <<((dtm.getNodeType(context))-1)));
        switch(nodeBit){
            case DTMFilter.SHOW_DOCUMENT_FRAGMENT:
            case DTMFilter.SHOW_DOCUMENT:
                return SCORE_OTHER;
            case DTMFilter.SHOW_COMMENT:
                return m_score;
            case DTMFilter.SHOW_CDATA_SECTION:
            case DTMFilter.SHOW_TEXT:
                // was:
                // return (!xctxt.getDOMHelper().shouldStripSourceNode(context))
                //       ? m_score : SCORE_NONE;
                return m_score;
            case DTMFilter.SHOW_PROCESSING_INSTRUCTION:
                return subPartMatch(dtm.getNodeName(context),m_name)
                        ?m_score:SCORE_NONE;
            // From the draft: "Two expanded names are equal if they
            // have the same local part, and either both have no URI or
            // both have the same URI."
            // "A node test * is true for any node of the principal node type.
            // For example, child::* will select all element children of the
            // context node, and attribute::* will select all attributes of
            // the context node."
            // "A node test can have the form NCName:*. In this case, the prefix
            // is expanded in the same way as with a QName using the context
            // namespace declarations. The node test will be true for any node
            // of the principal type whose expanded name has the URI to which
            // the prefix expands, regardless of the local part of the name."
            case DTMFilter.SHOW_NAMESPACE:{
                String ns=dtm.getLocalName(context);
                return (subPartMatch(ns,m_name))?m_score:SCORE_NONE;
            }
            case DTMFilter.SHOW_ATTRIBUTE:
            case DTMFilter.SHOW_ELEMENT:{
                return (m_isTotallyWild||(subPartMatchNS(dtm.getNamespaceURI(context),m_namespace)&&subPartMatch(dtm.getLocalName(context),m_name)))
                        ?m_score:SCORE_NONE;
            }
            default:
                return SCORE_NONE;
        }  // end switch(testType)
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return execute(xctxt,xctxt.getCurrentNode());
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        // no-op
    }


}
