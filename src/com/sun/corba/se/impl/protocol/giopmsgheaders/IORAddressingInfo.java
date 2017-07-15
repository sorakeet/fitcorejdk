/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol.giopmsgheaders;

public final class IORAddressingInfo implements org.omg.CORBA.portable.IDLEntity{
    public int selected_profile_index=(int)0;
    public org.omg.IOP.IOR ior=null;

    public IORAddressingInfo(){
    } // ctor

    public IORAddressingInfo(int _selected_profile_index,org.omg.IOP.IOR _ior){
        selected_profile_index=_selected_profile_index;
        ior=_ior;
    } // ctor
} // class IORAddressingInfo
