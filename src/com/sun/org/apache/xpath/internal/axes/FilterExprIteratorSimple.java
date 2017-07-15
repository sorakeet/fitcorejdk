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
 * $Id: FilterExprIteratorSimple.java,v 1.2.4.2 2005/09/14 19:45:21 jeffsuttor Exp $
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
 * $Id: FilterExprIteratorSimple.java,v 1.2.4.2 2005/09/14 19:45:21 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.*;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;

public class FilterExprIteratorSimple extends LocPathIterator{
    static final long serialVersionUID=-6978977187025375579L;
    private Expression m_expr;
    transient private XNodeSet m_exprObj;
    private boolean m_mustHardReset=false;
    private boolean m_canDetachNodeset=true;

    public FilterExprIteratorSimple(){
        super(null);
    }

    public FilterExprIteratorSimple(Expression expr){
        super(null);
        m_expr=expr;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        m_expr.fixupVariables(vars,globalsSize);
    }

    public void callPredicateVisitors(XPathVisitor visitor){
        m_expr.callVisitors(new filterExprOwner(),visitor);
        super.callPredicateVisitors(visitor);
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        FilterExprIteratorSimple fet=(FilterExprIteratorSimple)expr;
        if(!m_expr.deepEquals(fet.m_expr))
            return false;
        return true;
    }

    public Expression getInnerExpression(){
        return m_expr;
    }

    public void setInnerExpression(Expression expr){
        expr.exprSetParent(this);
        m_expr=expr;
    }

    public int getAnalysisBits(){
        if(null!=m_expr&&m_expr instanceof PathComponent){
            return ((PathComponent)m_expr).getAnalysisBits();
        }
        return WalkerFactory.BIT_FILTER;
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        m_exprObj=executeFilterExpr(context,m_execContext,getPrefixResolver(),
                getIsTopLevel(),m_stackFrame,m_expr);
    }

    public static XNodeSet executeFilterExpr(int context,XPathContext xctxt,
                                             PrefixResolver prefixResolver,
                                             boolean isTopLevel,
                                             int stackFrame,
                                             Expression expr)
            throws com.sun.org.apache.xml.internal.utils.WrappedRuntimeException{
        PrefixResolver savedResolver=xctxt.getNamespaceContext();
        XNodeSet result=null;
        try{
            xctxt.pushCurrentNode(context);
            xctxt.setNamespaceContext(prefixResolver);
            // The setRoot operation can take place with a reset operation,
            // and so we may not be in the context of LocPathIterator#nextNode,
            // so we have to set up the variable context, execute the expression,
            // and then restore the variable context.
            if(isTopLevel){
                // System.out.println("calling m_expr.execute(getXPathContext())");
                VariableStack vars=xctxt.getVarStack();
                // These three statements need to be combined into one operation.
                int savedStart=vars.getStackFrame();
                vars.setStackFrame(stackFrame);
                result=(XNodeSet)expr.execute(xctxt);
                result.setShouldCacheNodes(true);
                // These two statements need to be combined into one operation.
                vars.setStackFrame(savedStart);
            }else
                result=(XNodeSet)expr.execute(xctxt);
        }catch(javax.xml.transform.TransformerException se){
            // TODO: Fix...
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(se);
        }finally{
            xctxt.popCurrentNode();
            xctxt.setNamespaceContext(savedResolver);
        }
        return result;
    }

    public void detach(){
        if(m_allowDetach){
            super.detach();
            m_exprObj.detach();
            m_exprObj=null;
        }
    }

    public int nextNode(){
        if(m_foundLast)
            return DTM.NULL;
        int next;
        if(null!=m_exprObj){
            m_lastFetched=next=m_exprObj.nextNode();
        }else
            m_lastFetched=next=DTM.NULL;
        // m_lastFetched = next;
        if(DTM.NULL!=next){
            m_pos++;
            return next;
        }else{
            m_foundLast=true;
            return DTM.NULL;
        }
    }

    public boolean isDocOrdered(){
        return m_exprObj.isDocOrdered();
    }

    public int getAxis(){
        if(null!=m_exprObj)
            return m_exprObj.getAxis();
        else
            return Axis.FILTEREDLIST;
    }

    class filterExprOwner implements ExpressionOwner{
        public Expression getExpression(){
            return m_expr;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FilterExprIteratorSimple.this);
            m_expr=exp;
        }
    }
}
