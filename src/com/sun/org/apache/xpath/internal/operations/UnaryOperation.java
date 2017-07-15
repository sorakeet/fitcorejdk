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
 * $Id: UnaryOperation.java,v 1.2.4.1 2005/09/14 21:31:44 jeffsuttor Exp $
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
 * $Id: UnaryOperation.java,v 1.2.4.1 2005/09/14 21:31:44 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.operations;

import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.objects.XObject;

public abstract class UnaryOperation extends Expression implements ExpressionOwner{
    static final long serialVersionUID=6536083808424286166L;
    protected Expression m_right;

    public boolean canTraverseOutsideSubtree(){
        if(null!=m_right&&m_right.canTraverseOutsideSubtree())
            return true;
        return false;
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        return operate(m_right.execute(xctxt));
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        m_right.fixupVariables(vars,globalsSize);
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        if(!m_right.deepEquals(((UnaryOperation)expr).m_right))
            return false;
        return true;
    }

    public abstract XObject operate(XObject right)
            throws javax.xml.transform.TransformerException;

    public void setRight(Expression r){
        m_right=r;
        r.exprSetParent(this);
    }

    public Expression getOperand(){
        return m_right;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitUnaryOperation(owner,this)){
            m_right.callVisitors(this,visitor);
        }
    }

    public Expression getExpression(){
        return m_right;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_right=exp;
    }
}
