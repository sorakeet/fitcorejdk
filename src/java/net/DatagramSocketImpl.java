/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;

public abstract class DatagramSocketImpl implements SocketOptions{
    protected int localPort;
    protected FileDescriptor fd;
    DatagramSocket socket;

    int dataAvailable(){
        // default impl returns zero, which disables the calling
        // functionality
        return 0;
    }

    protected abstract void create() throws SocketException;

    protected abstract void bind(int lport,InetAddress laddr) throws SocketException;

    protected abstract void send(DatagramPacket p) throws IOException;

    protected void connect(InetAddress address,int port) throws SocketException{
    }

    protected void disconnect(){
    }

    protected abstract int peek(InetAddress i) throws IOException;

    protected abstract int peekData(DatagramPacket p) throws IOException;

    protected abstract void receive(DatagramPacket p) throws IOException;

    @Deprecated
    protected abstract byte getTTL() throws IOException;

    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    protected abstract void join(InetAddress inetaddr) throws IOException;

    protected abstract void leave(InetAddress inetaddr) throws IOException;

    protected abstract void joinGroup(SocketAddress mcastaddr,
                                      NetworkInterface netIf)
            throws IOException;

    protected abstract void leaveGroup(SocketAddress mcastaddr,
                                       NetworkInterface netIf)
            throws IOException;

    protected abstract void close();

    protected int getLocalPort(){
        return localPort;
    }

    <T> void setOption(SocketOption<T> name,T value) throws IOException{
        if(name==StandardSocketOptions.SO_SNDBUF){
            setOption(SocketOptions.SO_SNDBUF,value);
        }else if(name==StandardSocketOptions.SO_RCVBUF){
            setOption(SocketOptions.SO_RCVBUF,value);
        }else if(name==StandardSocketOptions.SO_REUSEADDR){
            setOption(SocketOptions.SO_REUSEADDR,value);
        }else if(name==StandardSocketOptions.IP_TOS){
            setOption(SocketOptions.IP_TOS,value);
        }else if(name==StandardSocketOptions.IP_MULTICAST_IF&&
                (getDatagramSocket() instanceof MulticastSocket)){
            setOption(SocketOptions.IP_MULTICAST_IF2,value);
        }else if(name==StandardSocketOptions.IP_MULTICAST_TTL&&
                (getDatagramSocket() instanceof MulticastSocket)){
            if(!(value instanceof Integer)){
                throw new IllegalArgumentException("not an integer");
            }
            setTimeToLive((Integer)value);
        }else if(name==StandardSocketOptions.IP_MULTICAST_LOOP&&
                (getDatagramSocket() instanceof MulticastSocket)){
            setOption(SocketOptions.IP_MULTICAST_LOOP,value);
        }else{
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    DatagramSocket getDatagramSocket(){
        return socket;
    }

    void setDatagramSocket(DatagramSocket socket){
        this.socket=socket;
    }

    <T> T getOption(SocketOption<T> name) throws IOException{
        if(name==StandardSocketOptions.SO_SNDBUF){
            return (T)getOption(SocketOptions.SO_SNDBUF);
        }else if(name==StandardSocketOptions.SO_RCVBUF){
            return (T)getOption(SocketOptions.SO_RCVBUF);
        }else if(name==StandardSocketOptions.SO_REUSEADDR){
            return (T)getOption(SocketOptions.SO_REUSEADDR);
        }else if(name==StandardSocketOptions.IP_TOS){
            return (T)getOption(SocketOptions.IP_TOS);
        }else if(name==StandardSocketOptions.IP_MULTICAST_IF&&
                (getDatagramSocket() instanceof MulticastSocket)){
            return (T)getOption(SocketOptions.IP_MULTICAST_IF2);
        }else if(name==StandardSocketOptions.IP_MULTICAST_TTL&&
                (getDatagramSocket() instanceof MulticastSocket)){
            Integer ttl=getTimeToLive();
            return (T)ttl;
        }else if(name==StandardSocketOptions.IP_MULTICAST_LOOP&&
                (getDatagramSocket() instanceof MulticastSocket)){
            return (T)getOption(SocketOptions.IP_MULTICAST_LOOP);
        }else{
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    protected abstract int getTimeToLive() throws IOException;

    protected abstract void setTimeToLive(int ttl) throws IOException;

    protected FileDescriptor getFileDescriptor(){
        return fd;
    }
}
