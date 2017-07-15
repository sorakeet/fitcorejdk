/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

class PrincipalImpl implements java.security.Principal, Serializable{
    private static final long serialVersionUID=-7910027842878976761L;
    private InetAddress[] add=null;

    public PrincipalImpl() throws UnknownHostException{
        add=new InetAddress[1];
        add[0]=InetAddress.getLocalHost();
    }

    public PrincipalImpl(String hostName) throws UnknownHostException{
        if((hostName.equals("localhost"))||(hostName.equals("127.0.0.1"))){
            add=new InetAddress[1];
            add[0]=InetAddress.getByName(hostName);
        }else
            add=InetAddress.getAllByName(hostName);
    }

    public PrincipalImpl(InetAddress address){
        add=new InetAddress[1];
        add[0]=address;
    }

    public String getName(){
        return add[0].toString();
    }

    public int hashCode(){
        return add[0].hashCode();
    }

    public boolean equals(Object a){
        if(a instanceof PrincipalImpl){
            for(int i=0;i<add.length;i++){
                if(add[i].equals(((PrincipalImpl)a).getAddress()))
                    return true;
            }
            return false;
        }else{
            return false;
        }
    }

    public String toString(){
        return ("PrincipalImpl :"+add[0].toString());
    }

    public InetAddress getAddress(){
        return add[0];
    }

    public InetAddress[] getAddresses(){
        return add;
    }
}
