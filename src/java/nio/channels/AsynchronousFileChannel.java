/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AsynchronousFileChannel
        implements AsynchronousChannel{
    @SuppressWarnings({"unchecked","rawtypes"}) // generic array construction
    private static final FileAttribute<?>[] NO_ATTRIBUTES=new FileAttribute[0];

    protected AsynchronousFileChannel(){
    }

    public static AsynchronousFileChannel open(Path file,OpenOption... options)
            throws IOException{
        Set<OpenOption> set=new HashSet<OpenOption>(options.length);
        Collections.addAll(set,options);
        return open(file,set,null,NO_ATTRIBUTES);
    }

    public static AsynchronousFileChannel open(Path file,
                                               Set<? extends OpenOption> options,
                                               ExecutorService executor,
                                               FileAttribute<?>... attrs)
            throws IOException{
        FileSystemProvider provider=file.getFileSystem().provider();
        return provider.newAsynchronousFileChannel(file,options,executor,attrs);
    }

    public abstract long size() throws IOException;

    public abstract AsynchronousFileChannel truncate(long size) throws IOException;

    public abstract void force(boolean metaData) throws IOException;

    public final <A> void lock(A attachment,
                               CompletionHandler<FileLock,? super A> handler){
        lock(0L,Long.MAX_VALUE,false,attachment,handler);
    }

    public abstract <A> void lock(long position,
                                  long size,
                                  boolean shared,
                                  A attachment,
                                  CompletionHandler<FileLock,? super A> handler);

    public final Future<FileLock> lock(){
        return lock(0L,Long.MAX_VALUE,false);
    }

    public abstract Future<FileLock> lock(long position,long size,boolean shared);

    public final FileLock tryLock() throws IOException{
        return tryLock(0L,Long.MAX_VALUE,false);
    }

    public abstract FileLock tryLock(long position,long size,boolean shared)
            throws IOException;

    public abstract <A> void read(ByteBuffer dst,
                                  long position,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);

    public abstract Future<Integer> read(ByteBuffer dst,long position);

    public abstract <A> void write(ByteBuffer src,
                                   long position,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);

    public abstract Future<Integer> write(ByteBuffer src,long position);
}
