/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.servicecontext;

import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public abstract class ServiceContext{
    protected InputStream in=null;

    protected ServiceContext(){
    }

    protected ServiceContext(InputStream s,GIOPVersion gv) throws SystemException{
        in=s;
    }

    private void dprint(String msg){
        ORBUtility.dprint(this,msg);
    }

    public void write(OutputStream s,GIOPVersion gv) throws SystemException{
        EncapsOutputStream os=
                sun.corba.OutputStreamFactory.newEncapsOutputStream((ORB)(s.orb()),gv);
        os.putEndian();
        writeData(os);
        byte[] data=os.toByteArray();
        s.write_long(getId());
        s.write_long(data.length);
        s.write_octet_array(data,0,data.length);
    }

    public abstract int getId();

    protected abstract void writeData(OutputStream os);

    public String toString(){
        return "ServiceContext[ id="+getId()+" ]";
    }
}
