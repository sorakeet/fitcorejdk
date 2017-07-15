/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior.iiop;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.spi.ior.TaggedComponentBase;
import org.omg.CORBA_2_3.portable.OutputStream;

public class JavaSerializationComponent extends TaggedComponentBase{
    private static JavaSerializationComponent singleton;
    private byte version;

    public JavaSerializationComponent(byte version){
        this.version=version;
    }

    public static JavaSerializationComponent singleton(){
        if(singleton==null){
            synchronized(JavaSerializationComponent.class){
                singleton=
                        new JavaSerializationComponent(Message.JAVA_ENC_VERSION);
            }
        }
        return singleton;
    }

    public byte javaSerializationVersion(){
        return this.version;
    }

    public void writeContents(OutputStream os){
        os.write_octet(version);
    }

    public int getId(){
        return ORBConstants.TAG_JAVA_SERIALIZATION_ID;
    }

    public int hashCode(){
        return this.version;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof JavaSerializationComponent)){
            return false;
        }
        JavaSerializationComponent other=(JavaSerializationComponent)obj;
        return this.version==other.version;
    }
}
