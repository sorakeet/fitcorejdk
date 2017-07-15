/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// Be very careful: com.sun.corba imports must not depend on
// PEORB internal classes in ways that prevent portability to
// other vendor's ORBs.

public class StubIORImpl{
    // cached hash code
    private int hashCode;
    // IOR components
    private byte[] typeData;
    private int[] profileTags;
    private byte[][] profileData;

    public StubIORImpl(){
        hashCode=0;
        typeData=null;
        profileTags=null;
        profileData=null;
    }

    public StubIORImpl(org.omg.CORBA.Object obj){
        // write the IOR to an OutputStream and get an InputStream
        OutputStream ostr=StubAdapter.getORB(obj).create_output_stream();
        ostr.write_Object(obj);
        InputStream istr=ostr.create_input_stream();
        // read the IOR components back from the stream
        int typeLength=istr.read_long();
        typeData=new byte[typeLength];
        istr.read_octet_array(typeData,0,typeLength);
        int numProfiles=istr.read_long();
        profileTags=new int[numProfiles];
        profileData=new byte[numProfiles][];
        for(int i=0;i<numProfiles;i++){
            profileTags[i]=istr.read_long();
            profileData[i]=new byte[istr.read_long()];
            istr.read_octet_array(profileData[i],0,profileData[i].length);
        }
    }

    public String getRepositoryId(){
        if(typeData==null)
            return null;
        return new String(typeData);
    }

    public Delegate getDelegate(ORB orb){
        // write the IOR components to an org.omg.CORBA.portable.OutputStream
        OutputStream ostr=orb.create_output_stream();
        ostr.write_long(typeData.length);
        ostr.write_octet_array(typeData,0,typeData.length);
        ostr.write_long(profileTags.length);
        for(int i=0;i<profileTags.length;i++){
            ostr.write_long(profileTags[i]);
            ostr.write_long(profileData[i].length);
            ostr.write_octet_array(profileData[i],0,profileData[i].length);
        }
        InputStream istr=ostr.create_input_stream();
        // read the IOR back from the stream
        org.omg.CORBA.Object obj=(org.omg.CORBA.Object)istr.read_Object();
        return StubAdapter.getDelegate(obj);
    }

    public void doRead(ObjectInputStream stream)
            throws IOException, ClassNotFoundException{
        // read the IOR from the ObjectInputStream
        int typeLength=stream.readInt();
        typeData=new byte[typeLength];
        stream.readFully(typeData);
        int numProfiles=stream.readInt();
        profileTags=new int[numProfiles];
        profileData=new byte[numProfiles][];
        for(int i=0;i<numProfiles;i++){
            profileTags[i]=stream.readInt();
            profileData[i]=new byte[stream.readInt()];
            stream.readFully(profileData[i]);
        }
    }

    public void doWrite(ObjectOutputStream stream)
            throws IOException{
        // write the IOR to the ObjectOutputStream
        stream.writeInt(typeData.length);
        stream.write(typeData);
        stream.writeInt(profileTags.length);
        for(int i=0;i<profileTags.length;i++){
            stream.writeInt(profileTags[i]);
            stream.writeInt(profileData[i].length);
            stream.write(profileData[i]);
        }
    }

    public synchronized int hashCode(){
        if(hashCode==0){
            // compute the hash code
            for(int i=0;i<typeData.length;i++){
                hashCode=hashCode*37+typeData[i];
            }
            for(int i=0;i<profileTags.length;i++){
                hashCode=hashCode*37+profileTags[i];
                for(int j=0;j<profileData[i].length;j++){
                    hashCode=hashCode*37+profileData[i][j];
                }
            }
        }
        return hashCode;
    }

    private boolean equalArrays(int[] data1,int[] data2){
        if(data1.length!=data2.length)
            return false;
        for(int ctr=0;ctr<data1.length;ctr++){
            if(data1[ctr]!=data2[ctr])
                return false;
        }
        return true;
    }

    private boolean equalArrays(byte[] data1,byte[] data2){
        if(data1.length!=data2.length)
            return false;
        for(int ctr=0;ctr<data1.length;ctr++){
            if(data1[ctr]!=data2[ctr])
                return false;
        }
        return true;
    }

    private boolean equalArrays(byte[][] data1,byte[][] data2){
        if(data1.length!=data2.length)
            return false;
        for(int ctr=0;ctr<data1.length;ctr++){
            if(!equalArrays(data1[ctr],data2[ctr]))
                return false;
        }
        return true;
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(!(obj instanceof StubIORImpl)){
            return false;
        }
        StubIORImpl other=(StubIORImpl)obj;
        if(other.hashCode()!=this.hashCode()){
            return false;
        }
        return equalArrays(typeData,other.typeData)&&
                equalArrays(profileTags,other.profileTags)&&
                equalArrays(profileData,other.profileData);
    }

    private void appendByteArray(StringBuffer result,byte[] data){
        for(int ctr=0;ctr<data.length;ctr++){
            result.append(Integer.toHexString(data[ctr]));
        }
    }

    public String toString(){
        StringBuffer result=new StringBuffer();
        result.append("SimpleIORImpl[");
        String repositoryId=new String(typeData);
        result.append(repositoryId);
        for(int ctr=0;ctr<profileTags.length;ctr++){
            result.append(",(");
            result.append(profileTags[ctr]);
            result.append(")");
            appendByteArray(result,profileData[ctr]);
        }
        result.append("]");
        return result.toString();
    }
}