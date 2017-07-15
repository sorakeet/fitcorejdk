/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AsynchronousSocketChannel
        implements AsynchronousByteChannel, NetworkChannel{
    private final AsynchronousChannelProvider provider;

    protected AsynchronousSocketChannel(AsynchronousChannelProvider provider){
        this.provider=provider;
    }

    public static AsynchronousSocketChannel open()
            throws IOException{
        return open(null);
    }

    public static AsynchronousSocketChannel open(AsynchronousChannelGroup group)
            throws IOException{
        AsynchronousChannelProvider provider=(group==null)?
                AsynchronousChannelProvider.provider():group.provider();
        return provider.openAsynchronousSocketChannel(group);
    }

    public final AsynchronousChannelProvider provider(){
        return provider;
    }
    // -- socket options and related --

    @Override
    public abstract AsynchronousSocketChannel bind(SocketAddress local)
            throws IOException;

    public abstract SocketAddress getLocalAddress() throws IOException;

    @Override
    public abstract <T> AsynchronousSocketChannel setOption(SocketOption<T> name,T value)
            throws IOException;

    public abstract AsynchronousSocketChannel shutdownInput() throws IOException;
    // -- state --

    public abstract AsynchronousSocketChannel shutdownOutput() throws IOException;
    // -- asynchronous operations --

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract <A> void connect(SocketAddress remote,
                                     A attachment,
                                     CompletionHandler<Void,? super A> handler);

    public abstract Future<Void> connect(SocketAddress remote);

    @Override
    public final <A> void read(ByteBuffer dst,
                               A attachment,
                               CompletionHandler<Integer,? super A> handler){
        read(dst,0L,TimeUnit.MILLISECONDS,attachment,handler);
    }

    public abstract <A> void read(ByteBuffer dst,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);

    @Override
    public abstract Future<Integer> read(ByteBuffer dst);

    @Override
    public final <A> void write(ByteBuffer src,
                                A attachment,
                                CompletionHandler<Integer,? super A> handler){
        write(src,0L,TimeUnit.MILLISECONDS,attachment,handler);
    }

    public abstract <A> void write(ByteBuffer src,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);

    @Override
    public abstract Future<Integer> write(ByteBuffer src);

    public abstract <A> void read(ByteBuffer[] dsts,
                                  int offset,
                                  int length,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Long,? super A> handler);

    public abstract <A> void write(ByteBuffer[] srcs,
                                   int offset,
                                   int length,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Long,? super A> handler);
}
