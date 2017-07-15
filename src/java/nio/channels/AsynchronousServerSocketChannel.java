/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Future;

public abstract class AsynchronousServerSocketChannel
        implements AsynchronousChannel, NetworkChannel{
    private final AsynchronousChannelProvider provider;

    protected AsynchronousServerSocketChannel(AsynchronousChannelProvider provider){
        this.provider=provider;
    }

    public static AsynchronousServerSocketChannel open()
            throws IOException{
        return open(null);
    }

    public static AsynchronousServerSocketChannel open(AsynchronousChannelGroup group)
            throws IOException{
        AsynchronousChannelProvider provider=(group==null)?
                AsynchronousChannelProvider.provider():group.provider();
        return provider.openAsynchronousServerSocketChannel(group);
    }

    public final AsynchronousChannelProvider provider(){
        return provider;
    }

    public final AsynchronousServerSocketChannel bind(SocketAddress local)
            throws IOException{
        return bind(local,0);
    }

    public abstract AsynchronousServerSocketChannel bind(SocketAddress local,int backlog)
            throws IOException;

    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

    public abstract <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name,T value)
            throws IOException;

    public abstract <A> void accept(A attachment,
                                    CompletionHandler<AsynchronousSocketChannel,? super A> handler);

    public abstract Future<AsynchronousSocketChannel> accept();
}
