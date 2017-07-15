/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.TypeCodePackage;

public final class BadKind extends org.omg.CORBA.UserException{
    public BadKind(){
        super();
    }

    public BadKind(String reason){
        super(reason);
    }
}
