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
 * $Id: FastStringBuffer.java,v 1.2.4.1 2005/09/15 08:15:44 suresh_emailid Exp $
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
 * $Id: FastStringBuffer.java,v 1.2.4.1 2005/09/15 08:15:44 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class FastStringBuffer{
    public static final int SUPPRESS_LEADING_WS=0x01;
    public static final int SUPPRESS_TRAILING_WS=0x02;
    public static final int SUPPRESS_BOTH
            =SUPPRESS_LEADING_WS|SUPPRESS_TRAILING_WS;
    // If nonzero, forces the inial chunk size.
  /**/static final int DEBUG_FORCE_INIT_BITS=0;
    // %BUG% %REVIEW% *****PROBLEM SUSPECTED: If data from an FSB is being copied
    // back into the same FSB (variable set from previous variable, for example)
    // and blocksize changes in mid-copy... there's risk of severe malfunction in
    // the read process, due to how the resizing code re-jiggers storage. Arggh.
    // If we want to retain the variable-size-block feature, we need to reconsider
    // that issue. For now, I have forced us into fixed-size mode.
    static final boolean DEBUG_FORCE_FIXED_CHUNKSIZE=true;
    static final char[] SINGLE_SPACE={' '};
    private static final int CARRY_WS=0x04;
    int m_chunkBits=15;
    int m_maxChunkBits=15;
    int m_rebundleBits=2;
    int m_chunkSize;  // =1<<(m_chunkBits-1);
    int m_chunkMask;  // =m_chunkSize-1;
    char[][] m_array;
    int m_lastChunk=0;
    int m_firstFree=0;
    FastStringBuffer m_innerFSB=null;

    public FastStringBuffer(int initChunkBits,int maxChunkBits){
        this(initChunkBits,maxChunkBits,2);
    }

    public FastStringBuffer(int initChunkBits,int maxChunkBits,
                            int rebundleBits){
        if(DEBUG_FORCE_INIT_BITS!=0) initChunkBits=DEBUG_FORCE_INIT_BITS;
        // %REVIEW%
        // Should this force to larger value, or smaller? Smaller less efficient, but if
        // someone requested variable mode it's because they care about storage space.
        // On the other hand, given the other changes I'm making, odds are that we should
        // adopt the larger size. Dither, dither, dither... This is just stopgap workaround
        // anyway; we need a permanant solution.
        //
        if(DEBUG_FORCE_FIXED_CHUNKSIZE) maxChunkBits=initChunkBits;
        //if(DEBUG_FORCE_FIXED_CHUNKSIZE) initChunkBits=maxChunkBits;
        m_array=new char[16][];
        // Don't bite off more than we're prepared to swallow!
        if(initChunkBits>maxChunkBits)
            initChunkBits=maxChunkBits;
        m_chunkBits=initChunkBits;
        m_maxChunkBits=maxChunkBits;
        m_rebundleBits=rebundleBits;
        m_chunkSize=1<<(initChunkBits);
        m_chunkMask=m_chunkSize-1;
        m_array[0]=new char[m_chunkSize];
    }

    public FastStringBuffer(int initChunkBits){
        this(initChunkBits,15,2);
    }

    public FastStringBuffer(){
        // 10 bits is 1K. 15 bits is 32K. Remember that these are character
        // counts, so actual memory allocation unit is doubled for UTF-16 chars.
        //
        // For reference: In the original FastStringBuffer, we simply
        // overallocated by blocksize (default 1KB) on each buffer-growth.
        this(10,15,2);
    }

    private FastStringBuffer(FastStringBuffer source){
        // Copy existing information into new encapsulation
        m_chunkBits=source.m_chunkBits;
        m_maxChunkBits=source.m_maxChunkBits;
        m_rebundleBits=source.m_rebundleBits;
        m_chunkSize=source.m_chunkSize;
        m_chunkMask=source.m_chunkMask;
        m_array=source.m_array;
        m_innerFSB=source.m_innerFSB;
        // These have to be adjusted because we're calling just at the time
        // when we would be about to allocate another chunk
        m_lastChunk=source.m_lastChunk-1;
        m_firstFree=source.m_chunkSize;
        // Establish capsule as the Inner FSB, reset chunk sizes/addressing
        source.m_array=new char[16][];
        source.m_innerFSB=this;
        // Since we encapsulated just as we were about to append another
        // chunk, return ready to create the chunk after the innerFSB
        // -- 1, not 0.
        source.m_lastChunk=1;
        source.m_firstFree=0;
        source.m_chunkBits+=m_rebundleBits;
        source.m_chunkSize=1<<(source.m_chunkBits);
        source.m_chunkMask=source.m_chunkSize-1;
    }

    public static void sendNormalizedSAXcharacters(char ch[],
                                                   int start,int length,
                                                   org.xml.sax.ContentHandler handler)
            throws org.xml.sax.SAXException{
        sendNormalizedSAXcharacters(ch,start,length,
                handler,SUPPRESS_BOTH);
    }

    public final int size(){
        return (m_lastChunk<<m_chunkBits)+m_firstFree;
    }

    public final int length(){
        return (m_lastChunk<<m_chunkBits)+m_firstFree;
    }

    public final void reset(){
        m_lastChunk=0;
        m_firstFree=0;
        // Recover the original chunk size
        FastStringBuffer innermost=this;
        while(innermost.m_innerFSB!=null){
            innermost=innermost.m_innerFSB;
        }
        m_chunkBits=innermost.m_chunkBits;
        m_chunkSize=innermost.m_chunkSize;
        m_chunkMask=innermost.m_chunkMask;
        // Discard the hierarchy
        m_innerFSB=null;
        m_array=new char[16][0];
        m_array[0]=new char[m_chunkSize];
    }

    public final void setLength(int l){
        m_lastChunk=l>>>m_chunkBits;
        if(m_lastChunk==0&&m_innerFSB!=null){
            // Replace this FSB with the appropriate inner FSB, truncated
            m_innerFSB.setLength(l,this);
        }else{
            m_firstFree=l&m_chunkMask;
            // There's an edge case if l is an exact multiple of m_chunkBits, which risks leaving
            // us pointing at the start of a chunk which has not yet been allocated. Rather than
            // pay the cost of dealing with that in the append loops (more scattered and more
            // inner-loop), we correct it here by moving to the safe side of that
            // line -- as we would have left the indexes had we appended up to that point.
            if(m_firstFree==0&&m_lastChunk>0){
                --m_lastChunk;
                m_firstFree=m_chunkSize;
            }
        }
    }

    private final void setLength(int l,FastStringBuffer rootFSB){
        m_lastChunk=l>>>m_chunkBits;
        if(m_lastChunk==0&&m_innerFSB!=null){
            m_innerFSB.setLength(l,rootFSB);
        }else{
            // Undo encapsulation -- pop the innerFSB data back up to root.
            // Inefficient, but attempts to keep the code simple.
            rootFSB.m_chunkBits=m_chunkBits;
            rootFSB.m_maxChunkBits=m_maxChunkBits;
            rootFSB.m_rebundleBits=m_rebundleBits;
            rootFSB.m_chunkSize=m_chunkSize;
            rootFSB.m_chunkMask=m_chunkMask;
            rootFSB.m_array=m_array;
            rootFSB.m_innerFSB=m_innerFSB;
            rootFSB.m_lastChunk=m_lastChunk;
            // Finally, truncate this sucker.
            rootFSB.m_firstFree=l&m_chunkMask;
        }
    }

    public final String toString(){
        int length=(m_lastChunk<<m_chunkBits)+m_firstFree;
        return getString(new StringBuffer(length),0,0,length).toString();
    }

    StringBuffer getString(StringBuffer sb,int startChunk,int startColumn,
                           int length){
        int stop=(startChunk<<m_chunkBits)+startColumn+length;
        int stopChunk=stop>>>m_chunkBits;
        int stopColumn=stop&m_chunkMask;
        // Factored out
        //StringBuffer sb=new StringBuffer(length);
        for(int i=startChunk;i<stopChunk;++i){
            if(i==0&&m_innerFSB!=null)
                m_innerFSB.getString(sb,startColumn,m_chunkSize-startColumn);
            else
                sb.append(m_array[i],startColumn,m_chunkSize-startColumn);
            startColumn=0;  // after first chunk
        }
        if(stopChunk==0&&m_innerFSB!=null)
            m_innerFSB.getString(sb,startColumn,stopColumn-startColumn);
        else if(stopColumn>startColumn)
            sb.append(m_array[stopChunk],startColumn,stopColumn-startColumn);
        return sb;
    }

    public final void append(char value){
        char[] chunk;
        // We may have preallocated chunks. If so, all but last should
        // be at full size.
        boolean lastchunk=(m_lastChunk+1==m_array.length);
        if(m_firstFree<m_chunkSize)  // Simplified test single-character-fits
            chunk=m_array[m_lastChunk];
        else{
            // Extend array?
            int i=m_array.length;
            if(m_lastChunk+1==i){
                char[][] newarray=new char[i+16][];
                System.arraycopy(m_array,0,newarray,0,i);
                m_array=newarray;
            }
            // Advance one chunk
            chunk=m_array[++m_lastChunk];
            if(chunk==null){
                // Hierarchical encapsulation
                if(m_lastChunk==1<<m_rebundleBits
                        &&m_chunkBits<m_maxChunkBits){
                    // Should do all the work of both encapsulating
                    // existing data and establishing new sizes/offsets
                    m_innerFSB=new FastStringBuffer(this);
                }
                // Add a chunk.
                chunk=m_array[m_lastChunk]=new char[m_chunkSize];
            }
            m_firstFree=0;
        }
        // Space exists in the chunk. Append the character.
        chunk[m_firstFree++]=value;
    }

    public final void append(String value){
        if(value==null)
            return;
        int strlen=value.length();
        if(0==strlen)
            return;
        int copyfrom=0;
        char[] chunk=m_array[m_lastChunk];
        int available=m_chunkSize-m_firstFree;
        // Repeat while data remains to be copied
        while(strlen>0){
            // Copy what fits
            if(available>strlen)
                available=strlen;
            value.getChars(copyfrom,copyfrom+available,m_array[m_lastChunk],
                    m_firstFree);
            strlen-=available;
            copyfrom+=available;
            // If there's more left, allocate another chunk and continue
            if(strlen>0){
                // Extend array?
                int i=m_array.length;
                if(m_lastChunk+1==i){
                    char[][] newarray=new char[i+16][];
                    System.arraycopy(m_array,0,newarray,0,i);
                    m_array=newarray;
                }
                // Advance one chunk
                chunk=m_array[++m_lastChunk];
                if(chunk==null){
                    // Hierarchical encapsulation
                    if(m_lastChunk==1<<m_rebundleBits
                            &&m_chunkBits<m_maxChunkBits){
                        // Should do all the work of both encapsulating
                        // existing data and establishing new sizes/offsets
                        m_innerFSB=new FastStringBuffer(this);
                    }
                    // Add a chunk.
                    chunk=m_array[m_lastChunk]=new char[m_chunkSize];
                }
                available=m_chunkSize;
                m_firstFree=0;
            }
        }
        // Adjust the insert point in the last chunk, when we've reached it.
        m_firstFree+=available;
    }

    public final void append(StringBuffer value){
        if(value==null)
            return;
        int strlen=value.length();
        if(0==strlen)
            return;
        int copyfrom=0;
        char[] chunk=m_array[m_lastChunk];
        int available=m_chunkSize-m_firstFree;
        // Repeat while data remains to be copied
        while(strlen>0){
            // Copy what fits
            if(available>strlen)
                available=strlen;
            value.getChars(copyfrom,copyfrom+available,m_array[m_lastChunk],
                    m_firstFree);
            strlen-=available;
            copyfrom+=available;
            // If there's more left, allocate another chunk and continue
            if(strlen>0){
                // Extend array?
                int i=m_array.length;
                if(m_lastChunk+1==i){
                    char[][] newarray=new char[i+16][];
                    System.arraycopy(m_array,0,newarray,0,i);
                    m_array=newarray;
                }
                // Advance one chunk
                chunk=m_array[++m_lastChunk];
                if(chunk==null){
                    // Hierarchical encapsulation
                    if(m_lastChunk==1<<m_rebundleBits
                            &&m_chunkBits<m_maxChunkBits){
                        // Should do all the work of both encapsulating
                        // existing data and establishing new sizes/offsets
                        m_innerFSB=new FastStringBuffer(this);
                    }
                    // Add a chunk.
                    chunk=m_array[m_lastChunk]=new char[m_chunkSize];
                }
                available=m_chunkSize;
                m_firstFree=0;
            }
        }
        // Adjust the insert point in the last chunk, when we've reached it.
        m_firstFree+=available;
    }

    public final void append(char[] chars,int start,int length){
        int strlen=length;
        if(0==strlen)
            return;
        int copyfrom=start;
        char[] chunk=m_array[m_lastChunk];
        int available=m_chunkSize-m_firstFree;
        // Repeat while data remains to be copied
        while(strlen>0){
            // Copy what fits
            if(available>strlen)
                available=strlen;
            System.arraycopy(chars,copyfrom,m_array[m_lastChunk],m_firstFree,
                    available);
            strlen-=available;
            copyfrom+=available;
            // If there's more left, allocate another chunk and continue
            if(strlen>0){
                // Extend array?
                int i=m_array.length;
                if(m_lastChunk+1==i){
                    char[][] newarray=new char[i+16][];
                    System.arraycopy(m_array,0,newarray,0,i);
                    m_array=newarray;
                }
                // Advance one chunk
                chunk=m_array[++m_lastChunk];
                if(chunk==null){
                    // Hierarchical encapsulation
                    if(m_lastChunk==1<<m_rebundleBits
                            &&m_chunkBits<m_maxChunkBits){
                        // Should do all the work of both encapsulating
                        // existing data and establishing new sizes/offsets
                        m_innerFSB=new FastStringBuffer(this);
                    }
                    // Add a chunk.
                    chunk=m_array[m_lastChunk]=new char[m_chunkSize];
                }
                available=m_chunkSize;
                m_firstFree=0;
            }
        }
        // Adjust the insert point in the last chunk, when we've reached it.
        m_firstFree+=available;
    }

    public final void append(FastStringBuffer value){
        // Complicating factor here is that the two buffers may use
        // different chunk sizes, and even if they're the same we're
        // probably on a different alignment due to previously appended
        // data. We have to work through the source in bite-sized chunks.
        if(value==null)
            return;
        int strlen=value.length();
        if(0==strlen)
            return;
        int copyfrom=0;
        char[] chunk=m_array[m_lastChunk];
        int available=m_chunkSize-m_firstFree;
        // Repeat while data remains to be copied
        while(strlen>0){
            // Copy what fits
            if(available>strlen)
                available=strlen;
            int sourcechunk=(copyfrom+value.m_chunkSize-1)
                    >>>value.m_chunkBits;
            int sourcecolumn=copyfrom&value.m_chunkMask;
            int runlength=value.m_chunkSize-sourcecolumn;
            if(runlength>available)
                runlength=available;
            System.arraycopy(value.m_array[sourcechunk],sourcecolumn,
                    m_array[m_lastChunk],m_firstFree,runlength);
            if(runlength!=available)
                System.arraycopy(value.m_array[sourcechunk+1],0,
                        m_array[m_lastChunk],m_firstFree+runlength,
                        available-runlength);
            strlen-=available;
            copyfrom+=available;
            // If there's more left, allocate another chunk and continue
            if(strlen>0){
                // Extend array?
                int i=m_array.length;
                if(m_lastChunk+1==i){
                    char[][] newarray=new char[i+16][];
                    System.arraycopy(m_array,0,newarray,0,i);
                    m_array=newarray;
                }
                // Advance one chunk
                chunk=m_array[++m_lastChunk];
                if(chunk==null){
                    // Hierarchical encapsulation
                    if(m_lastChunk==1<<m_rebundleBits
                            &&m_chunkBits<m_maxChunkBits){
                        // Should do all the work of both encapsulating
                        // existing data and establishing new sizes/offsets
                        m_innerFSB=new FastStringBuffer(this);
                    }
                    // Add a chunk.
                    chunk=m_array[m_lastChunk]=new char[m_chunkSize];
                }
                available=m_chunkSize;
                m_firstFree=0;
            }
        }
        // Adjust the insert point in the last chunk, when we've reached it.
        m_firstFree+=available;
    }

    public boolean isWhitespace(int start,int length){
        int sourcechunk=start>>>m_chunkBits;
        int sourcecolumn=start&m_chunkMask;
        int available=m_chunkSize-sourcecolumn;
        boolean chunkOK;
        while(length>0){
            int runlength=(length<=available)?length:available;
            if(sourcechunk==0&&m_innerFSB!=null)
                chunkOK=m_innerFSB.isWhitespace(sourcecolumn,runlength);
            else
                chunkOK=XMLCharacterRecognizer.isWhiteSpace(
                        m_array[sourcechunk],sourcecolumn,runlength);
            if(!chunkOK)
                return false;
            length-=runlength;
            ++sourcechunk;
            sourcecolumn=0;
            available=m_chunkSize;
        }
        return true;
    }

    StringBuffer getString(StringBuffer sb,int start,int length){
        return getString(sb,start>>>m_chunkBits,start&m_chunkMask,length);
    }

    public char charAt(int pos){
        int startChunk=pos>>>m_chunkBits;
        if(startChunk==0&&m_innerFSB!=null)
            return m_innerFSB.charAt(pos&m_chunkMask);
        else
            return m_array[startChunk][pos&m_chunkMask];
    }

    public void sendSAXcharacters(
            org.xml.sax.ContentHandler ch,int start,int length)
            throws org.xml.sax.SAXException{
        int startChunk=start>>>m_chunkBits;
        int startColumn=start&m_chunkMask;
        if(startColumn+length<m_chunkMask&&m_innerFSB==null){
            ch.characters(m_array[startChunk],startColumn,length);
            return;
        }
        int stop=start+length;
        int stopChunk=stop>>>m_chunkBits;
        int stopColumn=stop&m_chunkMask;
        for(int i=startChunk;i<stopChunk;++i){
            if(i==0&&m_innerFSB!=null)
                m_innerFSB.sendSAXcharacters(ch,startColumn,
                        m_chunkSize-startColumn);
            else
                ch.characters(m_array[i],startColumn,m_chunkSize-startColumn);
            startColumn=0;  // after first chunk
        }
        // Last, or only, chunk
        if(stopChunk==0&&m_innerFSB!=null)
            m_innerFSB.sendSAXcharacters(ch,startColumn,stopColumn-startColumn);
        else if(stopColumn>startColumn){
            ch.characters(m_array[stopChunk],startColumn,
                    stopColumn-startColumn);
        }
    }

    public int sendNormalizedSAXcharacters(
            org.xml.sax.ContentHandler ch,int start,int length)
            throws org.xml.sax.SAXException{
        // This call always starts at the beginning of the
        // string being written out, either because it was called directly or
        // because it was an m_innerFSB recursion. This is important since
        // it gives us a well-known initial state for this flag:
        int stateForNextChunk=SUPPRESS_LEADING_WS;
        int stop=start+length;
        int startChunk=start>>>m_chunkBits;
        int startColumn=start&m_chunkMask;
        int stopChunk=stop>>>m_chunkBits;
        int stopColumn=stop&m_chunkMask;
        for(int i=startChunk;i<stopChunk;++i){
            if(i==0&&m_innerFSB!=null)
                stateForNextChunk=
                        m_innerFSB.sendNormalizedSAXcharacters(ch,startColumn,
                                m_chunkSize-startColumn);
            else
                stateForNextChunk=
                        sendNormalizedSAXcharacters(m_array[i],startColumn,
                                m_chunkSize-startColumn,
                                ch,stateForNextChunk);
            startColumn=0;  // after first chunk
        }
        // Last, or only, chunk
        if(stopChunk==0&&m_innerFSB!=null)
            stateForNextChunk= // %REVIEW% Is this update really needed?
                    m_innerFSB.sendNormalizedSAXcharacters(ch,startColumn,stopColumn-startColumn);
        else if(stopColumn>startColumn){
            stateForNextChunk= // %REVIEW% Is this update really needed?
                    sendNormalizedSAXcharacters(m_array[stopChunk],
                            startColumn,stopColumn-startColumn,
                            ch,stateForNextChunk|SUPPRESS_TRAILING_WS);
        }
        return stateForNextChunk;
    }

    static int sendNormalizedSAXcharacters(char ch[],
                                           int start,int length,
                                           org.xml.sax.ContentHandler handler,
                                           int edgeTreatmentFlags)
            throws org.xml.sax.SAXException{
        boolean processingLeadingWhitespace=
                ((edgeTreatmentFlags&SUPPRESS_LEADING_WS)!=0);
        boolean seenWhitespace=((edgeTreatmentFlags&CARRY_WS)!=0);
        boolean suppressTrailingWhitespace=
                ((edgeTreatmentFlags&SUPPRESS_TRAILING_WS)!=0);
        int currPos=start;
        int limit=start+length;
        // Strip any leading spaces first, if required
        if(processingLeadingWhitespace){
            for(;currPos<limit
                    &&XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
                currPos++){
            }
            // If we've only encountered leading spaces, the
            // current state remains unchanged
            if(currPos==limit){
                return edgeTreatmentFlags;
            }
        }
        // If we get here, there are no more leading spaces to strip
        while(currPos<limit){
            int startNonWhitespace=currPos;
            // Grab a chunk of non-whitespace characters
            for(;currPos<limit
                    &&!XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
                currPos++){
            }
            // Non-whitespace seen - emit them, along with a single
            // space for any preceding whitespace characters
            if(startNonWhitespace!=currPos){
                if(seenWhitespace){
                    handler.characters(SINGLE_SPACE,0,1);
                    seenWhitespace=false;
                }
                handler.characters(ch,startNonWhitespace,
                        currPos-startNonWhitespace);
            }
            int startWhitespace=currPos;
            // Consume any whitespace characters
            for(;currPos<limit
                    &&XMLCharacterRecognizer.isWhiteSpace(ch[currPos]);
                currPos++){
            }
            if(startWhitespace!=currPos){
                seenWhitespace=true;
            }
        }
        return (seenWhitespace?CARRY_WS:0)
                |(edgeTreatmentFlags&SUPPRESS_TRAILING_WS);
    }

    public void sendSAXComment(
            org.xml.sax.ext.LexicalHandler ch,int start,int length)
            throws org.xml.sax.SAXException{
        // %OPT% Do it this way for now...
        String comment=getString(start,length);
        ch.comment(comment.toCharArray(),0,length);
    }

    public String getString(int start,int length){
        int startColumn=start&m_chunkMask;
        int startChunk=start>>>m_chunkBits;
        if(startColumn+length<m_chunkMask&&m_innerFSB==null){
            return getOneChunkString(startChunk,startColumn,length);
        }
        return getString(new StringBuffer(length),startChunk,startColumn,
                length).toString();
    }

    protected String getOneChunkString(int startChunk,int startColumn,
                                       int length){
        return new String(m_array[startChunk],startColumn,length);
    }

    private void getChars(int srcBegin,int srcEnd,char dst[],int dstBegin){
        // %TBD% Joe needs to write this function.  Make public when implemented.
    }
}
