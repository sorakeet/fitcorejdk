/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

public final class LoggingPermission extends java.security.BasicPermission{
    private static final long serialVersionUID=63564341580231582L;

    public LoggingPermission(String name,String actions) throws IllegalArgumentException{
        super(name);
        if(!name.equals("control")){
            throw new IllegalArgumentException("name: "+name);
        }
        if(actions!=null&&actions.length()>0){
            throw new IllegalArgumentException("actions: "+actions);
        }
    }
}
