/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.extension;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

public class ZeroPortPolicy extends LocalObject implements Policy{
    private static ZeroPortPolicy policy=new ZeroPortPolicy(true);
    private boolean flag=true;

    private ZeroPortPolicy(boolean type){
        this.flag=type;
    }

    public synchronized static ZeroPortPolicy getPolicy(){
        return policy;
    }

    public String toString(){
        return "ZeroPortPolicy["+flag+"]";
    }

    public boolean forceZeroPort(){
        return flag;
    }

    public int policy_type(){
        return ORBConstants.ZERO_PORT_POLICY;
    }

    public Policy copy(){
        return this;
    }

    public void destroy(){
        // NO-OP
    }
}
