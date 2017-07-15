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
 * $Id: ChunkedIntArray.java,v 1.2.4.1 2005/09/15 08:14:58 suresh_emailid Exp $
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
 * $Id: ChunkedIntArray.java,v 1.2.4.1 2005/09/15 08:14:58 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;

final class ChunkedIntArray{
    // Debugging tip: Cranking lowbits down to 4 or so is a good
    // way to pound on the array addressing code.
    static final int lowbits=10; // How many bits address within chunks
    static final int chunkalloc=1<<lowbits;
    static final int lowmask=chunkalloc-1;
    final int slotsize=4; // Locked, MUST be power of two in current code
    final int fastArray[]=new int[chunkalloc];
    ChunksVector chunks=new ChunksVector();
    int lastUsed=0;

    ChunkedIntArray(int slotsize){
        if(this.slotsize<slotsize)
            throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CHUNKEDINTARRAY_NOT_SUPPORTED,new Object[]{Integer.toString(slotsize)})); //"ChunkedIntArray("+slotsize+") not currently supported");
        else if(this.slotsize>slotsize)
            System.out.println("*****WARNING: ChunkedIntArray("+slotsize+") wasting "+(this.slotsize-slotsize)+" words per slot");
        chunks.addElement(fastArray);
    }

    int appendSlot(int w0,int w1,int w2,int w3){
        /**
         try
         {
         int newoffset = (lastUsed+1)*slotsize;
         fastArray[newoffset] = w0;
         fastArray[newoffset+1] = w1;
         fastArray[newoffset+2] = w2;
         fastArray[newoffset+3] = w3;
         return ++lastUsed;
         }
         catch(ArrayIndexOutOfBoundsException aioobe)
         */
        {
            final int slotsize=4;
            int newoffset=(lastUsed+1)*slotsize;
            int chunkpos=newoffset>>lowbits;
            int slotpos=(newoffset&lowmask);
            // Grow if needed
            if(chunkpos>chunks.size()-1)
                chunks.addElement(new int[chunkalloc]);
            int[] chunk=chunks.elementAt(chunkpos);
            chunk[slotpos]=w0;
            chunk[slotpos+1]=w1;
            chunk[slotpos+2]=w2;
            chunk[slotpos+3]=w3;
            return ++lastUsed;
        }
    }

    int readEntry(int position,int offset) throws ArrayIndexOutOfBoundsException{
        /**
         try
         {
         return fastArray[(position*slotsize)+offset];
         }
         catch(ArrayIndexOutOfBoundsException aioobe)
         */
        {
            // System.out.println("Using slow read (1)");
            if(offset>=slotsize)
                throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT,null)); //"Offset bigger than slot");
            position*=slotsize;
            int chunkpos=position>>lowbits;
            int slotpos=position&lowmask;
            int[] chunk=chunks.elementAt(chunkpos);
            return chunk[slotpos+offset];
        }
    }

    // Check that the node at index "position" is not an ancestor
    // of the node at index "startPos". IF IT IS, DO NOT ACCEPT IT AND
    // RETURN -1. If position is NOT an ancestor, return position.
    // Special case: The Document node (position==0) is acceptable.
    //
    // This test supports DTM.getNextPreceding.
    int specialFind(int startPos,int position){
        // We have to look all the way up the ancestor chain
        // to make sure we don't have an ancestor.
        int ancestor=startPos;
        while(ancestor>0){
            // Get the node whose index == ancestor
            ancestor*=slotsize;
            int chunkpos=ancestor>>lowbits;
            int slotpos=ancestor&lowmask;
            int[] chunk=chunks.elementAt(chunkpos);
            // Get that node's parent (Note that this assumes w[1]
            // is the parent node index. That's really a DTM feature
            // rather than a ChunkedIntArray feature.)
            ancestor=chunk[slotpos+1];
            if(ancestor==position)
                break;
        }
        if(ancestor<=0){
            return position;
        }
        return -1;
    }

    int slotsUsed(){
        return lastUsed;
    }

    void discardLast(){
        --lastUsed;
    }

    void writeEntry(int position,int offset,int value) throws ArrayIndexOutOfBoundsException{
        /**
         try
         {
         fastArray[( position*slotsize)+offset] = value;
         }
         catch(ArrayIndexOutOfBoundsException aioobe)
         */
        {
            if(offset>=slotsize)
                throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT,null)); //"Offset bigger than slot");
            position*=slotsize;
            int chunkpos=position>>lowbits;
            int slotpos=position&lowmask;
            int[] chunk=chunks.elementAt(chunkpos);
            chunk[slotpos+offset]=value; // ATOMIC!
        }
    }

    void writeSlot(int position,int w0,int w1,int w2,int w3){
        position*=slotsize;
        int chunkpos=position>>lowbits;
        int slotpos=(position&lowmask);
        // Grow if needed
        if(chunkpos>chunks.size()-1)
            chunks.addElement(new int[chunkalloc]);
        int[] chunk=chunks.elementAt(chunkpos);
        chunk[slotpos]=w0;
        chunk[slotpos+1]=w1;
        chunk[slotpos+2]=w2;
        chunk[slotpos+3]=w3;
    }

    void readSlot(int position,int[] buffer){
        /**
         try
         {
         System.arraycopy(fastArray, position*slotsize, buffer, 0, slotsize);
         }
         catch(ArrayIndexOutOfBoundsException aioobe)
         */
        {
            // System.out.println("Using slow read (2): "+position);
            position*=slotsize;
            int chunkpos=position>>lowbits;
            int slotpos=(position&lowmask);
            // Grow if needed
            if(chunkpos>chunks.size()-1)
                chunks.addElement(new int[chunkalloc]);
            int[] chunk=chunks.elementAt(chunkpos);
            System.arraycopy(chunk,slotpos,buffer,0,slotsize);
        }
    }

    class ChunksVector{
        final int BLOCKSIZE=64;
        int[] m_map[]=new int[BLOCKSIZE][];
        int m_mapSize=BLOCKSIZE;
        int pos=0;

        ChunksVector(){
        }

        final int size(){
            return pos;
        }

        void addElement(int[] value){
            if(pos>=m_mapSize){
                int orgMapSize=m_mapSize;
                while(pos>=m_mapSize)
                    m_mapSize+=BLOCKSIZE;
                int[] newMap[]=new int[m_mapSize][];
                System.arraycopy(m_map,0,newMap,0,orgMapSize);
                m_map=newMap;
            }
            // For now, just do a simple append.  A sorted insert only
            // makes sense if we're doing an binary search or some such.
            m_map[pos]=value;
            pos++;
        }

        final int[] elementAt(int pos){
            return m_map[pos];
        }
    }
}
