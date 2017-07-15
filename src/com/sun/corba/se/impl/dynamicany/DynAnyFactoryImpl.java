/**
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.dynamicany;

import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.TypeCode;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

public class DynAnyFactoryImpl
        extends LocalObject
        implements DynAnyFactory{
    //
    // Instance variables
    //
    private ORB orb;
    //
    // Constructors
    //
    // Needed for org.omg.CORBA.Object
    private String[] __ids={"IDL:omg.org/DynamicAny/DynAnyFactory:1.0"};

    private DynAnyFactoryImpl(){
        this.orb=null;
    }
    //
    // DynAnyFactory interface methods
    //

    public DynAnyFactoryImpl(ORB orb){
        this.orb=orb;
    }

    // Returns the most derived DynAny type based on the Anys TypeCode.
    public DynAny create_dyn_any(Any any)
            throws InconsistentTypeCode{
        return DynAnyUtil.createMostDerivedDynAny(any,orb,true);
    }

    // Returns the most derived DynAny type based on the TypeCode.
    public DynAny create_dyn_any_from_type_code(TypeCode type)
            throws InconsistentTypeCode{
        return DynAnyUtil.createMostDerivedDynAny(type,orb);
    }

    public String[] _ids(){
        return (String[])__ids.clone();
    }
}
