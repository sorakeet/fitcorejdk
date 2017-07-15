/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public final class ManagementPermission extends java.security.BasicPermission{
    private static final long serialVersionUID=1897496590799378737L;

    public ManagementPermission(String name){
        super(name);
        if(!name.equals("control")&&!name.equals("monitor")){
            throw new IllegalArgumentException("name: "+name);
        }
    }

    public ManagementPermission(String name,String actions)
            throws IllegalArgumentException{
        super(name);
        if(!name.equals("control")&&!name.equals("monitor")){
            throw new IllegalArgumentException("name: "+name);
        }
        if(actions!=null&&actions.length()>0){
            throw new IllegalArgumentException("actions: "+actions);
        }
    }
}
