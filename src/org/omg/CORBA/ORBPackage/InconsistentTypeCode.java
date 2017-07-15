/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.ORBPackage;

public final class InconsistentTypeCode
        extends org.omg.CORBA.UserException{
    public InconsistentTypeCode(){
        super();
    }

    public InconsistentTypeCode(String reason){
        super(reason);
    }
}
