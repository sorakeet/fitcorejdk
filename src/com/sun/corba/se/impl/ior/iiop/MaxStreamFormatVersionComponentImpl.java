/**
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 */
package com.sun.corba.se.impl.ior.iiop;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.ior.TaggedComponentBase;
import com.sun.corba.se.spi.ior.iiop.MaxStreamFormatVersionComponent;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT;

// Java to IDL ptc 02-01-12 1.4.11
// TAG_RMI_CUSTOM_MAX_STREAM_FORMAT
public class MaxStreamFormatVersionComponentImpl extends TaggedComponentBase
        implements MaxStreamFormatVersionComponent{
    public static final MaxStreamFormatVersionComponentImpl singleton
            =new MaxStreamFormatVersionComponentImpl();
    private byte version;

    public MaxStreamFormatVersionComponentImpl(){
        version=ORBUtility.getMaxStreamFormatVersion();
    }

    public MaxStreamFormatVersionComponentImpl(byte streamFormatVersion){
        version=streamFormatVersion;
    }

    public int hashCode(){
        return version;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof MaxStreamFormatVersionComponentImpl))
            return false;
        MaxStreamFormatVersionComponentImpl other=
                (MaxStreamFormatVersionComponentImpl)obj;
        return version==other.version;
    }

    public String toString(){
        return "MaxStreamFormatVersionComponentImpl[version="+version+"]";
    }

    public byte getMaxStreamFormatVersion(){
        return version;
    }

    public void writeContents(OutputStream os){
        os.write_octet(version);
    }

    public int getId(){
        return TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value;
    }
}
