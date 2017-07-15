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
 * $Id: IntVector.java,v 1.2.4.1 2005/09/15 08:15:45 suresh_emailid Exp $
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
 * $Id: IntVector.java,v 1.2.4.1 2005/09/15 08:15:45 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class IntVector implements Cloneable{
    protected int m_blocksize;
    protected int m_map[]; // IntStack is trying to see this directly
    protected int m_firstFree=0;
    protected int m_mapSize;

    public IntVector(){
        m_blocksize=32;
        m_mapSize=m_blocksize;
        m_map=new int[m_blocksize];
    }

    public IntVector(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=blocksize;
        m_map=new int[blocksize];
    }

    public IntVector(int blocksize,int increaseSize){
        m_blocksize=increaseSize;
        m_mapSize=blocksize;
        m_map=new int[blocksize];
    }

    public IntVector(IntVector v){
        m_map=new int[v.m_mapSize];
        m_mapSize=v.m_mapSize;
        m_firstFree=v.m_firstFree;
        m_blocksize=v.m_blocksize;
        System.arraycopy(v.m_map,0,m_map,0,m_firstFree);
    }

    public final int size(){
        return m_firstFree;
    }

    public final void setSize(int sz){
        m_firstFree=sz;
    }

    public final void addElement(int value){
        if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            int newMap[]=new int[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public final void addElements(int value,int numberOfElements){
        if((m_firstFree+numberOfElements)>=m_mapSize){
            m_mapSize+=(m_blocksize+numberOfElements);
            int newMap[]=new int[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        for(int i=0;i<numberOfElements;i++){
            m_map[m_firstFree]=value;
            m_firstFree++;
        }
    }

    public final void addElements(int numberOfElements){
        if((m_firstFree+numberOfElements)>=m_mapSize){
            m_mapSize+=(m_blocksize+numberOfElements);
            int newMap[]=new int[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_firstFree+=numberOfElements;
    }

    public final void insertElementAt(int value,int at){
        if((m_firstFree+1)>=m_mapSize){
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

    public final void removeAllElements(){
        for(int i=0;i<m_firstFree;i++){
            m_map[i]=Integer.MIN_VALUE;
        }
        m_firstFree=0;
    }

    public final boolean removeElement(int s){
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i]==s){
                if((i+1)<m_firstFree)
                    System.arraycopy(m_map,i+1,m_map,i-1,m_firstFree-i);
                else
                    m_map[i]=Integer.MIN_VALUE;
                m_firstFree--;
                return true;
            }
        }
        return false;
    }

    public final void removeElementAt(int i){
        if(i>m_firstFree)
            System.arraycopy(m_map,i+1,m_map,i,m_firstFree);
        else
            m_map[i]=Integer.MIN_VALUE;
        m_firstFree--;
    }

    public final void setElementAt(int value,int index){
        m_map[index]=value;
    }

    public final int elementAt(int i){
        return m_map[i];
    }

    public final boolean contains(int s){
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i]==s)
                return true;
        }
        return false;
    }

    public final int indexOf(int elem,int index){
        for(int i=index;i<m_firstFree;i++){
            if(m_map[i]==elem)
                return i;
        }
        return Integer.MIN_VALUE;
    }

    public final int indexOf(int elem){
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i]==elem)
                return i;
        }
        return Integer.MIN_VALUE;
    }

    public final int lastIndexOf(int elem){
        for(int i=(m_firstFree-1);i>=0;i--){
            if(m_map[i]==elem)
                return i;
        }
        return Integer.MIN_VALUE;
    }

    public Object clone()
            throws CloneNotSupportedException{
        return new IntVector(this);
    }
}
