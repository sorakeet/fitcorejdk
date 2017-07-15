/**
 * Copyright (c) 2007, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.security.util.ObjectIdentifier;
import sun.security.x509.InvalidityDateExtension;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CertificateRevokedException extends CertificateException{
    private static final long serialVersionUID=7839996631571608627L;
    private final CRLReason reason;
    private final X500Principal authority;
    private Date revocationDate;
    private transient Map<String,Extension> extensions;

    public CertificateRevokedException(Date revocationDate,CRLReason reason,
                                       X500Principal authority,Map<String,Extension> extensions){
        if(revocationDate==null||reason==null||authority==null||
                extensions==null){
            throw new NullPointerException();
        }
        this.revocationDate=new Date(revocationDate.getTime());
        this.reason=reason;
        this.authority=authority;
        // make sure Map only contains correct types
        this.extensions=Collections.checkedMap(new HashMap<>(),
                String.class,Extension.class);
        this.extensions.putAll(extensions);
    }

    public Date getRevocationDate(){
        return (Date)revocationDate.clone();
    }

    public CRLReason getRevocationReason(){
        return reason;
    }

    public X500Principal getAuthorityName(){
        return authority;
    }

    public Date getInvalidityDate(){
        Extension ext=getExtensions().get("2.5.29.24");
        if(ext==null){
            return null;
        }else{
            try{
                Date invalidity=InvalidityDateExtension.toImpl(ext).get("DATE");
                return new Date(invalidity.getTime());
            }catch(IOException ioe){
                return null;
            }
        }
    }

    public Map<String,Extension> getExtensions(){
        return Collections.unmodifiableMap(extensions);
    }

    @Override
    public String getMessage(){
        return "Certificate has been revoked, reason: "
                +reason+", revocation date: "+revocationDate
                +", authority: "+authority+", extension OIDs: "
                +extensions.keySet();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException{
        // Write out the non-transient fields
        // (revocationDate, reason, authority)
        oos.defaultWriteObject();
        // Write out the size (number of mappings) of the extensions map
        oos.writeInt(extensions.size());
        // For each extension in the map, the following are emitted (in order):
        // the OID String (Object), the criticality flag (boolean), the length
        // of the encoded extension value byte array (int), and the encoded
        // extension value byte array. The extensions themselves are emitted
        // in no particular order.
        for(Map.Entry<String,Extension> entry : extensions.entrySet()){
            Extension ext=entry.getValue();
            oos.writeObject(ext.getId());
            oos.writeBoolean(ext.isCritical());
            byte[] extVal=ext.getValue();
            oos.writeInt(extVal.length);
            oos.write(extVal);
        }
    }

    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException{
        // Read in the non-transient fields
        // (revocationDate, reason, authority)
        ois.defaultReadObject();
        // Defensively copy the revocation date
        revocationDate=new Date(revocationDate.getTime());
        // Read in the size (number of mappings) of the extensions map
        // and create the extensions map
        int size=ois.readInt();
        if(size==0){
            extensions=Collections.emptyMap();
        }else{
            extensions=new HashMap<String,Extension>(size);
        }
        // Read in the extensions and put the mappings in the extensions map
        for(int i=0;i<size;i++){
            String oid=(String)ois.readObject();
            boolean critical=ois.readBoolean();
            int length=ois.readInt();
            byte[] extVal=new byte[length];
            ois.readFully(extVal);
            Extension ext=sun.security.x509.Extension.newExtension
                    (new ObjectIdentifier(oid),critical,extVal);
            extensions.put(oid,ext);
        }
    }
}
