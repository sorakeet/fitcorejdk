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
 * $Id: NodeSet.java,v 1.2.4.1 2005/09/10 17:39:49 jeffsuttor Exp $
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
 * $Id: NodeSet.java,v 1.2.4.1 2005/09/10 17:39:49 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.DOM2Helper;
import com.sun.org.apache.xpath.internal.axes.ContextNodeList;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class NodeSet
        implements NodeList, NodeIterator, Cloneable, ContextNodeList{
    transient protected int m_next=0;
    transient protected boolean m_mutable=true;
    transient protected boolean m_cacheNodes=true;
    protected int m_firstFree=0;
    Node m_map[];
    transient private int m_last=0;
    private int m_blocksize;
    private int m_mapSize;  // lazy initialization

    public NodeSet(){
        m_blocksize=32;
        m_mapSize=0;
    }

    public NodeSet(NodeList nodelist){
        this(32);
        addNodes(nodelist);
    }

    public NodeSet(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=0;
    }

    public void addNodes(NodeList nodelist){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if(null!=nodelist)  // defensive to fix a bug that Sanjiva reported.
        {
            int nChildren=nodelist.getLength();
            for(int i=0;i<nChildren;i++){
                Node obj=nodelist.item(i);
                if(null!=obj){
                    addElement(obj);
                }
            }
        }
        // checkDups();
    }

    public void addElement(Node value){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if((m_firstFree+1)>=m_mapSize){
            if(null==m_map){
                m_map=new Node[m_blocksize];
                m_mapSize=m_blocksize;
            }else{
                m_mapSize+=m_blocksize;
                Node newMap[]=new Node[m_mapSize];
                System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
                m_map=newMap;
            }
        }
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public NodeSet(NodeSet nodelist){
        this(32);
        addNodes((NodeIterator)nodelist);
    }

    public void addNodes(NodeIterator iterator){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if(null!=iterator)  // defensive to fix a bug that Sanjiva reported.
        {
            Node obj;
            while(null!=(obj=iterator.nextNode())){
                addElement(obj);
            }
        }
        // checkDups();
    }

    public NodeSet(NodeIterator ni){
        this(32);
        addNodes(ni);
    }

    public NodeSet(Node node){
        this(32);
        addNode(node);
    }

    public void addNode(Node n){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        this.addElement(n);
    }

    public Node getRoot(){
        return null;
    }

    public int getWhatToShow(){
        return NodeFilter.SHOW_ALL&~NodeFilter.SHOW_ENTITY_REFERENCE;
    }

    public NodeFilter getFilter(){
        return null;
    }

    public boolean getExpandEntityReferences(){
        return true;
    }

    public Node nextNode() throws DOMException{
        if((m_next)<this.size()){
            Node next=this.elementAt(m_next);
            m_next++;
            return next;
        }else
            return null;
    }

    public Node previousNode() throws DOMException{
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_ITERATE,null)); //"This NodeSet can not iterate to a previous node!");
        if((m_next-1)>0){
            m_next--;
            return this.elementAt(m_next);
        }else
            return null;
    }

    public void detach(){
    }

    public Node elementAt(int i){
        if(null==m_map)
            return null;
        return m_map[i];
    }

    public void insertNode(Node n,int pos){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        insertElementAt(n,pos);
    }

    public void insertElementAt(Node value,int at){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if(null==m_map){
            m_map=new Node[m_blocksize];
            m_mapSize=m_blocksize;
        }else if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            Node newMap[]=new Node[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        if(at<=(m_firstFree-1)){
            System.arraycopy(m_map,at,m_map,at+1,m_firstFree-at);
        }
        m_map[at]=value;
        m_firstFree++;
    }

    public void removeNode(Node n){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        this.removeElement(n);
    }

    public boolean removeElement(Node s){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if(null==m_map)
            return false;
        for(int i=0;i<m_firstFree;i++){
            Node node=m_map[i];
            if((null!=node)&&node.equals(s)){
                if(i<m_firstFree-1)
                    System.arraycopy(m_map,i+1,m_map,i,m_firstFree-i-1);
                m_firstFree--;
                m_map[m_firstFree]=null;
                return true;
            }
        }
        return false;
    }

    public void addNodes(NodeSet ns){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        addNodes((NodeIterator)ns);
    }

    public void addNodesInDocOrder(NodeList nodelist,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        int nChildren=nodelist.getLength();
        for(int i=0;i<nChildren;i++){
            Node node=nodelist.item(i);
            if(null!=node){
                addNodeInDocOrder(node,support);
            }
        }
    }

    public int addNodeInDocOrder(Node node,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        return addNodeInDocOrder(node,true,support);
    }  // end addNodeInDocOrder(Vector v, Object obj)

    public int addNodeInDocOrder(Node node,boolean test,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null)); //"This NodeSet is not mutable!");
        int insertIndex=-1;
        if(test){
            // This needs to do a binary search, but a binary search
            // is somewhat tough because the sequence test involves
            // two nodes.
            int size=size(), i;
            for(i=size-1;i>=0;i--){
                Node child=(Node)elementAt(i);
                if(child==node){
                    i=-2;  // Duplicate, suppress insert
                    break;
                }
                if(!DOM2Helper.isNodeAfter(node,child)){
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
                if(this.item(i).equals(node)){
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

    public Node item(int index){
        runTo(index);
        return (Node)this.elementAt(index);
    }

    public int getLength(){
        runTo(-1);
        return this.size();
    }

    public void addNodesInDocOrder(NodeIterator iterator,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        Node node;
        while(null!=(node=iterator.nextNode())){
            addNodeInDocOrder(node,support);
        }
    }

    private boolean addNodesInDocOrder(int start,int end,int testIndex,
                                       NodeList nodelist,XPathContext support){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        boolean foundit=false;
        int i;
        Node node=nodelist.item(testIndex);
        for(i=end;i>=start;i--){
            Node child=(Node)elementAt(i);
            if(child==node){
                i=-2;  // Duplicate, suppress insert
                break;
            }
            if(!DOM2Helper.isNodeAfter(node,child)){
                insertElementAt(node,i+1);
                testIndex--;
                if(testIndex>0){
                    boolean foundPrev=addNodesInDocOrder(0,i,testIndex,nodelist,
                            support);
                    if(!foundPrev){
                        addNodesInDocOrder(i,size()-1,testIndex,nodelist,support);
                    }
                }
                break;
            }
        }
        if(i==-1){
            insertElementAt(node,0);
        }
        return foundit;
    }

    public Node getCurrentNode(){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX,null)); //"This NodeSet can not do indexing or counting functions!");
        int saved=m_next;
        Node n=(m_next<m_firstFree)?elementAt(m_next):null;
        m_next=saved; // HACK: I think this is a bit of a hack.  -sb
        return n;
    }

    public int getCurrentPos(){
        return m_next;
    }

    public void reset(){
        m_next=0;
    }

    public void setCurrentPos(int i){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX,null)); //"This NodeSet can not do indexing or counting functions!");
        m_next=i;
    }

    public int size(){
        return m_firstFree;
    }

    public boolean isFresh(){
        return (m_next==0);
    }

    public NodeIterator cloneWithReset() throws CloneNotSupportedException{
        NodeSet clone=(NodeSet)clone();
        clone.reset();
        return clone;
    }

    public int getLast(){
        return m_last;
    }

    public void setLast(int last){
        m_last=last;
    }

    public Object clone() throws CloneNotSupportedException{
        NodeSet clone=(NodeSet)super.clone();
        if((null!=this.m_map)&&(this.m_map==clone.m_map)){
            clone.m_map=new Node[this.m_map.length];
            System.arraycopy(this.m_map,0,clone.m_map,0,this.m_map.length);
        }
        return clone;
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

    public void runTo(int index){
        if(!m_cacheNodes)
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX,null)); //"This NodeSet can not do indexing or counting functions!");
        if((index>=0)&&(m_next<m_firstFree))
            m_next=index;
        else
            m_next=m_firstFree-1;
    }

    public final void push(Node value){
        int ff=m_firstFree;
        if((ff+1)>=m_mapSize){
            if(null==m_map){
                m_map=new Node[m_blocksize];
                m_mapSize=m_blocksize;
            }else{
                m_mapSize+=m_blocksize;
                Node newMap[]=new Node[m_mapSize];
                System.arraycopy(m_map,0,newMap,0,ff+1);
                m_map=newMap;
            }
        }
        m_map[ff]=value;
        ff++;
        m_firstFree=ff;
    }

    public final Node pop(){
        m_firstFree--;
        Node n=m_map[m_firstFree];
        m_map[m_firstFree]=null;
        return n;
    }

    public final Node popAndTop(){
        m_firstFree--;
        m_map[m_firstFree]=null;
        return (m_firstFree==0)?null:m_map[m_firstFree-1];
    }

    public final void popQuick(){
        m_firstFree--;
        m_map[m_firstFree]=null;
    }

    public final Node peepOrNull(){
        return ((null!=m_map)&&(m_firstFree>0))
                ?m_map[m_firstFree-1]:null;
    }

    public final void pushPair(Node v1,Node v2){
        if(null==m_map){
            m_map=new Node[m_blocksize];
            m_mapSize=m_blocksize;
        }else{
            if((m_firstFree+2)>=m_mapSize){
                m_mapSize+=m_blocksize;
                Node newMap[]=new Node[m_mapSize];
                System.arraycopy(m_map,0,newMap,0,m_firstFree);
                m_map=newMap;
            }
        }
        m_map[m_firstFree]=v1;
        m_map[m_firstFree+1]=v2;
        m_firstFree+=2;
    }

    public final void popPair(){
        m_firstFree-=2;
        m_map[m_firstFree]=null;
        m_map[m_firstFree+1]=null;
    }

    public final void setTail(Node n){
        m_map[m_firstFree-1]=n;
    }

    public final void setTailSub1(Node n){
        m_map[m_firstFree-2]=n;
    }

    public final Node peepTail(){
        return m_map[m_firstFree-1];
    }

    public final Node peepTailSub1(){
        return m_map[m_firstFree-2];
    }

    public void appendNodes(NodeSet nodes){
        int nNodes=nodes.size();
        if(null==m_map){
            m_mapSize=nNodes+m_blocksize;
            m_map=new Node[m_mapSize];
        }else if((m_firstFree+nNodes)>=m_mapSize){
            m_mapSize+=(nNodes+m_blocksize);
            Node newMap[]=new Node[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+nNodes);
            m_map=newMap;
        }
        System.arraycopy(nodes.m_map,0,m_map,m_firstFree,nNodes);
        m_firstFree+=nNodes;
    }

    public void removeAllElements(){
        if(null==m_map)
            return;
        for(int i=0;i<m_firstFree;i++){
            m_map[i]=null;
        }
        m_firstFree=0;
    }

    public void removeElementAt(int i){
        if(null==m_map)
            return;
        if(i>=m_firstFree)
            throw new ArrayIndexOutOfBoundsException(i+" >= "+m_firstFree);
        else if(i<0)
            throw new ArrayIndexOutOfBoundsException(i);
        if(i<m_firstFree-1)
            System.arraycopy(m_map,i+1,m_map,i,m_firstFree-i-1);
        m_firstFree--;
        m_map[m_firstFree]=null;
    }

    public void setElementAt(Node node,int index){
        if(!m_mutable)
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE,null)); //"This NodeSet is not mutable!");
        if(null==m_map){
            m_map=new Node[m_blocksize];
            m_mapSize=m_blocksize;
        }
        m_map[index]=node;
    }

    public boolean contains(Node s){
        runTo(-1);
        if(null==m_map)
            return false;
        for(int i=0;i<m_firstFree;i++){
            Node node=m_map[i];
            if((null!=node)&&node.equals(s))
                return true;
        }
        return false;
    }

    public int indexOf(Node elem,int index){
        runTo(-1);
        if(null==m_map)
            return -1;
        for(int i=index;i<m_firstFree;i++){
            Node node=m_map[i];
            if((null!=node)&&node.equals(elem))
                return i;
        }
        return -1;
    }

    public int indexOf(Node elem){
        runTo(-1);
        if(null==m_map)
            return -1;
        for(int i=0;i<m_firstFree;i++){
            Node node=m_map[i];
            if((null!=node)&&node.equals(elem))
                return i;
        }
        return -1;
    }
}
