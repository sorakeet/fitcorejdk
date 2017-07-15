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
 * $Id: Expression.java,v 1.2.4.2 2005/09/14 19:50:20 jeffsuttor Exp $
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
 * $Id: Expression.java,v 1.2.4.2 2005/09/14 19:50:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.xml.sax.ContentHandler;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public abstract class Expression implements java.io.Serializable, ExpressionNode, XPathVisitable{
    static final long serialVersionUID=565665869777906902L;
    private ExpressionNode m_parent;

    public boolean canTraverseOutsideSubtree(){
        return false;
    }
//  /**
//   * Set the location where this expression was built from.
//   *
//   *
//   * @param locator the location where this expression was built from, may be
//   *                null.
//   */
//  public void setSourceLocator(SourceLocator locator)
//  {
//    m_slocator = locator;
//  }

    public XObject execute(XPathContext xctxt,int currentNode)
            throws TransformerException{
        // For now, the current node is already pushed.
        return execute(xctxt);
    }

    public abstract XObject execute(XPathContext xctxt)
            throws TransformerException;

    public XObject execute(
            XPathContext xctxt,int currentNode,DTM dtm,int expType)
            throws TransformerException{
        // For now, the current node is already pushed.
        return execute(xctxt);
    }

    public XObject execute(XPathContext xctxt,boolean destructiveOK)
            throws TransformerException{
        return execute(xctxt);
    }

    public double num(XPathContext xctxt)
            throws TransformerException{
        return execute(xctxt).num();
    }

    public boolean bool(XPathContext xctxt)
            throws TransformerException{
        return execute(xctxt).bool();
    }

    public XMLString xstr(XPathContext xctxt)
            throws TransformerException{
        return execute(xctxt).xstr();
    }

    public boolean isNodesetExpr(){
        return false;
    }

    public int asNode(XPathContext xctxt)
            throws TransformerException{
        DTMIterator iter=execute(xctxt).iter();
        return iter.nextNode();
    }

    public DTMIterator asIterator(XPathContext xctxt,int contextNode)
            throws TransformerException{
        try{
            xctxt.pushCurrentNodeAndExpression(contextNode,contextNode);
            return execute(xctxt).iter();
        }finally{
            xctxt.popCurrentNodeAndExpression();
        }
    }

    public DTMIterator asIteratorRaw(XPathContext xctxt,int contextNode)
            throws TransformerException{
        try{
            xctxt.pushCurrentNodeAndExpression(contextNode,contextNode);
            XNodeSet nodeset=(XNodeSet)execute(xctxt);
            return nodeset.iterRaw();
        }finally{
            xctxt.popCurrentNodeAndExpression();
        }
    }

    public void executeCharsToContentHandler(
            XPathContext xctxt,ContentHandler handler)
            throws TransformerException,
            org.xml.sax.SAXException{
        XObject obj=execute(xctxt);
        obj.dispatchCharactersEvents(handler);
        obj.detach();
    }

    public boolean isStableNumber(){
        return false;
    }

    public abstract void fixupVariables(java.util.Vector vars,int globalsSize);

    public abstract boolean deepEquals(Expression expr);

    protected final boolean isSameClass(Expression expr){
        if(null==expr)
            return false;
        return (getClass()==expr.getClass());
    }

    public void warn(XPathContext xctxt,String msg,Object[] args)
            throws TransformerException{
        String fmsg=XSLMessages.createXPATHWarning(msg,args);
        if(null!=xctxt){
            ErrorListener eh=xctxt.getErrorListener();
            // TO DO: Need to get stylesheet Locator from here.
            eh.warning(new TransformerException(fmsg,xctxt.getSAXLocator()));
        }
    }

    public void error(XPathContext xctxt,String msg,Object[] args)
            throws TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        if(null!=xctxt){
            ErrorListener eh=xctxt.getErrorListener();
            TransformerException te=new TransformerException(fmsg,this);
            eh.fatalError(te);
        }
    }

    public ExpressionNode getExpressionOwner(){
        ExpressionNode parent=exprGetParent();
        while((null!=parent)&&(parent instanceof Expression))
            parent=parent.exprGetParent();
        return parent;
    }

    public void exprSetParent(ExpressionNode n){
        assertion(n!=this,"Can not parent an expression to itself!");
        m_parent=n;
    }
    //=============== ExpressionNode methods ================

    public void assertion(boolean b,String msg){
        if(!b){
            String fMsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                    new Object[]{msg});
            throw new RuntimeException(fMsg);
        }
    }

    public ExpressionNode exprGetParent(){
        return m_parent;
    }

    public void exprAddChild(ExpressionNode n,int i){
        assertion(false,"exprAddChild method not implemented!");
    }

    public ExpressionNode exprGetChild(int i){
        return null;
    }

    public int exprGetNumChildren(){
        return 0;
    }
    //=============== SourceLocator methods ================

    public String getPublicId(){
        if(null==m_parent)
            return null;
        return m_parent.getPublicId();
    }

    public String getSystemId(){
        if(null==m_parent)
            return null;
        return m_parent.getSystemId();
    }

    public int getLineNumber(){
        if(null==m_parent)
            return 0;
        return m_parent.getLineNumber();
    }

    public int getColumnNumber(){
        if(null==m_parent)
            return 0;
        return m_parent.getColumnNumber();
    }
}
