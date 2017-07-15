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
 * $Id: FunctionPattern.java,v 1.2.4.2 2005/09/15 00:21:15 jeffsuttor Exp $
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
 * $Id: FunctionPattern.java,v 1.2.4.2 2005/09/15 00:21:15 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.patterns;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class FunctionPattern extends StepPattern{
    static final long serialVersionUID=-5426793413091209944L;
    Expression m_functionExpr;

    public FunctionPattern(Expression expr,int axis,int predaxis){
        super(0,null,null,axis,predaxis);
        m_functionExpr=expr;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        m_functionExpr.fixupVariables(vars,globalsSize);
    }

    public final void calcScore(){
        m_score=SCORE_OTHER;
        if(null==m_targetString)
            calcTargetString();
    }

    public XObject execute(XPathContext xctxt,int context)
            throws javax.xml.transform.TransformerException{
        DTMIterator nl=m_functionExpr.asIterator(xctxt,context);
        XNumber score=SCORE_NONE;
        if(null!=nl){
            int n;
            while(DTM.NULL!=(n=nl.nextNode())){
                score=(n==context)?SCORE_OTHER:SCORE_NONE;
                if(score==SCORE_OTHER){
                    context=n;
                    break;
                }
            }
            // nl.detach();
        }
        nl.detach();
        return score;
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        int context=xctxt.getCurrentNode();
        DTMIterator nl=m_functionExpr.asIterator(xctxt,context);
        XNumber score=SCORE_NONE;
        if(null!=nl){
            int n;
            while(DTM.NULL!=(n=nl.nextNode())){
                score=(n==context)?SCORE_OTHER:SCORE_NONE;
                if(score==SCORE_OTHER){
                    context=n;
                    break;
                }
            }
            nl.detach();
        }
        return score;
    }

    public XObject execute(XPathContext xctxt,int context,
                           DTM dtm,int expType)
            throws javax.xml.transform.TransformerException{
        DTMIterator nl=m_functionExpr.asIterator(xctxt,context);
        XNumber score=SCORE_NONE;
        if(null!=nl){
            int n;
            while(DTM.NULL!=(n=nl.nextNode())){
                score=(n==context)?SCORE_OTHER:SCORE_NONE;
                if(score==SCORE_OTHER){
                    context=n;
                    break;
                }
            }
            nl.detach();
        }
        return score;
    }

    protected void callSubtreeVisitors(XPathVisitor visitor){
        m_functionExpr.callVisitors(new FunctionOwner(),visitor);
        super.callSubtreeVisitors(visitor);
    }

    class FunctionOwner implements ExpressionOwner{
        public Expression getExpression(){
            return m_functionExpr;
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(FunctionPattern.this);
            m_functionExpr=exp;
        }
    }
}
