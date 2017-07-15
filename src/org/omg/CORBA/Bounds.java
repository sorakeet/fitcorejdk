/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class Bounds extends UserException{
    public Bounds(){
        super();
    }

    public Bounds(String reason){
        super(reason);
    }
}
