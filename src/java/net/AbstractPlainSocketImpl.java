/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class AbstractPlainSocketImpl extends SocketImpl{
    public final static int SHUT_RD=0;
    public final static int SHUT_WR=1;

    /**
     * Load net library into runtime.
     */
    static{
        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>(){
                    public Void run(){
                        System.loadLibrary("net");
                        return null;
                    }
                });
    }

    protected final Object fdLock=new Object();
    private final Object resetLock=new Object();
    protected int fdUseCount=0;
    protected boolean closePending=false;
    protected boolean stream;
    int timeout;   // timeout in millisec
    // traffic class
    private int trafficClass;
    private boolean shut_rd=false;
    private boolean shut_wr=false;
    private SocketInputStream socketInputStream=null;
    private SocketOutputStream socketOutputStream=null;
    private int CONNECTION_NOT_RESET=0;
    private int CONNECTION_RESET_PENDING=1;
    private int CONNECTION_RESET=2;
    private int resetState;

    protected synchronized void create(boolean stream) throws IOException{
        this.stream=stream;
        if(!stream){
            ResourceManager.beforeUdpCreate();
            // only create the fd after we know we will be able to create the socket
            fd=new FileDescriptor();
            try{
                socketCreate(false);
            }catch(IOException ioe){
                ResourceManager.afterUdpClose();
                fd=null;
                throw ioe;
            }
        }else{
            fd=new FileDescriptor();
            socketCreate(true);
        }
        if(socket!=null)
            socket.setCreated();
        if(serverSocket!=null)
            serverSocket.setCreated();
    }

    protected void connect(String host,int port)
            throws UnknownHostException, IOException{
        boolean connected=false;
        try{
            InetAddress address=InetAddress.getByName(host);
            this.port=port;
            this.address=address;
            connectToAddress(address,port,timeout);
            connected=true;
        }finally{
            if(!connected){
                try{
                    close();
                }catch(IOException ioe){
                    /** Do nothing. If connect threw an exception then
                     it will be passed up the call stack */
                }
            }
        }
    }

    protected void connect(InetAddress address,int port) throws IOException{
        this.port=port;
        this.address=address;
        try{
            connectToAddress(address,port,timeout);
            return;
        }catch(IOException e){
            // everything failed
            close();
            throw e;
        }
    }

    protected void connect(SocketAddress address,int timeout)
            throws IOException{
        boolean connected=false;
        try{
            if(address==null||!(address instanceof InetSocketAddress))
                throw new IllegalArgumentException("unsupported address type");
            InetSocketAddress addr=(InetSocketAddress)address;
            if(addr.isUnresolved())
                throw new UnknownHostException(addr.getHostName());
            this.port=addr.getPort();
            this.address=addr.getAddress();
            connectToAddress(this.address,port,timeout);
            connected=true;
        }finally{
            if(!connected){
                try{
                    close();
                }catch(IOException ioe){
                    /** Do nothing. If connect threw an exception then
                     it will be passed up the call stack */
                }
            }
        }
    }

    protected synchronized void bind(InetAddress address,int lport)
            throws IOException{
        synchronized(fdLock){
            if(!closePending&&(socket==null||!socket.isBound())){
                NetHooks.beforeTcpBind(fd,address,lport);
            }
        }
        socketBind(address,lport);
        if(socket!=null)
            socket.setBound();
        if(serverSocket!=null)
            serverSocket.setBound();
    }

    protected synchronized void listen(int count) throws IOException{
        socketListen(count);
    }

    protected void accept(SocketImpl s) throws IOException{
        acquireFD();
        try{
            socketAccept(s);
        }finally{
            releaseFD();
        }
    }

    protected synchronized InputStream getInputStream() throws IOException{
        synchronized(fdLock){
            if(isClosedOrPending())
                throw new IOException("Socket Closed");
            if(shut_rd)
                throw new IOException("Socket input is shutdown");
            if(socketInputStream==null)
                socketInputStream=new SocketInputStream(this);
        }
        return socketInputStream;
    }

    void setInputStream(SocketInputStream in){
        socketInputStream=in;
    }

    protected synchronized OutputStream getOutputStream() throws IOException{
        synchronized(fdLock){
            if(isClosedOrPending())
                throw new IOException("Socket Closed");
            if(shut_wr)
                throw new IOException("Socket output is shutdown");
            if(socketOutputStream==null)
                socketOutputStream=new SocketOutputStream(this);
        }
        return socketOutputStream;
    }

    protected synchronized int available() throws IOException{
        if(isClosedOrPending()){
            throw new IOException("Stream closed.");
        }
        /**
         * If connection has been reset or shut down for input, then return 0
         * to indicate there are no buffered bytes.
         */
        if(isConnectionReset()||shut_rd){
            return 0;
        }
        /**
         * If no bytes available and we were previously notified
         * of a connection reset then we move to the reset state.
         *
         * If are notified of a connection reset then check
         * again if there are bytes buffered on the socket.
         */
        int n=0;
        try{
            n=socketAvailable();
            if(n==0&&isConnectionResetPending()){
                setConnectionReset();
            }
        }catch(ConnectionResetException exc1){
            setConnectionResetPending();
            try{
                n=socketAvailable();
                if(n==0){
                    setConnectionReset();
                }
            }catch(ConnectionResetException exc2){
            }
        }
        return n;
    }

    protected void close() throws IOException{
        synchronized(fdLock){
            if(fd!=null){
                if(!stream){
                    ResourceManager.afterUdpClose();
                }
                if(fdUseCount==0){
                    if(closePending){
                        return;
                    }
                    closePending=true;
                    /**
                     * We close the FileDescriptor in two-steps - first the
                     * "pre-close" which closes the socket but doesn't
                     * release the underlying file descriptor. This operation
                     * may be lengthy due to untransmitted data and a long
                     * linger interval. Once the pre-close is done we do the
                     * actual socket to release the fd.
                     */
                    try{
                        socketPreClose();
                    }finally{
                        socketClose();
                    }
                    fd=null;
                    return;
                }else{
                    /**
                     * If a thread has acquired the fd and a close
                     * isn't pending then use a deferred close.
                     * Also decrement fdUseCount to signal the last
                     * thread that releases the fd to close it.
                     */
                    if(!closePending){
                        closePending=true;
                        fdUseCount--;
                        socketPreClose();
                    }
                }
            }
        }
    }

    protected void shutdownInput() throws IOException{
        if(fd!=null){
            socketShutdown(SHUT_RD);
            if(socketInputStream!=null){
                socketInputStream.setEOF(true);
            }
            shut_rd=true;
        }
    }

    protected void shutdownOutput() throws IOException{
        if(fd!=null){
            socketShutdown(SHUT_WR);
            shut_wr=true;
        }
    }

    protected boolean supportsUrgentData(){
        return true;
    }

    protected void sendUrgentData(int data) throws IOException{
        if(fd==null){
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData(data);
    }

    void reset() throws IOException{
        if(fd!=null){
            socketClose();
        }
        fd=null;
        super.reset();
    }

    abstract void socketSendUrgentData(int data)
            throws IOException;

    abstract void socketShutdown(int howto)
            throws IOException;

    private void socketPreClose() throws IOException{
        socketClose0(true);
    }

    public boolean isConnectionReset(){
        synchronized(resetLock){
            return (resetState==CONNECTION_RESET);
        }
    }

    public boolean isConnectionResetPending(){
        synchronized(resetLock){
            return (resetState==CONNECTION_RESET_PENDING);
        }
    }

    public void setConnectionReset(){
        synchronized(resetLock){
            resetState=CONNECTION_RESET;
        }
    }

    public void setConnectionResetPending(){
        synchronized(resetLock){
            if(resetState==CONNECTION_NOT_RESET){
                resetState=CONNECTION_RESET_PENDING;
            }
        }
    }

    abstract int socketAvailable()
            throws IOException;

    public boolean isClosedOrPending(){
        /**
         * Lock on fdLock to ensure that we wait if a
         * close is in progress.
         */
        synchronized(fdLock){
            if(closePending||(fd==null)){
                return true;
            }else{
                return false;
            }
        }
    }

    FileDescriptor acquireFD(){
        synchronized(fdLock){
            fdUseCount++;
            return fd;
        }
    }

    void releaseFD(){
        synchronized(fdLock){
            fdUseCount--;
            if(fdUseCount==-1){
                if(fd!=null){
                    try{
                        socketClose();
                    }catch(IOException e){
                    }finally{
                        fd=null;
                    }
                }
            }
        }
    }

    protected void socketClose() throws IOException{
        socketClose0(false);
    }

    abstract void socketClose0(boolean useDeferredClose)
            throws IOException;

    abstract void socketAccept(SocketImpl s)
            throws IOException;

    abstract void socketListen(int count)
            throws IOException;

    abstract void socketBind(InetAddress address,int port)
            throws IOException;

    abstract void socketCreate(boolean isServer) throws IOException;

    private void connectToAddress(InetAddress address,int port,int timeout) throws IOException{
        if(address.isAnyLocalAddress()){
            doConnect(InetAddress.getLocalHost(),port,timeout);
        }else{
            doConnect(address,port,timeout);
        }
    }

    public void setOption(int opt,Object val) throws SocketException{
        if(isClosedOrPending()){
            throw new SocketException("Socket Closed");
        }
        boolean on=true;
        switch(opt){
            /** check type safety b4 going native.  These should never
             * fail, since only java.Socket* has access to
             * PlainSocketImpl.setOption().
             */
            case SO_LINGER:
                if(val==null||(!(val instanceof Integer)&&!(val instanceof Boolean)))
                    throw new SocketException("Bad parameter for option");
                if(val instanceof Boolean){
                    /** true only if disabling - enabling should be Integer */
                    on=false;
                }
                break;
            case SO_TIMEOUT:
                if(val==null||(!(val instanceof Integer)))
                    throw new SocketException("Bad parameter for SO_TIMEOUT");
                int tmp=((Integer)val).intValue();
                if(tmp<0)
                    throw new IllegalArgumentException("timeout < 0");
                timeout=tmp;
                break;
            case IP_TOS:
                if(val==null||!(val instanceof Integer)){
                    throw new SocketException("bad argument for IP_TOS");
                }
                trafficClass=((Integer)val).intValue();
                break;
            case SO_BINDADDR:
                throw new SocketException("Cannot re-bind socket");
            case TCP_NODELAY:
                if(val==null||!(val instanceof Boolean))
                    throw new SocketException("bad parameter for TCP_NODELAY");
                on=((Boolean)val).booleanValue();
                break;
            case SO_SNDBUF:
            case SO_RCVBUF:
                if(val==null||!(val instanceof Integer)||
                        !(((Integer)val).intValue()>0)){
                    throw new SocketException("bad parameter for SO_SNDBUF "+
                            "or SO_RCVBUF");
                }
                break;
            case SO_KEEPALIVE:
                if(val==null||!(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_KEEPALIVE");
                on=((Boolean)val).booleanValue();
                break;
            case SO_OOBINLINE:
                if(val==null||!(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_OOBINLINE");
                on=((Boolean)val).booleanValue();
                break;
            case SO_REUSEADDR:
                if(val==null||!(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_REUSEADDR");
                on=((Boolean)val).booleanValue();
                break;
            default:
                throw new SocketException("unrecognized TCP option: "+opt);
        }
        socketSetOption(opt,on,val);
    }

    public Object getOption(int opt) throws SocketException{
        if(isClosedOrPending()){
            throw new SocketException("Socket Closed");
        }
        if(opt==SO_TIMEOUT){
            return new Integer(timeout);
        }
        int ret=0;
        /**
         * The native socketGetOption() knows about 3 options.
         * The 32 bit value it returns will be interpreted according
         * to what we're asking.  A return of -1 means it understands
         * the option but its turned off.  It will raise a SocketException
         * if "opt" isn't one it understands.
         */
        switch(opt){
            case TCP_NODELAY:
                ret=socketGetOption(opt,null);
                return Boolean.valueOf(ret!=-1);
            case SO_OOBINLINE:
                ret=socketGetOption(opt,null);
                return Boolean.valueOf(ret!=-1);
            case SO_LINGER:
                ret=socketGetOption(opt,null);
                return (ret==-1)?Boolean.FALSE:(Object)(new Integer(ret));
            case SO_REUSEADDR:
                ret=socketGetOption(opt,null);
                return Boolean.valueOf(ret!=-1);
            case SO_BINDADDR:
                InetAddressContainer in=new InetAddressContainer();
                ret=socketGetOption(opt,in);
                return in.addr;
            case SO_SNDBUF:
            case SO_RCVBUF:
                ret=socketGetOption(opt,null);
                return new Integer(ret);
            case IP_TOS:
                try{
                    ret=socketGetOption(opt,null);
                    if(ret==-1){ // ipv6 tos
                        return trafficClass;
                    }else{
                        return ret;
                    }
                }catch(SocketException se){
                    // TODO - should make better effort to read TOS or TCLASS
                    return trafficClass; // ipv6 tos
                }
            case SO_KEEPALIVE:
                ret=socketGetOption(opt,null);
                return Boolean.valueOf(ret!=-1);
            // should never get here
            default:
                return null;
        }
    }

    abstract int socketGetOption(int opt,Object iaContainerObj) throws SocketException;

    abstract void socketSetOption(int cmd,boolean on,Object value)
            throws SocketException;

    synchronized void doConnect(InetAddress address,int port,int timeout) throws IOException{
        synchronized(fdLock){
            if(!closePending&&(socket==null||!socket.isBound())){
                NetHooks.beforeTcpConnect(fd,address,port);
            }
        }
        try{
            acquireFD();
            try{
                socketConnect(address,port,timeout);
                /** socket may have been closed during poll/select */
                synchronized(fdLock){
                    if(closePending){
                        throw new SocketException("Socket closed");
                    }
                }
                // If we have a ref. to the Socket, then sets the flags
                // created, bound & connected to true.
                // This is normally done in Socket.connect() but some
                // subclasses of Socket may call impl.connect() directly!
                if(socket!=null){
                    socket.setBound();
                    socket.setConnected();
                }
            }finally{
                releaseFD();
            }
        }catch(IOException e){
            close();
            throw e;
        }
    }

    void setFileDescriptor(FileDescriptor fd){
        this.fd=fd;
    }

    void setAddress(InetAddress address){
        this.address=address;
    }

    void setPort(int port){
        this.port=port;
    }

    void setLocalPort(int localport){
        this.localport=localport;
    }

    protected void finalize() throws IOException{
        close();
    }

    public int getTimeout(){
        return timeout;
    }

    abstract void socketConnect(InetAddress address,int port,int timeout)
            throws IOException;
}
