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
 * $Id: Operation.java,v 1.2.4.1 2005/09/14 21:31:42 jeffsuttor Exp $
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
 * $Id: Operation.java,v 1.2.4.1 2005/09/14 21:31:42 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.operations;

import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class Operation extends Expression implements ExpressionOwner{
    static final long serialVersionUID=-3037139537171050430L;
    protected Expression m_left;
    protected Expression m_right;

    public boolean canTraverseOutsideSubtree(){
        if(null!=m_left&&m_left.canTraverseOutsideSubtree())
            return true;
        if(null!=m_right&&m_right.canTraverseOutsideSubtree())
            return true;
        return false;
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        XObject left=m_left.execute(xctxt,true);
        XObject right=m_right.execute(xctxt,true);
        XObject result=operate(left,right);
        left.detach();
        right.detach();
        return result;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        m_left.fixupVariables(vars,globalsSize);
        m_right.fixupVariables(vars,globalsSize);
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        if(!m_left.deepEquals(((Operation)expr).m_left))
            return false;
        if(!m_right.deepEquals(((Operation)expr).m_right))
            return false;
        return true;
    }

    public XObject operate(XObject left,XObject right)
            throws javax.xml.transform.TransformerException{
        return null;  // no-op
    }

    public void setLeftRight(Expression l,Expression r){
        m_left=l;
        m_right=r;
        l.exprSetParent(this);
        r.exprSetParent(this);
    }

    public Expression getLeftOperand(){
        return m_left;
    }

    public Expression getRightOperand(){
        return m_right;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitBinaryOperation(owner,this)){
            m_left.callVisitors(new LeftExprOwner(),visitor);
            m_right.callVisitors(this,visitor);
        }
    }

    class LeftExprOwner implements ExpressionOwner{
        public Expression getExpression(){
            return m_left;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(Operation.this);
            m_left=exp;
        }
    }    public Expression getExpression(){
        return m_right;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_right=exp;
    }


}
