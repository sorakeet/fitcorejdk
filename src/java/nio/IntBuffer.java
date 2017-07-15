/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// -- This file was mechanically generated: Do not edit! -- //
package java.nio;

public abstract class IntBuffer
        extends Buffer
        implements Comparable<IntBuffer>{
    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    final int[] hb;                  // Non-null only for heap buffers
    final int offset;
    boolean isReadOnly;                 // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, and capacity
    //
    IntBuffer(int mark,int pos,int lim,int cap){ // package-private
        this(mark,pos,lim,cap,null,0);
    }

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    IntBuffer(int mark,int pos,int lim,int cap,   // package-private
              int[] hb,int offset){
        super(mark,pos,lim,cap);
        this.hb=hb;
        this.offset=offset;
    }

    public static IntBuffer allocate(int capacity){
        if(capacity<0)
            throw new IllegalArgumentException();
        return new HeapIntBuffer(capacity,capacity);
    }

    public static IntBuffer wrap(int[] array){
        return wrap(array,0,array.length);
    }

    public static IntBuffer wrap(int[] array,
                                 int offset,int length){
        try{
            return new HeapIntBuffer(array,offset,length);
        }catch(IllegalArgumentException x){
            throw new IndexOutOfBoundsException();
        }
    }

    public abstract IntBuffer slice();

    public abstract IntBuffer duplicate();

    public abstract IntBuffer asReadOnlyBuffer();
    // -- Singleton get/put methods --

    public abstract IntBuffer put(int index,int i);

    public IntBuffer get(int[] dst){
        return get(dst,0,dst.length);
    }

    public IntBuffer get(int[] dst,int offset,int length){
        checkBounds(offset,length,dst.length);
        if(length>remaining())
            throw new BufferUnderflowException();
        int end=offset+length;
        for(int i=offset;i<end;i++)
            dst[i]=get();
        return this;
    }

    public abstract int get();
    // -- Bulk get operations --

    public IntBuffer put(IntBuffer src){
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

    public abstract IntBuffer put(int i);
    // -- Bulk put operations --

    public final IntBuffer put(int[] src){
        return put(src,0,src.length);
    }

    public IntBuffer put(int[] src,int offset,int length){
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
    // -- Other stuff --

    public final int[] array(){
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

    public abstract boolean isDirect();

    public abstract IntBuffer compact();

    public int hashCode(){
        int h=1;
        int p=position();
        for(int i=limit()-1;i>=p;i--)
            h=31*h+get(i);
        return h;
    }

    public abstract int get(int index);

    public boolean equals(Object ob){
        if(this==ob)
            return true;
        if(!(ob instanceof IntBuffer))
            return false;
        IntBuffer that=(IntBuffer)ob;
        if(this.remaining()!=that.remaining())
            return false;
        int p=this.position();
        for(int i=this.limit()-1, j=that.limit()-1;i>=p;i--,j--)
            if(!equals(this.get(i),that.get(j)))
                return false;
        return true;
    }

    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }

    private static boolean equals(int x,int y){
        return x==y;
    }

    public int compareTo(IntBuffer that){
        int n=this.position()+Math.min(this.remaining(),that.remaining());
        for(int i=this.position(), j=that.position();i<n;i++,j++){
            int cmp=compare(this.get(i),that.get(j));
            if(cmp!=0)
                return cmp;
        }
        return this.remaining()-that.remaining();
    }

    private static int compare(int x,int y){
        return Integer.compare(x,y);
    }
    // -- Other char stuff --
    // -- Other byte stuff: Access to binary data --

    public abstract ByteOrder order();
}
