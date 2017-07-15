/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Vector;

class GroupImpl extends PrincipalImpl implements Group, Serializable{
    private static final long serialVersionUID=-7777387035032541168L;

    public GroupImpl() throws UnknownHostException{
    }

    public GroupImpl(String mask) throws UnknownHostException{
        super(mask);
    }

    public boolean addMember(Principal p){
        // we don't need to add members because the ip address is a
        // subnet mask
        return true;
    }

    public boolean removeMember(Principal p){
        return true;
    }

    public boolean isMember(Principal p){
        if((p.hashCode()&super.hashCode())==p.hashCode()) return true;
        else return false;
    }

    public Enumeration<? extends Principal> members(){
        Vector<Principal> v=new Vector<Principal>(1);
        v.addElement(this);
        return v.elements();
    }

    public boolean equals(Object p){
        if(p instanceof PrincipalImpl||p instanceof GroupImpl){
            if((super.hashCode()&p.hashCode())==p.hashCode()) return true;
            else return false;
        }else{
            return false;
        }
    }

    public int hashCode(){
        return super.hashCode();
    }

    public String toString(){
        return ("GroupImpl :"+super.getAddress().toString());
    }
}
