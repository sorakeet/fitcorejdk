/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class UserException extends Exception implements org.omg.CORBA.portable.IDLEntity{
    protected UserException(){
        super();
    }

    protected UserException(String reason){
        super(reason);
    }
}
