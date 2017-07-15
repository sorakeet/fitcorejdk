/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.security.Principal;

@jdk.Exported
public final class LdapPrincipal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=6820120005580754861L;
    private final String nameString;
    private final LdapName name;

    public LdapPrincipal(String name) throws InvalidNameException{
        if(name==null){
            throw new NullPointerException("null name is illegal");
        }
        this.name=getLdapName(name);
        nameString=name;
    }

    // Create an LdapName object from a string distinguished name.
    private LdapName getLdapName(String name) throws InvalidNameException{
        return new LdapName(name);
    }

    public int hashCode(){
        return name.hashCode();
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(object instanceof LdapPrincipal){
            try{
                return
                        name.equals(getLdapName(((LdapPrincipal)object).getName()));
            }catch(InvalidNameException e){
                return false;
            }
        }
        return false;
    }

    public String getName(){
        return nameString;
    }

    public String toString(){
        return name.toString();
    }
}
