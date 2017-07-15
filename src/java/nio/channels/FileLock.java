/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;

public abstract class FileLock implements AutoCloseable{
    private final Channel channel;
    private final long position;
    private final long size;
    private final boolean shared;

    protected FileLock(FileChannel channel,
                       long position,long size,boolean shared){
        if(position<0)
            throw new IllegalArgumentException("Negative position");
        if(size<0)
            throw new IllegalArgumentException("Negative size");
        if(position+size<0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel=channel;
        this.position=position;
        this.size=size;
        this.shared=shared;
    }

    protected FileLock(AsynchronousFileChannel channel,
                       long position,long size,boolean shared){
        if(position<0)
            throw new IllegalArgumentException("Negative position");
        if(size<0)
            throw new IllegalArgumentException("Negative size");
        if(position+size<0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel=channel;
        this.position=position;
        this.size=size;
        this.shared=shared;
    }

    public final FileChannel channel(){
        return (channel instanceof FileChannel)?(FileChannel)channel:null;
    }

    public Channel acquiredBy(){
        return channel;
    }

    public final long position(){
        return position;
    }

    public final long size(){
        return size;
    }

    public final boolean isShared(){
        return shared;
    }

    public final boolean overlaps(long position,long size){
        if(position+size<=this.position)
            return false;               // That is below this
        if(this.position+this.size<=position)
            return false;               // This is below that
        return true;
    }

    public final void close() throws IOException{
        release();
    }

    public abstract void release() throws IOException;

    public final String toString(){
        return (this.getClass().getName()
                +"["+position
                +":"+size
                +" "+(shared?"shared":"exclusive")
                +" "+(isValid()?"valid":"invalid")
                +"]");
    }

    public abstract boolean isValid();
}
