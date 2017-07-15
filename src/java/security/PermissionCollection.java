/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public abstract class PermissionCollection implements java.io.Serializable{
    private static final long serialVersionUID=-6727011328946861783L;
    // when set, add will throw an exception.
    private volatile boolean readOnly;

    public abstract void add(Permission permission);

    public abstract boolean implies(Permission permission);

    public void setReadOnly(){
        readOnly=true;
    }

    public boolean isReadOnly(){
        return readOnly;
    }

    public String toString(){
        Enumeration<Permission> enum_=elements();
        StringBuilder sb=new StringBuilder();
        sb.append(super.toString()+" (\n");
        while(enum_.hasMoreElements()){
            try{
                sb.append(" ");
                sb.append(enum_.nextElement().toString());
                sb.append("\n");
            }catch(NoSuchElementException e){
                // ignore
            }
        }
        sb.append(")\n");
        return sb.toString();
    }

    public abstract Enumeration<Permission> elements();
}
