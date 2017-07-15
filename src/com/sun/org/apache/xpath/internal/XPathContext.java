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
 * $Id: XPathContext.java,v 1.2.4.2 2005/09/15 01:37:55 jeffsuttor Exp $
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
 * $Id: XPathContext.java,v 1.2.4.2 2005/09/15 01:37:55 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2RTFDTM;
import com.sun.org.apache.xml.internal.utils.*;
import com.sun.org.apache.xpath.internal.axes.SubContextList;
import com.sun.org.apache.xpath.internal.objects.DTMXRTreeFrag;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.objects.XString;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.xml.sax.XMLReader;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

public class XPathContext extends DTMManager // implements ExpressionContext
{
    public static final int RECURSIONLIMIT=(1024*4);
    // =================================================
    public XMLReader m_primaryReader;
    protected DTMManager m_dtmManager=null;
    IntStack m_last_pushed_rtfdtm=new IntStack();
    ObjectStack m_saxLocations=new ObjectStack(RECURSIONLIMIT);
    XPathExpressionContext expressionContext=new XPathExpressionContext();
    private Vector m_rtfdtm_stack=null;
    private int m_which_rtfdtm=-1;
    private SAX2RTFDTM m_global_rtfdtm=null;
    private HashMap m_DTMXRTreeFrags=null;
    private boolean m_isSecureProcessing=false;
    private boolean m_useServicesMechanism=true;
    private Object m_owner;
    private Method m_ownerGetErrorListener;
    // ================ VarStack ===================
    private VariableStack m_variableStacks=new VariableStack();
    // ================ SourceTreeManager ===================
    private SourceTreeManager m_sourceTreeManager=new SourceTreeManager();
    // =================================================
    private ErrorListener m_errorListener;
    private ErrorListener m_defaultErrorListener;
    // =================================================
    private URIResolver m_uriResolver;
    // =================================================
    // private static XSLMessages m_XSLMessages = new XSLMessages();
    //==========================================================
    // SECTION: Execution context state tracking
    //==========================================================
    private Stack m_contextNodeLists=new Stack();
    private IntStack m_currentNodes=new IntStack(RECURSIONLIMIT);
    private NodeVector m_iteratorRoots=new NodeVector();
    private NodeVector m_predicateRoots=new NodeVector();
    private IntStack m_currentExpressionNodes=new IntStack(RECURSIONLIMIT);
    private IntStack m_predicatePos=new IntStack();
    private ObjectStack m_prefixResolvers
            =new ObjectStack(RECURSIONLIMIT);
    //==========================================================
    // SECTION: Current TreeWalker contexts (for internal use)
    //==========================================================
    private Stack m_axesIteratorStack=new Stack();

    public XPathContext(){
        this(true);
    }

    public XPathContext(boolean useServicesMechanism){
        init(useServicesMechanism);
    }

    private void init(boolean useServicesMechanism){
        m_prefixResolvers.push(null);
        m_currentNodes.push(DTM.NULL);
        m_currentExpressionNodes.push(DTM.NULL);
        m_saxLocations.push(null);
        m_useServicesMechanism=useServicesMechanism;
        m_dtmManager=DTMManager.newInstance(
                com.sun.org.apache.xpath.internal.objects.XMLStringFactoryImpl.getFactory()
        );
    }

    public XPathContext(Object owner){
        m_owner=owner;
        try{
            m_ownerGetErrorListener=m_owner.getClass().getMethod("getErrorListener",new Class[]{});
        }catch(NoSuchMethodException nsme){
        }
        init(true);
    }

    public DTMManager getDTMManager(){
        return m_dtmManager;
    }

    public boolean isSecureProcessing(){
        return m_isSecureProcessing;
    }

    public void setSecureProcessing(boolean flag){
        m_isSecureProcessing=flag;
    }

    public DTM getDTM(javax.xml.transform.Source source,boolean unique,
                      DTMWSFilter wsfilter,
                      boolean incremental,
                      boolean doIndexing){
        return m_dtmManager.getDTM(source,unique,wsfilter,
                incremental,doIndexing);
    }

    public DTM getDTM(int nodeHandle){
        return m_dtmManager.getDTM(nodeHandle);
    }

    public int getDTMHandleFromNode(org.w3c.dom.Node node){
        return m_dtmManager.getDTMHandleFromNode(node);
    }

    //
    public DTM createDocumentFragment(){
        return m_dtmManager.createDocumentFragment();
    }

    //
    public boolean release(DTM dtm,boolean shouldHardDelete){
        // %REVIEW% If it's a DTM which may contain multiple Result Tree
        // Fragments, we can't discard it unless we know not only that it
        // is empty, but that the XPathContext itself is going away. So do
        // _not_ accept the request. (May want to do it as part of
        // reset(), though.)
        if(m_rtfdtm_stack!=null&&m_rtfdtm_stack.contains(dtm)){
            return false;
        }
        return m_dtmManager.release(dtm,shouldHardDelete);
    }

    public DTMIterator createDTMIterator(Object xpathCompiler,int pos){
        return m_dtmManager.createDTMIterator(xpathCompiler,pos);
    }

    //
    public DTMIterator createDTMIterator(String xpathString,
                                         PrefixResolver presolver){
        return m_dtmManager.createDTMIterator(xpathString,presolver);
    }

    //
    public DTMIterator createDTMIterator(int whatToShow,
                                         DTMFilter filter,boolean entityReferenceExpansion){
        return m_dtmManager.createDTMIterator(whatToShow,filter,entityReferenceExpansion);
    }

    public DTMIterator createDTMIterator(int node){
        // DescendantIterator iter = new DescendantIterator();
        DTMIterator iter=new com.sun.org.apache.xpath.internal.axes.OneStepIteratorForward(Axis.SELF);
        iter.setRoot(node,this);
        return iter;
        // return m_dtmManager.createDTMIterator(node);
    }

    //
//
    public int getDTMIdentity(DTM dtm){
        return m_dtmManager.getDTMIdentity(dtm);
    }

    public void reset(){
        releaseDTMXRTreeFrags();
        // These couldn't be disposed of earlier (see comments in release()); zap them now.
        if(m_rtfdtm_stack!=null)
            for(java.util.Enumeration e=m_rtfdtm_stack.elements();e.hasMoreElements();)
                m_dtmManager.release((DTM)e.nextElement(),true);
        m_rtfdtm_stack=null; // drop our references too
        m_which_rtfdtm=-1;
        if(m_global_rtfdtm!=null)
            m_dtmManager.release(m_global_rtfdtm,true);
        m_global_rtfdtm=null;
        m_dtmManager=DTMManager.newInstance(
                com.sun.org.apache.xpath.internal.objects.XMLStringFactoryImpl.getFactory()
        );
        m_saxLocations.removeAllElements();
        m_axesIteratorStack.removeAllElements();
        m_contextNodeLists.removeAllElements();
        m_currentExpressionNodes.removeAllElements();
        m_currentNodes.removeAllElements();
        m_iteratorRoots.RemoveAllNoClear();
        m_predicatePos.removeAllElements();
        m_predicateRoots.RemoveAllNoClear();
        m_prefixResolvers.removeAllElements();
        m_prefixResolvers.push(null);
        m_currentNodes.push(DTM.NULL);
        m_currentExpressionNodes.push(DTM.NULL);
        m_saxLocations.push(null);
    }

    private final void releaseDTMXRTreeFrags(){
        if(m_DTMXRTreeFrags==null){
            return;
        }
        final Iterator iter=(m_DTMXRTreeFrags.values()).iterator();
        while(iter.hasNext()){
            DTMXRTreeFrag frag=(DTMXRTreeFrag)iter.next();
            frag.destruct();
            iter.remove();
        }
        m_DTMXRTreeFrags=null;
    }

    public void pushSAXLocator(SourceLocator location){
        m_saxLocations.push(location);
    }

    public void pushSAXLocatorNull(){
        m_saxLocations.push(null);
    }

    public void popSAXLocator(){
        m_saxLocations.pop();
    }

    public SourceLocator getSAXLocator(){
        return (SourceLocator)m_saxLocations.peek();
    }

    public void setSAXLocator(SourceLocator location){
        m_saxLocations.setTop(location);
    }

    public Object getOwnerObject(){
        return m_owner;
    }

    public final VariableStack getVarStack(){
        return m_variableStacks;
    }

    public final void setVarStack(VariableStack varStack){
        m_variableStacks=varStack;
    }

    public final SourceTreeManager getSourceTreeManager(){
        return m_sourceTreeManager;
    }

    public void setSourceTreeManager(SourceTreeManager mgr){
        m_sourceTreeManager=mgr;
    }

    public final ErrorListener getErrorListener(){
        if(null!=m_errorListener)
            return m_errorListener;
        ErrorListener retval=null;
        try{
            if(null!=m_ownerGetErrorListener)
                retval=(ErrorListener)m_ownerGetErrorListener.invoke(m_owner,new Object[]{});
        }catch(Exception e){
        }
        if(null==retval){
            if(null==m_defaultErrorListener)
                m_defaultErrorListener=new com.sun.org.apache.xml.internal.utils.DefaultErrorHandler();
            retval=m_defaultErrorListener;
        }
        return retval;
    }

    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException{
        if(listener==null)
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER,null)); //"Null error handler");
        m_errorListener=listener;
    }

    public final URIResolver getURIResolver(){
        return m_uriResolver;
    }
//  private NodeVector m_currentNodes = new NodeVector();

    public void setURIResolver(URIResolver resolver){
        m_uriResolver=resolver;
    }

    public final XMLReader getPrimaryReader(){
        return m_primaryReader;
    }

    public void setPrimaryReader(XMLReader reader){
        m_primaryReader=reader;
    }

    public Stack getContextNodeListsStack(){
        return m_contextNodeLists;
    }

    public void setContextNodeListsStack(Stack s){
        m_contextNodeLists=s;
    }

    public final void pushContextNodeList(DTMIterator nl){
        m_contextNodeLists.push(nl);
    }

    public final void popContextNodeList(){
        if(m_contextNodeLists.isEmpty())
            System.err.println("Warning: popContextNodeList when stack is empty!");
        else
            m_contextNodeLists.pop();
    }

    public IntStack getCurrentNodeStack(){
        return m_currentNodes;
    }

    public void setCurrentNodeStack(IntStack nv){
        m_currentNodes=nv;
    }

    public final void pushCurrentNodeAndExpression(int cn,int en){
        m_currentNodes.push(cn);
        m_currentExpressionNodes.push(cn);
    }

    public final void popCurrentNodeAndExpression(){
        m_currentNodes.quickPop(1);
        m_currentExpressionNodes.quickPop(1);
    }

    public final void pushExpressionState(int cn,int en,PrefixResolver nc){
        m_currentNodes.push(cn);
        m_currentExpressionNodes.push(cn);
        m_prefixResolvers.push(nc);
    }

    public final void popExpressionState(){
        m_currentNodes.quickPop(1);
        m_currentExpressionNodes.quickPop(1);
        m_prefixResolvers.pop();
    }

    public final void pushCurrentNode(int n){
        m_currentNodes.push(n);
    }

    public final void popCurrentNode(){
        m_currentNodes.quickPop(1);
    }

    public final void pushPredicateRoot(int n){
        m_predicateRoots.push(n);
    }

    public final void popPredicateRoot(){
        m_predicateRoots.popQuick();
    }

    public final int getPredicateRoot(){
        return m_predicateRoots.peepOrNull();
    }

    public final void pushIteratorRoot(int n){
        m_iteratorRoots.push(n);
    }

    public final void popIteratorRoot(){
        m_iteratorRoots.popQuick();
    }

    public final int getIteratorRoot(){
        return m_iteratorRoots.peepOrNull();
    }

    public IntStack getCurrentExpressionNodeStack(){
        return m_currentExpressionNodes;
    }

    public void setCurrentExpressionNodeStack(IntStack nv){
        m_currentExpressionNodes=nv;
    }

    public final int getPredicatePos(){
        return m_predicatePos.peek();
    }

    public final void pushPredicatePos(int n){
        m_predicatePos.push(n);
    }

    public final void popPredicatePos(){
        m_predicatePos.pop();
    }

    public final int getCurrentExpressionNode(){
        return m_currentExpressionNodes.peek();
    }

    public final void pushCurrentExpressionNode(int n){
        m_currentExpressionNodes.push(n);
    }

    public final void popCurrentExpressionNode(){
        m_currentExpressionNodes.quickPop(1);
    }

    public final PrefixResolver getNamespaceContext(){
        return (PrefixResolver)m_prefixResolvers.peek();
    }

    public final void setNamespaceContext(PrefixResolver pr){
        m_prefixResolvers.setTop(pr);
    }

    public final void pushNamespaceContext(PrefixResolver pr){
        m_prefixResolvers.push(pr);
    }

    public final void pushNamespaceContextNull(){
        m_prefixResolvers.push(null);
    }

    public final void popNamespaceContext(){
        m_prefixResolvers.pop();
    }

    public Stack getAxesIteratorStackStacks(){
        return m_axesIteratorStack;
    }

    public void setAxesIteratorStackStacks(Stack s){
        m_axesIteratorStack=s;
    }

    public final void pushSubContextList(SubContextList iter){
        m_axesIteratorStack.push(iter);
    }

    public final void popSubContextList(){
        m_axesIteratorStack.pop();
    }

    public SubContextList getSubContextList(){
        return m_axesIteratorStack.isEmpty()
                ?null:(SubContextList)m_axesIteratorStack.peek();
    }

    public SubContextList getCurrentNodeList(){
        return m_axesIteratorStack.isEmpty()
                ?null:(SubContextList)m_axesIteratorStack.elementAt(0);
    }
    //==========================================================
    // SECTION: Implementation of ExpressionContext interface
    //==========================================================

    public final int getContextNode(){
        return this.getCurrentNode();
    }

    public final int getCurrentNode(){
        return m_currentNodes.peek();
    }

    public final DTMIterator getContextNodes(){
        try{
            DTMIterator cnl=getContextNodeList();
            if(null!=cnl)
                return cnl.cloneWithReset();
            else
                return null;  // for now... this might ought to be an empty iterator.
        }catch(CloneNotSupportedException cnse){
            return null;  // error reporting?
        }
    }

    public final DTMIterator getContextNodeList(){
        if(m_contextNodeLists.size()>0)
            return (DTMIterator)m_contextNodeLists.peek();
        else
            return null;
    }

    public ExpressionContext getExpressionContext(){
        return expressionContext;
    }

    public DTM getGlobalRTFDTM(){
        // We probably should _NOT_ be applying whitespace filtering at this stage!
        //
        // Some magic has been applied in DTMManagerDefault to recognize this set of options
        // and generate an instance of DTM which can contain multiple documents
        // (SAX2RTFDTM). Perhaps not the optimal way of achieving that result, but
        // I didn't want to change the manager API at this time, or expose
        // too many dependencies on its internals. (Ideally, I'd like to move
        // isTreeIncomplete all the way up to DTM, so we wouldn't need to explicitly
        // specify the subclass here.)
        // If it doesn't exist, or if the one already existing is in the middle of
        // being constructed, we need to obtain a new DTM to write into. I'm not sure
        // the latter will ever arise, but I'd rather be just a bit paranoid..
        if(m_global_rtfdtm==null||m_global_rtfdtm.isTreeIncomplete()){
            m_global_rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
        }
        return m_global_rtfdtm;
    }

    public void pushRTFContext(){
        m_last_pushed_rtfdtm.push(m_which_rtfdtm);
        if(null!=m_rtfdtm_stack)
            ((SAX2RTFDTM)(getRTFDTM())).pushRewindMark();
    }

    public DTM getRTFDTM(){
        SAX2RTFDTM rtfdtm;
        // We probably should _NOT_ be applying whitespace filtering at this stage!
        //
        // Some magic has been applied in DTMManagerDefault to recognize this set of options
        // and generate an instance of DTM which can contain multiple documents
        // (SAX2RTFDTM). Perhaps not the optimal way of achieving that result, but
        // I didn't want to change the manager API at this time, or expose
        // too many dependencies on its internals. (Ideally, I'd like to move
        // isTreeIncomplete all the way up to DTM, so we wouldn't need to explicitly
        // specify the subclass here.)
        if(m_rtfdtm_stack==null){
            m_rtfdtm_stack=new Vector();
            rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
            m_rtfdtm_stack.addElement(rtfdtm);
            ++m_which_rtfdtm;
        }else if(m_which_rtfdtm<0){
            rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(++m_which_rtfdtm);
        }else{
            rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(m_which_rtfdtm);
            // It might already be under construction -- the classic example would be
            // an xsl:variable which uses xsl:call-template as part of its value. To
            // handle this recursion, we have to start a new RTF DTM, pushing the old
            // one onto a stack so we can return to it. This is not as uncommon a case
            // as we might wish, unfortunately, as some folks insist on coding XSLT
            // as if it were a procedural language...
            if(rtfdtm.isTreeIncomplete()){
                if(++m_which_rtfdtm<m_rtfdtm_stack.size())
                    rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(m_which_rtfdtm);
                else{
                    rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
                    m_rtfdtm_stack.addElement(rtfdtm);
                }
            }
        }
        return rtfdtm;
    }

    public void popRTFContext(){
        int previous=m_last_pushed_rtfdtm.pop();
        if(null==m_rtfdtm_stack)
            return;
        if(m_which_rtfdtm==previous){
            if(previous>=0) // guard against none-active
            {
                boolean isEmpty=((SAX2RTFDTM)(m_rtfdtm_stack.elementAt(previous))).popRewindMark();
            }
        }else while(m_which_rtfdtm!=previous){
            // Empty each DTM before popping, so it's ready for reuse
            // _DON'T_ pop the previous, since it's still open (which is why we
            // stacked up more of these) and did not receive a mark.
            boolean isEmpty=((SAX2RTFDTM)(m_rtfdtm_stack.elementAt(m_which_rtfdtm))).popRewindMark();
            --m_which_rtfdtm;
        }
    }

    public DTMXRTreeFrag getDTMXRTreeFrag(int dtmIdentity){
        if(m_DTMXRTreeFrags==null){
            m_DTMXRTreeFrags=new HashMap();
        }
        if(m_DTMXRTreeFrags.containsKey(new Integer(dtmIdentity))){
            return (DTMXRTreeFrag)m_DTMXRTreeFrags.get(new Integer(dtmIdentity));
        }else{
            final DTMXRTreeFrag frag=new DTMXRTreeFrag(dtmIdentity,this);
            m_DTMXRTreeFrags.put(new Integer(dtmIdentity),frag);
            return frag;
        }
    }

    public class XPathExpressionContext implements ExpressionContext{
        public DTMManager getDTMManager(){
            return m_dtmManager;
        }

        public org.w3c.dom.Node getContextNode(){
            int context=getCurrentNode();
            return getDTM(context).getNode(context);
        }

        public org.w3c.dom.traversal.NodeIterator getContextNodes(){
            return new com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator(getContextNodeList());
        }

        public ErrorListener getErrorListener(){
            return XPathContext.this.getErrorListener();
        }

        public double toNumber(org.w3c.dom.Node n){
            // %REVIEW% You can't get much uglier than this...
            int nodeHandle=getDTMHandleFromNode(n);
            DTM dtm=getDTM(nodeHandle);
            XString xobj=(XString)dtm.getStringValue(nodeHandle);
            return xobj.num();
        }

        public String toString(org.w3c.dom.Node n){
            // %REVIEW% You can't get much uglier than this...
            int nodeHandle=getDTMHandleFromNode(n);
            DTM dtm=getDTM(nodeHandle);
            XMLString strVal=dtm.getStringValue(nodeHandle);
            return strVal.toString();
        }

        public final XObject getVariableOrParam(com.sun.org.apache.xml.internal.utils.QName qname)
                throws TransformerException{
            return m_variableStacks.getVariableOrParam(XPathContext.this,qname);
        }

        public XPathContext getXPathContext(){
            return XPathContext.this;
        }

        public boolean useServicesMechnism(){
            return m_useServicesMechanism;
        }

        public void setServicesMechnism(boolean flag){
            m_useServicesMechanism=flag;
        }
    }
}
