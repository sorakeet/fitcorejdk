/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior.iiop;

import com.sun.corba.se.impl.encoding.EncapsInputStream;
import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.ior.EncapsulationUtility;
import com.sun.corba.se.impl.logging.IORSystemException;
import com.sun.corba.se.impl.util.JDKBridge;
import com.sun.corba.se.spi.ior.*;
import com.sun.corba.se.spi.ior.iiop.*;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_JAVA_CODEBASE;
import sun.corba.EncapsInputStreamFactory;

import java.util.Iterator;

public class IIOPProfileImpl extends IdentifiableBase implements IIOPProfile{
    // Cached lookups
    protected String codebase=null;
    protected boolean cachedCodebase=false;
    private ORB orb;
    private IORSystemException wrapper;
    private ObjectId oid;
    private IIOPProfileTemplate proftemp;
    private ObjectKeyTemplate oktemp;
    private boolean checkedIsLocal=false;
    private boolean cachedIsLocal=false;
    private GIOPVersion giopVersion=null;

    public IIOPProfileImpl(ORB orb,ObjectKeyTemplate oktemp,ObjectId oid,
                           IIOPProfileTemplate proftemp){
        this(orb);
        this.oktemp=oktemp;
        this.oid=oid;
        this.proftemp=proftemp;
    }

    private IIOPProfileImpl(ORB orb){
        this.orb=orb;
        wrapper=IORSystemException.get(orb,
                CORBALogDomains.OA_IOR);
    }

    public IIOPProfileImpl(InputStream is){
        this((ORB)(is.orb()));
        init(is);
    }

    private void init(InputStream istr){
        // First, read all of the IIOP IOR data
        GIOPVersion version=new GIOPVersion();
        version.read(istr);
        IIOPAddress primary=new IIOPAddressImpl(istr);
        byte[] key=EncapsulationUtility.readOctets(istr);
        ObjectKey okey=orb.getObjectKeyFactory().create(key);
        oktemp=okey.getTemplate();
        oid=okey.getId();
        proftemp=IIOPFactories.makeIIOPProfileTemplate(orb,
                version,primary);
        // Handle any tagged components (if applicable)
        if(version.getMinor()>0)
            EncapsulationUtility.readIdentifiableSequence(proftemp,
                    orb.getTaggedComponentFactoryFinder(),istr);
        // If there is no codebase in this IOR and there IS a
        // java.rmi.server.codebase property set, we need to
        // update the IOR with the local codebase.  Note that
        // there is only one instance of the local codebase, but it
        // can be safely shared in multiple IORs since it is immutable.
        if(uncachedGetCodeBase()==null){
            JavaCodebaseComponent jcc=LocalCodeBaseSingletonHolder.comp;
            if(jcc!=null){
                if(version.getMinor()>0)
                    proftemp.add(jcc);
                codebase=jcc.getURLs();
            }
            // Whether codebase is null or not, we have it,
            // and so getCodebase ned never call uncachedGetCodebase.
            cachedCodebase=true;
        }
    }

    private String uncachedGetCodeBase(){
        Iterator iter=proftemp.iteratorById(TAG_JAVA_CODEBASE.value);
        if(iter.hasNext()){
            JavaCodebaseComponent jcbc=(JavaCodebaseComponent)(iter.next());
            return jcbc.getURLs();
        }
        return null;
    }

    public IIOPProfileImpl(ORB orb,org.omg.IOP.TaggedProfile profile){
        this(orb);
        if(profile==null||profile.tag!=TAG_INTERNET_IOP.value||
                profile.profile_data==null){
            throw wrapper.invalidTaggedProfile();
        }
        EncapsInputStream istr=EncapsInputStreamFactory.newEncapsInputStream((ORB)orb,profile.profile_data,
                profile.profile_data.length);
        istr.consumeEndian();
        init(istr);
    }

    public int hashCode(){
        return oid.hashCode()^proftemp.hashCode()^oktemp.hashCode();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof IIOPProfileImpl))
            return false;
        IIOPProfileImpl other=(IIOPProfileImpl)obj;
        return oid.equals(other.oid)&&proftemp.equals(other.proftemp)&&
                oktemp.equals(other.oktemp);
    }

    public TaggedProfileTemplate getTaggedProfileTemplate(){
        return proftemp;
    }

    public ObjectId getObjectId(){
        return oid;
    }

    public ObjectKeyTemplate getObjectKeyTemplate(){
        return oktemp;
    }

    public ObjectKey getObjectKey(){
        ObjectKey result=IORFactories.makeObjectKey(oktemp,oid);
        return result;
    }

    public boolean isEquivalent(TaggedProfile prof){
        if(!(prof instanceof IIOPProfile))
            return false;
        IIOPProfile other=(IIOPProfile)prof;
        return oid.equals(other.getObjectId())&&
                proftemp.isEquivalent(other.getTaggedProfileTemplate())&&
                oktemp.equals(other.getObjectKeyTemplate());
    }

    public org.omg.IOP.TaggedProfile getIOPProfile(){
        EncapsOutputStream os=
                sun.corba.OutputStreamFactory.newEncapsOutputStream(orb);
        os.write_long(getId());
        write(os);
        InputStream is=(InputStream)(os.create_input_stream());
        return org.omg.IOP.TaggedProfileHelper.read(is);
    }

    public int getId(){
        return proftemp.getId();
    }

    public synchronized boolean isLocal(){
        if(!checkedIsLocal){
            checkedIsLocal=true;
            String host=proftemp.getPrimaryAddress().getHost();
            cachedIsLocal=orb.isLocalHost(host)&&
                    orb.isLocalServerId(oktemp.getSubcontractId(),
                            oktemp.getServerId())&&
                    orb.getLegacyServerSocketManager()
                            .legacyIsLocalServerPort(
                                    proftemp.getPrimaryAddress().getPort());
        }
        return cachedIsLocal;
    }

    public void writeContents(OutputStream os){
        proftemp.write(oktemp,oid,os);
    }

    public ORBVersion getORBVersion(){
        return oktemp.getORBVersion();
    }

    public Object getServant(){
        if(!isLocal())
            return null;
        RequestDispatcherRegistry scr=orb.getRequestDispatcherRegistry();
        ObjectAdapterFactory oaf=scr.getObjectAdapterFactory(
                oktemp.getSubcontractId());
        ObjectAdapterId oaid=oktemp.getObjectAdapterId();
        ObjectAdapter oa=null;
        try{
            oa=oaf.find(oaid);
        }catch(SystemException exc){
            // Could not find the OA, so just return null.
            // This usually happens when POAs are being deleted,
            // and the POA always return null for getLocalServant anyway.
            wrapper.getLocalServantFailure(exc,oaid.toString());
            return null;
        }
        byte[] boid=oid.getId();
        Object servant=oa.getLocalServant(boid);
        return servant;
    }

    public synchronized GIOPVersion getGIOPVersion(){
        return proftemp.getGIOPVersion();
    }

    public synchronized String getCodebase(){
        if(!cachedCodebase){
            cachedCodebase=true;
            codebase=uncachedGetCodeBase();
        }
        return codebase;
    }

    public void makeImmutable(){
        proftemp.makeImmutable();
    }

    // initialize-on-demand holder
    private static class LocalCodeBaseSingletonHolder{
        public static JavaCodebaseComponent comp;

        static{
            String localCodebase=JDKBridge.getLocalCodebase();
            if(localCodebase==null)
                comp=null;
            else
                comp=IIOPFactories.makeJavaCodebaseComponent(
                        localCodebase);
        }
    }
}
