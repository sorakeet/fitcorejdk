/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class PolicyError extends UserException{
    public short reason;

    public PolicyError(){
        super();
    }

    public PolicyError(short __reason){
        super();
        reason=__reason;
    }

    public PolicyError(String reason_string,short __reason){
        super(reason_string);
        reason=__reason;
    }
}
