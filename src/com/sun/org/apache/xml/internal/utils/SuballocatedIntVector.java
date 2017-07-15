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
 * $Id: SuballocatedIntVector.java,v 1.3 2005/09/28 13:49:22 pvedula Exp $
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
 * $Id: SuballocatedIntVector.java,v 1.3 2005/09/28 13:49:22 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class SuballocatedIntVector{
    protected static final int NUMBLOCKS_DEFAULT=32;
    protected int m_blocksize;
    protected int m_SHIFT, m_MASK;
    protected int m_numblocks=NUMBLOCKS_DEFAULT;
    protected int m_map[][];
    protected int m_firstFree=0;
    protected int m_map0[];
    protected int m_buildCache[];
    protected int m_buildCacheStartIndex;

    public SuballocatedIntVector(){
        this(2048);
    }

    public SuballocatedIntVector(int blocksize){
        this(blocksize,NUMBLOCKS_DEFAULT);
    }

    public SuballocatedIntVector(int blocksize,int numblocks){
        //m_blocksize = blocksize;
        for(m_SHIFT=0;0!=(blocksize>>>=1);++m_SHIFT)
            ;
        m_blocksize=1<<m_SHIFT;
        m_MASK=m_blocksize-1;
        m_numblocks=numblocks;
        m_map0=new int[m_blocksize];
        m_map=new int[numblocks][];
        m_map[0]=m_map0;
        m_buildCache=m_map0;
        m_buildCacheStartIndex=0;
    }

    public int size(){
        return m_firstFree;
    }

    public void setSize(int sz){
        if(m_firstFree>sz) // Whups; had that backward!
            m_firstFree=sz;
    }

    private void addElements(int value,int numberOfElements){
        if(m_firstFree+numberOfElements<m_blocksize)
            for(int i=0;i<numberOfElements;i++){
                m_map0[m_firstFree++]=value;
            }
        else{
            int index=m_firstFree>>>m_SHIFT;
            int offset=m_firstFree&m_MASK;
            m_firstFree+=numberOfElements;
            while(numberOfElements>0){
                if(index>=m_map.length){
                    int newsize=index+m_numblocks;
                    int[][] newMap=new int[newsize][];
                    System.arraycopy(m_map,0,newMap,0,m_map.length);
                    m_map=newMap;
                }
                int[] block=m_map[index];
                if(null==block)
                    block=m_map[index]=new int[m_blocksize];
                int copied=(m_blocksize-offset<numberOfElements)
                        ?m_blocksize-offset:numberOfElements;
                numberOfElements-=copied;
                while(copied-->0)
                    block[offset++]=value;
                ++index;
                offset=0;
            }
        }
    }

    private void addElements(int numberOfElements){
        int newlen=m_firstFree+numberOfElements;
        if(newlen>m_blocksize){
            int index=m_firstFree>>>m_SHIFT;
            int newindex=(m_firstFree+numberOfElements)>>>m_SHIFT;
            for(int i=index+1;i<=newindex;++i)
                m_map[i]=new int[m_blocksize];
        }
        m_firstFree=newlen;
    }

    private void insertElementAt(int value,int at){
        if(at==m_firstFree)
            addElement(value);
        else if(at>m_firstFree){
            int index=at>>>m_SHIFT;
            if(index>=m_map.length){
                int newsize=index+m_numblocks;
                int[][] newMap=new int[newsize][];
                System.arraycopy(m_map,0,newMap,0,m_map.length);
                m_map=newMap;
            }
            int[] block=m_map[index];
            if(null==block)
                block=m_map[index]=new int[m_blocksize];
            int offset=at&m_MASK;
            block[offset]=value;
            m_firstFree=offset+1;
        }else{
            int index=at>>>m_SHIFT;
            int maxindex=m_firstFree>>>m_SHIFT; // %REVIEW% (m_firstFree+1?)
            ++m_firstFree;
            int offset=at&m_MASK;
            int push;
            // ***** Easier to work down from top?
            while(index<=maxindex){
                int copylen=m_blocksize-offset-1;
                int[] block=m_map[index];
                if(null==block){
                    push=0;
                    block=m_map[index]=new int[m_blocksize];
                }else{
                    push=block[m_blocksize-1];
                    System.arraycopy(block,offset,block,offset+1,copylen);
                }
                block[offset]=value;
                value=push;
                offset=0;
                ++index;
            }
        }
    }

    public void addElement(int value){
        int indexRelativeToCache=m_firstFree-m_buildCacheStartIndex;
        // Is the new index an index into the cache row of m_map?
        if(indexRelativeToCache>=0&&indexRelativeToCache<m_blocksize){
            m_buildCache[indexRelativeToCache]=value;
            ++m_firstFree;
        }else{
            // Growing the outer array should be rare. We initialize to a
            // total of m_blocksize squared elements, which at the default
            // size is 4M integers... and we grow by at least that much each
            // time.  However, attempts to microoptimize for this (assume
            // long enough and catch exceptions) yield no noticable
            // improvement.
            int index=m_firstFree>>>m_SHIFT;
            int offset=m_firstFree&m_MASK;
            if(index>=m_map.length){
                int newsize=index+m_numblocks;
                int[][] newMap=new int[newsize][];
                System.arraycopy(m_map,0,newMap,0,m_map.length);
                m_map=newMap;
            }
            int[] block=m_map[index];
            if(null==block)
                block=m_map[index]=new int[m_blocksize];
            block[offset]=value;
            // Cache the current row of m_map.  Next m_blocksize-1
            // values added will go to this row.
            m_buildCache=block;
            m_buildCacheStartIndex=m_firstFree-offset;
            ++m_firstFree;
        }
    }

    public void removeAllElements(){
        m_firstFree=0;
        m_buildCache=m_map0;
        m_buildCacheStartIndex=0;
    }

    private boolean removeElement(int s){
        int at=indexOf(s,0);
        if(at<0)
            return false;
        removeElementAt(at);
        return true;
    }

    private void removeElementAt(int at){
        // No point in removing elements that "don't exist"...
        if(at<m_firstFree){
            int index=at>>>m_SHIFT;
            int maxindex=m_firstFree>>>m_SHIFT;
            int offset=at&m_MASK;
            while(index<=maxindex){
                int copylen=m_blocksize-offset-1;
                int[] block=m_map[index];
                if(null==block)
                    block=m_map[index]=new int[m_blocksize];
                else
                    System.arraycopy(block,offset+1,block,offset,copylen);
                if(index<maxindex){
                    int[] next=m_map[index+1];
                    if(next!=null)
                        block[m_blocksize-1]=(next!=null)?next[0]:0;
                }else
                    block[m_blocksize-1]=0;
                offset=0;
                ++index;
            }
        }
        --m_firstFree;
    }

    public int indexOf(int elem,int index){
        if(index>=m_firstFree)
            return -1;
        int bindex=index>>>m_SHIFT;
        int boffset=index&m_MASK;
        int maxindex=m_firstFree>>>m_SHIFT;
        int[] block;
        for(;bindex<maxindex;++bindex){
            block=m_map[bindex];
            if(block!=null)
                for(int offset=boffset;offset<m_blocksize;++offset)
                    if(block[offset]==elem)
                        return offset+bindex*m_blocksize;
            boffset=0; // after first
        }
        // Last block may need to stop before end
        int maxoffset=m_firstFree&m_MASK;
        block=m_map[maxindex];
        for(int offset=boffset;offset<maxoffset;++offset)
            if(block[offset]==elem)
                return offset+maxindex*m_blocksize;
        return -1;
    }

    public void setElementAt(int value,int at){
        if(at<m_blocksize)
            m_map0[at]=value;
        else{
            int index=at>>>m_SHIFT;
            int offset=at&m_MASK;
            if(index>=m_map.length){
                int newsize=index+m_numblocks;
                int[][] newMap=new int[newsize][];
                System.arraycopy(m_map,0,newMap,0,m_map.length);
                m_map=newMap;
            }
            int[] block=m_map[index];
            if(null==block)
                block=m_map[index]=new int[m_blocksize];
            block[offset]=value;
        }
        if(at>=m_firstFree)
            m_firstFree=at+1;
    }

    public int elementAt(int i){
        // This is actually a significant optimization!
        if(i<m_blocksize)
            return m_map0[i];
        return m_map[i>>>m_SHIFT][i&m_MASK];
    }

    private boolean contains(int s){
        return (indexOf(s,0)>=0);
    }

    public int indexOf(int elem){
        return indexOf(elem,0);
    }

    private int lastIndexOf(int elem){
        int boffset=m_firstFree&m_MASK;
        for(int index=m_firstFree>>>m_SHIFT;
            index>=0;
            --index){
            int[] block=m_map[index];
            if(block!=null)
                for(int offset=boffset;offset>=0;--offset)
                    if(block[offset]==elem)
                        return offset+index*m_blocksize;
            boffset=0; // after first
        }
        return -1;
    }

    public final int[] getMap0(){
        return m_map0;
    }

    public final int[][] getMap(){
        return m_map;
    }
}
