/**
 * Copyright (c) 2007, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import sun.net.ResourceManager;

import java.io.FileDescriptor;
import java.io.IOException;

class TwoStacksPlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl{
    static{
        init();
    }

    // true if this socket is exclusively bound
    private final boolean exclusiveBind;
    private FileDescriptor fd1;
    private InetAddress anyLocalBoundAddr=null;
    private int fduse=-1;
    /** saved between peek() and receive() calls */
    private int lastfd=-1;
    private boolean reuseAddressEmulated;
    // emulates SO_REUSEADDR when exclusiveBind is true and socket is bound
    private boolean isReuseAddress;

    TwoStacksPlainDatagramSocketImpl(boolean exclBind){
        exclusiveBind=exclBind;
    }

    private native static void init();

    protected synchronized void create() throws SocketException{
        fd1=new FileDescriptor();
        try{
            super.create();
        }catch(SocketException e){
            fd1=null;
            throw e;
        }
    }

    protected synchronized void bind(int lport,InetAddress laddr)
            throws SocketException{
        super.bind(lport,laddr);
        if(laddr.isAnyLocalAddress()){
            anyLocalBoundAddr=laddr;
        }
    }

    @Override
    protected synchronized void bind0(int lport,InetAddress laddr)
            throws SocketException{
        bind0(lport,laddr,exclusiveBind);
    }

    protected synchronized native void bind0(int lport,InetAddress laddr,
                                             boolean exclBind)
            throws SocketException;

    protected native void send(DatagramPacket p) throws IOException;    public Object getOption(int optID) throws SocketException{
        if(isClosed()){
            throw new SocketException("Socket Closed");
        }
        if(optID==SO_BINDADDR){
            if((fd!=null&&fd1!=null)&&!connected){
                return anyLocalBoundAddr;
            }
            int family=connectedAddress==null?-1:connectedAddress.holder().getFamily();
            return socketLocalAddress(family);
        }else if(optID==SO_REUSEADDR&&reuseAddressEmulated){
            return isReuseAddress;
        }else{
            return super.getOption(optID);
        }
    }

    protected synchronized native int peek(InetAddress i) throws IOException;

    protected synchronized native int peekData(DatagramPacket p) throws IOException;    protected void socketSetOption(int opt,Object val)
            throws SocketException{
        if(opt==SO_REUSEADDR&&exclusiveBind&&localPort!=0){
            // socket already bound, emulate
            reuseAddressEmulated=true;
            isReuseAddress=(Boolean)val;
        }else{
            socketNativeSetOption(opt,val);
        }
    }

    protected synchronized void receive(DatagramPacket p)
            throws IOException{
        try{
            receive0(p);
        }finally{
            fduse=-1;
        }
    }

    protected synchronized native void receive0(DatagramPacket p)
            throws IOException;    protected boolean isClosed(){
        return (fd==null&&fd1==null)?true:false;
    }



    protected void close(){
        if(fd!=null||fd1!=null){
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            fd=null;
            fd1=null;
        }
    }





    protected native void setTimeToLive(int ttl) throws IOException;

    protected native int getTimeToLive() throws IOException;

    @Deprecated
    protected native void setTTL(byte ttl) throws IOException;

    @Deprecated
    protected native byte getTTL() throws IOException;

    protected native void join(InetAddress inetaddr,NetworkInterface netIf)
            throws IOException;

    protected native void leave(InetAddress inetaddr,NetworkInterface netIf)
            throws IOException;

    protected native void datagramSocketCreate() throws SocketException;

    protected native void datagramSocketClose();

    protected native void socketNativeSetOption(int opt,Object val)
            throws SocketException;

    protected native Object socketGetOption(int opt) throws SocketException;

    protected native void connect0(InetAddress address,int port) throws SocketException;

    protected native Object socketLocalAddress(int family) throws SocketException;

    protected native void disconnect0(int family);

    native int dataAvailable();
}
