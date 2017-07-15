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
 * $Id: WalkingIterator.java,v 1.2.4.2 2005/09/14 19:45:19 jeffsuttor Exp $
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
 * $Id: WalkingIterator.java,v 1.2.4.2 2005/09/14 19:45:19 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.VariableStack;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpMap;

public class WalkingIterator extends LocPathIterator implements ExpressionOwner{
    static final long serialVersionUID=9110225941815665906L;
    protected AxesWalker m_lastUsedWalker;
    protected AxesWalker m_firstWalker;

    WalkingIterator(
            Compiler compiler,int opPos,int analysis,boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,shouldLoadWalkers);
        int firstStepPos=OpMap.getFirstChildPos(opPos);
        if(shouldLoadWalkers){
            m_firstWalker=WalkerFactory.loadWalkers(this,compiler,firstStepPos,0);
            m_lastUsedWalker=m_firstWalker;
        }
    }

    public WalkingIterator(PrefixResolver nscontext){
        super(nscontext);
    }

    public int getAnalysisBits(){
        int bits=0;
        if(null!=m_firstWalker){
            AxesWalker walker=m_firstWalker;
            while(null!=walker){
                int bit=walker.getAnalysisBits();
                bits|=bit;
                walker=walker.getNextWalker();
            }
        }
        return bits;
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        if(null!=m_firstWalker){
            m_firstWalker.setRoot(context);
            m_lastUsedWalker=m_firstWalker;
        }
    }

    public void detach(){
        if(m_allowDetach){
            AxesWalker walker=m_firstWalker;
            while(null!=walker){
                walker.detach();
                walker=walker.getNextWalker();
            }
            m_lastUsedWalker=null;
            // Always call the superclass detach last!
            super.detach();
        }
    }

    public void reset(){
        super.reset();
        if(null!=m_firstWalker){
            m_lastUsedWalker=m_firstWalker;
            m_firstWalker.setRoot(m_context);
        }
    }

    public int nextNode(){
        if(m_foundLast)
            return DTM.NULL;
        // If the variable stack position is not -1, we'll have to
        // set our position in the variable stack, so our variable access
        // will be correct.  Iterators that are at the top level of the
        // expression need to reset the variable stack, while iterators
        // in predicates do not need to, and should not, since their execution
        // may be much later than top-level iterators.
        // m_varStackPos is set in setRoot, which is called
        // from the execute method.
        if(-1==m_stackFrame){
            return returnNextNode(m_firstWalker.nextNode());
        }else{
            VariableStack vars=m_execContext.getVarStack();
            // These three statements need to be combined into one operation.
            int savedStart=vars.getStackFrame();
            vars.setStackFrame(m_stackFrame);
            int n=returnNextNode(m_firstWalker.nextNode());
            // These two statements need to be combined into one operation.
            vars.setStackFrame(savedStart);
            return n;
        }
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitLocationPath(owner,this)){
            if(null!=m_firstWalker){
                m_firstWalker.callVisitors(this,visitor);
            }
        }
    }

    public Object clone() throws CloneNotSupportedException{
        WalkingIterator clone=(WalkingIterator)super.clone();
        //    clone.m_varStackPos = this.m_varStackPos;
        //    clone.m_varStackContext = this.m_varStackContext;
        if(null!=m_firstWalker){
            clone.m_firstWalker=m_firstWalker.cloneDeep(clone,null);
        }
        return clone;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        m_predicateIndex=-1;
        AxesWalker walker=m_firstWalker;
        while(null!=walker){
            walker.fixupVariables(vars,globalsSize);
            walker=walker.getNextWalker();
        }
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        AxesWalker walker1=m_firstWalker;
        AxesWalker walker2=((WalkingIterator)expr).m_firstWalker;
        while((null!=walker1)&&(null!=walker2)){
            if(!walker1.deepEquals(walker2))
                return false;
            walker1=walker1.getNextWalker();
            walker2=walker2.getNextWalker();
        }
        if((null!=walker1)||(null!=walker2))
            return false;
        return true;
    }

    public final AxesWalker getFirstWalker(){
        return m_firstWalker;
    }

    public final void setFirstWalker(AxesWalker walker){
        m_firstWalker=walker;
    }

    public final AxesWalker getLastUsedWalker(){
        return m_lastUsedWalker;
    }

    public final void setLastUsedWalker(AxesWalker walker){
        m_lastUsedWalker=walker;
    }

    public Expression getExpression(){
        return m_firstWalker;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_firstWalker=(AxesWalker)exp;
    }
}
