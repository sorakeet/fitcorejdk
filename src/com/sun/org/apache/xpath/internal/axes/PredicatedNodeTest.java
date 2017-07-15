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
 * $Id: PredicatedNodeTest.java,v 1.2.4.2 2005/09/14 19:45:20 jeffsuttor Exp $
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
 * $Id: PredicatedNodeTest.java,v 1.2.4.2 2005/09/14 19:45:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;

public abstract class PredicatedNodeTest extends NodeTest implements SubContextList{
    static final long serialVersionUID=-6193530757296377351L;
    static final boolean DEBUG_PREDICATECOUNTING=false;
    // Only for clones for findLastPos.  See bug4638.
    protected int m_predCount=-1;
    transient protected boolean m_foundLast=false;
    protected LocPathIterator m_lpi;
    transient protected int[] m_proximityPositions;
    transient int m_predicateIndex=-1;
    private Expression[] m_predicates;

    PredicatedNodeTest(LocPathIterator locPathIterator){
        m_lpi=locPathIterator;
    }

    PredicatedNodeTest(){
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, javax.xml.transform.TransformerException{
        try{
            stream.defaultReadObject();
            m_predicateIndex=-1;
            resetProximityPositions();
        }catch(ClassNotFoundException cnfe){
            throw new javax.xml.transform.TransformerException(cnfe);
        }
    }

    public void resetProximityPositions(){
        int nPredicates=getPredicateCount();
        if(nPredicates>0){
            if(null==m_proximityPositions)
                m_proximityPositions=new int[nPredicates];
            for(int i=0;i<nPredicates;i++){
                try{
                    initProximityPosition(i);
                }catch(Exception e){
                    // TODO: Fix this...
                    throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
                }
            }
        }
    }

    public int getPredicateCount(){
        if(-1==m_predCount)
            return (null==m_predicates)?0:m_predicates.length;
        else
            return m_predCount;
    }

    public void setPredicateCount(int count){
        if(count>0){
            Expression[] newPredicates=new Expression[count];
            for(int i=0;i<count;i++){
                newPredicates[i]=m_predicates[i];
            }
            m_predicates=newPredicates;
        }else
            m_predicates=null;
    }

    public void initProximityPosition(int i) throws javax.xml.transform.TransformerException{
        m_proximityPositions[i]=0;
    }

    public Object clone() throws CloneNotSupportedException{
        // Do not access the location path itterator during this operation!
        PredicatedNodeTest clone=(PredicatedNodeTest)super.clone();
        if((null!=this.m_proximityPositions)
                &&(this.m_proximityPositions==clone.m_proximityPositions)){
            clone.m_proximityPositions=new int[this.m_proximityPositions.length];
            System.arraycopy(this.m_proximityPositions,0,
                    clone.m_proximityPositions,0,
                    this.m_proximityPositions.length);
        }
        if(clone.m_lpi==this)
            clone.m_lpi=(LocPathIterator)clone;
        return clone;
    }

    protected void initPredicateInfo(Compiler compiler,int opPos)
            throws javax.xml.transform.TransformerException{
        int pos=compiler.getFirstPredicateOpPos(opPos);
        if(pos>0){
            m_predicates=compiler.getCompiledPredicates(pos);
            if(null!=m_predicates){
                for(int i=0;i<m_predicates.length;i++){
                    m_predicates[i].exprSetParent(this);
                }
            }
        }
    }

    public abstract int getLastPos(XPathContext xctxt);

    public int getProximityPosition(XPathContext xctxt){
        return getProximityPosition();
    }

    public int getProximityPosition(){
        // System.out.println("getProximityPosition - m_predicateIndex: "+m_predicateIndex);
        return getProximityPosition(m_predicateIndex);
    }

    protected int getProximityPosition(int predicateIndex){
        return (predicateIndex>=0)?m_proximityPositions[predicateIndex]:0;
    }

    public boolean isReverseAxes(){
        return false;
    }
    //=============== NodeFilter Implementation ===============

    public int getPredicateIndex(){
        return m_predicateIndex;
    }

    public short acceptNode(int n){
        XPathContext xctxt=m_lpi.getXPathContext();
        try{
            xctxt.pushCurrentNode(n);
            XObject score=execute(xctxt,n);
            // System.out.println("\n::acceptNode - score: "+score.num()+"::");
            if(score!=NodeTest.SCORE_NONE){
                if(getPredicateCount()>0){
                    countProximityPosition(0);
                    if(!executePredicates(n,xctxt))
                        return DTMIterator.FILTER_SKIP;
                }
                return DTMIterator.FILTER_ACCEPT;
            }
        }catch(javax.xml.transform.TransformerException se){
            // TODO: Fix this.
            throw new RuntimeException(se.getMessage());
        }finally{
            xctxt.popCurrentNode();
        }
        return DTMIterator.FILTER_SKIP;
    }

    protected void countProximityPosition(int i){
        // Note that in the case of a UnionChildIterator, this may be a
        // static object and so m_proximityPositions may indeed be null!
        int[] pp=m_proximityPositions;
        if((null!=pp)&&(i<pp.length))
            pp[i]++;
    }

    boolean executePredicates(int context,XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        int nPredicates=getPredicateCount();
        // System.out.println("nPredicates: "+nPredicates);
        if(nPredicates==0)
            return true;
        PrefixResolver savedResolver=xctxt.getNamespaceContext();
        try{
            m_predicateIndex=0;
            xctxt.pushSubContextList(this);
            xctxt.pushNamespaceContext(m_lpi.getPrefixResolver());
            xctxt.pushCurrentNode(context);
            for(int i=0;i<nPredicates;i++){
                // System.out.println("Executing predicate expression - waiting count: "+m_lpi.getWaitingCount());
                XObject pred=m_predicates[i].execute(xctxt);
                // System.out.println("\nBack from executing predicate expression - waiting count: "+m_lpi.getWaitingCount());
                // System.out.println("pred.getType(): "+pred.getType());
                if(XObject.CLASS_NUMBER==pred.getType()){
                    if(DEBUG_PREDICATECOUNTING){
                        System.out.flush();
                        System.out.println("\n===== start predicate count ========");
                        System.out.println("m_predicateIndex: "+m_predicateIndex);
                        // System.out.println("getProximityPosition(m_predicateIndex): "
                        //                   + getProximityPosition(m_predicateIndex));
                        System.out.println("pred.num(): "+pred.num());
                    }
                    int proxPos=this.getProximityPosition(m_predicateIndex);
                    int predIndex=(int)pred.num();
                    if(proxPos!=predIndex){
                        if(DEBUG_PREDICATECOUNTING){
                            System.out.println("\nnode context: "+nodeToString(context));
                            System.out.println("index predicate is false: "+proxPos);
                            System.out.println("\n===== end predicate count ========");
                        }
                        return false;
                    }else if(DEBUG_PREDICATECOUNTING){
                        System.out.println("\nnode context: "+nodeToString(context));
                        System.out.println("index predicate is true: "+proxPos);
                        System.out.println("\n===== end predicate count ========");
                    }
                    // If there is a proximity index that will not change during the
                    // course of itteration, then we know there can be no more true
                    // occurances of this predicate, so flag that we're done after
                    // this.
                    //
                    // bugzilla 14365
                    // We can't set m_foundLast = true unless we're sure that -all-
                    // remaining parameters are stable, or else last() fails. Fixed so
                    // only sets m_foundLast if on the last predicate
                    if(m_predicates[i].isStableNumber()&&i==nPredicates-1){
                        m_foundLast=true;
                    }
                }else if(!pred.bool())
                    return false;
                countProximityPosition(++m_predicateIndex);
            }
        }finally{
            xctxt.popCurrentNode();
            xctxt.popNamespaceContext();
            xctxt.popSubContextList();
            m_predicateIndex=-1;
        }
        return true;
    }

    protected String nodeToString(int n){
        if(DTM.NULL!=n){
            DTM dtm=m_lpi.getXPathContext().getDTM(n);
            return dtm.getNodeName(n)+"{"+(n+1)+"}";
        }else{
            return "null";
        }
    }

    public LocPathIterator getLocPathIterator(){
        return m_lpi;
    }

    public void setLocPathIterator(LocPathIterator li){
        m_lpi=li;
        if(this!=li)
            li.exprSetParent(this);
    }

    public boolean canTraverseOutsideSubtree(){
        int n=getPredicateCount();
        for(int i=0;i<n;i++){
            if(getPredicate(i).canTraverseOutsideSubtree())
                return true;
        }
        return false;
    }

    public Expression getPredicate(int index){
        return m_predicates[index];
    }

    public void callPredicateVisitors(XPathVisitor visitor){
        if(null!=m_predicates){
            int n=m_predicates.length;
            for(int i=0;i<n;i++){
                ExpressionOwner predOwner=new PredOwner(i);
                if(visitor.visitPredicate(predOwner,m_predicates[i])){
                    m_predicates[i].callVisitors(predOwner,visitor);
                }
            }
        }
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        PredicatedNodeTest pnt=(PredicatedNodeTest)expr;
        if(null!=m_predicates){
            int n=m_predicates.length;
            if((null==pnt.m_predicates)||(pnt.m_predicates.length!=n))
                return false;
            for(int i=0;i<n;i++){
                if(!m_predicates[i].deepEquals(pnt.m_predicates[i]))
                    return false;
            }
        }else if(null!=pnt.m_predicates)
            return false;
        return true;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        int nPredicates=getPredicateCount();
        for(int i=0;i<nPredicates;i++){
            m_predicates[i].fixupVariables(vars,globalsSize);
        }
    }

    class PredOwner implements ExpressionOwner{
        int m_index;

        PredOwner(int index){
            m_index=index;
        }

        public Expression getExpression(){
            return m_predicates[m_index];
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(PredicatedNodeTest.this);
            m_predicates[m_index]=exp;
        }
    }
}
