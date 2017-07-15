/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.logging.IORSystemException;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.ior.ObjectId;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class WireObjectKeyTemplate implements ObjectKeyTemplate{
    private ORB orb;
    private IORSystemException wrapper;

    public WireObjectKeyTemplate(ORB orb){
        initORB(orb);
    }

    private void initORB(ORB orb){
        this.orb=orb;
        wrapper=IORSystemException.get(orb,
                CORBALogDomains.OA_IOR);
    }

    public WireObjectKeyTemplate(InputStream is,OctetSeqHolder osh){
        osh.value=getId(is);
        initORB((ORB)(is.orb()));
    }

    private byte[] getId(InputStream is){
        CDRInputStream cis=(CDRInputStream)is;
        int len=cis.getBufferLength();
        byte[] result=new byte[len];
        cis.read_octet_array(result,0,len);
        return result;
    }

    public int hashCode(){
        return 53; // All WireObjectKeyTemplates are the same, so they should
        // have the same hashCode.
    }

    public boolean equals(Object obj){
        if(obj==null)
            return false;
        return obj instanceof WireObjectKeyTemplate;
    }

    public void write(OutputStream os){
        // Does nothing
    }

    public ORBVersion getORBVersion(){
        return ORBVersionFactory.getFOREIGN();
    }

    public int getSubcontractId(){
        return ORBConstants.DEFAULT_SCID;
    }

    public int getServerId(){
        return -1;
    }

    public String getORBId(){
        throw wrapper.orbIdNotAvailable();
    }

    public ObjectAdapterId getObjectAdapterId(){
        throw wrapper.objectAdapterIdNotAvailable();
    }

    public byte[] getAdapterId(){
        throw wrapper.adapterIdNotAvailable();
    }

    public void write(ObjectId id,OutputStream os){
        byte[] key=id.getId();
        os.write_octet_array(key,0,key.length);
    }

    public CorbaServerRequestDispatcher getServerRequestDispatcher(ORB orb,ObjectId id){
        byte[] bid=id.getId();
        String str=new String(bid);
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(str);
    }
}
