/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported
public final class UserPrincipal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=892106070870210969L;
    private final String name;

    public UserPrincipal(String name){
        if(name==null){
            throw new NullPointerException("null name is illegal");
        }
        this.name=name;
    }

    public int hashCode(){
        return name.hashCode();
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(object instanceof UserPrincipal){
            return name.equals(((UserPrincipal)object).getName());
        }
        return false;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        return name;
    }
}
