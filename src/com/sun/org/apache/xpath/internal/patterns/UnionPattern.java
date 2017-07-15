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
 * $Id: UnionPattern.java,v 1.2.4.1 2005/09/15 00:21:15 jeffsuttor Exp $
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
 * $Id: UnionPattern.java,v 1.2.4.1 2005/09/15 00:21:15 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.patterns;

import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class UnionPattern extends Expression{
    static final long serialVersionUID=-6670449967116905820L;
    private StepPattern[] m_patterns;

    public boolean canTraverseOutsideSubtree(){
        if(null!=m_patterns){
            int n=m_patterns.length;
            for(int i=0;i<n;i++){
                if(m_patterns[i].canTraverseOutsideSubtree())
                    return true;
            }
        }
        return false;
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        XObject bestScore=null;
        int n=m_patterns.length;
        for(int i=0;i<n;i++){
            XObject score=m_patterns[i].execute(xctxt);
            if(score!=NodeTest.SCORE_NONE){
                if(null==bestScore)
                    bestScore=score;
                else if(score.num()>bestScore.num())
                    bestScore=score;
            }
        }
        if(null==bestScore){
            bestScore=NodeTest.SCORE_NONE;
        }
        return bestScore;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        for(int i=0;i<m_patterns.length;i++){
            m_patterns[i].fixupVariables(vars,globalsSize);
        }
    }

    public boolean deepEquals(Expression expr){
        if(!isSameClass(expr))
            return false;
        UnionPattern up=(UnionPattern)expr;
        if(null!=m_patterns){
            int n=m_patterns.length;
            if((null==up.m_patterns)||(up.m_patterns.length!=n))
                return false;
            for(int i=0;i<n;i++){
                if(!m_patterns[i].deepEquals(up.m_patterns[i]))
                    return false;
            }
        }else if(up.m_patterns!=null)
            return false;
        return true;
    }

    public StepPattern[] getPatterns(){
        return m_patterns;
    }

    public void setPatterns(StepPattern[] patterns){
        m_patterns=patterns;
        if(null!=patterns){
            for(int i=0;i<patterns.length;i++){
                patterns[i].exprSetParent(this);
            }
        }
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        visitor.visitUnionPattern(owner,this);
        if(null!=m_patterns){
            int n=m_patterns.length;
            for(int i=0;i<n;i++){
                m_patterns[i].callVisitors(new UnionPathPartOwner(i),visitor);
            }
        }
    }

    class UnionPathPartOwner implements ExpressionOwner{
        int m_index;

        UnionPathPartOwner(int index){
            m_index=index;
        }

        public Expression getExpression(){
            return m_patterns[m_index];
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(UnionPattern.this);
            m_patterns[m_index]=(StepPattern)exp;
        }
    }
}
