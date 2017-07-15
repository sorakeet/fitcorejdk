/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.DynAnyPackage;

public final class Invalid
        extends org.omg.CORBA.UserException{
    public Invalid(){
        super();
    }

    public Invalid(String reason){
        super(reason);
    }
}
