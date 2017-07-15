/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Owner;
import java.util.Vector;

class OwnerImpl implements Owner, Serializable{
    private static final long serialVersionUID=-576066072046319874L;
    private Vector<Principal> ownerList=null;

    public OwnerImpl(){
        ownerList=new Vector<Principal>();
    }

    public OwnerImpl(PrincipalImpl owner){
        ownerList=new Vector<Principal>();
        ownerList.addElement(owner);
    }

    public boolean addOwner(Principal caller,Principal owner)
            throws NotOwnerException{
        if(!ownerList.contains(caller))
            throw new NotOwnerException();
        if(ownerList.contains(owner)){
            return false;
        }else{
            ownerList.addElement(owner);
            return true;
        }
    }

    public boolean deleteOwner(Principal caller,Principal owner)
            throws NotOwnerException, LastOwnerException{
        if(!ownerList.contains(caller))
            throw new NotOwnerException();
        if(!ownerList.contains(owner)){
            return false;
        }else{
            if(ownerList.size()==1)
                throw new LastOwnerException();
            ownerList.removeElement(owner);
            return true;
        }
    }

    public boolean isOwner(Principal owner){
        return ownerList.contains(owner);
    }
}
