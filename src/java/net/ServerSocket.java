/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class ServerSocket implements java.io.Closeable{
    private static SocketImplFactory factory=null;
    private boolean created=false;
    private boolean bound=false;
    private boolean closed=false;
    private Object closeLock=new Object();
    private SocketImpl impl;
    private boolean oldImpl=false;

    ServerSocket(SocketImpl impl){
        this.impl=impl;
        impl.setServerSocket(this);
    }

    public ServerSocket() throws IOException{
        setImpl();
    }

    private void setImpl(){
        if(factory!=null){
            impl=factory.createSocketImpl();
            checkOldImpl();
        }else{
            // No need to do a checkOldImpl() here, we know it's an up to date
            // SocketImpl!
            impl=new SocksSocketImpl();
        }
        if(impl!=null)
            impl.setServerSocket(this);
    }

    private void checkOldImpl(){
        if(impl==null)
            return;
        // SocketImpl.connect() is a protected method, therefore we need to use
        // getDeclaredMethod, therefore we need permission to access the member
        try{
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>(){
                        public Void run() throws NoSuchMethodException{
                            impl.getClass().getDeclaredMethod("connect",
                                    SocketAddress.class,
                                    int.class);
                            return null;
                        }
                    });
        }catch(java.security.PrivilegedActionException e){
            oldImpl=true;
        }
    }

    public ServerSocket(int port) throws IOException{
        this(port,50,null);
    }

    public ServerSocket(int port,int backlog,InetAddress bindAddr) throws IOException{
        setImpl();
        if(port<0||port>0xFFFF)
            throw new IllegalArgumentException(
                    "Port value out of range: "+port);
        if(backlog<1)
            backlog=50;
        try{
            bind(new InetSocketAddress(bindAddr,port),backlog);
        }catch(SecurityException e){
            close();
            throw e;
        }catch(IOException e){
            close();
            throw e;
        }
    }

    public void bind(SocketAddress endpoint,int backlog) throws IOException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        if(!oldImpl&&isBound())
            throw new SocketException("Already bound");
        if(endpoint==null)
            endpoint=new InetSocketAddress(0);
        if(!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint=(InetSocketAddress)endpoint;
        if(epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if(backlog<1)
            backlog=50;
        try{
            SecurityManager security=System.getSecurityManager();
            if(security!=null)
                security.checkListen(epoint.getPort());
            getImpl().bind(epoint.getAddress(),epoint.getPort());
            getImpl().listen(backlog);
            bound=true;
        }catch(SecurityException e){
            bound=false;
            throw e;
        }catch(IOException e){
            bound=false;
            throw e;
        }
    }

    SocketImpl getImpl() throws SocketException{
        if(!created)
            createImpl();
        return impl;
    }

    void createImpl() throws SocketException{
        if(impl==null)
            setImpl();
        try{
            impl.create(true);
            created=true;
        }catch(IOException e){
            throw new SocketException(e.getMessage());
        }
    }

    public boolean isBound(){
        // Before 1.3 ServerSockets were always bound during creation
        return bound||oldImpl;
    }

    public boolean isClosed(){
        synchronized(closeLock){
            return closed;
        }
    }

    public void close() throws IOException{
        synchronized(closeLock){
            if(isClosed())
                return;
            if(created)
                impl.close();
            closed=true;
        }
    }

    public ServerSocket(int port,int backlog) throws IOException{
        this(port,backlog,null);
    }

    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException{
        if(factory!=null){
            throw new SocketException("factory already defined");
        }
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkSetFactory();
        }
        factory=fac;
    }

    public void bind(SocketAddress endpoint) throws IOException{
        bind(endpoint,50);
    }

    public InetAddress getInetAddress(){
        if(!isBound())
            return null;
        try{
            InetAddress in=getImpl().getInetAddress();
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null)
                sm.checkConnect(in.getHostAddress(),-1);
            return in;
        }catch(SecurityException e){
            return InetAddress.getLoopbackAddress();
        }catch(SocketException e){
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return null;
    }

    public int getLocalPort(){
        if(!isBound())
            return -1;
        try{
            return getImpl().getLocalPort();
        }catch(SocketException e){
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return -1;
    }

    public SocketAddress getLocalSocketAddress(){
        if(!isBound())
            return null;
        return new InetSocketAddress(getInetAddress(),getLocalPort());
    }

    public Socket accept() throws IOException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        if(!isBound())
            throw new SocketException("Socket is not bound yet");
        Socket s=new Socket((SocketImpl)null);
        implAccept(s);
        return s;
    }

    protected final void implAccept(Socket s) throws IOException{
        SocketImpl si=null;
        try{
            if(s.impl==null)
                s.setImpl();
            else{
                s.impl.reset();
            }
            si=s.impl;
            s.impl=null;
            si.address=new InetAddress();
            si.fd=new FileDescriptor();
            getImpl().accept(si);
            SecurityManager security=System.getSecurityManager();
            if(security!=null){
                security.checkAccept(si.getInetAddress().getHostAddress(),
                        si.getPort());
            }
        }catch(IOException e){
            if(si!=null)
                si.reset();
            s.impl=si;
            throw e;
        }catch(SecurityException e){
            if(si!=null)
                si.reset();
            s.impl=si;
            throw e;
        }
        s.impl=si;
        s.postAccept();
    }

    public ServerSocketChannel getChannel(){
        return null;
    }

    public synchronized int getSoTimeout() throws IOException{
        if(isClosed())
            throw new SocketException("Socket is closed");
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

    public boolean getReuseAddress() throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean)(getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }

    public void setReuseAddress(boolean on) throws SocketException{
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_REUSEADDR,Boolean.valueOf(on));
    }

    public String toString(){
        if(!isBound())
            return "ServerSocket[unbound]";
        InetAddress in;
        if(System.getSecurityManager()!=null)
            in=InetAddress.getLoopbackAddress();
        else
            in=impl.getInetAddress();
        return "ServerSocket[addr="+in+
                ",localport="+impl.getLocalPort()+"]";
    }

    void setBound(){
        bound=true;
    }

    void setCreated(){
        created=true;
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

    public synchronized void setReceiveBufferSize(int size) throws SocketException{
        if(!(size>0)){
            throw new IllegalArgumentException("negative receive size");
        }
        if(isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF,new Integer(size));
    }

    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth){
        /** Not implemented yet */
    }
}
