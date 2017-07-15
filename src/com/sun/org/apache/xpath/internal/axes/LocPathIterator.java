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
 * $Id: LocPathIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: LocPathIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public abstract class LocPathIterator extends PredicatedNodeTest
        implements Cloneable, DTMIterator, java.io.Serializable, PathComponent{
    static final long serialVersionUID=-4602476357268405754L;
    transient public int m_lastFetched=DTM.NULL;
    protected boolean m_allowDetach=true;
    //============= State Data =============
    transient protected IteratorPool m_clones=new IteratorPool(this);
    transient protected DTM m_cdtm;
    transient protected int m_context=DTM.NULL;
    transient protected int m_currentContextNode=DTM.NULL;
    transient protected int m_pos=0;
    transient protected int m_length=-1;
    transient protected XPathContext m_execContext;
    transient int m_stackFrame=-1;
    private boolean m_isTopLevel=false;
    private PrefixResolver m_prefixResolver;

    protected LocPathIterator(){
    }

    protected LocPathIterator(PrefixResolver nscontext){
        setLocPathIterator(this);
        m_prefixResolver=nscontext;
    }

    protected LocPathIterator(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        this(compiler,opPos,analysis,true);
    }

    protected LocPathIterator(
            Compiler compiler,int opPos,int analysis,boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException{
        setLocPathIterator(this);
    }

    public int getAnalysisBits(){
        int axis=getAxis();
        int bit=WalkerFactory.getAnalysisBitFromAxes(axis);
        return bit;
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

    public void setEnvironment(Object environment){
        // no-op for now.
    }

    public DTM getDTM(int nodeHandle){
        // %OPT%
        return m_execContext.getDTM(nodeHandle);
    }

    public DTMManager getDTMManager(){
        return m_execContext.getDTMManager();
    }

    public int getRoot(){
        return m_context;
    }

    public void setRoot(int context,Object environment){
        m_context=context;
        XPathContext xctxt=(XPathContext)environment;
        m_execContext=xctxt;
        m_cdtm=xctxt.getDTM(context);
        m_currentContextNode=context; // only if top level?
        // Yech, shouldn't have to do this.  -sb
        if(null==m_prefixResolver)
            m_prefixResolver=xctxt.getNamespaceContext();
        m_lastFetched=DTM.NULL;
        m_foundLast=false;
        m_pos=0;
        m_length=-1;
        if(m_isTopLevel)
            this.m_stackFrame=xctxt.getVarStack().getStackFrame();
        // reset();
    }

    public void reset(){
        assertion(false,"This iterator can not reset!");
    }

    public boolean getExpandEntityReferences(){
        return true;
    }

    public abstract int nextNode();

    public int previousNode(){
        throw new RuntimeException(
                XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE,null)); //"This NodeSetDTM can not iterate to a previous node!");
    }

    public void detach(){
        if(m_allowDetach){
            // sb: allow reusing of cached nodes when possible?
            // m_cachedNodes = null;
            m_execContext=null;
            // m_prefixResolver = null;  sb: Why would this ever want to be null?
            m_cdtm=null;
            m_length=-1;
            m_pos=0;
            m_lastFetched=DTM.NULL;
            m_context=DTM.NULL;
            m_currentContextNode=DTM.NULL;
            m_clones.freeInstance(this);
        }
    }

    public void allowDetachToRelease(boolean allowRelease){
        m_allowDetach=allowRelease;
    }

    public int getCurrentNode(){
        return m_lastFetched;
    }

    public boolean isFresh(){
        return (m_pos==0);
    }

    public void setShouldCacheNodes(boolean b){
        assertion(false,"setShouldCacheNodes not supported by this iterater!");
    }

    public boolean isMutable(){
        return false;
    }

    public final int getCurrentPos(){
        return m_pos;
    }

    public void setCurrentPos(int i){
        assertion(false,"setCurrentPos not supported by this iterator!");
    }

    public int item(int index){
        assertion(false,"item(int index) not supported by this iterator!");
        return 0;
    }

    public void setItem(int node,int index){
        assertion(false,"setItem not supported by this iterator!");
    }

    public int getLength(){
        // Tell if this is being called from within a predicate.
        boolean isPredicateTest=(this==m_execContext.getSubContextList());
        // And get how many total predicates are part of this step.
        int predCount=getPredicateCount();
        // If we have already calculated the length, and the current predicate
        // is the first predicate, then return the length.  We don't cache
        // the anything but the length of the list to the first predicate.
        if(-1!=m_length&&isPredicateTest&&m_predicateIndex<1)
            return m_length;
        // I'm a bit worried about this one, since it doesn't have the
        // checks found above.  I suspect it's fine.  -sb
        if(m_foundLast)
            return m_pos;
        // Create a clone, and count from the current position to the end
        // of the list, not taking into account the current predicate and
        // predicates after the current one.
        int pos=(m_predicateIndex>=0)?getProximityPosition():m_pos;
        LocPathIterator clone;
        try{
            clone=(LocPathIterator)clone();
        }catch(CloneNotSupportedException cnse){
            return -1;
        }
        // We want to clip off the last predicate, but only if we are a sub
        // context node list, NOT if we are a context list.  See pos68 test,
        // also test against bug4638.
        if(predCount>0&&isPredicateTest){
            // Don't call setPredicateCount, because it clones and is slower.
            clone.m_predCount=m_predicateIndex;
            // The line above used to be:
            // clone.m_predCount = predCount - 1;
            // ...which looks like a dumb bug to me. -sb
        }
        int next;
        while(DTM.NULL!=(next=clone.nextNode())){
            pos++;
        }
        if(isPredicateTest&&m_predicateIndex<1)
            m_length=pos;
        return pos;
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        LocPathIterator clone;
//    clone = (LocPathIterator) clone();
        clone=(LocPathIterator)m_clones.getInstanceOrThrow();
        clone.m_execContext=m_execContext;
        clone.m_cdtm=m_cdtm;
        clone.m_context=m_context;
        clone.m_currentContextNode=m_currentContextNode;
        clone.m_stackFrame=m_stackFrame;
        // clone.reset();
        return clone;
    }
//  /**
//   * Get a cloned LocPathIterator that holds the same
//   * position as this iterator.
//   *
//   * @return A clone of this iterator that holds the same node position.
//   *
//   * @throws CloneNotSupportedException
//   */
//  public Object clone() throws CloneNotSupportedException
//  {
//
//    LocPathIterator clone = (LocPathIterator) super.clone();
//
//    return clone;
//  }

    public boolean isDocOrdered(){
        return true;
    }

    public int getAxis(){
        return -1;
    }

    public void runTo(int index){
        if(m_foundLast||((index>=0)&&(index<=getCurrentPos())))
            return;
        int n;
        if(-1==index){
            while(DTM.NULL!=(n=nextNode())) ;
        }else{
            while(DTM.NULL!=(n=nextNode())){
                if(getCurrentPos()>=index)
                    break;
            }
        }
    }

    public boolean bool(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return (asNode(xctxt)!=DTM.NULL);
    }

    public boolean isNodesetExpr(){
        return true;
    }

    public int asNode(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        DTMIterator iter=(DTMIterator)m_clones.getInstance();
        int current=xctxt.getCurrentNode();
        iter.setRoot(current,xctxt);
        int next=iter.nextNode();
        // m_clones.freeInstance(iter);
        iter.detach();
        return next;
    }

    public DTMIterator asIterator(
            XPathContext xctxt,int contextNode)
            throws javax.xml.transform.TransformerException{
        XNodeSet iter=new XNodeSet((LocPathIterator)m_clones.getInstance());
        iter.setRoot(contextNode,xctxt);
        return iter;
    }

    public void executeCharsToContentHandler(
            XPathContext xctxt,org.xml.sax.ContentHandler handler)
            throws javax.xml.transform.TransformerException,
            org.xml.sax.SAXException{
        LocPathIterator clone=(LocPathIterator)m_clones.getInstance();
        int current=xctxt.getCurrentNode();
        clone.setRoot(current,xctxt);
        int node=clone.nextNode();
        DTM dtm=clone.getDTM(node);
        clone.detach();
        if(node!=DTM.NULL){
            dtm.dispatchCharactersEvents(node,handler,false);
        }
    }

    public boolean getIsTopLevel(){
        return m_isTopLevel;
    }
//  /**
//   * Set the current context node for this iterator.
//   *
//   * @param n Must be a non-null reference to the node context.
//   */
//  public void setRoot(int n)
//  {
//    m_context = n;
//    m_cdtm = m_execContext.getDTM(n);
//  }

    public void setIsTopLevel(boolean b){
        m_isTopLevel=b;
    }
//  /**
//   * Get the analysis pattern built by the WalkerFactory.
//   *
//   * @return The analysis pattern built by the WalkerFactory.
//   */
//  int getAnalysis()
//  {
//    return m_analysis;
//  }
//  /**
//   * Set the analysis pattern built by the WalkerFactory.
//   *
//   * @param a The analysis pattern built by the WalkerFactory.
//   */
//  void setAnalysis(int a)
//  {
//    m_analysis = a;
//  }

    protected void setNextPosition(int next){
        assertion(false,"setNextPosition not supported in this iterator!");
    }

    public void incrementCurrentPos(){
        m_pos++;
    }

    public int size(){
        assertion(false,"size() not supported by this iterator!");
        return 0;
    }

    public int getWhatToShow(){
        // TODO: ??
        return DTMFilter.SHOW_ALL&~DTMFilter.SHOW_ENTITY_REFERENCE;
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        XNodeSet iter=new XNodeSet((LocPathIterator)m_clones.getInstance());
        iter.setRoot(xctxt.getCurrentNode(),xctxt);
        return iter;
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitLocationPath(owner,this)){
            visitor.visitStep(owner,this);
            callPredicateVisitors(visitor);
        }
    }

    public DTMFilter getFilter(){
        return null;
    }

    protected int returnNextNode(int nextNode){
        if(DTM.NULL!=nextNode){
            m_pos++;
        }
        m_lastFetched=nextNode;
        if(DTM.NULL==nextNode)
            m_foundLast=true;
        return nextNode;
    }

    public final boolean getFoundLast(){
        return m_foundLast;
    }

    public final XPathContext getXPathContext(){
        return m_execContext;
    }

    public final int getContext(){
        return m_context;
    }

    public final int getCurrentContextNode(){
        return m_currentContextNode;
    }

    public final void setCurrentContextNode(int n){
        m_currentContextNode=n;
    }

    public final PrefixResolver getPrefixResolver(){
        if(null==m_prefixResolver){
            m_prefixResolver=(PrefixResolver)getExpressionOwner();
        }
        return m_prefixResolver;
    }

    //  /**
//   * The analysis pattern built by the WalkerFactory.
//   * TODO: Move to LocPathIterator.
//   * @see com.sun.org.apache.xpath.internal.axes.WalkerFactory
//   * @serial
//   */
//  protected int m_analysis = 0x00000000;
    public int getLastPos(XPathContext xctxt){
        return getLength();
    }
}
