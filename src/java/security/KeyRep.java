/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import javax.crypto.spec.SecretKeySpec;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

public class KeyRep implements Serializable{
    private static final long serialVersionUID=-4757683898830641853L;
    private static final String PKCS8="PKCS#8";
    private static final String X509="X.509";
    private static final String RAW="RAW";
    private Type type;
    private String algorithm;
    private String format;
    private byte[] encoded;

    public KeyRep(Type type,String algorithm,
                  String format,byte[] encoded){
        if(type==null||algorithm==null||
                format==null||encoded==null){
            throw new NullPointerException("invalid null input(s)");
        }
        this.type=type;
        this.algorithm=algorithm;
        this.format=format.toUpperCase(Locale.ENGLISH);
        this.encoded=encoded.clone();
    }

    protected Object readResolve() throws ObjectStreamException{
        try{
            if(type==Type.SECRET&&RAW.equals(format)){
                return new SecretKeySpec(encoded,algorithm);
            }else if(type==Type.PUBLIC&&X509.equals(format)){
                KeyFactory f=KeyFactory.getInstance(algorithm);
                return f.generatePublic(new X509EncodedKeySpec(encoded));
            }else if(type==Type.PRIVATE&&PKCS8.equals(format)){
                KeyFactory f=KeyFactory.getInstance(algorithm);
                return f.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }else{
                throw new NotSerializableException
                        ("unrecognized type/format combination: "+
                                type+"/"+format);
            }
        }catch(NotSerializableException nse){
            throw nse;
        }catch(Exception e){
            NotSerializableException nse=new NotSerializableException
                    ("java.security.Key: "+
                            "["+type+"] "+
                            "["+algorithm+"] "+
                            "["+format+"]");
            nse.initCause(e);
            throw nse;
        }
    }

    public static enum Type{
        SECRET,
        PUBLIC,
        PRIVATE,
    }
}
