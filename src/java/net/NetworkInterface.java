/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.security.AccessController;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public final class NetworkInterface{
    private static final NetworkInterface defaultInterface;
    private static final int defaultIndex;

    /** index of defaultInterface */
    static{
        AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>(){
                    public Void run(){
                        System.loadLibrary("net");
                        return null;
                    }
                });
        init();
        defaultInterface=DefaultInterface.getDefault();
        if(defaultInterface!=null){
            defaultIndex=defaultInterface.getIndex();
        }else{
            defaultIndex=0;
        }
    }

    private String name;
    private String displayName;
    private int index;
    private InetAddress addrs[];
    private InterfaceAddress bindings[];
    private NetworkInterface childs[];
    private NetworkInterface parent=null;
    private boolean virtual=false;

    NetworkInterface(){
    }

    NetworkInterface(String name,int index,InetAddress[] addrs){
        this.name=name;
        this.index=index;
        this.addrs=addrs;
    }

    public static NetworkInterface getByName(String name) throws SocketException{
        if(name==null)
            throw new NullPointerException();
        return getByName0(name);
    }

    private native static NetworkInterface getByName0(String name)
            throws SocketException;

    public static NetworkInterface getByIndex(int index) throws SocketException{
        if(index<0)
            throw new IllegalArgumentException("Interface index can't be negative");
        return getByIndex0(index);
    }

    private native static NetworkInterface getByIndex0(int index)
            throws SocketException;

    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException{
        if(addr==null){
            throw new NullPointerException();
        }
        if(!(addr instanceof Inet4Address||addr instanceof Inet6Address)){
            throw new IllegalArgumentException("invalid address type");
        }
        return getByInetAddress0(addr);
    }

    private native static NetworkInterface getByInetAddress0(InetAddress addr)
            throws SocketException;

    public static Enumeration<NetworkInterface> getNetworkInterfaces()
            throws SocketException{
        final NetworkInterface[] netifs=getAll();
        // specified to return null if no network interfaces
        if(netifs==null)
            return null;
        return new Enumeration<NetworkInterface>(){
            private int i=0;

            public boolean hasMoreElements(){
                return (netifs!=null&&i<netifs.length);
            }            public NetworkInterface nextElement(){
                if(netifs!=null&&i<netifs.length){
                    NetworkInterface netif=netifs[i++];
                    return netif;
                }else{
                    throw new NoSuchElementException();
                }
            }


        };
    }

    private native static NetworkInterface[] getAll()
            throws SocketException;

    private static native void init();

    static NetworkInterface getDefault(){
        return defaultInterface;
    }

    public String getName(){
        return name;
    }

    public java.util.List<InterfaceAddress> getInterfaceAddresses(){
        java.util.List<InterfaceAddress> lst=new java.util.ArrayList<InterfaceAddress>(1);
        SecurityManager sec=System.getSecurityManager();
        for(int j=0;j<bindings.length;j++){
            try{
                if(sec!=null){
                    sec.checkConnect(bindings[j].getAddress().getHostAddress(),-1);
                }
                lst.add(bindings[j]);
            }catch(SecurityException e){
            }
        }
        return lst;
    }

    public Enumeration<NetworkInterface> getSubInterfaces(){
        class subIFs implements Enumeration<NetworkInterface>{
            private int i=0;

            subIFs(){
            }

            public NetworkInterface nextElement(){
                if(i<childs.length){
                    return childs[i++];
                }else{
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements(){
                return (i<childs.length);
            }
        }
        return new subIFs();
    }

    public NetworkInterface getParent(){
        return parent;
    }

    public int getIndex(){
        return index;
    }

    public String getDisplayName(){
        /** strict TCK conformance */
        return "".equals(displayName)?null:displayName;
    }

    public boolean isUp() throws SocketException{
        return isUp0(name,index);
    }

    private native static boolean isUp0(String name,int ind) throws SocketException;

    public boolean isLoopback() throws SocketException{
        return isLoopback0(name,index);
    }

    private native static boolean isLoopback0(String name,int ind) throws SocketException;

    public boolean isPointToPoint() throws SocketException{
        return isP2P0(name,index);
    }

    private native static boolean isP2P0(String name,int ind) throws SocketException;

    public boolean supportsMulticast() throws SocketException{
        return supportsMulticast0(name,index);
    }

    private native static boolean supportsMulticast0(String name,int ind) throws SocketException;

    public byte[] getHardwareAddress() throws SocketException{
        SecurityManager sec=System.getSecurityManager();
        if(sec!=null){
            try{
                sec.checkPermission(new NetPermission("getNetworkInformation"));
            }catch(SecurityException e){
                if(!getInetAddresses().hasMoreElements()){
                    // don't have connect permission to any local address
                    return null;
                }
            }
        }
        for(InetAddress addr : addrs){
            if(addr instanceof Inet4Address){
                return getMacAddr0(((Inet4Address)addr).getAddress(),name,index);
            }
        }
        return getMacAddr0(null,name,index);
    }

    public Enumeration<InetAddress> getInetAddresses(){
        class checkedAddresses implements Enumeration<InetAddress>{
            private int i=0, count=0;
            private InetAddress local_addrs[];

            checkedAddresses(){
                local_addrs=new InetAddress[addrs.length];
                boolean trusted=true;
                SecurityManager sec=System.getSecurityManager();
                if(sec!=null){
                    try{
                        sec.checkPermission(new NetPermission("getNetworkInformation"));
                    }catch(SecurityException e){
                        trusted=false;
                    }
                }
                for(int j=0;j<addrs.length;j++){
                    try{
                        if(sec!=null&&!trusted){
                            sec.checkConnect(addrs[j].getHostAddress(),-1);
                        }
                        local_addrs[count++]=addrs[j];
                    }catch(SecurityException e){
                    }
                }
            }

            public boolean hasMoreElements(){
                return (i<count);
            }

            public InetAddress nextElement(){
                if(i<count){
                    return local_addrs[i++];
                }else{
                    throw new NoSuchElementException();
                }
            }
        }
        return new checkedAddresses();
    }

    private native static byte[] getMacAddr0(byte[] inAddr,String name,int ind) throws SocketException;

    public int getMTU() throws SocketException{
        return getMTU0(name,index);
    }

    private native static int getMTU0(String name,int ind) throws SocketException;

    public boolean isVirtual(){
        return virtual;
    }

    public int hashCode(){
        return name==null?0:name.hashCode();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof NetworkInterface)){
            return false;
        }
        NetworkInterface that=(NetworkInterface)obj;
        if(this.name!=null){
            if(!this.name.equals(that.name)){
                return false;
            }
        }else{
            if(that.name!=null){
                return false;
            }
        }
        if(this.addrs==null){
            return that.addrs==null;
        }else if(that.addrs==null){
            return false;
        }
        /** Both addrs not null. Compare number of addresses */
        if(this.addrs.length!=that.addrs.length){
            return false;
        }
        InetAddress[] thatAddrs=that.addrs;
        int count=thatAddrs.length;
        for(int i=0;i<count;i++){
            boolean found=false;
            for(int j=0;j<count;j++){
                if(addrs[i].equals(thatAddrs[j])){
                    found=true;
                    break;
                }
            }
            if(!found){
                return false;
            }
        }
        return true;
    }

    public String toString(){
        String result="name:";
        result+=name==null?"null":name;
        if(displayName!=null){
            result+=" ("+displayName+")";
        }
        return result;
    }
}
