/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;

public class RequestProcessingPolicyImpl
        extends LocalObject implements RequestProcessingPolicy{
    private RequestProcessingPolicyValue value;

    public RequestProcessingPolicyImpl(RequestProcessingPolicyValue
                                               value){
        this.value=value;
    }

    public RequestProcessingPolicyValue value(){
        return value;
    }

    public int policy_type(){
        return REQUEST_PROCESSING_POLICY_ID.value;
    }

    public Policy copy(){
        return new RequestProcessingPolicyImpl(value);
    }

    public void destroy(){
        value=null;
    }

    public String toString(){
        String type=null;
        switch(value.value()){
            case RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY:
                type="USE_ACTIVE_OBJECT_MAP_ONLY";
                break;
            case RequestProcessingPolicyValue._USE_DEFAULT_SERVANT:
                type="USE_DEFAULT_SERVANT";
                break;
            case RequestProcessingPolicyValue._USE_SERVANT_MANAGER:
                type="USE_SERVANT_MANAGER";
                break;
        }
        return "RequestProcessingPolicy["+type+"]";
    }
}
