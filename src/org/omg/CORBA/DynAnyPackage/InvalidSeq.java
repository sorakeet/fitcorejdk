/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.DynAnyPackage;

public final class InvalidSeq
        extends org.omg.CORBA.UserException{
    public InvalidSeq(){
        super();
    }

    public InvalidSeq(String reason){
        super(reason);
    }
}
