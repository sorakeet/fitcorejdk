/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

public class InterfaceAddress{
    private InetAddress address=null;
    private Inet4Address broadcast=null;
    private short maskLength=0;

    InterfaceAddress(){
    }

    public InetAddress getAddress(){
        return address;
    }

    public InetAddress getBroadcast(){
        return broadcast;
    }

    public short getNetworkPrefixLength(){
        return maskLength;
    }

    public int hashCode(){
        return address.hashCode()+((broadcast!=null)?broadcast.hashCode():0)+maskLength;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof InterfaceAddress)){
            return false;
        }
        InterfaceAddress cmp=(InterfaceAddress)obj;
        if(!(address==null?cmp.address==null:address.equals(cmp.address)))
            return false;
        if(!(broadcast==null?cmp.broadcast==null:broadcast.equals(cmp.broadcast)))
            return false;
        if(maskLength!=cmp.maskLength)
            return false;
        return true;
    }

    public String toString(){
        return address+"/"+maskLength+" ["+broadcast+"]";
    }
}
