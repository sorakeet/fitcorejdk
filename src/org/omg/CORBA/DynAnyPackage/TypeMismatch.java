/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.DynAnyPackage;

public final class TypeMismatch
        extends org.omg.CORBA.UserException{
    public TypeMismatch(){
        super();
    }

    public TypeMismatch(String reason){
        super(reason);
    }
}
