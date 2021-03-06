/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol.giopmsgheaders;

public final class TargetAddress implements org.omg.CORBA.portable.IDLEntity{
    private byte[] ___object_key;
    private org.omg.IOP.TaggedProfile ___profile;
    private IORAddressingInfo ___ior;
    private short __discriminator;
    private boolean __uninitialized=true;

    public TargetAddress(){
    }

    public short discriminator(){
        if(__uninitialized)
            throw new org.omg.CORBA.BAD_OPERATION();
        return __discriminator;
    }

    public byte[] object_key(){
        if(__uninitialized)
            throw new org.omg.CORBA.BAD_OPERATION();
        verifyobject_key(__discriminator);
        return ___object_key;
    }

    private void verifyobject_key(short discriminator){
        if(discriminator!=KeyAddr.value)
            throw new org.omg.CORBA.BAD_OPERATION();
    }

    public void object_key(byte[] value){
        __discriminator=KeyAddr.value;
        ___object_key=value;
        __uninitialized=false;
    }

    public org.omg.IOP.TaggedProfile profile(){
        if(__uninitialized)
            throw new org.omg.CORBA.BAD_OPERATION();
        verifyprofile(__discriminator);
        return ___profile;
    }

    private void verifyprofile(short discriminator){
        if(discriminator!=ProfileAddr.value)
            throw new org.omg.CORBA.BAD_OPERATION();
    }

    public void profile(org.omg.IOP.TaggedProfile value){
        __discriminator=ProfileAddr.value;
        ___profile=value;
        __uninitialized=false;
    }

    public IORAddressingInfo ior(){
        if(__uninitialized)
            throw new org.omg.CORBA.BAD_OPERATION();
        verifyior(__discriminator);
        return ___ior;
    }

    private void verifyior(short discriminator){
        if(discriminator!=ReferenceAddr.value)
            throw new org.omg.CORBA.BAD_OPERATION();
    }

    public void ior(IORAddressingInfo value){
        __discriminator=ReferenceAddr.value;
        ___ior=value;
        __uninitialized=false;
    }

    public void _default(){
        __discriminator=-32768;
        __uninitialized=false;
    }
} // class TargetAddress
