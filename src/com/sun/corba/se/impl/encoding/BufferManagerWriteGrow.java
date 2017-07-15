/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.spi.orb.ORB;

public class BufferManagerWriteGrow extends BufferManagerWrite{
    BufferManagerWriteGrow(ORB orb){
        super(orb);
    }

    public boolean sentFragment(){
        return false;
    }

    public int getBufferSize(){
        return orb.getORBData().getGIOPBufferSize();
    }

    public void overflow(ByteBufferWithInfo bbwi){
        // The code that once lived directly in CDROutputStream.grow()
        // has been moved ByteBufferWithInfo.growBuffer().
        // Grow ByteBufferWithInfo to a larger size.
        bbwi.growBuffer(orb);
        // Must be false for the grow case
        bbwi.fragmented=false;
    }

    public void sendMessage(){
        Connection conn=
                ((OutputObject)outputObject).getMessageMediator().getConnection();
        conn.writeLock();
        try{
            conn.sendWithoutLock((OutputObject)outputObject);
            sentFullMessage=true;
        }finally{
            conn.writeUnlock();
        }
    }

    public void close(){
    }
}
