/**
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.io.Serializable;
import java.util.Vector;

@Deprecated
public abstract class Identity implements Principal, Serializable{
    private static final long serialVersionUID=3609922007826600659L;
    String info="No further information available.";
    IdentityScope scope;
    Vector<Certificate> certificates;
    private String name;
    private PublicKey publicKey;

    protected Identity(){
        this("restoring...");
    }

    public Identity(String name){
        this.name=name;
    }

    public Identity(String name,IdentityScope scope) throws
            KeyManagementException{
        this(name);
        if(scope!=null){
            scope.addIdentity(this);
        }
        this.scope=scope;
    }

    public final String getName(){
        return name;
    }

    public final IdentityScope getScope(){
        return scope;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(PublicKey key) throws KeyManagementException{
        check("setIdentityPublicKey");
        this.publicKey=key;
        certificates=new Vector<Certificate>();
    }

    private static void check(String directive){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkSecurityAccess(directive);
        }
    }

    public String getInfo(){
        return info;
    }

    public void setInfo(String info){
        check("setIdentityInfo");
        this.info=info;
    }

    public void addCertificate(Certificate certificate)
            throws KeyManagementException{
        check("addIdentityCertificate");
        if(certificates==null){
            certificates=new Vector<Certificate>();
        }
        if(publicKey!=null){
            if(!keyEquals(publicKey,certificate.getPublicKey())){
                throw new KeyManagementException(
                        "public key different from cert public key");
            }
        }else{
            publicKey=certificate.getPublicKey();
        }
        certificates.addElement(certificate);
    }

    private boolean keyEquals(PublicKey aKey,PublicKey anotherKey){
        String aKeyFormat=aKey.getFormat();
        String anotherKeyFormat=anotherKey.getFormat();
        if((aKeyFormat==null)^(anotherKeyFormat==null))
            return false;
        if(aKeyFormat!=null&&anotherKeyFormat!=null)
            if(!aKeyFormat.equalsIgnoreCase(anotherKeyFormat))
                return false;
        return java.util.Arrays.equals(aKey.getEncoded(),
                anotherKey.getEncoded());
    }

    public void removeCertificate(Certificate certificate)
            throws KeyManagementException{
        check("removeIdentityCertificate");
        if(certificates!=null){
            certificates.removeElement(certificate);
        }
    }

    public Certificate[] certificates(){
        if(certificates==null){
            return new Certificate[0];
        }
        int len=certificates.size();
        Certificate[] certs=new Certificate[len];
        certificates.copyInto(certs);
        return certs;
    }

    public String toString(boolean detailed){
        String out=toString();
        if(detailed){
            out+="\n";
            out+=printKeys();
            out+="\n"+printCertificates();
            if(info!=null){
                out+="\n\t"+info;
            }else{
                out+="\n\tno additional information available.";
            }
        }
        return out;
    }

    String printKeys(){
        String key="";
        if(publicKey!=null){
            key="\tpublic key initialized";
        }else{
            key="\tno public key";
        }
        return key;
    }

    String printCertificates(){
        String out="";
        if(certificates==null){
            return "\tno certificates";
        }else{
            out+="\tcertificates: \n";
            int i=1;
            for(Certificate cert : certificates){
                out+="\tcertificate "+i+++
                        "\tfor  : "+cert.getPrincipal()+"\n";
                out+="\t\t\tfrom : "+
                        cert.getGuarantor()+"\n";
            }
        }
        return out;
    }

    public int hashCode(){
        return name.hashCode();
    }

    public final boolean equals(Object identity){
        if(identity==this){
            return true;
        }
        if(identity instanceof Identity){
            Identity i=(Identity)identity;
            if(this.fullName().equals(i.fullName())){
                return true;
            }else{
                return identityEquals(i);
            }
        }
        return false;
    }

    protected boolean identityEquals(Identity identity){
        if(!name.equalsIgnoreCase(identity.name))
            return false;
        if((publicKey==null)^(identity.publicKey==null))
            return false;
        if(publicKey!=null&&identity.publicKey!=null)
            if(!publicKey.equals(identity.publicKey))
                return false;
        return true;
    }

    String fullName(){
        String parsable=name;
        if(scope!=null){
            parsable+="."+scope.getName();
        }
        return parsable;
    }

    public String toString(){
        check("printIdentity");
        String printable=name;
        if(scope!=null){
            printable+="["+scope.getName()+"]";
        }
        return printable;
    }
}
