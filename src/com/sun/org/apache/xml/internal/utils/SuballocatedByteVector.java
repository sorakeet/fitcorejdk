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
 * $Id: SuballocatedByteVector.java,v 1.2.4.1 2005/09/15 08:15:57 suresh_emailid Exp $
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
 * $Id: SuballocatedByteVector.java,v 1.2.4.1 2005/09/15 08:15:57 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class SuballocatedByteVector{
    protected int m_blocksize;
    protected int m_numblocks=32;
    protected byte m_map[][];
    protected int m_firstFree=0;
    protected byte m_map0[];

    public SuballocatedByteVector(){
        this(2048);
    }

    public SuballocatedByteVector(int blocksize){
        m_blocksize=blocksize;
        m_map0=new byte[blocksize];
        m_map=new byte[m_numblocks][];
        m_map[0]=m_map0;
    }

    public SuballocatedByteVector(int blocksize,int increaseSize){
        // increaseSize not currently used.
        this(blocksize);
    }

    public int size(){
        return m_firstFree;
    }

    private void setSize(int sz){
        if(m_firstFree<sz)
            m_firstFree=sz;
    }

    private void addElements(byte value,int numberOfElements){
        if(m_firstFree+numberOfElements<m_blocksize)
            for(int i=0;i<numberOfElements;i++){
                m_map0[m_firstFree++]=value;
            }
        else{
            int index=m_firstFree/m_blocksize;
            int offset=m_firstFree%m_blocksize;
            m_firstFree+=numberOfElements;
            while(numberOfElements>0){
                if(index>=m_map.length){
                    int newsize=index+m_numblocks;
                    byte[][] newMap=new byte[newsize][];
                    System.arraycopy(m_map,0,newMap,0,m_map.length);
                    m_map=newMap;
                }
                byte[] block=m_map[index];
                if(null==block)
                    block=m_map[index]=new byte[m_blocksize];
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
            int index=m_firstFree%m_blocksize;
            int newindex=(m_firstFree+numberOfElements)%m_blocksize;
            for(int i=index+1;i<=newindex;++i)
                m_map[i]=new byte[m_blocksize];
        }
        m_firstFree=newlen;
    }

    private void insertElementAt(byte value,int at){
        if(at==m_firstFree)
            addElement(value);
        else if(at>m_firstFree){
            int index=at/m_blocksize;
            if(index>=m_map.length){
                int newsize=index+m_numblocks;
                byte[][] newMap=new byte[newsize][];
                System.arraycopy(m_map,0,newMap,0,m_map.length);
                m_map=newMap;
            }
            byte[] block=m_map[index];
            if(null==block)
                block=m_map[index]=new byte[m_blocksize];
            int offset=at%m_blocksize;
            block[offset]=value;
            m_firstFree=offset+1;
        }else{
            int index=at/m_blocksize;
            int maxindex=m_firstFree+1/m_blocksize;
            ++m_firstFree;
            int offset=at%m_blocksize;
            byte push;
            // ***** Easier to work down from top?
            while(index<=maxindex){
                int copylen=m_blocksize-offset-1;
                byte[] block=m_map[index];
                if(null==block){
                    push=0;
                    block=m_map[index]=new byte[m_blocksize];
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

    public void addElement(byte value){
        if(m_firstFree<m_blocksize)
            m_map0[m_firstFree++]=value;
        else{
            int index=m_firstFree/m_blocksize;
            int offset=m_firstFree%m_blocksize;
            ++m_firstFree;
            if(index>=m_map.length){
                int newsize=index+m_numblocks;
                byte[][] newMap=new byte[newsize][];
                System.arraycopy(m_map,0,newMap,0,m_map.length);
                m_map=newMap;
            }
            byte[] block=m_map[index];
            if(null==block)
                block=m_map[index]=new byte[m_blocksize];
            block[offset]=value;
        }
    }

    public void removeAllElements(){
        m_firstFree=0;
    }

    private boolean removeElement(byte s){
        int at=indexOf(s,0);
        if(at<0)
            return false;
        removeElementAt(at);
        return true;
    }

    private void removeElementAt(int at){
        // No point in removing elements that "don't exist"...
        if(at<m_firstFree){
            int index=at/m_blocksize;
            int maxindex=m_firstFree/m_blocksize;
            int offset=at%m_blocksize;
            while(index<=maxindex){
                int copylen=m_blocksize-offset-1;
                byte[] block=m_map[index];
                if(null==block)
                    block=m_map[index]=new byte[m_blocksize];
                else
                    System.arraycopy(block,offset+1,block,offset,copylen);
                if(index<maxindex){
                    byte[] next=m_map[index+1];
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

    public int indexOf(byte elem,int index){
        if(index>=m_firstFree)
            return -1;
        int bindex=index/m_blocksize;
        int boffset=index%m_blocksize;
        int maxindex=m_firstFree/m_blocksize;
        byte[] block;
        for(;bindex<maxindex;++bindex){
            block=m_map[bindex];
            if(block!=null)
                for(int offset=boffset;offset<m_blocksize;++offset)
                    if(block[offset]==elem)
                        return offset+bindex*m_blocksize;
            boffset=0; // after first
        }
        // Last block may need to stop before end
        int maxoffset=m_firstFree%m_blocksize;
        block=m_map[maxindex];
        for(int offset=boffset;offset<maxoffset;++offset)
            if(block[offset]==elem)
                return offset+maxindex*m_blocksize;
        return -1;
    }

    public void setElementAt(byte value,int at){
        if(at<m_blocksize){
            m_map0[at]=value;
            return;
        }
        int index=at/m_blocksize;
        int offset=at%m_blocksize;
        if(index>=m_map.length){
            int newsize=index+m_numblocks;
            byte[][] newMap=new byte[newsize][];
            System.arraycopy(m_map,0,newMap,0,m_map.length);
            m_map=newMap;
        }
        byte[] block=m_map[index];
        if(null==block)
            block=m_map[index]=new byte[m_blocksize];
        block[offset]=value;
        if(at>=m_firstFree)
            m_firstFree=at+1;
    }

    public byte elementAt(int i){
        // %OPT% Does this really buy us anything? Test versus division for small,
        // test _plus_ division for big docs.
        if(i<m_blocksize)
            return m_map0[i];
        return m_map[i/m_blocksize][i%m_blocksize];
    }

    private boolean contains(byte s){
        return (indexOf(s,0)>=0);
    }

    public int indexOf(byte elem){
        return indexOf(elem,0);
    }

    private int lastIndexOf(byte elem){
        int boffset=m_firstFree%m_blocksize;
        for(int index=m_firstFree/m_blocksize;
            index>=0;
            --index){
            byte[] block=m_map[index];
            if(block!=null)
                for(int offset=boffset;offset>=0;--offset)
                    if(block[offset]==elem)
                        return offset+index*m_blocksize;
            boffset=0; // after first
        }
        return -1;
    }
}
