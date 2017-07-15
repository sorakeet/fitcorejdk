/**
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class UnknownUserException extends UserException{
    public Any except;

    public UnknownUserException(){
        super();
    }

    public UnknownUserException(Any a){
        super();
        except=a;
    }
}
