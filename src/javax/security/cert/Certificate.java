/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.cert;

import java.security.*;

public abstract class Certificate{
    public int hashCode(){
        int retval=0;
        try{
            byte[] certData=this.getEncoded();
            for(int i=1;i<certData.length;i++){
                retval+=certData[i]*i;
            }
            return (retval);
        }catch(CertificateException e){
            return (retval);
        }
    }

    public boolean equals(Object other){
        if(this==other)
            return true;
        if(!(other instanceof Certificate))
            return false;
        try{
            byte[] thisCert=this.getEncoded();
            byte[] otherCert=((Certificate)other).getEncoded();
            if(thisCert.length!=otherCert.length)
                return false;
            for(int i=0;i<thisCert.length;i++)
                if(thisCert[i]!=otherCert[i])
                    return false;
            return true;
        }catch(CertificateException e){
            return false;
        }
    }

    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract String toString();

    public abstract void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException;

    public abstract void verify(PublicKey key,String sigProvider)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException;

    public abstract PublicKey getPublicKey();
}
