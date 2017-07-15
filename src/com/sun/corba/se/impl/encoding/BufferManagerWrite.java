/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;

public abstract class BufferManagerWrite{
    protected ORB orb;
    protected ORBUtilSystemException wrapper;
    // XREVISIT - Currently a java.lang.Object during
    // the rip-int-generic transition.  Should eventually
    // become a GIOPOutputObject.
    protected Object outputObject;
    protected boolean sentFullMessage=false;

    BufferManagerWrite(ORB orb){
        this.orb=orb;
        this.wrapper=ORBUtilSystemException.get(orb,
                CORBALogDomains.RPC_ENCODING);
    }

    public abstract boolean sentFragment();

    public boolean sentFullMessage(){
        return sentFullMessage;
    }

    public abstract int getBufferSize();

    public abstract void overflow(ByteBufferWithInfo bbwi);

    public abstract void sendMessage();

    public void setOutputObject(Object outputObject){
        this.outputObject=outputObject;
    }

    abstract public void close();
}
