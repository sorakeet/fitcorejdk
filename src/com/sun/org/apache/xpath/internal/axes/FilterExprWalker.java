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
 * $Id: FilterExprWalker.java,v 1.2.4.2 2005/09/14 19:45:23 jeffsuttor Exp $
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
 * $Id: FilterExprWalker.java,v 1.2.4.2 2005/09/14 19:45:23 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpCodes;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;

public class FilterExprWalker extends AxesWalker{
    static final long serialVersionUID=5457182471424488375L;
    private Expression m_expr;
    transient private XNodeSet m_exprObj;
    private boolean m_mustHardReset=false;
    private boolean m_canDetachNodeset=true;

    public FilterExprWalker(WalkingIterator locPathIterator){
        super(locPathIterator,Axis.FILTEREDLIST);
    }

    public void init(Compiler compiler,int opPos,int stepType)
            throws javax.xml.transform.TransformerException{
        super.init(compiler,opPos,stepType);
        // Smooth over an anomily in the opcode map...
        switch(stepType){
            case OpCodes.OP_FUNCTION:
            case OpCodes.OP_EXTFUNCTION:
                m_mustHardReset=true;
            case OpCodes.OP_GROUP:
            case OpCodes.OP_VARIABLE:
                m_expr=compiler.compile(opPos);
                m_expr.exprSetParent(this);
                //if((OpCodes.OP_FUNCTION == stepType) && (m_expr instanceof com.sun.org.apache.xalan.internal.templates.FuncKey))
                if(m_expr instanceof com.sun.org.apache.xpath.internal.operations.Variable){
                    // hack/temp workaround
                    m_canDetachNodeset=false;
                }
                break;
            default:
                m_expr=compiler.compile(opPos+2);
                m_expr.exprSetParent(this);
        }
//    if(m_expr instanceof WalkingIterator)
//    {
//      WalkingIterator wi = (WalkingIterator)m_expr;
//      if(wi.getFirstWalker() instanceof FilterExprWalker)
//      {
//              FilterExprWalker fw = (FilterExprWalker)wi.getFirstWalker();
//              if(null == fw.getNextWalker())
//              {
//                      m_expr = fw.m_expr;
//                      m_expr.exprSetParent(this);
//              }
//      }
//
//    }
    }

    public Object clone() throws CloneNotSupportedException{
        FilterExprWalker clone=(FilterExprWalker)super.clone();
        // clone.m_expr = (Expression)((Expression)m_expr).clone();
        if(null!=m_exprObj)
            clone.m_exprObj=(XNodeSet)m_exprObj.clone();
        return clone;
    }

    public void detach(){
        super.detach();
        if(m_canDetachNodeset){
            m_exprObj.detach();
        }
        m_exprObj=null;
    }

    public int getAnalysisBits(){
        if(null!=m_expr&&m_expr instanceof PathComponent){
            return ((PathComponent)m_expr).getAnalysisBits();
        }
        return WalkerFactory.BIT_FILTER;
    }

    public void setRoot(int root){
        super.setRoot(root);
        m_exprObj=FilterExprIteratorSimple.executeFilterExpr(root,
                m_lpi.getXPathContext(),m_lpi.getPrefixResolver(),
                m_lpi.getIsTopLevel(),m_lpi.m_stackFrame,m_expr);
    }

    public int getNextNode(){
        if(null!=m_exprObj){
            int next=m_exprObj.nextNode();
            return next;
        }else
            return DTM.NULL;
    }

    public int getLastPos(XPathContext xctxt){
        return m_exprObj.getLength();
    }

    public boolean isDocOrdered(){
        return m_exprObj.isDocOrdered();
    }

    public int getAxis(){
        return m_exprObj.getAxis();
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        FilterExprWalker walker=(FilterExprWalker)expr;
        if(!m_expr.deepEquals(walker.m_expr))
            return false;
        return true;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        m_expr.fixupVariables(vars,globalsSize);
    }

    public short acceptNode(int n){
        try{
            if(getPredicateCount()>0){
                countProximityPosition(0);
                if(!executePredicates(n,m_lpi.getXPathContext()))
                    return DTMIterator.FILTER_SKIP;
            }
            return DTMIterator.FILTER_ACCEPT;
        }catch(javax.xml.transform.TransformerException se){
            throw new RuntimeException(se.getMessage());
        }
    }

    public void callPredicateVisitors(XPathVisitor visitor){
        m_expr.callVisitors(new filterExprOwner(),visitor);
        super.callPredicateVisitors(visitor);
    }

    public Expression getInnerExpression(){
        return m_expr;
    }

    public void setInnerExpression(Expression expr){
        expr.exprSetParent(this);
        m_expr=expr;
    }

    class filterExprOwner implements ExpressionOwner{
        public Expression getExpression(){
            return m_expr;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FilterExprWalker.this);
            m_expr=exp;
        }
    }
}
