/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: NodeSequence.java,v 1.6 2007/01/12 19:26:42 spericas Exp $
 */
/**
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: NodeSequence.java,v 1.6 2007/01/12 19:26:42 spericas Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.utils.NodeVector;
import com.sun.org.apache.xpath.internal.NodeSetDTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;

import java.util.Vector;

public class NodeSequence extends XObject
        implements DTMIterator, Cloneable, PathComponent{
    static final long serialVersionUID=3866261934726581044L;
    protected int m_last=-1;
    protected int m_next=0;
    protected DTMIterator m_iter;
    protected DTMManager m_dtmMgr;
    private IteratorCache m_cache;

    private NodeSequence(DTMIterator iter,int context,XPathContext xctxt,boolean shouldCacheNodes){
        setIter(iter);
        setRoot(context,xctxt);
        setShouldCacheNodes(shouldCacheNodes);
    }

    public NodeSequence(Object nodeVector){
        super(nodeVector);
        if(nodeVector instanceof NodeVector){
            SetVector((NodeVector)nodeVector);
        }
        if(null!=nodeVector){
            assertion(nodeVector instanceof NodeVector,
                    "Must have a NodeVector as the object for NodeSequence!");
            if(nodeVector instanceof DTMIterator){
                setIter((DTMIterator)nodeVector);
                m_last=((DTMIterator)nodeVector).getLength();
            }
        }
    }

    protected void SetVector(NodeVector v){
        setObject(v);
    }

    protected void setObject(Object obj){
        if(obj instanceof NodeVector){
            // Keep our superclass informed of the current NodeVector
            // ... if we don't the smoketest fails (don't know why).
            super.setObject(obj);
            // A copy of the code of what SetVector() would do.
            NodeVector v=(NodeVector)obj;
            if(m_cache!=null){
                m_cache.setVector(v);
            }else if(v!=null){
                m_cache=new IteratorCache();
                m_cache.setVector(v);
            }
        }else if(obj instanceof IteratorCache){
            IteratorCache cache=(IteratorCache)obj;
            m_cache=cache;
            m_cache.increaseUseCount();
            // Keep our superclass informed of the current NodeVector
            super.setObject(cache.getVector());
        }else{
            super.setObject(obj);
        }
    }

    public void allowDetachToRelease(boolean allowRelease){
        if((false==allowRelease)&&!hasCache()){
            setShouldCacheNodes(true);
        }
        if(null!=m_iter)
            m_iter.allowDetachToRelease(allowRelease);
        super.allowDetachToRelease(allowRelease);
    }

    public void detach(){
        if(null!=m_iter)
            m_iter.detach();
        super.detach();
    }

    public void reset(){
        m_next=0;
        // not resetting the iterator on purpose!!!
    }

    public void fixupVariables(Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
    }
    // ==== Constructors ====

    public final void setIter(DTMIterator iter){
        m_iter=iter;
    }

    private NodeSequence(DTMManager dtmMgr){
        super(new NodeVector());
        m_last=0;
        m_dtmMgr=dtmMgr;
    }

    public NodeSequence(){
        return;
    }

    private IteratorCache getCache(){
        return m_cache;
    }

    private boolean cacheComplete(){
        final boolean complete;
        if(m_cache!=null){
            complete=m_cache.isComplete();
        }else{
            complete=false;
        }
        return complete;
    }

    private void markCacheComplete(){
        NodeVector nv=getVector();
        if(nv!=null){
            m_cache.setCacheComplete(true);
        }
    }

    public final DTMIterator getContainedIter(){
        return m_iter;
    }

    public DTM getDTM(int nodeHandle){
        DTMManager mgr=getDTMManager();
        if(null!=mgr)
            return getDTMManager().getDTM(nodeHandle);
        else{
            assertion(false,"Can not get a DTM Unless a DTMManager has been set!");
            return null;
        }
    }

    public DTMManager getDTMManager(){
        return m_dtmMgr;
    }

    public int getRoot(){
        if(null!=m_iter)
            return m_iter.getRoot();
        else{
            // NodeSetDTM will call this, and so it's not a good thing to throw
            // an assertion here.
            // assertion(false, "Can not get the root from a non-iterated NodeSequence!");
            return DTM.NULL;
        }
    }

    public void setRoot(int nodeHandle,Object environment){
        // If root is DTM.NULL, then something's wrong with the context
        if(nodeHandle==DTM.NULL){
            throw new RuntimeException("Unable to evaluate expression using "+
                    "this context");
        }
        if(null!=m_iter){
            XPathContext xctxt=(XPathContext)environment;
            m_dtmMgr=xctxt.getDTMManager();
            m_iter.setRoot(nodeHandle,environment);
            if(!m_iter.isDocOrdered()){
                if(!hasCache())
                    setShouldCacheNodes(true);
                runTo(-1);
                m_next=0;
            }
        }else
            assertion(false,"Can not setRoot on a non-iterated NodeSequence!");
    }

    public int getWhatToShow(){
        return hasCache()?(DTMFilter.SHOW_ALL&~DTMFilter.SHOW_ENTITY_REFERENCE)
                :m_iter.getWhatToShow();
    }

    public boolean hasCache(){
        final NodeVector nv=getVector();
        return (nv!=null);
    }

    protected NodeVector getVector(){
        NodeVector nv=(m_cache!=null)?m_cache.getVector():null;
        return nv;
    }

    public boolean getExpandEntityReferences(){
        if(null!=m_iter)
            return m_iter.getExpandEntityReferences();
        else
            return true;
    }

    public int nextNode(){
        // If the cache is on, and the node has already been found, then
        // just return from the list.
        NodeVector vec=getVector();
        if(null!=vec){
            // There is a cache
            if(m_next<vec.size()){
                // The node is in the cache, so just return it.
                int next=vec.elementAt(m_next);
                m_next++;
                return next;
            }else if(cacheComplete()||(-1!=m_last)||(null==m_iter)){
                m_next++;
                return DTM.NULL;
            }
        }
        if(null==m_iter)
            return DTM.NULL;
        int next=m_iter.nextNode();
        if(DTM.NULL!=next){
            if(hasCache()){
                if(m_iter.isDocOrdered()){
                    getVector().addElement(next);
                    m_next++;
                }else{
                    int insertIndex=addNodeInDocOrder(next);
                    if(insertIndex>=0)
                        m_next++;
                }
            }else
                m_next++;
        }else{
            // We have exhausted the iterator, and if there is a cache
            // it must have all nodes in it by now, so let the cache
            // know that it is complete.
            markCacheComplete();
            m_last=m_next;
            m_next++;
        }
        return next;
    }

    public int previousNode(){
        if(hasCache()){
            if(m_next<=0)
                return DTM.NULL;
            else{
                m_next--;
                return item(m_next);
            }
        }else{
            int n=m_iter.previousNode();
            m_next=m_iter.getCurrentPos();
            return m_next;
        }
    }

    public int getCurrentNode(){
        if(hasCache()){
            int currentIndex=m_next-1;
            NodeVector vec=getVector();
            if((currentIndex>=0)&&(currentIndex<vec.size()))
                return vec.elementAt(currentIndex);
            else
                return DTM.NULL;
        }
        if(null!=m_iter){
            return m_iter.getCurrentNode();
        }else
            return DTM.NULL;
    }

    public boolean isFresh(){
        return (0==m_next);
    }

    public void setShouldCacheNodes(boolean b){
        if(b){
            if(!hasCache()){
                SetVector(new NodeVector());
            }
//        else
//          getVector().RemoveAllNoClear();  // Is this good?
        }else
            SetVector(null);
    }

    public boolean isMutable(){
        return hasCache(); // though may be surprising if it also has an iterator!
    }

    public int getCurrentPos(){
        return m_next;
    }

    public void runTo(int index){
        int n;
        if(-1==index){
            int pos=m_next;
            while(DTM.NULL!=(n=nextNode())) ;
            m_next=pos;
        }else if(m_next==index){
            return;
        }else if(hasCache()&&index<getVector().size()){
            m_next=index;
        }else if((null==getVector())&&(index<m_next)){
            while((m_next>=index)&&DTM.NULL!=(n=previousNode())) ;
        }else{
            while((m_next<index)&&DTM.NULL!=(n=nextNode())) ;
        }
    }

    public void setCurrentPos(int i){
        runTo(i);
    }

    public int item(int index){
        setCurrentPos(index);
        int n=nextNode();
        m_next=index;
        return n;
    }

    public void setItem(int node,int index){
        NodeVector vec=getVector();
        if(null!=vec){
            int oldNode=vec.elementAt(index);
            if(oldNode!=node&&m_cache.useCount()>1){
                /** If we are going to set the node at the given index
                 * to a different value, and the cache is shared
                 * (has a use count greater than 1)
                 * then make a copy of the cache and use it
                 * so we don't overwrite the value for other
                 * users of the cache.
                 */
                IteratorCache newCache=new IteratorCache();
                final NodeVector nv;
                try{
                    nv=(NodeVector)vec.clone();
                }catch(CloneNotSupportedException e){
                    // This should never happen
                    e.printStackTrace();
                    RuntimeException rte=new RuntimeException(e.getMessage());
                    throw rte;
                }
                newCache.setVector(nv);
                newCache.setCacheComplete(true);
                m_cache=newCache;
                vec=nv;
                // Keep our superclass informed of the current NodeVector
                super.setObject(nv);
                /** When we get to here the new cache has
                 * a use count of 1 and when setting a
                 * bunch of values on the same NodeSequence,
                 * such as when sorting, we will keep setting
                 * values in that same copy which has a use count of 1.
                 */
            }
            vec.setElementAt(node,index);
            m_last=vec.size();
        }else
            m_iter.setItem(node,index);
    }

    public int getLength(){
        IteratorCache cache=getCache();
        if(cache!=null){
            // Nodes from the iterator are cached
            if(cache.isComplete()){
                // All of the nodes from the iterator are cached
                // so just return the number of nodes in the cache
                NodeVector nv=cache.getVector();
                return nv.size();
            }
            // If this NodeSequence wraps a mutable nodeset, then
            // m_last will not reflect the size of the nodeset if
            // it has been mutated...
            if(m_iter instanceof NodeSetDTM){
                return m_iter.getLength();
            }
            if(-1==m_last){
                int pos=m_next;
                runTo(-1);
                m_next=pos;
            }
            return m_last;
        }else{
            return (-1==m_last)?(m_last=m_iter.getLength()):m_last;
        }
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        NodeSequence seq=(NodeSequence)super.clone();
        seq.m_next=0;
        if(m_cache!=null){
            // In making this clone of an iterator we are making
            // another NodeSequence object it has a reference
            // to the same IteratorCache object as the original
            // so we need to remember that more than one
            // NodeSequence object shares the cache.
            m_cache.increaseUseCount();
        }
        return seq;
    }

    public boolean isDocOrdered(){
        if(null!=m_iter)
            return m_iter.isDocOrdered();
        else
            return true; // can't be sure?
    }

    public int getAxis(){
        if(null!=m_iter)
            return m_iter.getAxis();
        else{
            assertion(false,"Can not getAxis from a non-iterated node sequence!");
            return 0;
        }
    }

    public Object clone() throws CloneNotSupportedException{
        NodeSequence clone=(NodeSequence)super.clone();
        if(null!=m_iter) clone.m_iter=(DTMIterator)m_iter.clone();
        if(m_cache!=null){
            // In making this clone of an iterator we are making
            // another NodeSequence object it has a reference
            // to the same IteratorCache object as the original
            // so we need to remember that more than one
            // NodeSequence object shares the cache.
            m_cache.increaseUseCount();
        }
        return clone;
    }

    public int getAnalysisBits(){
        if((null!=m_iter)&&(m_iter instanceof PathComponent))
            return ((PathComponent)m_iter).getAnalysisBits();
        else
            return 0;
    }

    protected int addNodeInDocOrder(int node){
        assertion(hasCache(),"addNodeInDocOrder must be done on a mutable sequence!");
        int insertIndex=-1;
        NodeVector vec=getVector();
        // This needs to do a binary search, but a binary search
        // is somewhat tough because the sequence test involves
        // two nodes.
        int size=vec.size(), i;
        for(i=size-1;i>=0;i--){
            int child=vec.elementAt(i);
            if(child==node){
                i=-2; // Duplicate, suppress insert
                break;
            }
            DTM dtm=m_dtmMgr.getDTM(node);
            if(!dtm.isNodeAfter(node,child)){
                break;
            }
        }
        if(i!=-2){
            insertIndex=i+1;
            vec.insertElementAt(node,insertIndex);
        }
        // checkDups();
        return insertIndex;
    } // end addNodeInDocOrder(Vector v, Object obj)

    protected IteratorCache getIteratorCache(){
        return m_cache;
    }

    private final static class IteratorCache{
        private NodeVector m_vec2;
        private boolean m_isComplete2;
        private int m_useCount2;

        IteratorCache(){
            m_vec2=null;
            m_isComplete2=false;
            m_useCount2=1;
            return;
        }

        private int useCount(){
            return m_useCount2;
        }

        private void increaseUseCount(){
            if(m_vec2!=null)
                m_useCount2++;
        }

        private NodeVector getVector(){
            return m_vec2;
        }

        private void setVector(NodeVector nv){
            m_vec2=nv;
            m_useCount2=1;
        }

        private void setCacheComplete(boolean b){
            m_isComplete2=b;
        }

        private boolean isComplete(){
            return m_isComplete2;
        }
    }
}
