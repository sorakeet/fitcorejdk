/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.impl.logging.IORSystemException;
import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.ior.ObjectId;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.util.Iterator;

public abstract class ObjectKeyTemplateBase implements ObjectKeyTemplate{
    // Fixed constants for Java IDL object key template forms
    public static final String JIDL_ORB_ID="";
    private static final String[] JIDL_OAID_STRINGS={"TransientObjectAdapter"};
    public static final ObjectAdapterId JIDL_OAID=new ObjectAdapterIdArray(JIDL_OAID_STRINGS);
    protected IORSystemException wrapper;
    private ORB orb;
    private ORBVersion version;
    private int magic;
    private int scid;
    private int serverid;
    private String orbid;
    private ObjectAdapterId oaid;
    private byte[] adapterId;

    public ObjectKeyTemplateBase(ORB orb,int magic,int scid,int serverid,
                                 String orbid,ObjectAdapterId oaid){
        this.orb=orb;
        this.wrapper=IORSystemException.get(orb,
                CORBALogDomains.OA_IOR);
        this.magic=magic;
        this.scid=scid;
        this.serverid=serverid;
        this.orbid=orbid;
        this.oaid=oaid;
        adapterId=computeAdapterId();
    }

    private byte[] computeAdapterId(){
        // write out serverid, orbid, oaid
        ByteBuffer buff=new ByteBuffer();
        buff.append(getServerId());
        buff.append(orbid);
        buff.append(oaid.getNumLevels());
        Iterator iter=oaid.iterator();
        while(iter.hasNext()){
            String comp=(String)(iter.next());
            buff.append(comp);
        }
        buff.trimToSize();
        return buff.toArray();
    }

    public int hashCode(){
        int result=17;
        result=37*result+magic;
        result=37*result+scid;
        result=37*result+serverid;
        result=37*result+version.hashCode();
        result=37*result+orbid.hashCode();
        result=37*result+oaid.hashCode();
        return result;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof ObjectKeyTemplateBase))
            return false;
        ObjectKeyTemplateBase other=(ObjectKeyTemplateBase)obj;
        return (magic==other.magic)&&(scid==other.scid)&&
                (serverid==other.serverid)&&(version.equals(other.version)&&
                orbid.equals(other.orbid)&&oaid.equals(other.oaid));
    }

    public void write(OutputStream os){
        writeTemplate(os);
    }

    protected int getMagic(){
        return magic;
    }

    public ORBVersion getORBVersion(){
        return version;
    }

    public int getSubcontractId(){
        return scid;
    }

    public int getServerId(){
        return serverid;
    }

    public String getORBId(){
        return orbid;
    }

    public ObjectAdapterId getObjectAdapterId(){
        return oaid;
    }

    public byte[] getAdapterId(){
        return (byte[])(adapterId.clone());
    }

    public void write(ObjectId objectId,OutputStream os){
        writeTemplate(os);
        objectId.write(os);
    }

    abstract protected void writeTemplate(OutputStream os);

    public CorbaServerRequestDispatcher getServerRequestDispatcher(ORB orb,ObjectId id){
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(scid);
    }

    // All subclasses should set the version in their constructors.
    // Public so it can be used in a white-box test.
    public void setORBVersion(ORBVersion version){
        this.version=version;
    }

    protected byte[] readObjectKey(InputStream is){
        int len=is.read_long();
        byte[] result=new byte[len];
        is.read_octet_array(result,0,len);
        return result;
    }
}
