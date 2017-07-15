/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.security.x509.X509CRLEntryImpl;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.util.Date;

public abstract class X509CRLEntry implements X509Extension{
    public int hashCode(){
        int retval=0;
        try{
            byte[] entryData=this.getEncoded();
            for(int i=1;i<entryData.length;i++)
                retval+=entryData[i]*i;
        }catch(CRLException ce){
            return (retval);
        }
        return (retval);
    }

    public boolean equals(Object other){
        if(this==other)
            return true;
        if(!(other instanceof X509CRLEntry))
            return false;
        try{
            byte[] thisCRLEntry=this.getEncoded();
            byte[] otherCRLEntry=((X509CRLEntry)other).getEncoded();
            if(thisCRLEntry.length!=otherCRLEntry.length)
                return false;
            for(int i=0;i<thisCRLEntry.length;i++)
                if(thisCRLEntry[i]!=otherCRLEntry[i])
                    return false;
        }catch(CRLException ce){
            return false;
        }
        return true;
    }

    public abstract String toString();

    public abstract byte[] getEncoded() throws CRLException;

    public abstract BigInteger getSerialNumber();

    public X500Principal getCertificateIssuer(){
        return null;
    }

    public abstract Date getRevocationDate();

    public CRLReason getRevocationReason(){
        if(!hasExtensions()){
            return null;
        }
        return X509CRLEntryImpl.getRevocationReason(this);
    }

    public abstract boolean hasExtensions();
}
