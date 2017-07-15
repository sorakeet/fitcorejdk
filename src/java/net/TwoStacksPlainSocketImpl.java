/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.net.ResourceManager;

import java.io.FileDescriptor;
import java.io.IOException;

class TwoStacksPlainSocketImpl extends AbstractPlainSocketImpl{
    static{
        initProto();
    }

    // true if this socket is exclusively bound
    private final boolean exclusiveBind;
    private FileDescriptor fd1;
    private InetAddress anyLocalBoundAddr=null;
    private int lastfd=-1;
    // emulates SO_REUSEADDR when exclusiveBind is true
    private boolean isReuseAddress;

    public TwoStacksPlainSocketImpl(boolean exclBind){
        exclusiveBind=exclBind;
    }

    public TwoStacksPlainSocketImpl(FileDescriptor fd,boolean exclBind){
        this.fd=fd;
        exclusiveBind=exclBind;
    }

    static native void initProto();

    protected synchronized void create(boolean stream) throws IOException{
        fd1=new FileDescriptor();
        try{
            super.create(stream);
        }catch(IOException e){
            fd1=null;
            throw e;
        }
    }

    public Object getOption(int opt) throws SocketException{
        if(isClosedOrPending()){
            throw new SocketException("Socket Closed");
        }
        if(opt==SO_BINDADDR){
            if(fd!=null&&fd1!=null){
                /** must be unbound or else bound to anyLocal */
                return anyLocalBoundAddr;
            }
            InetAddressContainer in=new InetAddressContainer();
            socketGetOption(opt,in);
            return in.addr;
        }else if(opt==SO_REUSEADDR&&exclusiveBind){
            // SO_REUSEADDR emulated when using exclusive bind
            return isReuseAddress;
        }else
            return super.getOption(opt);
    }

    protected synchronized void bind(InetAddress address,int lport)
            throws IOException{
        super.bind(address,lport);
        if(address.isAnyLocalAddress()){
            anyLocalBoundAddr=address;
        }
    }

    @Override
    protected void close() throws IOException{
        synchronized(fdLock){
            if(fd!=null||fd1!=null){
                if(!stream){
                    ResourceManager.afterUdpClose();
                }
                if(fdUseCount==0){
                    if(closePending){
                        return;
                    }
                    closePending=true;
                    socketClose();
                    fd=null;
                    fd1=null;
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
                        socketClose();
                    }
                }
            }
        }
    }

    @Override
    void reset() throws IOException{
        if(fd!=null||fd1!=null){
            socketClose();
        }
        fd=null;
        fd1=null;
        super.reset();
    }

    @Override
    public boolean isClosedOrPending(){
        /**
         * Lock on fdLock to ensure that we wait if a
         * close is in progress.
         */
        synchronized(fdLock){
            if(closePending||(fd==null&&fd1==null)){
                return true;
            }else{
                return false;
            }
        }
    }

    native void socketCreate(boolean isServer) throws IOException;

    native void socketConnect(InetAddress address,int port,int timeout)
            throws IOException;

    @Override
    void socketBind(InetAddress address,int port) throws IOException{
        socketBind(address,port,exclusiveBind);
    }

    native void socketBind(InetAddress address,int port,boolean exclBind)
            throws IOException;

    native void socketListen(int count) throws IOException;

    native void socketAccept(SocketImpl s) throws IOException;

    native int socketAvailable() throws IOException;

    native void socketClose0(boolean useDeferredClose) throws IOException;

    native void socketShutdown(int howto) throws IOException;

    @Override
    void socketSetOption(int opt,boolean on,Object value)
            throws SocketException{
        // SO_REUSEADDR emulated when using exclusive bind
        if(opt==SO_REUSEADDR&&exclusiveBind)
            isReuseAddress=on;
        else
            socketNativeSetOption(opt,on,value);
    }

    native void socketNativeSetOption(int cmd,boolean on,Object value)
            throws SocketException;

    native int socketGetOption(int opt,Object iaContainerObj) throws SocketException;

    native void socketSendUrgentData(int data) throws IOException;
}
