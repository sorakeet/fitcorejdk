/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package org.omg.CORBA_2_3.portable;

import java.io.SerializablePermission;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class OutputStream extends org.omg.CORBA.portable.OutputStream{
    private static final String ALLOW_SUBCLASS_PROP="jdk.corba.allowOutputStreamSubclass";
    private static final boolean allowSubclass=AccessController.doPrivileged(
            new PrivilegedAction<Boolean>(){
                @Override
                public Boolean run(){
                    String prop=System.getProperty(ALLOW_SUBCLASS_PROP);
                    return prop==null?false:
                            (prop.equalsIgnoreCase("false")?false:true);
                }
            });

    public OutputStream(){
        this(checkPermission());
    }

    private static Void checkPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            if(!allowSubclass)
                sm.checkPermission(new
                        SerializablePermission("enableSubclassImplementation"));
        }
        return null;
    }

    private OutputStream(Void ignore){
    }

    public void write_value(java.io.Serializable value){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value,Class clz){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value,String repository_id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value,org.omg.CORBA.portable.BoxedValueHelper factory){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_abstract_interface(Object obj){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
