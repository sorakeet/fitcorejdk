/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;

final class IdAssignmentPolicyImpl
        extends LocalObject
        implements IdAssignmentPolicy{
    private IdAssignmentPolicyValue value;

    public IdAssignmentPolicyImpl(IdAssignmentPolicyValue value){
        this.value=value;
    }

    public IdAssignmentPolicyValue value(){
        return value;
    }

    public int policy_type(){
        return ID_ASSIGNMENT_POLICY_ID.value;
    }

    public Policy copy(){
        return new IdAssignmentPolicyImpl(value);
    }

    public void destroy(){
        value=null;
    }

    public String toString(){
        return "IdAssignmentPolicy["+
                ((value.value()==IdAssignmentPolicyValue._USER_ID)?
                        "USER_ID":"SYSTEM_ID"+"]");
    }
}
