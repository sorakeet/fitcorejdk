/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.Permission;
import java.util.Enumeration;
import java.util.Vector;

class AclEntryImpl implements AclEntry, Serializable{
    private static final long serialVersionUID=-5047185131260073216L;
    private Principal princ=null;
    private boolean neg=false;
    private Vector<Permission> permList=null;
    private Vector<String> commList=null;

    private AclEntryImpl(AclEntryImpl i) throws UnknownHostException{
        setPrincipal(i.getPrincipal());
        permList=new Vector<Permission>();
        commList=new Vector<String>();
        for(Enumeration<String> en=i.communities();en.hasMoreElements();){
            addCommunity(en.nextElement());
        }
        for(Enumeration<Permission> en=i.permissions();en.hasMoreElements();){
            addPermission(en.nextElement());
        }
        if(i.isNegative()) setNegativePermissions();
    }

    public boolean setPrincipal(Principal p){
        if(princ!=null)
            return false;
        princ=p;
        return true;
    }

    public Principal getPrincipal(){
        return princ;
    }

    public void setNegativePermissions(){
        neg=true;
    }

    public boolean isNegative(){
        return neg;
    }

    public boolean addPermission(Permission perm){
        if(permList.contains(perm)) return false;
        permList.addElement(perm);
        return true;
    }

    public boolean removePermission(Permission perm){
        if(!permList.contains(perm)) return false;
        permList.removeElement(perm);
        return true;
    }

    public boolean checkPermission(Permission perm){
        return (permList.contains(perm));
    }

    public Enumeration<Permission> permissions(){
        return permList.elements();
    }

    public boolean addCommunity(String comm){
        if(commList.contains(comm)) return false;
        commList.addElement(comm);
        return true;
    }

    public AclEntryImpl(){
        princ=null;
        permList=new Vector<Permission>();
        commList=new Vector<String>();
    }

    public AclEntryImpl(Principal p) throws UnknownHostException{
        princ=p;
        permList=new Vector<Permission>();
        commList=new Vector<String>();
    }

    public Object clone(){
        AclEntryImpl i;
        try{
            i=new AclEntryImpl(this);
        }catch(UnknownHostException e){
            i=null;
        }
        return (Object)i;
    }

    public String toString(){
        return "AclEntry:"+princ.toString();
    }

    public Enumeration<String> communities(){
        return commList.elements();
    }

    public boolean removeCommunity(String comm){
        if(!commList.contains(comm)) return false;
        commList.removeElement(comm);
        return true;
    }

    public boolean checkCommunity(String comm){
        return (commList.contains(comm));
    }
}
