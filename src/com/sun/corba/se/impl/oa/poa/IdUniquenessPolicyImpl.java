/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;

final class IdUniquenessPolicyImpl
        extends LocalObject implements IdUniquenessPolicy{
    private IdUniquenessPolicyValue value;

    public IdUniquenessPolicyImpl(IdUniquenessPolicyValue value){
        this.value=value;
    }

    public IdUniquenessPolicyValue value(){
        return value;
    }

    public int policy_type(){
        return ID_UNIQUENESS_POLICY_ID.value;
    }

    public Policy copy(){
        return new IdUniquenessPolicyImpl(value);
    }

    public void destroy(){
        value=null;
    }

    public String toString(){
        return "IdUniquenessPolicy["+
                ((value.value()==IdUniquenessPolicyValue._UNIQUE_ID)?
                        "UNIQUE_ID":"MULTIPLE_ID"+"]");
    }
}
