/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class ServerSocketChannel
        extends AbstractSelectableChannel
        implements NetworkChannel{
    protected ServerSocketChannel(SelectorProvider provider){
        super(provider);
    }

    public static ServerSocketChannel open() throws IOException{
        return SelectorProvider.provider().openServerSocketChannel();
    }

    public final int validOps(){
        return SelectionKey.OP_ACCEPT;
    }
    // -- ServerSocket-specific operations --

    public final ServerSocketChannel bind(SocketAddress local)
            throws IOException{
        return bind(local,0);
    }

    public abstract ServerSocketChannel bind(SocketAddress local,int backlog)
            throws IOException;

    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

    public abstract <T> ServerSocketChannel setOption(SocketOption<T> name,T value)
            throws IOException;

    public abstract ServerSocket socket();

    public abstract SocketChannel accept() throws IOException;
}
