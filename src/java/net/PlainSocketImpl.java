/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedAction;

class PlainSocketImpl extends AbstractPlainSocketImpl{
    private static float version;
    private static boolean preferIPv4Stack=false;
    private static boolean useDualStackImpl=false;
    private static String exclBindProp;
    private static boolean exclusiveBind=true;

    static{
        java.security.AccessController.doPrivileged(new PrivilegedAction<Object>(){
            public Object run(){
                version=0;
                try{
                    version=Float.parseFloat(System.getProperties().getProperty("os.version"));
                    preferIPv4Stack=Boolean.parseBoolean(
                            System.getProperties().getProperty("java.net.preferIPv4Stack"));
                    exclBindProp=System.getProperty("sun.net.useExclusiveBind");
                }catch(NumberFormatException e){
                    assert false:e;
                }
                return null; // nothing to return
            }
        });
        // (version >= 6.0) implies Vista or greater.
        if(version>=6.0&&!preferIPv4Stack){
            useDualStackImpl=true;
        }
        if(exclBindProp!=null){
            // sun.net.useExclusiveBind is true
            exclusiveBind=exclBindProp.length()==0?true
                    :Boolean.parseBoolean(exclBindProp);
        }else if(version<6.0){
            exclusiveBind=false;
        }
    }

    private AbstractPlainSocketImpl impl;

    PlainSocketImpl(){
        if(useDualStackImpl){
            impl=new DualStackPlainSocketImpl(exclusiveBind);
        }else{
            impl=new TwoStacksPlainSocketImpl(exclusiveBind);
        }
    }

    PlainSocketImpl(FileDescriptor fd){
        if(useDualStackImpl){
            impl=new DualStackPlainSocketImpl(fd,exclusiveBind);
        }else{
            impl=new TwoStacksPlainSocketImpl(fd,exclusiveBind);
        }
    }
    // Override methods in SocketImpl that access impl's fields.

    protected FileDescriptor getFileDescriptor(){
        return impl.getFileDescriptor();
    }

    protected InetAddress getInetAddress(){
        return impl.getInetAddress();
    }

    protected int getPort(){
        return impl.getPort();
    }

    protected int getLocalPort(){
        return impl.getLocalPort();
    }

    void setLocalPort(int localPort){
        impl.setLocalPort(localPort);
    }

    protected void close() throws IOException{
        try{
            impl.close();
        }finally{
            // set fd to delegate's fd to be compatible with older releases
            this.fd=null;
        }
    }    void setSocket(Socket soc){
        impl.setSocket(soc);
    }

    void reset() throws IOException{
        try{
            impl.reset();
        }finally{
            // set fd to delegate's fd to be compatible with older releases
            this.fd=null;
        }
    }

    protected void shutdownInput() throws IOException{
        impl.shutdownInput();
    }    Socket getSocket(){
        return impl.getSocket();
    }

    protected void shutdownOutput() throws IOException{
        impl.shutdownOutput();
    }

    protected void sendUrgentData(int data) throws IOException{
        impl.sendUrgentData(data);
    }    void setServerSocket(ServerSocket soc){
        impl.setServerSocket(soc);
    }

    FileDescriptor acquireFD(){
        return impl.acquireFD();
    }

    void releaseFD(){
        impl.releaseFD();
    }    ServerSocket getServerSocket(){
        return impl.getServerSocket();
    }

    public boolean isConnectionReset(){
        return impl.isConnectionReset();
    }

    public boolean isConnectionResetPending(){
        return impl.isConnectionResetPending();
    }    public String toString(){
        return impl.toString();
    }
    // Override methods in AbstractPlainSocketImpl that access impl's fields.

    public void setConnectionReset(){
        impl.setConnectionReset();
    }

    public void setConnectionResetPending(){
        impl.setConnectionResetPending();
    }

    public boolean isClosedOrPending(){
        return impl.isClosedOrPending();
    }

    public int getTimeout(){
        return impl.getTimeout();
    }

    void socketCreate(boolean isServer) throws IOException{
        impl.socketCreate(isServer);
    }

    void socketConnect(InetAddress address,int port,int timeout)
            throws IOException{
        impl.socketConnect(address,port,timeout);
    }

    void socketBind(InetAddress address,int port)
            throws IOException{
        impl.socketBind(address,port);
    }

    void socketListen(int count) throws IOException{
        impl.socketListen(count);
    }

    void socketAccept(SocketImpl s) throws IOException{
        impl.socketAccept(s);
    }

    int socketAvailable() throws IOException{
        return impl.socketAvailable();
    }

    void socketClose0(boolean useDeferredClose) throws IOException{
        impl.socketClose0(useDeferredClose);
    }

    void socketShutdown(int howto) throws IOException{
        impl.socketShutdown(howto);
    }

    void socketSetOption(int cmd,boolean on,Object value)
            throws SocketException{
        impl.socketSetOption(cmd,on,value);
    }

    int socketGetOption(int opt,Object iaContainerObj) throws SocketException{
        return impl.socketGetOption(opt,iaContainerObj);
    }

    void socketSendUrgentData(int data) throws IOException{
        impl.socketSendUrgentData(data);
    }

    void setPort(int port){
        impl.setPort(port);
    }

    void setFileDescriptor(FileDescriptor fd){
        impl.setFileDescriptor(fd);
    }

    void setAddress(InetAddress address){
        impl.setAddress(address);
    }

    protected synchronized void create(boolean stream) throws IOException{
        impl.create(stream);
        // set fd to delegate's fd to be compatible with older releases
        this.fd=impl.fd;
    }

    protected void connect(String host,int port)
            throws UnknownHostException, IOException{
        impl.connect(host,port);
    }

    protected void connect(InetAddress address,int port) throws IOException{
        impl.connect(address,port);
    }

    protected void connect(SocketAddress address,int timeout) throws IOException{
        impl.connect(address,timeout);
    }

    public void setOption(int opt,Object val) throws SocketException{
        impl.setOption(opt,val);
    }

    public Object getOption(int opt) throws SocketException{
        return impl.getOption(opt);
    }

    synchronized void doConnect(InetAddress address,int port,int timeout) throws IOException{
        impl.doConnect(address,port,timeout);
    }

    protected synchronized void bind(InetAddress address,int lport)
            throws IOException{
        impl.bind(address,lport);
    }

    protected synchronized void accept(SocketImpl s) throws IOException{
        if(s instanceof PlainSocketImpl){
            // pass in the real impl not the wrapper.
            SocketImpl delegate=((PlainSocketImpl)s).impl;
            delegate.address=new InetAddress();
            delegate.fd=new FileDescriptor();
            impl.accept(delegate);
            // set fd to delegate's fd to be compatible with older releases
            s.fd=delegate.fd;
        }else{
            impl.accept(s);
        }
    }

    protected synchronized InputStream getInputStream() throws IOException{
        return impl.getInputStream();
    }

    void setInputStream(SocketInputStream in){
        impl.setInputStream(in);
    }
    // Override methods in AbstractPlainSocketImpl that need to be implemented.

    protected synchronized OutputStream getOutputStream() throws IOException{
        return impl.getOutputStream();
    }










}
