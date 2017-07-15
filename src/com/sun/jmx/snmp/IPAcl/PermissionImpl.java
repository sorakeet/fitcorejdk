/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.IPAcl;

import java.io.Serializable;

class PermissionImpl implements java.security.acl.Permission, Serializable{
    private static final long serialVersionUID=4478110422746916589L;
    private String perm=null;

    public PermissionImpl(String s){
        perm=s;
    }

    public int hashCode(){
        return super.hashCode();
    }

    public boolean equals(Object p){
        if(p instanceof PermissionImpl){
            return perm.equals(((PermissionImpl)p).getString());
        }else{
            return false;
        }
    }

    public String toString(){
        return perm;
    }

    public String getString(){
        return perm;
    }
}
