/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.impl.protocol.giopmsgheaders.KeyAddr;

public class AddressingDispositionException extends RuntimeException{
    private short expectedAddrDisp=KeyAddr.value;

    public AddressingDispositionException(short expectedAddrDisp){
        this.expectedAddrDisp=expectedAddrDisp;
    }

    public short expectedAddrDisp(){
        return this.expectedAddrDisp;
    }
}
