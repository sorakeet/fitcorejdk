/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class SocketChannel
        extends AbstractSelectableChannel
        implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel{
    protected SocketChannel(SelectorProvider provider){
        super(provider);
    }

    public static SocketChannel open(SocketAddress remote)
            throws IOException{
        SocketChannel sc=open();
        try{
            sc.connect(remote);
        }catch(Throwable x){
            try{
                sc.close();
            }catch(Throwable suppressed){
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        assert sc.isConnected();
        return sc;
    }

    public static SocketChannel open() throws IOException{
        return SelectorProvider.provider().openSocketChannel();
    }

    public final int validOps(){
        return (SelectionKey.OP_READ
                |SelectionKey.OP_WRITE
                |SelectionKey.OP_CONNECT);
    }
    // -- Socket-specific operations --

    @Override
    public abstract SocketChannel bind(SocketAddress local)
            throws IOException;

    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

    @Override
    public abstract <T> SocketChannel setOption(SocketOption<T> name,T value)
            throws IOException;

    public abstract SocketChannel shutdownInput() throws IOException;

    public abstract SocketChannel shutdownOutput() throws IOException;

    public abstract Socket socket();

    public abstract boolean isConnected();

    public abstract boolean isConnectionPending();

    public abstract boolean connect(SocketAddress remote) throws IOException;

    public abstract boolean finishConnect() throws IOException;
    // -- ByteChannel operations --

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract int read(ByteBuffer dst) throws IOException;

    public abstract int write(ByteBuffer src) throws IOException;    public abstract long read(ByteBuffer[] dsts,int offset,int length)
            throws IOException;



    public final long read(ByteBuffer[] dsts) throws IOException{
        return read(dsts,0,dsts.length);
    }

    public abstract long write(ByteBuffer[] srcs,int offset,int length)
            throws IOException;

    public final long write(ByteBuffer[] srcs) throws IOException{
        return write(srcs,0,srcs.length);
    }
}
