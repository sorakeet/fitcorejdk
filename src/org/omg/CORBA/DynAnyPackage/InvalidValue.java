/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.DynAnyPackage;

public final class InvalidValue
        extends org.omg.CORBA.UserException{
    public InvalidValue(){
        super();
    }

    public InvalidValue(String reason){
        super(reason);
    }
}
