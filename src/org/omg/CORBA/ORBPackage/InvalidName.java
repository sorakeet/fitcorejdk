/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.ORBPackage;

final public class InvalidName extends org.omg.CORBA.UserException{
    public InvalidName(){
        super();
    }

    public InvalidName(String reason){
        super(reason);
    }
}
