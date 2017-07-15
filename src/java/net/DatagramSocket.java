/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class DatagramSocket implements java.io.Closeable{
    static final int ST_NOT_CONNECTED=0;
    static final int ST_CONNECTED=1;
    static final int ST_CONNECTED_NO_IMPL=2;
    static Class<?> implClass=null;
    static DatagramSocketImplFactory factory;
    DatagramSocketImpl impl;
    boolean oldImpl=false;
    int connectState=ST_NOT_CONNECTED;
    InetAddress connectedAddress=null;
    int connectedPort=-1;
    private boolean created=false;
    private boolean bound=false;
    private boolean closed=false;
    private Object closeLock=new Object();
    private boolean explicitFilter=false;
    private int bytesLeftToFilter;

    public DatagramSocket() throws SocketException{
        this(new InetSocketAddress(0));
    }

    public DatagramSocket(SocketAddress bindaddr) throws SocketException{
        // create a datagram socket.
        createImpl();
        if(bindaddr!=null){
            try{
                bind(bindaddr);
            }finally{
                if(!isBound())
                    close();
            }
        }
    }

    void createImpl() throws SocketException{
        if(impl==null){
            if(factory!=null){
                impl=factory.createDatagramSocketImpl();
                checkOldImpl();
            }else{
                boolean isMulticast=(this instanceof MulticastSocket)?true:false;
                impl=DefaultDatagramSocketImplFactory.createDatagramSocketImpl(isMulticast);
                checkOldImpl();
            }
        }
        // creates a udp socket
        impl.create();
        impl.setDatagramSocket(this);
        created=true;
    }

    private void checkOldImpl(){
        if(impl==null)
            return;
        // DatagramSocketImpl.peekdata() is a protected method, therefore we need to use
        // getDeclaredMethod, therefore we need permission to access the member
        try{
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>(){
                        public Void run() throws NoSuchMethodException{
                            Class<?>[] cl=new Class<?>[1];
                            cl[0]=DatagramPacket.class;
                            impl.getClass().getDeclaredMethod("peekData",cl);
                            return null;
                        }
                    });
        }catch(java.security.PrivilegedActionException e){
            oldImpl=true;
        }
    }

    public synchronized void bind(SocketAddress addr) throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        if(isBound())
            throw new SocketException("already bound");
        if(addr==null)
            addr=new InetSocketAddress(0);
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type!");
        InetSocketAddress epoint=(InetSocketAddress)addr;
        if(epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        InetAddress iaddr=epoint.getAddress();
        int port=epoint.getPort();
        checkAddress(iaddr,"bind");
        SecurityManager sec=System.getSecurityManager();
        if(sec!=null){
            sec.checkListen(port);
        }
        try{
            getImpl().bind(port,iaddr);
        }catch(SocketException e){
            getImpl().close();
            throw e;
        }
        bound=true;
    }

    DatagramSocketImpl getImpl() throws SocketException{
        if(!created)
            createImpl();
        return impl;
    }

    void checkAddress(InetAddress addr,String op){
        if(addr==null){
            return;
        }
        if(!(addr instanceof Inet4Address||addr instanceof Inet6Address)){
            throw new IllegalArgumentException(op+": invalid address type");
        }
    }

    public boolean isBound(){
        return bound;
    }

    public boolean isClosed(){
        synchronized(closeLock){
            return closed;
        }
    }

    public void close(){
        synchronized(closeLock){
            if(isClosed())
                return;
            impl.close();
            closed=true;
        }
    }

    protected DatagramSocket(DatagramSocketImpl impl){
        if(impl==null)
            throw new NullPointerException();
        this.impl=impl;
        checkOldImpl();
    }

    public DatagramSocket(int port) throws SocketException{
        this(port,null);
    }

    public DatagramSocket(int port,InetAddress laddr) throws SocketException{
        this(new InetSocketAddress(laddr,port));
    }

    public static synchronized void
    setDatagramSocketImplFactory(DatagramSocketImplFactory fac)
            throws IOException{
        if(factory!=null){
            throw new SocketException("factory already defined");
        }
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkSetFactory();
        }
        factory=fac;
    }

    private synchronized void connectInternal(InetAddress address,int port) throws SocketException{
        if(port<0||port>0xFFFF){
            throw new IllegalArgumentException("connect: "+port);
        }
        if(address==null){
            throw new IllegalArgumentException("connect: null address");
        }
        checkAddress(address,"connect");
        if(isClosed())
            return;
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            if(address.isMulticastAddress()){
                security.checkMulticast(address);
            }else{
                security.checkConnect(address.getHostAddress(),port);
                security.checkAccept(address.getHostAddress(),port);
            }
        }
        if(!isBound())
            bind(new InetSocketAddress(0));
        // old impls do not support connect/disconnect
        if(oldImpl||(impl instanceof AbstractPlainDatagramSocketImpl&&
                ((AbstractPlainDatagramSocketImpl)impl).nativeConnectDisabled())){
            connectState=ST_CONNECTED_NO_IMPL;
        }else{
            try{
                getImpl().connect(address,port);
                // socket is now connected by the impl
                connectState=ST_CONNECTED;
                // Do we need to filter some packets?
                int avail=getImpl().dataAvailable();
                if(avail==-1){
                    throw new SocketException();
                }
                explicitFilter=avail>0;
                if(explicitFilter){
                    bytesLeftToFilter=getReceiveBufferSize();
                }
            }catch(SocketException se){
                // connection will be emulated by DatagramSocket
                connectState=ST_CONNECTED_NO_IMPL;
            }
        }
        connectedAddress=address;
        connectedPort=port;
    }

    public void connect(InetAddress address,int port){
        try{
            connectInternal(address,port);
        }catch(SocketException se){
            throw new Error("connect failed",se);
        }
    }

    public void connect(SocketAddress addr) throws SocketException{
        if(addr==null)
            throw new IllegalArgumentException("Address can't be null");
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint=(InetSocketAddress)addr;
        if(epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        connectInternal(epoint.getAddress(),epoint.getPort());
    }

    public void disconnect(){
        synchronized(this){
            if(isClosed())
                return;
            if(connectState==ST_CONNECTED){
                impl.disconnect();
            }
            connectedAddress=null;
            connectedPort=-1;
            connectState=ST_NOT_CONNECTED;
            explicitFilter=false;
        }
    }

    public SocketAddress getRemoteSocketAddress(){
        if(!isConnected())
            return null;
        return new InetSocketAddress(getInetAddress(),getPort());
    }

    public boolean isConnected(){
        return connectState!=ST_NOT_CONNECTED;
    }

    public InetAddress getInetAddress(){
        return connectedAddress;
    }

    public int getPort(){
        return connectedPort;
    }

    public SocketAddress getLocalSocketAddress(){
        if(isClosed())
            return null;
        if(!isBound())
            return null;
        return new InetSocketAddress(getLocalAddress(),getLocalPort());
    }

    public void send(DatagramPacket p) throws IOException{
        InetAddress packetAddress=null;
        synchronized(p){
            if(isClosed())
                throw new SocketException("Socket is closed");
            checkAddress(p.getAddress(),"send");
            if(connectState==ST_NOT_CONNECTED){
                // check the address is ok wiht the security manager on every send.
                SecurityManager security=System.getSecurityManager();
                // The reason you want to synchronize on datagram packet
                // is because you don't want an applet to change the address
                // while you are trying to send the packet for example
                // after the security check but before the send.
                if(security!=null){
                    if(p.getAddress().isMulticastAddress()){
                        security.checkMulticast(p.getAddress());
                    }else{
                        security.checkConnect(p.getAddress().getHostAddress(),
                                p.getPort());
                    }
                }
            }else{
                // we're connected
                packetAddress=p.getAddress();
                if(packetAddress==null){
                    p.setAddress(connectedAddress);
                    p.setPort(connectedPort);
                }else if((!packetAddress.equals(connectedAddress))||
                        p.getPort()!=connectedPort){
                    throw new IllegalArgumentException("connected address "+
                            "and packet address"+
                            " differ");
                }
            }
            // Check whether the socket is bound
            if(!isBound())
                bind(new InetSocketAddress(0));
            // call the  method to send
            getImpl().send(p);
        }
    }

    public synchronized void receive(DatagramPacket p) throws IOException{
        synchronized(p){
            if(!isBound())
                bind(new InetSocketAddress(0));
            if(connectState==ST_NOT_CONNECTED){
                // check the address is ok with the security manager before every recv.
                SecurityManager security=System.getSecurityManager();
                if(security!=null){
                    while(true){
                        String peekAd=null;
                        int peekPort=0;
                        // peek at the packet to see who it is from.
                        if(!oldImpl){
                            // We can use the new peekData() API
                            DatagramPacket peekPacket=new DatagramPacket(new byte[1],1);
                            peekPort=getImpl().peekData(peekPacket);
                            peekAd=peekPacket.getAddress().getHostAddress();
                        }else{
                            InetAddress adr=new InetAddress();
                            peekPort=getImpl().peek(adr);
                            peekAd=adr.getHostAddress();
                        }
                        try{
                            security.checkAccept(peekAd,peekPort);
                            // security check succeeded - so now break
                            // and recv the packet.
                            break;
                        }catch(SecurityException se){
                            // Throw away the offending packet by consuming
                            // it in a tmp buffer.
                            DatagramPacket tmp=new DatagramPacket(new byte[1],1);
                            getImpl().receive(tmp);
                            // silently discard the offending packet
                            // and continue: unknown/malicious
                            // entities on nets should not make
                            // runtime throw security exception and
                            // disrupt the applet by sending random
                            // datagram packets.
                            continue;
                        }
                    } // end of while
                }
            }
            DatagramPacket tmp=null;
            if((connectState==ST_CONNECTED_NO_IMPL)||explicitFilter){
                // We have to do the filtering the old fashioned way since
                // the native impl doesn't support connect or the connect
                // via the impl failed, or .. "explicitFilter" may be set when
                // a socket is connected via the impl, for a period of time
                // when packets from other sources might be queued on socket.
                boolean stop=false;
                while(!stop){
                    InetAddress peekAddress=null;
                    int peekPort=-1;
                    // peek at the packet to see who it is from.
                    if(!oldImpl){
                        // We can use the new peekData() API
                        DatagramPacket peekPacket=new DatagramPacket(new byte[1],1);
                        peekPort=getImpl().peekData(peekPacket);
                        peekAddress=peekPacket.getAddress();
                    }else{
                        // this api only works for IPv4
                        peekAddress=new InetAddress();
                        peekPort=getImpl().peek(peekAddress);
                    }
                    if((!connectedAddress.equals(peekAddress))||
                            (connectedPort!=peekPort)){
                        // throw the packet away and silently continue
                        tmp=new DatagramPacket(
                                new byte[1024],1024);
                        getImpl().receive(tmp);
                        if(explicitFilter){
                            if(checkFiltering(tmp)){
                                stop=true;
                            }
                        }
                    }else{
                        stop=true;
                    }
                }
            }
            // If the security check succeeds, or the datagram is
            // connected then receive the packet
            getImpl().receive(p);
            if(explicitFilter&&tmp==null){
                // packet was not filtered, account for it here
                checkFiltering(p);
            }
        }
    }

    private boolean checkFiltering(DatagramPacket p) throws SocketException{
        bytesLeftToFilter-=p.getLength();
        if(bytesLeftToFilter<=0||getImpl().dataAvailable()<=0){
            explicitFilter=false;
            return true;
        }
        return false;
    }

    public InetAddress getLocalAddress(){
        if(isClosed())
            return null;
        InetAddress in=null;
        try{
            in=(InetAddress)getImpl().getOption(SocketOptions.SO_BINDADDR);
            if(in.isAnyLocalAddress()){
                in=InetAddress.anyLocalAddress();
            }
            SecurityManager s=System.getSecurityManager();
            if(s!=null){
                s.checkConnect(in.getHostAddress(),-1);
            }
        }catch(Exception e){
            in=InetAddress.anyLocalAddress(); // "0.0.0.0"
        }
        return in;
    }

    public int getLocalPort(){
        if(isClosed())
            return -1;
        try{
            return getImpl().getLocalPort();
        }catch(Exception e){
            return 0;
        }
    }

    public synchronized int getSoTimeout() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        if(getImpl()==null)
            return 0;
        Object o=getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /** extra type safety */
        if(o instanceof Integer){
            return ((Integer)o).intValue();
        }else{
            return 0;
        }
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_TIMEOUT,new Integer(timeout));
    }

    public synchronized int getSendBufferSize() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        int result=0;
        Object o=getImpl().getOption(SocketOptions.SO_SNDBUF);
        if(o instanceof Integer){
            result=((Integer)o).intValue();
        }
        return result;
    }

    public synchronized void setSendBufferSize(int size)
            throws SocketException{
        if(!(size>0)){
            throw new IllegalArgumentException("negative send size");
        }
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_SNDBUF,new Integer(size));
    }

    public synchronized int getReceiveBufferSize()
            throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        int result=0;
        Object o=getImpl().getOption(SocketOptions.SO_RCVBUF);
        if(o instanceof Integer){
            result=((Integer)o).intValue();
        }
        return result;
    }

    public synchronized void setReceiveBufferSize(int size)
            throws SocketException{
        if(size<=0){
            throw new IllegalArgumentException("invalid receive size");
        }
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF,new Integer(size));
    }

    public synchronized boolean getReuseAddress() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        Object o=getImpl().getOption(SocketOptions.SO_REUSEADDR);
        return ((Boolean)o).booleanValue();
    }

    public synchronized void setReuseAddress(boolean on) throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        // Integer instead of Boolean for compatibility with older DatagramSocketImpl
        if(oldImpl)
            getImpl().setOption(SocketOptions.SO_REUSEADDR,new Integer(on?-1:0));
        else
            getImpl().setOption(SocketOptions.SO_REUSEADDR,Boolean.valueOf(on));
    }

    public synchronized boolean getBroadcast() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean)(getImpl().getOption(SocketOptions.SO_BROADCAST))).booleanValue();
    }

    public synchronized void setBroadcast(boolean on) throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_BROADCAST,Boolean.valueOf(on));
    }

    public synchronized int getTrafficClass() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        return ((Integer)(getImpl().getOption(SocketOptions.IP_TOS))).intValue();
    }

    public synchronized void setTrafficClass(int tc) throws SocketException{
        if(tc<0||tc>255)
            throw new IllegalArgumentException("tc is not in range 0 -- 255");
        if(isClosed())
            throw new SocketException("Socket is closed");
        try{
            getImpl().setOption(SocketOptions.IP_TOS,tc);
        }catch(SocketException se){
            // not supported if socket already connected
            // Solaris returns error in such cases
            if(!isConnected())
                throw se;
        }
    }

    public DatagramChannel getChannel(){
        return null;
    }
}
