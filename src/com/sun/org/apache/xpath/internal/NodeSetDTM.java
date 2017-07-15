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
 * $Id: NodeSetDTM.java,v 1.2.4.2 2005/09/14 20:30:06 jeffsuttor Exp $
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
 * $Id: NodeSetDTM.java,v 1.2.4.2 2005/09/14 20:30:06 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.utils.NodeVector;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class NodeSetDTM extends NodeVector
        implements /** NodeList, NodeIterator, */DTMIterator,
        Cloneable{
    static final long serialVersionUID=7686480133331317070L;
    transient protected int m_next=0;
    transient protected boolean m_mutable=true;
    // %TBD%
//  /**
//   * Create a NodeSetDTM, and copy the members of the
//   * given nodelist into it.
//   *
//   * @param nodelist List of Nodes to be made members of the new set.
//   */
//  public NodeSetDTM(NodeList nodelist)
//  {
//
//    super();
//
//    addNodes(nodelist);
//  }
    transient protected boolean m_cacheNodes=true;
    protected int m_root=DTM.NULL;
    DTMManager m_manager;
    transient private int m_last=0;

    public NodeSetDTM(DTMManager dtmManager){
        super();
        m_manager=dtmManager;
    }

    public NodeSetDTM(int blocksize,int dummy,DTMManager dtmManager){
        super(blocksize);
        m_manager=dtmManager;
    }

    public NodeSetDTM(NodeSetDTM nodelist){
        super();
        m_manager=nodelist.getDTMManager();
        m_root=nodelist.getRoot();
        addNodes((DTMIterator)nodelist);
    }

    public void addNodes(DTMIterator iterator){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        if(null!=iterator)  // defensive to fix a bug that Sanjiva reported.
        {
            int obj;
            while(DTM.NULL!=(obj=iterator.nextNode())){
                addElement(obj);
            }
        }
        // checkDups();
    }

    public NodeSetDTM(DTMIterator ni){
        super();
        m_manager=ni.getDTMManager();
        m_root=ni.getRoot();
        addNodes(ni);
    }

    public NodeSetDTM(NodeIterator iterator,XPathContext xctxt){
        super();
        Node node;
        m_manager=xctxt.getDTMManager();
        while(null!=(node=iterator.nextNode())){
            int handle=xctxt.getDTMHandleFromNode(node);
            addNodeInDocOrder(handle,xctxt);
        }
    }

    public int addNodeInDocOrder(int node,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        return addNodeInDocOrder(node,true,support);
    }  // end addNodeInDocOrder(Vector v, Object obj)

    public int addNodeInDocOrder(int node,boolean test,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        int insertIndex=-1;
        if(test){
            // This needs to do a binary search, but a binary search
            // is somewhat tough because the sequence test involves
            // two nodes.
            int size=size(), i;
            for(i=size-1;i>=0;i--){
                int child=elementAt(i);
                if(child==node){
                    i=-2;  // Duplicate, suppress insert
                    break;
                }
                DTM dtm=support.getDTM(node);
                if(!dtm.isNodeAfter(node,child)){
                    break;
                }
            }
            if(i!=-2){
                insertIndex=i+1;
                insertElementAt(node,insertIndex);
            }
        }else{
            insertIndex=this.size();
            boolean foundit=false;
            for(int i=0;i<insertIndex;i++){
                if(i==node){
                    foundit=true;
                    break;
                }
            }
            if(!foundit)
                addElement(node);
        }
        // checkDups();
        return insertIndex;
    }  // end addNodeInDocOrder(Vector v, Object obj)

    public NodeSetDTM(NodeList nodeList,XPathContext xctxt){
        super();
        m_manager=xctxt.getDTMManager();
        int n=nodeList.getLength();
        for(int i=0;i<n;i++){
            Node node=nodeList.item(i);
            int handle=xctxt.getDTMHandleFromNode(node);
            // Do not reorder or strip duplicate nodes from the given DOM nodelist
            addNode(handle); // addNodeInDocOrder(handle, xctxt);
        }
    }

    public void addNode(int n){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        this.addElement(n);
    }

    public NodeSetDTM(int node,DTMManager dtmManager){
        super();
        m_manager=dtmManager;
        addNode(node);
    }

    public void setEnvironment(Object environment){
        // no-op
    }

    public DTMFilter getFilter(){
        return null;
    }

    public DTM getDTM(int nodeHandle){
        return m_manager.getDTM(nodeHandle);
    }

    public DTMManager getDTMManager(){
        return m_manager;
    }

    public int getRoot(){
        if(DTM.NULL==m_root){
            if(size()>0)
                return item(0);
            else
                return DTM.NULL;
        }else
            return m_root;
    }

    public void setRoot(int context,Object environment){
        // no-op, I guess...  (-sb)
    }

    public void reset(){
        m_next=0;
    }

    public int getWhatToShow(){
        return DTMFilter.SHOW_ALL&~DTMFilter.SHOW_ENTITY_REFERENCE;
    }

    public boolean getExpandEntityReferences(){
        return true;
    }

    public int nextNode(){
        if((m_next)<this.size()){
            int next=this.elementAt(m_next);
            m_next++;
            return next;
        }else
            return DTM.NULL;
    }

    public int previousNode(){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE,null)); //"This NodeSetDTM can not iterate to a previous node!");
        if((m_next-1)>0){
            m_next--;
            return this.elementAt(m_next);
        }else
            return DTM.NULL;
    }

    public void detach(){
    }

    public void allowDetachToRelease(boolean allowRelease){
        // no action for right now.
    }
    // %TBD%
//  /**
//   * Copy NodeList members into this nodelist, adding in
//   * document order.  If a node is null, don't add it.
//   *
//   * @param nodelist List of nodes which should now be referenced by
//   * this NodeSetDTM.
//   * @throws RuntimeException thrown if this NodeSetDTM is not of
//   * a mutable type.
//   */
//  public void addNodes(NodeList nodelist)
//  {
//
//    if (!m_mutable)
//      throw new RuntimeException("This NodeSetDTM is not mutable!");
//
//    if (null != nodelist)  // defensive to fix a bug that Sanjiva reported.
//    {
//      int nChildren = nodelist.getLength();
//
//      for (int i = 0; i < nChildren; i++)
//      {
//        int obj = nodelist.item(i);
//
//        if (null != obj)
//        {
//          addElement(obj);
//        }
//      }
//    }
//
//    // checkDups();
//  }
    // %TBD%
//  /**
//   * <p>Copy NodeList members into this nodelist, adding in
//   * document order.  Only genuine node references will be copied;
//   * nulls appearing in the source NodeSetDTM will
//   * not be added to this one. </p>
//   *
//   * <p> In case you're wondering why this function is needed: NodeSetDTM
//   * implements both DTMIterator and NodeList. If this method isn't
//   * provided, Java can't decide which of those to use when addNodes()
//   * is invoked. Providing the more-explicit match avoids that
//   * ambiguity.)</p>
//   *
//   * @param ns NodeSetDTM whose members should be merged into this NodeSetDTM.
//   * @throws RuntimeException thrown if this NodeSetDTM is not of
//   * a mutable type.
//   */
//  public void addNodes(NodeSetDTM ns)
//  {
//
//    if (!m_mutable)
//      throw new RuntimeException("This NodeSetDTM is not mutable!");
//
//    addNodes((DTMIterator) ns);
//  }

    public int getCurrentNode(){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    "This NodeSetDTM can not do indexing or counting functions!");
        int saved=m_next;
        // because nextNode always increments
        // But watch out for copy29, where the root iterator didn't
        // have nextNode called on it.
        int current=(m_next>0)?m_next-1:m_next;
        int n=(current<m_firstFree)?elementAt(current):DTM.NULL;
        m_next=saved; // HACK: I think this is a bit of a hack.  -sb
        return n;
    }
    // %TBD%
//  /**
//   * Copy NodeList members into this nodelist, adding in
//   * document order.  If a node is null, don't add it.
//   *
//   * @param nodelist List of nodes to be added
//   * @param support The XPath runtime context.
//   * @throws RuntimeException thrown if this NodeSetDTM is not of
//   * a mutable type.
//   */
//  public void addNodesInDocOrder(NodeList nodelist, XPathContext support)
//  {
//
//    if (!m_mutable)
//      throw new RuntimeException("This NodeSetDTM is not mutable!");
//
//    int nChildren = nodelist.getLength();
//
//    for (int i = 0; i < nChildren; i++)
//    {
//      int node = nodelist.item(i);
//
//      if (null != node)
//      {
//        addNodeInDocOrder(node, support);
//      }
//    }
//  }

    public boolean isFresh(){
        return (m_next==0);
    }
    // %TBD%
//  /**
//   * Add the node list to this node set in document order.
//   *
//   * @param start index.
//   * @param end index.
//   * @param testIndex index.
//   * @param nodelist The nodelist to add.
//   * @param support The XPath runtime context.
//   *
//   * @return false always.
//   * @throws RuntimeException thrown if this NodeSetDTM is not of
//   * a mutable type.
//   */
//  private boolean addNodesInDocOrder(int start, int end, int testIndex,
//                                     NodeList nodelist, XPathContext support)
//  {
//
//    if (!m_mutable)
//      throw new RuntimeException("This NodeSetDTM is not mutable!");
//
//    boolean foundit = false;
//    int i;
//    int node = nodelist.item(testIndex);
//
//    for (i = end; i >= start; i--)
//    {
//      int child = elementAt(i);
//
//      if (child == node)
//      {
//        i = -2;  // Duplicate, suppress insert
//
//        break;
//      }
//
//      if (!support.getDOMHelper().isNodeAfter(node, child))
//      {
//        insertElementAt(node, i + 1);
//
//        testIndex--;
//
//        if (testIndex > 0)
//        {
//          boolean foundPrev = addNodesInDocOrder(0, i, testIndex, nodelist,
//                                                 support);
//
//          if (!foundPrev)
//          {
//            addNodesInDocOrder(i, size() - 1, testIndex, nodelist, support);
//          }
//        }
//
//        break;
//      }
//    }
//
//    if (i == -1)
//    {
//      insertElementAt(node, 0);
//    }
//
//    return foundit;
//  }

    public void insertNode(int n,int pos){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        insertElementAt(n,pos);
    }

    public void removeNode(int n){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        this.removeElement(n);
    }

    public void addNodesInDocOrder(DTMIterator iterator,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        int node;
        while(DTM.NULL!=(node=iterator.nextNode())){
            addNodeInDocOrder(node,support);
        }
    }

    public boolean getShouldCacheNodes(){
        return m_cacheNodes;
    }

    public void setShouldCacheNodes(boolean b){
        if(!isFresh())
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_CALL_SETSHOULDCACHENODE,null)); //"Can not call setShouldCacheNodes after nextNode has been called!");
        m_cacheNodes=b;
        m_mutable=true;
    }

    public boolean isMutable(){
        return m_mutable;
    }

    public int getCurrentPos(){
        return m_next;
    }

    public void runTo(int index){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX,null)); //"This NodeSetDTM can not do indexing or counting functions!");
        if((index>=0)&&(m_next<m_firstFree))
            m_next=index;
        else
            m_next=m_firstFree-1;
    }

    public void setCurrentPos(int i){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX,null)); //"This NodeSetDTM can not do indexing or counting functions!");
        m_next=i;
    }

    public int item(int index){
        runTo(index);
        return this.elementAt(index);
    }

    public void setItem(int node,int index){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.setElementAt(node,index);
    }

    public int getLength(){
        runTo(-1);
        return this.size();
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        NodeSetDTM clone=(NodeSetDTM)clone();
        clone.reset();
        return clone;
    }

    public Object clone() throws CloneNotSupportedException{
        NodeSetDTM clone=(NodeSetDTM)super.clone();
        return clone;
    }

    public int size(){
        return super.size();
    }

    public void addElement(int value){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.addElement(value);
    }

    public void insertElementAt(int value,int at){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.insertElementAt(value,at);
    }

    public void appendNodes(NodeVector nodes){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.appendNodes(nodes);
    }

    public void removeAllElements(){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.removeAllElements();
    }

    public boolean removeElement(int s){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        return super.removeElement(s);
    }

    public void removeElementAt(int i){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.removeElementAt(i);
    }

    public void setElementAt(int node,int index){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE,null)); //"This NodeSetDTM is not mutable!");
        super.setElementAt(node,index);
    }

    public int elementAt(int i){
        runTo(i);
        return super.elementAt(i);
    }

    public boolean contains(int s){
        runTo(-1);
        return super.contains(s);
    }

    public int indexOf(int elem,int index){
        runTo(-1);
        return super.indexOf(elem,index);
    }

    public int indexOf(int elem){
        runTo(-1);
        return super.indexOf(elem);
    }

    public boolean isDocOrdered(){
        return true;
    }

    public int getAxis(){
        return -1;
    }

    public int getLast(){
        return m_last;
    }

    public void setLast(int last){
        m_last=last;
    }
}
