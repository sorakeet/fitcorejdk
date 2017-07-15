/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SocketImpl implements SocketOptions{
    protected FileDescriptor fd;
    protected InetAddress address;
    protected int port;
    protected int localport;
    Socket socket=null;
    ServerSocket serverSocket=null;

    protected abstract void create(boolean stream) throws IOException;

    protected abstract void connect(String host,int port) throws IOException;

    protected abstract void connect(InetAddress address,int port) throws IOException;

    protected abstract void connect(SocketAddress address,int timeout) throws IOException;

    protected abstract void bind(InetAddress host,int port) throws IOException;

    protected abstract void listen(int backlog) throws IOException;

    protected abstract void accept(SocketImpl s) throws IOException;

    protected abstract InputStream getInputStream() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    protected abstract int available() throws IOException;

    protected abstract void close() throws IOException;

    protected void shutdownInput() throws IOException{
        throw new IOException("Method not implemented!");
    }

    protected void shutdownOutput() throws IOException{
        throw new IOException("Method not implemented!");
    }

    protected FileDescriptor getFileDescriptor(){
        return fd;
    }

    protected boolean supportsUrgentData(){
        return false; // must be overridden in sub-class
    }

    protected abstract void sendUrgentData(int data) throws IOException;

    Socket getSocket(){
        return socket;
    }

    void setSocket(Socket soc){
        this.socket=soc;
    }

    ServerSocket getServerSocket(){
        return serverSocket;
    }

    void setServerSocket(ServerSocket soc){
        this.serverSocket=soc;
    }

    public String toString(){
        return "Socket[addr="+getInetAddress()+
                ",port="+getPort()+",localport="+getLocalPort()+"]";
    }

    protected InetAddress getInetAddress(){
        return address;
    }

    protected int getPort(){
        return port;
    }

    protected int getLocalPort(){
        return localport;
    }

    void reset() throws IOException{
        address=null;
        port=0;
        localport=0;
    }

    protected void setPerformancePreferences(int connectionTime,
                                             int latency,
                                             int bandwidth){
        /** Not implemented yet */
    }

    <T> void setOption(SocketOption<T> name,T value) throws IOException{
        if(name==StandardSocketOptions.SO_KEEPALIVE){
            setOption(SocketOptions.SO_KEEPALIVE,value);
        }else if(name==StandardSocketOptions.SO_SNDBUF){
            setOption(SocketOptions.SO_SNDBUF,value);
        }else if(name==StandardSocketOptions.SO_RCVBUF){
            setOption(SocketOptions.SO_RCVBUF,value);
        }else if(name==StandardSocketOptions.SO_REUSEADDR){
            setOption(SocketOptions.SO_REUSEADDR,value);
        }else if(name==StandardSocketOptions.SO_LINGER){
            setOption(SocketOptions.SO_LINGER,value);
        }else if(name==StandardSocketOptions.IP_TOS){
            setOption(SocketOptions.IP_TOS,value);
        }else if(name==StandardSocketOptions.TCP_NODELAY){
            setOption(SocketOptions.TCP_NODELAY,value);
        }else{
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    <T> T getOption(SocketOption<T> name) throws IOException{
        if(name==StandardSocketOptions.SO_KEEPALIVE){
            return (T)getOption(SocketOptions.SO_KEEPALIVE);
        }else if(name==StandardSocketOptions.SO_SNDBUF){
            return (T)getOption(SocketOptions.SO_SNDBUF);
        }else if(name==StandardSocketOptions.SO_RCVBUF){
            return (T)getOption(SocketOptions.SO_RCVBUF);
        }else if(name==StandardSocketOptions.SO_REUSEADDR){
            return (T)getOption(SocketOptions.SO_REUSEADDR);
        }else if(name==StandardSocketOptions.SO_LINGER){
            return (T)getOption(SocketOptions.SO_LINGER);
        }else if(name==StandardSocketOptions.IP_TOS){
            return (T)getOption(SocketOptions.IP_TOS);
        }else if(name==StandardSocketOptions.TCP_NODELAY){
            return (T)getOption(SocketOptions.TCP_NODELAY);
        }else{
            throw new UnsupportedOperationException("unsupported option");
        }
    }
}
