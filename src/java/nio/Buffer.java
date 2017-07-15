/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio;

import java.util.Spliterator;

public abstract class Buffer{
    static final int SPLITERATOR_CHARACTERISTICS=
            Spliterator.SIZED|Spliterator.SUBSIZED|Spliterator.ORDERED;
    // Used only by direct buffers
    // NOTE: hoisted here for speed in JNI GetDirectBufferAddress
    long address;
    // Invariants: mark <= position <= limit <= capacity
    private int mark=-1;
    private int position=0;
    private int limit;
    private int capacity;

    // Creates a new buffer with the given mark, position, limit, and capacity,
    // after checking invariants.
    //
    Buffer(int mark,int pos,int lim,int cap){       // package-private
        if(cap<0)
            throw new IllegalArgumentException("Negative capacity: "+cap);
        this.capacity=cap;
        limit(lim);
        position(pos);
        if(mark>=0){
            if(mark>pos)
                throw new IllegalArgumentException("mark > position: ("
                        +mark+" > "+pos+")");
            this.mark=mark;
        }
    }

    public final Buffer position(int newPosition){
        if((newPosition>limit)||(newPosition<0))
            throw new IllegalArgumentException();
        position=newPosition;
        if(mark>position) mark=-1;
        return this;
    }

    public final Buffer limit(int newLimit){
        if((newLimit>capacity)||(newLimit<0))
            throw new IllegalArgumentException();
        limit=newLimit;
        if(position>limit) position=limit;
        if(mark>limit) mark=-1;
        return this;
    }

    static void checkBounds(int off,int len,int size){ // package-private
        if((off|len|(off+len)|(size-(off+len)))<0)
            throw new IndexOutOfBoundsException();
    }

    public final int capacity(){
        return capacity;
    }

    public final int position(){
        return position;
    }

    public final int limit(){
        return limit;
    }

    public final Buffer mark(){
        mark=position;
        return this;
    }

    public final Buffer reset(){
        int m=mark;
        if(m<0)
            throw new InvalidMarkException();
        position=m;
        return this;
    }

    public final Buffer clear(){
        position=0;
        limit=capacity;
        mark=-1;
        return this;
    }

    public final Buffer flip(){
        limit=position;
        position=0;
        mark=-1;
        return this;
    }

    public final Buffer rewind(){
        position=0;
        mark=-1;
        return this;
    }

    public final int remaining(){
        return limit-position;
    }

    public final boolean hasRemaining(){
        return position<limit;
    }

    public abstract boolean isReadOnly();

    public abstract boolean hasArray();

    public abstract Object array();

    public abstract int arrayOffset();
    // -- Package-private methods for bounds checking, etc. --

    public abstract boolean isDirect();

    final int nextGetIndex(){                          // package-private
        if(position>=limit)
            throw new BufferUnderflowException();
        return position++;
    }

    final int nextGetIndex(int nb){                    // package-private
        if(limit-position<nb)
            throw new BufferUnderflowException();
        int p=position;
        position+=nb;
        return p;
    }

    final int nextPutIndex(){                          // package-private
        if(position>=limit)
            throw new BufferOverflowException();
        return position++;
    }

    final int nextPutIndex(int nb){                    // package-private
        if(limit-position<nb)
            throw new BufferOverflowException();
        int p=position;
        position+=nb;
        return p;
    }

    final int checkIndex(int i){                       // package-private
        if((i<0)||(i>=limit))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int checkIndex(int i,int nb){               // package-private
        if((i<0)||(nb>limit-i))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int markValue(){                             // package-private
        return mark;
    }

    final void truncate(){                             // package-private
        mark=-1;
        position=0;
        limit=0;
        capacity=0;
    }

    final void discardMark(){                          // package-private
        mark=-1;
    }
}
