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
 * $Id: UnionPathIterator.java,v 1.2.4.1 2005/09/14 19:43:25 jeffsuttor Exp $
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
 * $Id: UnionPathIterator.java,v 1.2.4.1 2005/09/14 19:43:25 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpCodes;
import com.sun.org.apache.xpath.internal.compiler.OpMap;

public class UnionPathIterator extends LocPathIterator
        implements Cloneable, DTMIterator, java.io.Serializable, PathComponent{
    static final long serialVersionUID=-3910351546843826781L;
    protected LocPathIterator[] m_exprs;
    protected DTMIterator[] m_iterators;

    public UnionPathIterator(){
        super();
        // m_mutable = false;
        // m_cacheNodes = false;
        m_iterators=null;
        m_exprs=null;
    }

    public UnionPathIterator(Compiler compiler,int opPos)
            throws javax.xml.transform.TransformerException{
        super();
        opPos=OpMap.getFirstChildPos(opPos);
        loadLocationPaths(compiler,opPos,0);
    }

    public static LocPathIterator createUnionIterator(Compiler compiler,int opPos)
            throws javax.xml.transform.TransformerException{
        // For the moment, I'm going to first create a full UnionPathIterator, and
        // then see if I can reduce it to a UnionChildIterator.  It would obviously
        // be more effecient to just test for the conditions for a UnionChildIterator,
        // and then create that directly.
        UnionPathIterator upi=new UnionPathIterator(compiler,opPos);
        int nPaths=upi.m_exprs.length;
        boolean isAllChildIterators=true;
        for(int i=0;i<nPaths;i++){
            LocPathIterator lpi=upi.m_exprs[i];
            if(lpi.getAxis()!=Axis.CHILD){
                isAllChildIterators=false;
                break;
            }else{
                // check for positional predicates or position function, which won't work.
                if(HasPositionalPredChecker.check(lpi)){
                    isAllChildIterators=false;
                    break;
                }
            }
        }
        if(isAllChildIterators){
            UnionChildIterator uci=new UnionChildIterator();
            for(int i=0;i<nPaths;i++){
                PredicatedNodeTest lpi=upi.m_exprs[i];
                // I could strip the lpi down to a pure PredicatedNodeTest, but
                // I don't think it's worth it.  Note that the test can be used
                // as a static object... so it doesn't have to be cloned.
                uci.addNodeTest(lpi);
            }
            return uci;
        }else
            return upi;
    }

    public void addIterator(DTMIterator expr){
        // Increase array size by only 1 at a time.  Fix this
        // if it looks to be a problem.
        if(null==m_iterators){
            m_iterators=new DTMIterator[1];
            m_iterators[0]=expr;
        }else{
            DTMIterator[] exprs=m_iterators;
            int len=m_iterators.length;
            m_iterators=new DTMIterator[len+1];
            System.arraycopy(exprs,0,m_iterators,0,len);
            m_iterators[len]=expr;
        }
        expr.nextNode();
        if(expr instanceof Expression)
            ((Expression)expr).exprSetParent(this);
    }

    public int getAnalysisBits(){
        int bits=0;
        if(m_exprs!=null){
            int n=m_exprs.length;
            for(int i=0;i<n;i++){
                int bit=m_exprs[i].getAnalysisBits();
                bits|=bit;
            }
        }
        return bits;
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        try{
            if(null!=m_exprs){
                int n=m_exprs.length;
                DTMIterator newIters[]=new DTMIterator[n];
                for(int i=0;i<n;i++){
                    DTMIterator iter=m_exprs[i].asIterator(m_execContext,context);
                    newIters[i]=iter;
                    iter.nextNode();
                }
                m_iterators=newIters;
            }
        }catch(Exception e){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
        }
    }

    public void detach(){
        if(m_allowDetach&&null!=m_iterators){
            int n=m_iterators.length;
            for(int i=0;i<n;i++){
                m_iterators[i].detach();
            }
            m_iterators=null;
        }
    }

    public int nextNode(){
        if(m_foundLast)
            return DTM.NULL;
        // Loop through the iterators getting the current fetched
        // node, and get the earliest occuring in document order
        int earliestNode=DTM.NULL;
        if(null!=m_iterators){
            int n=m_iterators.length;
            int iteratorUsed=-1;
            for(int i=0;i<n;i++){
                int node=m_iterators[i].getCurrentNode();
                if(DTM.NULL==node)
                    continue;
                else if(DTM.NULL==earliestNode){
                    iteratorUsed=i;
                    earliestNode=node;
                }else{
                    if(node==earliestNode){
                        // Found a duplicate, so skip past it.
                        m_iterators[i].nextNode();
                    }else{
                        DTM dtm=getDTM(node);
                        if(dtm.isNodeAfter(node,earliestNode)){
                            iteratorUsed=i;
                            earliestNode=node;
                        }
                    }
                }
            }
            if(DTM.NULL!=earliestNode){
                m_iterators[iteratorUsed].nextNode();
                incrementCurrentPos();
            }else
                m_foundLast=true;
        }
        m_lastFetched=earliestNode;
        return earliestNode;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitUnionPath(owner,this)){
            if(null!=m_exprs){
                int n=m_exprs.length;
                for(int i=0;i<n;i++){
                    m_exprs[i].callVisitors(new iterOwner(i),visitor);
                }
            }
        }
    }

    public int getAxis(){
        // Could be smarter.
        return -1;
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, javax.xml.transform.TransformerException{
        try{
            stream.defaultReadObject();
            m_clones=new IteratorPool(this);
        }catch(ClassNotFoundException cnfe){
            throw new javax.xml.transform.TransformerException(cnfe);
        }
    }

    public Object clone() throws CloneNotSupportedException{
        UnionPathIterator clone=(UnionPathIterator)super.clone();
        if(m_iterators!=null){
            int n=m_iterators.length;
            clone.m_iterators=new DTMIterator[n];
            for(int i=0;i<n;i++){
                clone.m_iterators[i]=(DTMIterator)m_iterators[i].clone();
            }
        }
        return clone;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        for(int i=0;i<m_exprs.length;i++){
            m_exprs[i].fixupVariables(vars,globalsSize);
        }
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        UnionPathIterator upi=(UnionPathIterator)expr;
        if(null!=m_exprs){
            int n=m_exprs.length;
            if((null==upi.m_exprs)||(upi.m_exprs.length!=n))
                return false;
            for(int i=0;i<n;i++){
                if(!m_exprs[i].deepEquals(upi.m_exprs[i]))
                    return false;
            }
        }else if(null!=upi.m_exprs){
            return false;
        }
        return true;
    }

    protected LocPathIterator createDTMIterator(
            Compiler compiler,int opPos) throws javax.xml.transform.TransformerException{
        LocPathIterator lpi=(LocPathIterator)WalkerFactory.newDTMIterator(compiler,opPos,
                (compiler.getLocationPathDepth()<=0));
        return lpi;
    }

    protected void loadLocationPaths(Compiler compiler,int opPos,int count)
            throws javax.xml.transform.TransformerException{
        // TODO: Handle unwrapped FilterExpr
        int steptype=compiler.getOp(opPos);
        if(steptype==OpCodes.OP_LOCATIONPATH){
            loadLocationPaths(compiler,compiler.getNextOpPos(opPos),count+1);
            m_exprs[count]=createDTMIterator(compiler,opPos);
            m_exprs[count].exprSetParent(this);
        }else{
            // Have to check for unwrapped functions, which the LocPathIterator
            // doesn't handle.
            switch(steptype){
                case OpCodes.OP_VARIABLE:
                case OpCodes.OP_EXTFUNCTION:
                case OpCodes.OP_FUNCTION:
                case OpCodes.OP_GROUP:
                    loadLocationPaths(compiler,compiler.getNextOpPos(opPos),count+1);
                    WalkingIterator iter=
                            new WalkingIterator(compiler.getNamespaceContext());
                    iter.exprSetParent(this);
                    if(compiler.getLocationPathDepth()<=0)
                        iter.setIsTopLevel(true);
                    iter.m_firstWalker=new FilterExprWalker(iter);
                    iter.m_firstWalker.init(compiler,opPos,steptype);
                    m_exprs[count]=iter;
                    break;
                default:
                    m_exprs=new LocPathIterator[count];
            }
        }
    }

    class iterOwner implements ExpressionOwner{
        int m_index;

        iterOwner(int index){
            m_index=index;
        }

        public Expression getExpression(){
            return m_exprs[m_index];
        }

        public void setExpression(Expression exp){
            if(!(exp instanceof LocPathIterator)){
                // Yuck.  Need FilterExprIter.  Or make it so m_exprs can be just
                // plain expressions?
                WalkingIterator wi=new WalkingIterator(getPrefixResolver());
                FilterExprWalker few=new FilterExprWalker(wi);
                wi.setFirstWalker(few);
                few.setInnerExpression(exp);
                wi.exprSetParent(UnionPathIterator.this);
                few.exprSetParent(wi);
                exp.exprSetParent(few);
                exp=wi;
            }else
                exp.exprSetParent(UnionPathIterator.this);
            m_exprs[m_index]=(LocPathIterator)exp;
        }
    }
}
