/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.x500;

import sun.security.util.DerValue;
import sun.security.util.ResourcesMgr;
import sun.security.x509.X500Name;

import java.io.*;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

public final class X500Principal implements Principal, Serializable{
    public static final String RFC1779="RFC1779";
    public static final String RFC2253="RFC2253";
    public static final String CANONICAL="CANONICAL";
    private static final long serialVersionUID=-500463348111345721L;
    private transient X500Name thisX500Name;

    X500Principal(X500Name x500Name){
        thisX500Name=x500Name;
    }

    public X500Principal(String name){
        this(name,Collections.<String,String>emptyMap());
    }

    public X500Principal(String name,Map<String,String> keywordMap){
        if(name==null){
            throw new NullPointerException
                    (ResourcesMgr.getString
                            ("provided.null.name"));
        }
        if(keywordMap==null){
            throw new NullPointerException
                    (ResourcesMgr.getString
                            ("provided.null.keyword.map"));
        }
        try{
            thisX500Name=new X500Name(name,keywordMap);
        }catch(Exception e){
            IllegalArgumentException iae=new IllegalArgumentException
                    ("improperly specified input name: "+name);
            iae.initCause(e);
            throw iae;
        }
    }

    public X500Principal(byte[] name){
        try{
            thisX500Name=new X500Name(name);
        }catch(Exception e){
            IllegalArgumentException iae=new IllegalArgumentException
                    ("improperly specified input name");
            iae.initCause(e);
            throw iae;
        }
    }

    public X500Principal(InputStream is){
        if(is==null){
            throw new NullPointerException("provided null input stream");
        }
        try{
            if(is.markSupported())
                is.mark(is.available()+1);
            DerValue der=new DerValue(is);
            thisX500Name=new X500Name(der.data);
        }catch(Exception e){
            if(is.markSupported()){
                try{
                    is.reset();
                }catch(IOException ioe){
                    IllegalArgumentException iae=new IllegalArgumentException
                            ("improperly specified input stream "+
                                    ("and unable to reset input stream"));
                    iae.initCause(e);
                    throw iae;
                }
            }
            IllegalArgumentException iae=new IllegalArgumentException
                    ("improperly specified input stream");
            iae.initCause(e);
            throw iae;
        }
    }

    public String getName(){
        return getName(X500Principal.RFC2253);
    }

    public String getName(String format){
        if(format!=null){
            if(format.equalsIgnoreCase(RFC1779)){
                return thisX500Name.getRFC1779Name();
            }else if(format.equalsIgnoreCase(RFC2253)){
                return thisX500Name.getRFC2253Name();
            }else if(format.equalsIgnoreCase(CANONICAL)){
                return thisX500Name.getRFC2253CanonicalName();
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    public String getName(String format,Map<String,String> oidMap){
        if(oidMap==null){
            throw new NullPointerException
                    (ResourcesMgr.getString
                            ("provided.null.OID.map"));
        }
        if(format!=null){
            if(format.equalsIgnoreCase(RFC1779)){
                return thisX500Name.getRFC1779Name(oidMap);
            }else if(format.equalsIgnoreCase(RFC2253)){
                return thisX500Name.getRFC2253Name(oidMap);
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    public byte[] getEncoded(){
        try{
            return thisX500Name.getEncoded();
        }catch(IOException e){
            throw new RuntimeException("unable to get encoding",e);
        }
    }

    public int hashCode(){
        return thisX500Name.hashCode();
    }

    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        if(o instanceof X500Principal==false){
            return false;
        }
        X500Principal other=(X500Principal)o;
        return this.thisX500Name.equals(other.thisX500Name);
    }

    public String toString(){
        return thisX500Name.toString();
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.writeObject(thisX500Name.getEncodedInternal());
    }

    private void readObject(ObjectInputStream s)
            throws IOException,
            NotActiveException,
            ClassNotFoundException{
        // re-create thisX500Name
        thisX500Name=new X500Name((byte[])s.readObject());
    }
}
