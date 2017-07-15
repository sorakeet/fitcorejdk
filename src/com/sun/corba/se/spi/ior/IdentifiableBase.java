/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import com.sun.corba.se.impl.ior.EncapsulationUtility;
import org.omg.CORBA_2_3.portable.OutputStream;

public abstract class IdentifiableBase implements Identifiable,
        WriteContents{
    final public void write(OutputStream os){
        EncapsulationUtility.writeEncapsulation((WriteContents)this,os);
    }
}
