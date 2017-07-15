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
 * $Id: NodeVector.java,v 1.2.4.1 2005/09/15 08:15:50 suresh_emailid Exp $
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
 * $Id: NodeVector.java,v 1.2.4.1 2005/09/15 08:15:50 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.dtm.DTM;

import java.io.Serializable;

public class NodeVector implements Serializable, Cloneable{
    static final long serialVersionUID=-713473092200731870L;
    protected int m_firstFree=0;
    private int m_blocksize;
    private int m_map[];
    private int m_mapSize;  // lazy initialization

    public NodeVector(){
        m_blocksize=32;
        m_mapSize=0;
    }

    public NodeVector(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=0;
    }

    public Object clone() throws CloneNotSupportedException{
        NodeVector clone=(NodeVector)super.clone();
        if((null!=this.m_map)&&(this.m_map==clone.m_map)){
            clone.m_map=new int[this.m_map.length];
            System.arraycopy(this.m_map,0,clone.m_map,0,this.m_map.length);
        }
        return clone;
    }

    public int size(){
        return m_firstFree;
    }

    public final void push(int value){
        int ff=m_firstFree;
        if((ff+1)>=m_mapSize){
            if(null==m_map){
                m_map=new int[m_blocksize];
                m_mapSize=m_blocksize;
            }else{
                m_mapSize+=m_blocksize;
                int newMap[]=new int[m_mapSize];
                System.arraycopy(m_map,0,newMap,0,ff+1);
                m_map=newMap;
            }
        }
        m_map[ff]=value;
        ff++;
        m_firstFree=ff;
    }

    public final int pop(){
        m_firstFree--;
        int n=m_map[m_firstFree];
        m_map[m_firstFree]=DTM.NULL;
        return n;
    }

    public final int popAndTop(){
        m_firstFree--;
        m_map[m_firstFree]=DTM.NULL;
        return (m_firstFree==0)?DTM.NULL:m_map[m_firstFree-1];
    }

    public final void popQuick(){
        m_firstFree--;
        m_map[m_firstFree]=DTM.NULL;
    }

    public final int peepOrNull(){
        return ((null!=m_map)&&(m_firstFree>0))
                ?m_map[m_firstFree-1]:DTM.NULL;
    }

    public final void pushPair(int v1,int v2){
        if(null==m_map){
            m_map=new int[m_blocksize];
            m_mapSize=m_blocksize;
        }else{
            if((m_firstFree+2)>=m_mapSize){
                m_mapSize+=m_blocksize;
                int newMap[]=new int[m_mapSize];
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
        m_map[m_firstFree]=DTM.NULL;
        m_map[m_firstFree+1]=DTM.NULL;
    }

    public final void setTail(int n){
        m_map[m_firstFree-1]=n;
    }

    public final void setTailSub1(int n){
        m_map[m_firstFree-2]=n;
    }

    public final int peepTail(){
        return m_map[m_firstFree-1];
    }

    public final int peepTailSub1(){
        return m_map[m_firstFree-2];
    }

    public void insertInOrder(int value){
        for(int i=0;i<m_firstFree;i++){
            if(value<m_map[i]){
                insertElementAt(value,i);
                return;
            }
        }
        addElement(value);
    }

    public void addElement(int value){
        if((m_firstFree+1)>=m_mapSize){
            if(null==m_map){
                m_map=new int[m_blocksize];
                m_mapSize=m_blocksize;
            }else{
                m_mapSize+=m_blocksize;
                int newMap[]=new int[m_mapSize];
                System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
                m_map=newMap;
            }
        }
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public void insertElementAt(int value,int at){
        if(null==m_map){
            m_map=new int[m_blocksize];
            m_mapSize=m_blocksize;
        }else if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            int newMap[]=new int[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        if(at<=(m_firstFree-1)){
            System.arraycopy(m_map,at,m_map,at+1,m_firstFree-at);
        }
        m_map[at]=value;
        m_firstFree++;
    }

    public void appendNodes(NodeVector nodes){
        int nNodes=nodes.size();
        if(null==m_map){
            m_mapSize=nNodes+m_blocksize;
            m_map=new int[m_mapSize];
        }else if((m_firstFree+nNodes)>=m_mapSize){
            m_mapSize+=(nNodes+m_blocksize);
            int newMap[]=new int[m_mapSize];
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
            m_map[i]=DTM.NULL;
        }
        m_firstFree=0;
    }

    public void RemoveAllNoClear(){
        if(null==m_map)
            return;
        m_firstFree=0;
    }

    public boolean removeElement(int s){
        if(null==m_map)
            return false;
        for(int i=0;i<m_firstFree;i++){
            int node=m_map[i];
            if(node==s){
                if(i>m_firstFree)
                    System.arraycopy(m_map,i+1,m_map,i-1,m_firstFree-i);
                else
                    m_map[i]=DTM.NULL;
                m_firstFree--;
                return true;
            }
        }
        return false;
    }

    public void removeElementAt(int i){
        if(null==m_map)
            return;
        if(i>m_firstFree)
            System.arraycopy(m_map,i+1,m_map,i-1,m_firstFree-i);
        else
            m_map[i]=DTM.NULL;
    }

    public void setElementAt(int node,int index){
        if(null==m_map){
            m_map=new int[m_blocksize];
            m_mapSize=m_blocksize;
        }
        if(index==-1)
            addElement(node);
        m_map[index]=node;
    }

    public int elementAt(int i){
        if(null==m_map)
            return DTM.NULL;
        return m_map[i];
    }

    public boolean contains(int s){
        if(null==m_map)
            return false;
        for(int i=0;i<m_firstFree;i++){
            int node=m_map[i];
            if(node==s)
                return true;
        }
        return false;
    }

    public int indexOf(int elem,int index){
        if(null==m_map)
            return -1;
        for(int i=index;i<m_firstFree;i++){
            int node=m_map[i];
            if(node==elem)
                return i;
        }
        return -1;
    }

    public int indexOf(int elem){
        if(null==m_map)
            return -1;
        for(int i=0;i<m_firstFree;i++){
            int node=m_map[i];
            if(node==elem)
                return i;
        }
        return -1;
    }

    public void sort(int a[],int lo0,int hi0) throws Exception{
        int lo=lo0;
        int hi=hi0;
        // pause(lo, hi);
        if(lo>=hi){
            return;
        }else if(lo==hi-1){
            /**
             *  sort a two element list by swapping if necessary
             */
            if(a[lo]>a[hi]){
                int T=a[lo];
                a[lo]=a[hi];
                a[hi]=T;
            }
            return;
        }
        /**
         *  Pick a pivot and move it out of the way
         */
        int pivot=a[(lo+hi)/2];
        a[(lo+hi)/2]=a[hi];
        a[hi]=pivot;
        while(lo<hi){
            /**
             *  Search forward from a[lo] until an element is found that
             *  is greater than the pivot or lo >= hi
             */
            while(a[lo]<=pivot&&lo<hi){
                lo++;
            }
            /**
             *  Search backward from a[hi] until element is found that
             *  is less than the pivot, or lo >= hi
             */
            while(pivot<=a[hi]&&lo<hi){
                hi--;
            }
            /**
             *  Swap elements a[lo] and a[hi]
             */
            if(lo<hi){
                int T=a[lo];
                a[lo]=a[hi];
                a[hi]=T;
                // pause();
            }
            // if (stopRequested) {
            //    return;
            // }
        }
        /**
         *  Put the median in the "center" of the list
         */
        a[hi0]=a[hi];
        a[hi]=pivot;
        /**
         *  Recursive calls, elements a[lo0] to a[lo-1] are less than or
         *  equal to pivot, elements a[hi+1] to a[hi0] are greater than
         *  pivot.
         */
        sort(a,lo0,lo-1);
        sort(a,hi+1,hi0);
    }

    public void sort() throws Exception{
        sort(m_map,0,m_firstFree-1);
    }
}
