/**
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

import java.io.IOException;
import java.io.InputStream;

public class Oid{
    private ObjectIdentifier oid;
    private byte[] derEncoding;

    public Oid(String strOid) throws GSSException{
        try{
            oid=new ObjectIdentifier(strOid);
            derEncoding=null;
        }catch(Exception e){
            throw new GSSException(GSSException.FAILURE,
                    "Improperly formatted Object Identifier String - "
                            +strOid);
        }
    }

    public Oid(InputStream derOid) throws GSSException{
        try{
            DerValue derVal=new DerValue(derOid);
            derEncoding=derVal.toByteArray();
            oid=derVal.getOID();
        }catch(IOException e){
            throw new GSSException(GSSException.FAILURE,
                    "Improperly formatted ASN.1 DER encoding for Oid");
        }
    }

    public Oid(byte[] data) throws GSSException{
        try{
            DerValue derVal=new DerValue(data);
            derEncoding=derVal.toByteArray();
            oid=derVal.getOID();
        }catch(IOException e){
            throw new GSSException(GSSException.FAILURE,
                    "Improperly formatted ASN.1 DER encoding for Oid");
        }
    }

    static Oid getInstance(String strOid){
        Oid retVal=null;
        try{
            retVal=new Oid(strOid);
        }catch(GSSException e){
            // squelch it!
        }
        return retVal;
    }

    public byte[] getDER() throws GSSException{
        if(derEncoding==null){
            DerOutputStream dout=new DerOutputStream();
            try{
                dout.putOID(oid);
            }catch(IOException e){
                throw new GSSException(GSSException.FAILURE,e.getMessage());
            }
            derEncoding=dout.toByteArray();
        }
        return derEncoding.clone();
    }

    public boolean containedIn(Oid[] oids){
        for(int i=0;i<oids.length;i++){
            if(oids[i].equals(this))
                return (true);
        }
        return (false);
    }

    public int hashCode(){
        return oid.hashCode();
    }

    public boolean equals(Object other){
        //check if both reference the same object
        if(this==other)
            return (true);
        if(other instanceof Oid)
            return this.oid.equals((Object)((Oid)other).oid);
        else if(other instanceof ObjectIdentifier)
            return this.oid.equals(other);
        else
            return false;
    }

    public String toString(){
        return oid.toString();
    }
}
