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
 * $Id: AxesWalker.java,v 1.2.4.1 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: AxesWalker.java,v 1.2.4.1 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import java.util.Vector;

public class AxesWalker extends PredicatedNodeTest
        implements Cloneable, PathComponent, ExpressionOwner{
    static final long serialVersionUID=-2966031951306601247L;
    protected AxesWalker m_nextWalker;
    protected int m_axis=-1;
    protected DTMAxisTraverser m_traverser;
    transient int m_root=DTM.NULL;
    transient boolean m_isFresh;
    AxesWalker m_prevWalker;
    //============= State Data =============
    private DTM m_dtm;
    //=============== TreeWalker Implementation ===============
    private transient int m_currentNode=DTM.NULL;

    public AxesWalker(LocPathIterator locPathIterator,int axis){
        super(locPathIterator);
        m_axis=axis;
    }

    public void init(Compiler compiler,int opPos,int stepType)
            throws javax.xml.transform.TransformerException{
        initPredicateInfo(compiler,opPos);
        // int testType = compiler.getOp(nodeTestOpPos);
    }

    AxesWalker cloneDeep(WalkingIterator cloneOwner,Vector cloneList)
            throws CloneNotSupportedException{
        AxesWalker clone=findClone(this,cloneList);
        if(null!=clone)
            return clone;
        clone=(AxesWalker)this.clone();
        clone.setLocPathIterator(cloneOwner);
        if(null!=cloneList){
            cloneList.addElement(this);
            cloneList.addElement(clone);
        }
        if(wi().m_lastUsedWalker==this)
            cloneOwner.m_lastUsedWalker=clone;
        if(null!=m_nextWalker)
            clone.m_nextWalker=m_nextWalker.cloneDeep(cloneOwner,cloneList);
        // If you don't check for the cloneList here, you'll go into an
        // recursive infinate loop.
        if(null!=cloneList){
            if(null!=m_prevWalker)
                clone.m_prevWalker=m_prevWalker.cloneDeep(cloneOwner,cloneList);
        }else{
            if(null!=m_nextWalker)
                clone.m_nextWalker.m_prevWalker=clone;
        }
        return clone;
    }

    public final WalkingIterator wi(){
        return (WalkingIterator)m_lpi;
    }

    public Object clone() throws CloneNotSupportedException{
        // Do not access the location path itterator during this operation!
        AxesWalker clone=(AxesWalker)super.clone();
        //clone.setCurrentNode(clone.m_root);
        // clone.m_isFresh = true;
        return clone;
    }

    public int getLastPos(XPathContext xctxt){
        int pos=getProximityPosition();
        AxesWalker walker;
        try{
            walker=(AxesWalker)clone();
        }catch(CloneNotSupportedException cnse){
            return -1;
        }
        walker.setPredicateCount(m_predicateIndex);
        walker.setNextWalker(null);
        walker.setPrevWalker(null);
        WalkingIterator lpi=wi();
        AxesWalker savedWalker=lpi.getLastUsedWalker();
        try{
            lpi.setLastUsedWalker(walker);
            int next;
            while(DTM.NULL!=(next=walker.nextNode())){
                pos++;
            }
            // TODO: Should probably save this in the iterator.
        }finally{
            lpi.setLastUsedWalker(savedWalker);
        }
        // System.out.println("pos: "+pos);
        return pos;
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        AxesWalker walker=(AxesWalker)expr;
        if(this.m_axis!=walker.m_axis)
            return false;
        return true;
    }

    static AxesWalker findClone(AxesWalker key,Vector cloneList){
        if(null!=cloneList){
            // First, look for clone on list.
            int n=cloneList.size();
            for(int i=0;i<n;i+=2){
                if(key==cloneList.elementAt(i))
                    return (AxesWalker)cloneList.elementAt(i+1);
            }
        }
        return null;
    }

    public void detach(){
        m_currentNode=DTM.NULL;
        m_dtm=null;
        m_traverser=null;
        m_isFresh=true;
        m_root=DTM.NULL;
    }

    public int getRoot(){
        return m_root;
    }
    //============= End TreeWalker Implementation =============

    public void setRoot(int root){
        // %OPT% Get this directly from the lpi.
        XPathContext xctxt=wi().getXPathContext();
        m_dtm=xctxt.getDTM(root);
        m_traverser=m_dtm.getAxisTraverser(m_axis);
        m_isFresh=true;
        m_foundLast=false;
        m_root=root;
        m_currentNode=root;
        if(DTM.NULL==root){
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_SETTING_WALKER_ROOT_TO_NULL,null)); //"\n !!!! Error! Setting the root of a walker to null!!!");
        }
        resetProximityPositions();
    }

    public int getAnalysisBits(){
        int axis=getAxis();
        int bit=WalkerFactory.getAnalysisBitFromAxes(axis);
        return bit;
    }

    public int getAxis(){
        return m_axis;
    }

    public final int getCurrentNode(){
        return m_currentNode;
    }

    public AxesWalker getNextWalker(){
        return m_nextWalker;
    }

    public void setNextWalker(AxesWalker walker){
        m_nextWalker=walker;
    }

    public AxesWalker getPrevWalker(){
        return m_prevWalker;
    }

    public void setPrevWalker(AxesWalker walker){
        m_prevWalker=walker;
    }

    private int returnNextNode(int n){
        return n;
    }

    protected int getNextNode(){
        if(m_foundLast)
            return DTM.NULL;
        if(m_isFresh){
            m_currentNode=m_traverser.first(m_root);
            m_isFresh=false;
        }
        // I shouldn't have to do this the check for current node, I think.
        // numbering\numbering24.xsl fails if I don't do this.  I think
        // it occurs as the walkers are backing up. -sb
        else if(DTM.NULL!=m_currentNode){
            m_currentNode=m_traverser.next(m_root,m_currentNode);
        }
        if(DTM.NULL==m_currentNode)
            this.m_foundLast=true;
        return m_currentNode;
    }

    public int nextNode(){
        int nextNode=DTM.NULL;
        AxesWalker walker=wi().getLastUsedWalker();
        while(true){
            if(null==walker)
                break;
            nextNode=walker.getNextNode();
            if(DTM.NULL==nextNode){
                walker=walker.m_prevWalker;
            }else{
                if(walker.acceptNode(nextNode)!=DTMIterator.FILTER_ACCEPT){
                    continue;
                }
                if(null==walker.m_nextWalker){
                    wi().setLastUsedWalker(walker);
                    // return walker.returnNextNode(nextNode);
                    break;
                }else{
                    AxesWalker prev=walker;
                    walker=walker.m_nextWalker;
                    walker.setRoot(nextNode);
                    walker.m_prevWalker=prev;
                    continue;
                }
            }  // if(null != nextNode)
        }  // while(null != walker)
        return nextNode;
    }

    public void setDefaultDTM(DTM dtm){
        m_dtm=dtm;
    }

    public DTM getDTM(int node){
        //
        return wi().getXPathContext().getDTM(node);
    }

    public boolean isDocOrdered(){
        return true;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitStep(owner,this)){
            callPredicateVisitors(visitor);
            if(null!=m_nextWalker){
                m_nextWalker.callVisitors(this,visitor);
            }
        }
    }

    public Expression getExpression(){
        return m_nextWalker;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_nextWalker=(AxesWalker)exp;
    }
}
