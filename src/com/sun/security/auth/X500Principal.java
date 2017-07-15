/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import sun.security.x509.X500Name;

import java.security.Principal;

@jdk.Exported(false)
@Deprecated
public class X500Principal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=-8222422609431628648L;
    private static final java.util.ResourceBundle rb=
            java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedAction<java.util.ResourceBundle>(){
                        public java.util.ResourceBundle run(){
                            return (java.util.ResourceBundle.getBundle
                                    ("sun.security.util.AuthResources"));
                        }
                    });
    private String name;
    transient private X500Name thisX500Name;

    public X500Principal(String name){
        if(name==null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        try{
            thisX500Name=new X500Name(name);
        }catch(Exception e){
            throw new IllegalArgumentException(e.toString());
        }
        this.name=name;
    }

    public String getName(){
        return thisX500Name.getName();
    }

    public int hashCode(){
        return thisX500Name.hashCode();
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(o instanceof X500Principal){
            X500Principal that=(X500Principal)o;
            try{
                X500Name thatX500Name=new X500Name(that.getName());
                return thisX500Name.equals(thatX500Name);
            }catch(Exception e){
                // any parsing exceptions, return false
                return false;
            }
        }else if(o instanceof Principal){
            // this will return 'true' if 'o' is a sun.security.x509.X500Name
            // and the X500Names are equal
            return o.equals(thisX500Name);
        }
        return false;
    }

    public String toString(){
        return thisX500Name.toString();
    }

    private void readObject(java.io.ObjectInputStream s) throws
            java.io.IOException,
            java.io.NotActiveException,
            ClassNotFoundException{
        s.defaultReadObject();
        // re-create thisX500Name
        thisX500Name=new X500Name(name);
    }
}
