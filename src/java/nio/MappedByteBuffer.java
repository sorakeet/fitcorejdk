/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio;

import sun.misc.Unsafe;

import java.io.FileDescriptor;

public abstract class MappedByteBuffer
        extends ByteBuffer{
    // not used, but a potential target for a store, see load() for details.
    private static byte unused;
    // This is a little bit backwards: By rights MappedByteBuffer should be a
    // subclass of DirectByteBuffer, but to keep the spec clear and simple, and
    // for optimization purposes, it's easier to do it the other way around.
    // This works because DirectByteBuffer is a package-private class.
    // For mapped buffers, a FileDescriptor that may be used for mapping
    // operations if valid; null if the buffer is not mapped.
    private final FileDescriptor fd;

    // This should only be invoked by the DirectByteBuffer constructors
    //
    MappedByteBuffer(int mark,int pos,int lim,int cap, // package-private
                     FileDescriptor fd){
        super(mark,pos,lim,cap);
        this.fd=fd;
    }

    MappedByteBuffer(int mark,int pos,int lim,int cap){ // package-private
        super(mark,pos,lim,cap);
        this.fd=null;
    }

    public final boolean isLoaded(){
        checkMapped();
        if((address==0)||(capacity()==0))
            return true;
        long offset=mappingOffset();
        long length=mappingLength(offset);
        return isLoaded0(mappingAddress(offset),length,Bits.pageCount(length));
    }

    private void checkMapped(){
        if(fd==null)
            // Can only happen if a luser explicitly casts a direct byte buffer
            throw new UnsupportedOperationException();
    }

    // Returns the distance (in bytes) of the buffer from the page aligned address
    // of the mapping. Computed each time to avoid storing in every direct buffer.
    private long mappingOffset(){
        int ps=Bits.pageSize();
        long offset=address%ps;
        return (offset>=0)?offset:(ps+offset);
    }

    private long mappingAddress(long mappingOffset){
        return address-mappingOffset;
    }

    private long mappingLength(long mappingOffset){
        return (long)capacity()+mappingOffset;
    }

    private native boolean isLoaded0(long address,long length,int pageCount);

    public final MappedByteBuffer load(){
        checkMapped();
        if((address==0)||(capacity()==0))
            return this;
        long offset=mappingOffset();
        long length=mappingLength(offset);
        load0(mappingAddress(offset),length);
        // Read a byte from each page to bring it into memory. A checksum
        // is computed as we go along to prevent the compiler from otherwise
        // considering the loop as dead code.
        Unsafe unsafe=Unsafe.getUnsafe();
        int ps=Bits.pageSize();
        int count=Bits.pageCount(length);
        long a=mappingAddress(offset);
        byte x=0;
        for(int i=0;i<count;i++){
            x^=unsafe.getByte(a);
            a+=ps;
        }
        if(unused!=0)
            unused=x;
        return this;
    }

    private native void load0(long address,long length);

    public final MappedByteBuffer force(){
        checkMapped();
        if((address!=0)&&(capacity()!=0)){
            long offset=mappingOffset();
            force0(fd,mappingAddress(offset),mappingLength(offset));
        }
        return this;
    }

    private native void force0(FileDescriptor fd,long address,long length);
}
