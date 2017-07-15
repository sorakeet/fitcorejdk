/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// -- This file was mechanically generated: Do not edit! -- //
package java.nio;

public abstract class FloatBuffer
        extends Buffer
        implements Comparable<FloatBuffer>{
    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    final float[] hb;                  // Non-null only for heap buffers
    final int offset;
    boolean isReadOnly;                 // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, and capacity
    //
    FloatBuffer(int mark,int pos,int lim,int cap){ // package-private
        this(mark,pos,lim,cap,null,0);
    }

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    FloatBuffer(int mark,int pos,int lim,int cap,   // package-private
                float[] hb,int offset){
        super(mark,pos,lim,cap);
        this.hb=hb;
        this.offset=offset;
    }

    public static FloatBuffer allocate(int capacity){
        if(capacity<0)
            throw new IllegalArgumentException();
        return new HeapFloatBuffer(capacity,capacity);
    }

    public static FloatBuffer wrap(float[] array){
        return wrap(array,0,array.length);
    }

    public static FloatBuffer wrap(float[] array,
                                   int offset,int length){
        try{
            return new HeapFloatBuffer(array,offset,length);
        }catch(IllegalArgumentException x){
            throw new IndexOutOfBoundsException();
        }
    }

    public abstract FloatBuffer slice();

    public abstract FloatBuffer duplicate();

    public abstract FloatBuffer asReadOnlyBuffer();
    // -- Singleton get/put methods --

    public abstract FloatBuffer put(int index,float f);

    public FloatBuffer get(float[] dst){
        return get(dst,0,dst.length);
    }

    public FloatBuffer get(float[] dst,int offset,int length){
        checkBounds(offset,length,dst.length);
        if(length>remaining())
            throw new BufferUnderflowException();
        int end=offset+length;
        for(int i=offset;i<end;i++)
            dst[i]=get();
        return this;
    }

    public abstract float get();
    // -- Bulk get operations --

    public FloatBuffer put(FloatBuffer src){
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

    public abstract FloatBuffer put(float f);
    // -- Bulk put operations --

    public final FloatBuffer put(float[] src){
        return put(src,0,src.length);
    }

    public FloatBuffer put(float[] src,int offset,int length){
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

    public final float[] array(){
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

    public abstract FloatBuffer compact();

    public int hashCode(){
        int h=1;
        int p=position();
        for(int i=limit()-1;i>=p;i--)
            h=31*h+(int)get(i);
        return h;
    }

    public abstract float get(int index);

    public boolean equals(Object ob){
        if(this==ob)
            return true;
        if(!(ob instanceof FloatBuffer))
            return false;
        FloatBuffer that=(FloatBuffer)ob;
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

    private static boolean equals(float x,float y){
        return (x==y)||(Float.isNaN(x)&&Float.isNaN(y));
    }

    public int compareTo(FloatBuffer that){
        int n=this.position()+Math.min(this.remaining(),that.remaining());
        for(int i=this.position(), j=that.position();i<n;i++,j++){
            int cmp=compare(this.get(i),that.get(j));
            if(cmp!=0)
                return cmp;
        }
        return this.remaining()-that.remaining();
    }

    private static int compare(float x,float y){
        return ((x<y)?-1:
                (x>y)?+1:
                        (x==y)?0:
                                Float.isNaN(x)?(Float.isNaN(y)?0:+1):-1);
    }
    // -- Other char stuff --
    // -- Other byte stuff: Access to binary data --

    public abstract ByteOrder order();
}
