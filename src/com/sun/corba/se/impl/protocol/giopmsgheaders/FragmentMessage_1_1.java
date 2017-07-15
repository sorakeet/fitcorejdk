/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol.giopmsgheaders;

public final class FragmentMessage_1_1 extends Message_1_1
        implements FragmentMessage{
    // Constructors

    FragmentMessage_1_1(){
    }

    FragmentMessage_1_1(Message_1_1 msg11){
        this.magic=msg11.magic;
        this.GIOP_version=msg11.GIOP_version;
        this.flags=msg11.flags;
        this.message_type=GIOPFragment;
        this.message_size=0;
    }
    // Accessor methods

    public int getRequestId(){
        return -1; // 1.1 has no fragment header and so no request_id
    }

    public int getHeaderLength(){
        return GIOPMessageHeaderLength;
    }
    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream){
        super.read(istream);
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream){
        super.write(ostream);
    }

    public void callback(MessageHandler handler)
            throws java.io.IOException{
        handler.handleInput(this);
    }
} // class FragmentMessage_1_1
