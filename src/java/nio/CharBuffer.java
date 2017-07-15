/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// -- This file was mechanically generated: Do not edit! -- //
package java.nio;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public abstract class CharBuffer
        extends Buffer
        implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{
    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    final char[] hb;                  // Non-null only for heap buffers
    final int offset;
    boolean isReadOnly;                 // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, and capacity
    //
    CharBuffer(int mark,int pos,int lim,int cap){ // package-private
        this(mark,pos,lim,cap,null,0);
    }

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    CharBuffer(int mark,int pos,int lim,int cap,   // package-private
               char[] hb,int offset){
        super(mark,pos,lim,cap);
        this.hb=hb;
        this.offset=offset;
    }

    public static CharBuffer allocate(int capacity){
        if(capacity<0)
            throw new IllegalArgumentException();
        return new HeapCharBuffer(capacity,capacity);
    }

    public static CharBuffer wrap(char[] array){
        return wrap(array,0,array.length);
    }

    public static CharBuffer wrap(char[] array,
                                  int offset,int length){
        try{
            return new HeapCharBuffer(array,offset,length);
        }catch(IllegalArgumentException x){
            throw new IndexOutOfBoundsException();
        }
    }

    public static CharBuffer wrap(CharSequence csq){
        return wrap(csq,0,csq.length());
    }

    public static CharBuffer wrap(CharSequence csq,int start,int end){
        try{
            return new StringCharBuffer(csq,start,end);
        }catch(IllegalArgumentException x){
            throw new IndexOutOfBoundsException();
        }
    }

    public int read(CharBuffer target) throws IOException{
        // Determine the number of bytes n that can be transferred
        int targetRemaining=target.remaining();
        int remaining=remaining();
        if(remaining==0)
            return -1;
        int n=Math.min(remaining,targetRemaining);
        int limit=limit();
        // Set source limit to prevent target overflow
        if(targetRemaining<remaining)
            limit(position()+n);
        try{
            if(n>0)
                target.put(this);
        }finally{
            limit(limit); // restore real limit
        }
        return n;
    }

    public abstract CharBuffer slice();

    public abstract CharBuffer duplicate();

    public abstract CharBuffer asReadOnlyBuffer();
    // -- Singleton get/put methods --

    abstract char getUnchecked(int index);   // package-private

    public abstract CharBuffer put(int index,char c);

    public CharBuffer get(char[] dst){
        return get(dst,0,dst.length);
    }

    public CharBuffer get(char[] dst,int offset,int length){
        checkBounds(offset,length,dst.length);
        if(length>remaining())
            throw new BufferUnderflowException();
        int end=offset+length;
        for(int i=offset;i<end;i++)
            dst[i]=get();
        return this;
    }

    public abstract char get();
    // -- Bulk get operations --

    public CharBuffer put(CharBuffer src){
        if(src==this)
            throw new IllegalArgumentException();
        if(isReadOnly())
            throw new ReadOnlyBufferException();
        int n=src.remaining();
        if(n>remaining())
            throw new BufferOverflowException();
        for(int i=0;i<n;i++)
            put(src.get());
        return this;
    }

    public abstract CharBuffer put(char c);
    // -- Bulk put operations --

    public final CharBuffer put(char[] src){
        return put(src,0,src.length);
    }

    public CharBuffer put(char[] src,int offset,int length){
        checkBounds(offset,length,src.length);
        if(length>remaining())
            throw new BufferOverflowException();
        int end=offset+length;
        for(int i=offset;i<end;i++)
            this.put(src[i]);
        return this;
    }

    public final boolean hasArray(){
        return (hb!=null)&&!isReadOnly;
    }

    public final char[] array(){
        if(hb==null)
            throw new UnsupportedOperationException();
        if(isReadOnly)
            throw new ReadOnlyBufferException();
        return hb;
    }

    public final int arrayOffset(){
        if(hb==null)
            throw new UnsupportedOperationException();
        if(isReadOnly)
            throw new ReadOnlyBufferException();
        return offset;
    }
    // -- Other stuff --

    public abstract boolean isDirect();

    public abstract CharBuffer compact();

    public int hashCode(){
        int h=1;
        int p=position();
        for(int i=limit()-1;i>=p;i--)
            h=31*h+(int)get(i);
        return h;
    }

    public abstract char get(int index);

    public boolean equals(Object ob){
        if(this==ob)
            return true;
        if(!(ob instanceof CharBuffer))
            return false;
        CharBuffer that=(CharBuffer)ob;
        if(this.remaining()!=that.remaining())
            return false;
        int p=this.position();
        for(int i=this.limit()-1, j=that.limit()-1;i>=p;i--,j--)
            if(!equals(this.get(i),that.get(j)))
                return false;
        return true;
    }

    private static boolean equals(char x,char y){
        return x==y;
    }

    public String toString(){
        return toString(position(),limit());
    }

    abstract String toString(int start,int end);       // package-private

    public int compareTo(CharBuffer that){
        int n=this.position()+Math.min(this.remaining(),that.remaining());
        for(int i=this.position(), j=that.position();i<n;i++,j++){
            int cmp=compare(this.get(i),that.get(j));
            if(cmp!=0)
                return cmp;
        }
        return this.remaining()-that.remaining();
    }

    private static int compare(char x,char y){
        return Character.compare(x,y);
    }
    // -- Other char stuff --

    public final int length(){
        return remaining();
    }

    public final char charAt(int index){
        return get(position()+checkIndex(index,1));
    }
    // --- Methods to support CharSequence ---

    public abstract CharBuffer subSequence(int start,int end);

    @Override
    public IntStream chars(){
        return StreamSupport.intStream(()->new CharBufferSpliterator(this),
                Buffer.SPLITERATOR_CHARACTERISTICS,false);
    }

    public CharBuffer append(CharSequence csq){
        if(csq==null)
            return put("null");
        else
            return put(csq.toString());
    }
    // --- Methods to support Appendable ---

    public final CharBuffer put(String src){
        return put(src,0,src.length());
    }

    public CharBuffer put(String src,int start,int end){
        checkBounds(start,end-start,src.length());
        if(isReadOnly())
            throw new ReadOnlyBufferException();
        if(end-start>remaining())
            throw new BufferOverflowException();
        for(int i=start;i<end;i++)
            this.put(src.charAt(i));
        return this;
    }

    public CharBuffer append(CharSequence csq,int start,int end){
        CharSequence cs=(csq==null?"null":csq);
        return put(cs.subSequence(start,end).toString());
    }
    // -- Other byte stuff: Access to binary data --

    public CharBuffer append(char c){
        return put(c);
    }

    public abstract ByteOrder order();
}
