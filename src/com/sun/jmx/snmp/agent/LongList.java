/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

final class LongList{
    public static int DEFAULT_CAPACITY=10;
    public static int DEFAULT_INCREMENT=10;
    private final int DELTA;
    public long[] list;
    private int size;

    LongList(){
        this(DEFAULT_CAPACITY,DEFAULT_INCREMENT);
    }

    LongList(int initialCapacity,int delta){
        size=0;
        DELTA=delta;
        list=allocate(initialCapacity);
    }

    private final long[] allocate(final int length){
        return new long[length];
    }

    LongList(int initialCapacity){
        this(initialCapacity,DEFAULT_INCREMENT);
    }

    public final int size(){
        return size;
    }

    public final boolean add(final long o){
        if(size>=list.length)
            resize();
        list[size++]=o;
        return true;
    }

    private final void resize(){
        final long[] newlist=allocate(list.length+DELTA);
        System.arraycopy(list,0,newlist,0,size);
        list=newlist;
    }

    public final void add(final int index,final long o){
        if(index>size) throw new IndexOutOfBoundsException();
        if(index>=list.length) resize();
        if(index==size){
            list[size++]=o;
            return;
        }
        System.arraycopy(list,index,list,index+1,size-index);
        list[index]=o;
        size++;
    }

    public final void add(final int at,final long[] src,final int from,
                          final int count){
        if(count<=0) return;
        if(at>size) throw new IndexOutOfBoundsException();
        ensure(size+count);
        if(at<size){
            System.arraycopy(list,at,list,at+count,size-at);
        }
        System.arraycopy(src,from,list,at,count);
        size+=count;
    }

    private final void ensure(int length){
        if(list.length<length){
            final int min=list.length+DELTA;
            length=(length<min)?min:length;
            final long[] newlist=allocate(length);
            System.arraycopy(list,0,newlist,0,size);
            list=newlist;
        }
    }

    public final long remove(final int from,final int count){
        if(count<1||from<0) return -1;
        if(from+count>size) return -1;
        final long o=list[from];
        final int oldsize=size;
        size=size-count;
        if(from==size) return o;
        System.arraycopy(list,from+count,list,from,
                size-from);
        return o;
    }

    public final long remove(final int index){
        if(index>=size) return -1;
        final long o=list[index];
        list[index]=0;
        if(index==--size) return o;
        System.arraycopy(list,index+1,list,index,
                size-index);
        return o;
    }

    public final long[] toArray(){
        return toArray(new long[size]);
    }

    public final long[] toArray(long[] a){
        System.arraycopy(list,0,a,0,size);
        return a;
    }
}
