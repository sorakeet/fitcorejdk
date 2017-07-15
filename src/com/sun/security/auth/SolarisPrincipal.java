/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported(false)
@Deprecated
public class SolarisPrincipal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=-7840670002439379038L;
    private static final java.util.ResourceBundle rb=
            java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedAction<java.util.ResourceBundle>(){
                        public java.util.ResourceBundle run(){
                            return (java.util.ResourceBundle.getBundle
                                    ("sun.security.util.AuthResources"));
                        }
                    });
    private String name;

    public SolarisPrincipal(String name){
        if(name==null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        this.name=name;
    }

    public int hashCode(){
        return name.hashCode();
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof SolarisPrincipal))
            return false;
        SolarisPrincipal that=(SolarisPrincipal)o;
        if(this.getName().equals(that.getName()))
            return true;
        return false;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        return (rb.getString("SolarisPrincipal.")+name);
    }
}
