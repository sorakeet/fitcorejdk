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
 * $Id: FilterExprIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: FilterExprIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;

public class FilterExprIterator extends BasicTestIterator{
    static final long serialVersionUID=2552176105165737614L;
    private Expression m_expr;
    transient private XNodeSet m_exprObj;
    private boolean m_mustHardReset=false;
    private boolean m_canDetachNodeset=true;

    public FilterExprIterator(){
        super(null);
    }

    public FilterExprIterator(Expression expr){
        super(null);
        m_expr=expr;
    }

    protected int getNextNode(){
        if(null!=m_exprObj){
            m_lastFetched=m_exprObj.nextNode();
        }else
            m_lastFetched=DTM.NULL;
        return m_lastFetched;
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
        FilterExprIterator fet=(FilterExprIterator)expr;
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
        m_exprObj=FilterExprIteratorSimple.executeFilterExpr(context,
                m_execContext,getPrefixResolver(),
                getIsTopLevel(),m_stackFrame,m_expr);
    }

    public void detach(){
        super.detach();
        m_exprObj.detach();
        m_exprObj=null;
    }

    public boolean isDocOrdered(){
        return m_exprObj.isDocOrdered();
    }

    class filterExprOwner implements ExpressionOwner{
        public Expression getExpression(){
            return m_expr;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FilterExprIterator.this);
            m_expr=exp;
        }
    }
}
