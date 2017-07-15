/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

public class ObjectAdapterIdNumber extends ObjectAdapterIdArray{
    private int poaid;

    public ObjectAdapterIdNumber(int poaid){
        super("OldRootPOA",Integer.toString(poaid));
        this.poaid=poaid;
    }

    public int getOldPOAId(){
        return poaid;
    }
}
