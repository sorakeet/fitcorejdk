/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.*;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;

public class X509CertSelector implements CertSelector{
    static final int NAME_ANY=0;
    static final int NAME_RFC822=1;
    static final int NAME_DNS=2;
    static final int NAME_X400=3;
    static final int NAME_DIRECTORY=4;
    static final int NAME_EDI=5;
    static final int NAME_URI=6;
    static final int NAME_IP=7;
    static final int NAME_OID=8;
    private static final Debug debug=Debug.getInstance("certpath");
    private final static ObjectIdentifier ANY_EXTENDED_KEY_USAGE=
            ObjectIdentifier.newInternal(new int[]{2,5,29,37,0});
    private static final Boolean FALSE=Boolean.FALSE;
    private static final int PRIVATE_KEY_USAGE_ID=0;
    private static final int SUBJECT_ALT_NAME_ID=1;
    private static final int NAME_CONSTRAINTS_ID=2;
    private static final int CERT_POLICIES_ID=3;
    private static final int EXTENDED_KEY_USAGE_ID=4;
    private static final int NUM_OF_EXTENSIONS=5;
    private static final String[] EXTENSION_OIDS=new String[NUM_OF_EXTENSIONS];

    static{
        CertPathHelperImpl.initialize();
    }

    static{
        EXTENSION_OIDS[PRIVATE_KEY_USAGE_ID]="2.5.29.16";
        EXTENSION_OIDS[SUBJECT_ALT_NAME_ID]="2.5.29.17";
        EXTENSION_OIDS[NAME_CONSTRAINTS_ID]="2.5.29.30";
        EXTENSION_OIDS[CERT_POLICIES_ID]="2.5.29.32";
        EXTENSION_OIDS[EXTENDED_KEY_USAGE_ID]="2.5.29.37";
    }

    private BigInteger serialNumber;
    private X500Principal issuer;
    private X500Principal subject;
    private byte[] subjectKeyID;
    private byte[] authorityKeyID;
    private Date certificateValid;
    private Date privateKeyValid;
    private ObjectIdentifier subjectPublicKeyAlgID;
    private PublicKey subjectPublicKey;
    private byte[] subjectPublicKeyBytes;
    private boolean[] keyUsage;
    private Set<String> keyPurposeSet;
    private Set<ObjectIdentifier> keyPurposeOIDSet;
    private Set<List<?>> subjectAlternativeNames;
    private Set<GeneralNameInterface> subjectAlternativeGeneralNames;
    ;
    private CertificatePolicySet policy;
    private Set<String> policySet;
    private Set<List<?>> pathToNames;
    private Set<GeneralNameInterface> pathToGeneralNames;
    private NameConstraintsExtension nc;
    private byte[] ncBytes;
    private int basicConstraints=-1;
    private X509Certificate x509Cert;
    private boolean matchAllSubjectAltNames=true;

    public X509CertSelector(){
        // empty
    }

    static boolean equalNames(Collection<?> object1,Collection<?> object2){
        if((object1==null)||(object2==null)){
            return object1==object2;
        }
        return object1.equals(object2);
    }

    public void setIssuer(X500Principal issuer){
        this.issuer=issuer;
    }

    public void setIssuer(String issuerDN) throws IOException{
        if(issuerDN==null){
            issuer=null;
        }else{
            issuer=new X500Name(issuerDN).asX500Principal();
        }
    }

    public void setSubject(X500Principal subject){
        this.subject=subject;
    }

    public void setSubject(String subjectDN) throws IOException{
        if(subjectDN==null){
            subject=null;
        }else{
            subject=new X500Name(subjectDN).asX500Principal();
        }
    }

    public void setSubjectPublicKey(PublicKey key){
        if(key==null){
            subjectPublicKey=null;
            subjectPublicKeyBytes=null;
        }else{
            subjectPublicKey=key;
            subjectPublicKeyBytes=key.getEncoded();
        }
    }

    public void addSubjectAlternativeName(int type,String name)
            throws IOException{
        addSubjectAlternativeNameInternal(type,name);
    }

    private void addSubjectAlternativeNameInternal(int type,Object name)
            throws IOException{
        // First, ensure that the name parses
        GeneralNameInterface tempName=makeGeneralNameInterface(type,name);
        if(subjectAlternativeNames==null){
            subjectAlternativeNames=new HashSet<List<?>>();
        }
        if(subjectAlternativeGeneralNames==null){
            subjectAlternativeGeneralNames=new HashSet<GeneralNameInterface>();
        }
        List<Object> list=new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        subjectAlternativeNames.add(list);
        subjectAlternativeGeneralNames.add(tempName);
    }

    static GeneralNameInterface makeGeneralNameInterface(int type,Object name)
            throws IOException{
        GeneralNameInterface result;
        if(debug!=null){
            debug.println("X509CertSelector.makeGeneralNameInterface("
                    +type+")...");
        }
        if(name instanceof String){
            if(debug!=null){
                debug.println("X509CertSelector.makeGeneralNameInterface() "
                        +"name is String: "+name);
            }
            switch(type){
                case NAME_RFC822:
                    result=new RFC822Name((String)name);
                    break;
                case NAME_DNS:
                    result=new DNSName((String)name);
                    break;
                case NAME_DIRECTORY:
                    result=new X500Name((String)name);
                    break;
                case NAME_URI:
                    result=new URIName((String)name);
                    break;
                case NAME_IP:
                    result=new IPAddressName((String)name);
                    break;
                case NAME_OID:
                    result=new OIDName((String)name);
                    break;
                default:
                    throw new IOException("unable to parse String names of type "
                            +type);
            }
            if(debug!=null){
                debug.println("X509CertSelector.makeGeneralNameInterface() "
                        +"result: "+result.toString());
            }
        }else if(name instanceof byte[]){
            DerValue val=new DerValue((byte[])name);
            if(debug!=null){
                debug.println
                        ("X509CertSelector.makeGeneralNameInterface() is byte[]");
            }
            switch(type){
                case NAME_ANY:
                    result=new OtherName(val);
                    break;
                case NAME_RFC822:
                    result=new RFC822Name(val);
                    break;
                case NAME_DNS:
                    result=new DNSName(val);
                    break;
                case NAME_X400:
                    result=new X400Address(val);
                    break;
                case NAME_DIRECTORY:
                    result=new X500Name(val);
                    break;
                case NAME_EDI:
                    result=new EDIPartyName(val);
                    break;
                case NAME_URI:
                    result=new URIName(val);
                    break;
                case NAME_IP:
                    result=new IPAddressName(val);
                    break;
                case NAME_OID:
                    result=new OIDName(val);
                    break;
                default:
                    throw new IOException("unable to parse byte array names of "
                            +"type "+type);
            }
            if(debug!=null){
                debug.println("X509CertSelector.makeGeneralNameInterface() result: "
                        +result.toString());
            }
        }else{
            if(debug!=null){
                debug.println("X509CertSelector.makeGeneralName() input name "
                        +"not String or byte array");
            }
            throw new IOException("name not String or byte array");
        }
        return result;
    }

    public void addSubjectAlternativeName(int type,byte[] name)
            throws IOException{
        // clone because byte arrays are modifiable
        addSubjectAlternativeNameInternal(type,name.clone());
    }

    // called from CertPathHelper
    void setPathToNamesInternal(Set<GeneralNameInterface> names){
        // set names to non-null dummy value
        // this breaks getPathToNames()
        pathToNames=Collections.<List<?>>emptySet();
        pathToGeneralNames=names;
    }

    public void addPathToName(int type,String name) throws IOException{
        addPathToNameInternal(type,name);
    }

    private void addPathToNameInternal(int type,Object name)
            throws IOException{
        // First, ensure that the name parses
        GeneralNameInterface tempName=makeGeneralNameInterface(type,name);
        if(pathToGeneralNames==null){
            pathToNames=new HashSet<List<?>>();
            pathToGeneralNames=new HashSet<GeneralNameInterface>();
        }
        List<Object> list=new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        pathToNames.add(list);
        pathToGeneralNames.add(tempName);
    }

    public void addPathToName(int type,byte[] name) throws IOException{
        // clone because byte arrays are modifiable
        addPathToNameInternal(type,name.clone());
    }

    public X509Certificate getCertificate(){
        return x509Cert;
    }

    public void setCertificate(X509Certificate cert){
        x509Cert=cert;
    }

    public BigInteger getSerialNumber(){
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serial){
        serialNumber=serial;
    }

    public X500Principal getIssuer(){
        return issuer;
    }

    public void setIssuer(byte[] issuerDN) throws IOException{
        try{
            issuer=(issuerDN==null?null:new X500Principal(issuerDN));
        }catch(IllegalArgumentException e){
            throw new IOException("Invalid name",e);
        }
    }

    public byte[] getIssuerAsBytes() throws IOException{
        return (issuer==null?null:issuer.getEncoded());
    }

    public X500Principal getSubject(){
        return subject;
    }

    public void setSubject(byte[] subjectDN) throws IOException{
        try{
            subject=(subjectDN==null?null:new X500Principal(subjectDN));
        }catch(IllegalArgumentException e){
            throw new IOException("Invalid name",e);
        }
    }

    public byte[] getSubjectAsBytes() throws IOException{
        return (subject==null?null:subject.getEncoded());
    }

    public byte[] getSubjectKeyIdentifier(){
        if(subjectKeyID==null){
            return null;
        }
        return subjectKeyID.clone();
    }

    public void setSubjectKeyIdentifier(byte[] subjectKeyID){
        if(subjectKeyID==null){
            this.subjectKeyID=null;
        }else{
            this.subjectKeyID=subjectKeyID.clone();
        }
    }

    public byte[] getAuthorityKeyIdentifier(){
        if(authorityKeyID==null){
            return null;
        }
        return authorityKeyID.clone();
    }

    public void setAuthorityKeyIdentifier(byte[] authorityKeyID){
        if(authorityKeyID==null){
            this.authorityKeyID=null;
        }else{
            this.authorityKeyID=authorityKeyID.clone();
        }
    }

    public Date getCertificateValid(){
        if(certificateValid==null){
            return null;
        }
        return (Date)certificateValid.clone();
    }

    public void setCertificateValid(Date certValid){
        if(certValid==null){
            certificateValid=null;
        }else{
            certificateValid=(Date)certValid.clone();
        }
    }

    public Date getPrivateKeyValid(){
        if(privateKeyValid==null){
            return null;
        }
        return (Date)privateKeyValid.clone();
    }

    public void setPrivateKeyValid(Date privateKeyValid){
        if(privateKeyValid==null){
            this.privateKeyValid=null;
        }else{
            this.privateKeyValid=(Date)privateKeyValid.clone();
        }
    }

    public String getSubjectPublicKeyAlgID(){
        if(subjectPublicKeyAlgID==null){
            return null;
        }
        return subjectPublicKeyAlgID.toString();
    }

    public void setSubjectPublicKeyAlgID(String oid) throws IOException{
        if(oid==null){
            subjectPublicKeyAlgID=null;
        }else{
            subjectPublicKeyAlgID=new ObjectIdentifier(oid);
        }
    }

    public PublicKey getSubjectPublicKey(){
        return subjectPublicKey;
    }

    public void setSubjectPublicKey(byte[] key) throws IOException{
        if(key==null){
            subjectPublicKey=null;
            subjectPublicKeyBytes=null;
        }else{
            subjectPublicKeyBytes=key.clone();
            subjectPublicKey=X509Key.parse(new DerValue(subjectPublicKeyBytes));
        }
    }

    public boolean[] getKeyUsage(){
        if(keyUsage==null){
            return null;
        }
        return keyUsage.clone();
    }

    public void setKeyUsage(boolean[] keyUsage){
        if(keyUsage==null){
            this.keyUsage=null;
        }else{
            this.keyUsage=keyUsage.clone();
        }
    }

    public Set<String> getExtendedKeyUsage(){
        return keyPurposeSet;
    }

    public void setExtendedKeyUsage(Set<String> keyPurposeSet) throws IOException{
        if((keyPurposeSet==null)||keyPurposeSet.isEmpty()){
            this.keyPurposeSet=null;
            keyPurposeOIDSet=null;
        }else{
            this.keyPurposeSet=
                    Collections.unmodifiableSet(new HashSet<String>(keyPurposeSet));
            keyPurposeOIDSet=new HashSet<ObjectIdentifier>();
            for(String s : this.keyPurposeSet){
                keyPurposeOIDSet.add(new ObjectIdentifier(s));
            }
        }
    }

    public boolean getMatchAllSubjectAltNames(){
        return matchAllSubjectAltNames;
    }

    public void setMatchAllSubjectAltNames(boolean matchAllNames){
        this.matchAllSubjectAltNames=matchAllNames;
    }

    public Collection<List<?>> getSubjectAlternativeNames(){
        if(subjectAlternativeNames==null){
            return null;
        }
        return cloneNames(subjectAlternativeNames);
    }

    public void setSubjectAlternativeNames(Collection<List<?>> names)
            throws IOException{
        if(names==null){
            subjectAlternativeNames=null;
            subjectAlternativeGeneralNames=null;
        }else{
            if(names.isEmpty()){
                subjectAlternativeNames=null;
                subjectAlternativeGeneralNames=null;
                return;
            }
            Set<List<?>> tempNames=cloneAndCheckNames(names);
            // Ensure that we either set both of these or neither
            subjectAlternativeGeneralNames=parseNames(tempNames);
            subjectAlternativeNames=tempNames;
        }
    }

    private static Set<GeneralNameInterface> parseNames(Collection<List<?>> names) throws IOException{
        Set<GeneralNameInterface> genNames=new HashSet<GeneralNameInterface>();
        for(List<?> nameList : names){
            if(nameList.size()!=2){
                throw new IOException("name list size not 2");
            }
            Object o=nameList.get(0);
            if(!(o instanceof Integer)){
                throw new IOException("expected an Integer");
            }
            int nameType=((Integer)o).intValue();
            o=nameList.get(1);
            genNames.add(makeGeneralNameInterface(nameType,o));
        }
        return genNames;
    }

    private static Set<List<?>> cloneNames(Collection<List<?>> names){
        try{
            return cloneAndCheckNames(names);
        }catch(IOException e){
            throw new RuntimeException("cloneNames encountered IOException: "+
                    e.getMessage());
        }
    }

    private static Set<List<?>> cloneAndCheckNames(Collection<List<?>> names) throws IOException{
        // Copy the Lists and Collection
        Set<List<?>> namesCopy=new HashSet<List<?>>();
        for(List<?> o : names){
            namesCopy.add(new ArrayList<Object>(o));
        }
        // Check the contents of the Lists and clone any byte arrays
        for(List<?> list : namesCopy){
            @SuppressWarnings("unchecked") // See javadoc for parameter "names".
                    List<Object> nameList=(List<Object>)list;
            if(nameList.size()!=2){
                throw new IOException("name list size not 2");
            }
            Object o=nameList.get(0);
            if(!(o instanceof Integer)){
                throw new IOException("expected an Integer");
            }
            int nameType=((Integer)o).intValue();
            if((nameType<0)||(nameType>8)){
                throw new IOException("name type not 0-8");
            }
            Object nameObject=nameList.get(1);
            if(!(nameObject instanceof byte[])&&
                    !(nameObject instanceof String)){
                if(debug!=null){
                    debug.println("X509CertSelector.cloneAndCheckNames() "
                            +"name not byte array");
                }
                throw new IOException("name not byte array or String");
            }
            if(nameObject instanceof byte[]){
                nameList.set(1,((byte[])nameObject).clone());
            }
        }
        return namesCopy;
    }

    public byte[] getNameConstraints(){
        if(ncBytes==null){
            return null;
        }else{
            return ncBytes.clone();
        }
    }

    public void setNameConstraints(byte[] bytes) throws IOException{
        if(bytes==null){
            ncBytes=null;
            nc=null;
        }else{
            ncBytes=bytes.clone();
            nc=new NameConstraintsExtension(FALSE,bytes);
        }
    }

    public int getBasicConstraints(){
        return basicConstraints;
    }

    public void setBasicConstraints(int minMaxPathLen){
        if(minMaxPathLen<-2){
            throw new IllegalArgumentException("basic constraints less than -2");
        }
        basicConstraints=minMaxPathLen;
    }

    public Set<String> getPolicy(){
        return policySet;
    }

    public void setPolicy(Set<String> certPolicySet) throws IOException{
        if(certPolicySet==null){
            policySet=null;
            policy=null;
        }else{
            // Snapshot set and parse it
            Set<String> tempSet=Collections.unmodifiableSet
                    (new HashSet<String>(certPolicySet));
            /** Convert to Vector of ObjectIdentifiers */
            Iterator<String> i=tempSet.iterator();
            Vector<CertificatePolicyId> polIdVector=new Vector<CertificatePolicyId>();
            while(i.hasNext()){
                Object o=i.next();
                if(!(o instanceof String)){
                    throw new IOException("non String in certPolicySet");
                }
                polIdVector.add(new CertificatePolicyId(new ObjectIdentifier(
                        (String)o)));
            }
            // If everything went OK, make the changes
            policySet=tempSet;
            policy=new CertificatePolicySet(polIdVector);
        }
    }

    public Collection<List<?>> getPathToNames(){
        if(pathToNames==null){
            return null;
        }
        return cloneNames(pathToNames);
    }

    public void setPathToNames(Collection<List<?>> names) throws IOException{
        if((names==null)||names.isEmpty()){
            pathToNames=null;
            pathToGeneralNames=null;
        }else{
            Set<List<?>> tempNames=cloneAndCheckNames(names);
            pathToGeneralNames=parseNames(tempNames);
            // Ensure that we either set both of these or neither
            pathToNames=tempNames;
        }
    }

    public boolean match(Certificate cert){
        if(!(cert instanceof X509Certificate)){
            return false;
        }
        X509Certificate xcert=(X509Certificate)cert;
        if(debug!=null){
            debug.println("X509CertSelector.match(SN: "
                    +(xcert.getSerialNumber()).toString(16)+"\n  Issuer: "
                    +xcert.getIssuerDN()+"\n  Subject: "+xcert.getSubjectDN()
                    +")");
        }
        /** match on X509Certificate */
        if(x509Cert!=null){
            if(!x509Cert.equals(xcert)){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"certs don't match");
                }
                return false;
            }
        }
        /** match on serial number */
        if(serialNumber!=null){
            if(!serialNumber.equals(xcert.getSerialNumber())){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"serial numbers don't match");
                }
                return false;
            }
        }
        /** match on issuer name */
        if(issuer!=null){
            if(!issuer.equals(xcert.getIssuerX500Principal())){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"issuer DNs don't match");
                }
                return false;
            }
        }
        /** match on subject name */
        if(subject!=null){
            if(!subject.equals(xcert.getSubjectX500Principal())){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"subject DNs don't match");
                }
                return false;
            }
        }
        /** match on certificate validity range */
        if(certificateValid!=null){
            try{
                xcert.checkValidity(certificateValid);
            }catch(CertificateException e){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"certificate not within validity period");
                }
                return false;
            }
        }
        /** match on subject public key */
        if(subjectPublicKeyBytes!=null){
            byte[] certKey=xcert.getPublicKey().getEncoded();
            if(!Arrays.equals(subjectPublicKeyBytes,certKey)){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"subject public keys don't match");
                }
                return false;
            }
        }
        boolean result=matchBasicConstraints(xcert)
                &&matchKeyUsage(xcert)
                &&matchExtendedKeyUsage(xcert)
                &&matchSubjectKeyID(xcert)
                &&matchAuthorityKeyID(xcert)
                &&matchPrivateKeyValid(xcert)
                &&matchSubjectPublicKeyAlgID(xcert)
                &&matchPolicy(xcert)
                &&matchSubjectAlternativeNames(xcert)
                &&matchPathToNames(xcert)
                &&matchNameConstraints(xcert);
        if(result&&(debug!=null)){
            debug.println("X509CertSelector.match returning: true");
        }
        return result;
    }

    private boolean matchSubjectKeyID(X509Certificate xcert){
        if(subjectKeyID==null){
            return true;
        }
        try{
            byte[] extVal=xcert.getExtensionValue("2.5.29.14");
            if(extVal==null){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"no subject key ID extension");
                }
                return false;
            }
            DerInputStream in=new DerInputStream(extVal);
            byte[] certSubjectKeyID=in.getOctetString();
            if(certSubjectKeyID==null||
                    !Arrays.equals(subjectKeyID,certSubjectKeyID)){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"subject key IDs don't match");
                }
                return false;
            }
        }catch(IOException ex){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"exception in subject key ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchAuthorityKeyID(X509Certificate xcert){
        if(authorityKeyID==null){
            return true;
        }
        try{
            byte[] extVal=xcert.getExtensionValue("2.5.29.35");
            if(extVal==null){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"no authority key ID extension");
                }
                return false;
            }
            DerInputStream in=new DerInputStream(extVal);
            byte[] certAuthKeyID=in.getOctetString();
            if(certAuthKeyID==null||
                    !Arrays.equals(authorityKeyID,certAuthKeyID)){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"authority key IDs don't match");
                }
                return false;
            }
        }catch(IOException ex){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"exception in authority key ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPrivateKeyValid(X509Certificate xcert){
        if(privateKeyValid==null){
            return true;
        }
        PrivateKeyUsageExtension ext=null;
        try{
            ext=(PrivateKeyUsageExtension)
                    getExtensionObject(xcert,PRIVATE_KEY_USAGE_ID);
            if(ext!=null){
                ext.valid(privateKeyValid);
            }
        }catch(CertificateExpiredException e1){
            if(debug!=null){
                String time="n/a";
                try{
                    Date notAfter=ext.get(PrivateKeyUsageExtension.NOT_AFTER);
                    time=notAfter.toString();
                }catch(CertificateException ex){
                    // not able to retrieve notAfter value
                }
                debug.println("X509CertSelector.match: private key usage not "
                        +"within validity date; ext.NOT_After: "
                        +time+"; X509CertSelector: "
                        +this.toString());
                e1.printStackTrace();
            }
            return false;
        }catch(CertificateNotYetValidException e2){
            if(debug!=null){
                String time="n/a";
                try{
                    Date notBefore=ext.get(PrivateKeyUsageExtension.NOT_BEFORE);
                    time=notBefore.toString();
                }catch(CertificateException ex){
                    // not able to retrieve notBefore value
                }
                debug.println("X509CertSelector.match: private key usage not "
                        +"within validity date; ext.NOT_BEFORE: "
                        +time+"; X509CertSelector: "
                        +this.toString());
                e2.printStackTrace();
            }
            return false;
        }catch(IOException e4){
            if(debug!=null){
                debug.println("X509CertSelector.match: IOException in "
                        +"private key usage check; X509CertSelector: "
                        +this.toString());
                e4.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private boolean matchSubjectPublicKeyAlgID(X509Certificate xcert){
        if(subjectPublicKeyAlgID==null){
            return true;
        }
        try{
            byte[] encodedKey=xcert.getPublicKey().getEncoded();
            DerValue val=new DerValue(encodedKey);
            if(val.tag!=DerValue.tag_Sequence){
                throw new IOException("invalid key format");
            }
            AlgorithmId algID=AlgorithmId.parse(val.data.getDerValue());
            if(debug!=null){
                debug.println("X509CertSelector.match: subjectPublicKeyAlgID = "
                        +subjectPublicKeyAlgID+", xcert subjectPublicKeyAlgID = "
                        +algID.getOID());
            }
            if(!subjectPublicKeyAlgID.equals((Object)algID.getOID())){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"subject public key alg IDs don't match");
                }
                return false;
            }
        }catch(IOException e5){
            if(debug!=null){
                debug.println("X509CertSelector.match: IOException in subject "
                        +"public key algorithm OID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchKeyUsage(X509Certificate xcert){
        if(keyUsage==null){
            return true;
        }
        boolean[] certKeyUsage=xcert.getKeyUsage();
        if(certKeyUsage!=null){
            for(int keyBit=0;keyBit<keyUsage.length;keyBit++){
                if(keyUsage[keyBit]&&
                        ((keyBit>=certKeyUsage.length)||!certKeyUsage[keyBit])){
                    if(debug!=null){
                        debug.println("X509CertSelector.match: "
                                +"key usage bits don't match");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchExtendedKeyUsage(X509Certificate xcert){
        if((keyPurposeSet==null)||keyPurposeSet.isEmpty()){
            return true;
        }
        try{
            ExtendedKeyUsageExtension ext=
                    (ExtendedKeyUsageExtension)getExtensionObject(xcert,
                            EXTENDED_KEY_USAGE_ID);
            if(ext!=null){
                Vector<ObjectIdentifier> certKeyPurposeVector=
                        ext.get(ExtendedKeyUsageExtension.USAGES);
                if(!certKeyPurposeVector.contains(ANY_EXTENDED_KEY_USAGE)
                        &&!certKeyPurposeVector.containsAll(keyPurposeOIDSet)){
                    if(debug!=null){
                        debug.println("X509CertSelector.match: cert failed "
                                +"extendedKeyUsage criterion");
                    }
                    return false;
                }
            }
        }catch(IOException ex){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"IOException in extended key usage check");
            }
            return false;
        }
        return true;
    }

    private static Extension getExtensionObject(X509Certificate cert,int extId)
            throws IOException{
        if(cert instanceof X509CertImpl){
            X509CertImpl impl=(X509CertImpl)cert;
            switch(extId){
                case PRIVATE_KEY_USAGE_ID:
                    return impl.getPrivateKeyUsageExtension();
                case SUBJECT_ALT_NAME_ID:
                    return impl.getSubjectAlternativeNameExtension();
                case NAME_CONSTRAINTS_ID:
                    return impl.getNameConstraintsExtension();
                case CERT_POLICIES_ID:
                    return impl.getCertificatePoliciesExtension();
                case EXTENDED_KEY_USAGE_ID:
                    return impl.getExtendedKeyUsageExtension();
                default:
                    return null;
            }
        }
        byte[] rawExtVal=cert.getExtensionValue(EXTENSION_OIDS[extId]);
        if(rawExtVal==null){
            return null;
        }
        DerInputStream in=new DerInputStream(rawExtVal);
        byte[] encoded=in.getOctetString();
        switch(extId){
            case PRIVATE_KEY_USAGE_ID:
                try{
                    return new PrivateKeyUsageExtension(FALSE,encoded);
                }catch(CertificateException ex){
                    throw new IOException(ex.getMessage());
                }
            case SUBJECT_ALT_NAME_ID:
                return new SubjectAlternativeNameExtension(FALSE,encoded);
            case NAME_CONSTRAINTS_ID:
                return new NameConstraintsExtension(FALSE,encoded);
            case CERT_POLICIES_ID:
                return new CertificatePoliciesExtension(FALSE,encoded);
            case EXTENDED_KEY_USAGE_ID:
                return new ExtendedKeyUsageExtension(FALSE,encoded);
            default:
                return null;
        }
    }

    private boolean matchSubjectAlternativeNames(X509Certificate xcert){
        if((subjectAlternativeNames==null)||subjectAlternativeNames.isEmpty()){
            return true;
        }
        try{
            SubjectAlternativeNameExtension sanExt=
                    (SubjectAlternativeNameExtension)getExtensionObject(xcert,
                            SUBJECT_ALT_NAME_ID);
            if(sanExt==null){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"no subject alternative name extension");
                }
                return false;
            }
            GeneralNames certNames=
                    sanExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
            Iterator<GeneralNameInterface> i=
                    subjectAlternativeGeneralNames.iterator();
            while(i.hasNext()){
                GeneralNameInterface matchName=i.next();
                boolean found=false;
                for(Iterator<GeneralName> t=certNames.iterator();
                    t.hasNext()&&!found;){
                    GeneralNameInterface certName=(t.next()).getName();
                    found=certName.equals(matchName);
                }
                if(!found&&(matchAllSubjectAltNames||!i.hasNext())){
                    if(debug!=null){
                        debug.println("X509CertSelector.match: subject alternative "
                                +"name "+matchName+" not found");
                    }
                    return false;
                }else if(found&&!matchAllSubjectAltNames){
                    break;
                }
            }
        }catch(IOException ex){
            if(debug!=null)
                debug.println("X509CertSelector.match: IOException in subject "
                        +"alternative name check");
            return false;
        }
        return true;
    }

    private boolean matchNameConstraints(X509Certificate xcert){
        if(nc==null){
            return true;
        }
        try{
            if(!nc.verify(xcert)){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"name constraints not satisfied");
                }
                return false;
            }
        }catch(IOException e){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPolicy(X509Certificate xcert){
        if(policy==null){
            return true;
        }
        try{
            CertificatePoliciesExtension ext=(CertificatePoliciesExtension)
                    getExtensionObject(xcert,CERT_POLICIES_ID);
            if(ext==null){
                if(debug!=null){
                    debug.println("X509CertSelector.match: "
                            +"no certificate policy extension");
                }
                return false;
            }
            List<PolicyInformation> policies=ext.get(CertificatePoliciesExtension.POLICIES);
            /**
             * Convert the Vector of PolicyInformation to a Vector
             * of CertificatePolicyIds for easier comparison.
             */
            List<CertificatePolicyId> policyIDs=new ArrayList<CertificatePolicyId>(policies.size());
            for(PolicyInformation info : policies){
                policyIDs.add(info.getPolicyIdentifier());
            }
            if(policy!=null){
                boolean foundOne=false;
                /**
                 * if the user passes in an empty policy Set, then
                 * we just want to make sure that the candidate certificate
                 * has some policy OID in its CertPoliciesExtension
                 */
                if(policy.getCertPolicyIds().isEmpty()){
                    if(policyIDs.isEmpty()){
                        if(debug!=null){
                            debug.println("X509CertSelector.match: "
                                    +"cert failed policyAny criterion");
                        }
                        return false;
                    }
                }else{
                    for(CertificatePolicyId id : policy.getCertPolicyIds()){
                        if(policyIDs.contains(id)){
                            foundOne=true;
                            break;
                        }
                    }
                    if(!foundOne){
                        if(debug!=null){
                            debug.println("X509CertSelector.match: "
                                    +"cert failed policyAny criterion");
                        }
                        return false;
                    }
                }
            }
        }catch(IOException ex){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"IOException in certificate policy ID check");
            }
            return false;
        }
        return true;
    }

    private boolean matchPathToNames(X509Certificate xcert){
        if(pathToGeneralNames==null){
            return true;
        }
        try{
            NameConstraintsExtension ext=(NameConstraintsExtension)
                    getExtensionObject(xcert,NAME_CONSTRAINTS_ID);
            if(ext==null){
                return true;
            }
            if((debug!=null)&&Debug.isOn("certpath")){
                debug.println("X509CertSelector.match pathToNames:\n");
                Iterator<GeneralNameInterface> i=
                        pathToGeneralNames.iterator();
                while(i.hasNext()){
                    debug.println("    "+i.next()+"\n");
                }
            }
            GeneralSubtrees permitted=
                    ext.get(NameConstraintsExtension.PERMITTED_SUBTREES);
            GeneralSubtrees excluded=
                    ext.get(NameConstraintsExtension.EXCLUDED_SUBTREES);
            if(excluded!=null){
                if(matchExcluded(excluded)==false){
                    return false;
                }
            }
            if(permitted!=null){
                if(matchPermitted(permitted)==false){
                    return false;
                }
            }
        }catch(IOException ex){
            if(debug!=null){
                debug.println("X509CertSelector.match: "
                        +"IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    private boolean matchExcluded(GeneralSubtrees excluded){
        /**
         * Enumerate through excluded and compare each entry
         * to all pathToNames. If any pathToName is within any of the
         * subtrees listed in excluded, return false.
         */
        for(Iterator<GeneralSubtree> t=excluded.iterator();t.hasNext();){
            GeneralSubtree tree=t.next();
            GeneralNameInterface excludedName=tree.getName().getName();
            Iterator<GeneralNameInterface> i=pathToGeneralNames.iterator();
            while(i.hasNext()){
                GeneralNameInterface pathToName=i.next();
                if(excludedName.getType()==pathToName.getType()){
                    switch(pathToName.constrains(excludedName)){
                        case GeneralNameInterface.NAME_WIDENS:
                        case GeneralNameInterface.NAME_MATCH:
                            if(debug!=null){
                                debug.println("X509CertSelector.match: name constraints "
                                        +"inhibit path to specified name");
                                debug.println("X509CertSelector.match: excluded name: "+
                                        pathToName);
                            }
                            return false;
                        default:
                    }
                }
            }
        }
        return true;
    }

    private boolean matchPermitted(GeneralSubtrees permitted){
        /**
         * Enumerate through pathToNames, checking that each pathToName
         * is in at least one of the subtrees listed in permitted.
         * If not, return false. However, if no subtrees of a given type
         * are listed, all names of that type are permitted.
         */
        Iterator<GeneralNameInterface> i=pathToGeneralNames.iterator();
        while(i.hasNext()){
            GeneralNameInterface pathToName=i.next();
            Iterator<GeneralSubtree> t=permitted.iterator();
            boolean permittedNameFound=false;
            boolean nameTypeFound=false;
            String names="";
            while(t.hasNext()&&!permittedNameFound){
                GeneralSubtree tree=t.next();
                GeneralNameInterface permittedName=tree.getName().getName();
                if(permittedName.getType()==pathToName.getType()){
                    nameTypeFound=true;
                    names=names+"  "+permittedName;
                    switch(pathToName.constrains(permittedName)){
                        case GeneralNameInterface.NAME_WIDENS:
                        case GeneralNameInterface.NAME_MATCH:
                            permittedNameFound=true;
                            break;
                        default:
                    }
                }
            }
            if(!permittedNameFound&&nameTypeFound){
                if(debug!=null)
                    debug.println("X509CertSelector.match: "+
                            "name constraints inhibit path to specified name; "+
                            "permitted names of type "+pathToName.getType()+
                            ": "+names);
                return false;
            }
        }
        return true;
    }

    private boolean matchBasicConstraints(X509Certificate xcert){
        if(basicConstraints==-1){
            return true;
        }
        int maxPathLen=xcert.getBasicConstraints();
        if(basicConstraints==-2){
            if(maxPathLen!=-1){
                if(debug!=null){
                    debug.println("X509CertSelector.match: not an EE cert");
                }
                return false;
            }
        }else{
            if(maxPathLen<basicConstraints){
                if(debug!=null){
                    debug.println("X509CertSelector.match: cert's maxPathLen "+
                            "is less than the min maxPathLen set by "+
                            "basicConstraints. "+
                            "("+maxPathLen+" < "+basicConstraints+")");
                }
                return false;
            }
        }
        return true;
    }

    public Object clone(){
        try{
            X509CertSelector copy=(X509CertSelector)super.clone();
            // Must clone these because addPathToName et al. modify them
            if(subjectAlternativeNames!=null){
                copy.subjectAlternativeNames=
                        cloneSet(subjectAlternativeNames);
                copy.subjectAlternativeGeneralNames=
                        cloneSet(subjectAlternativeGeneralNames);
            }
            if(pathToGeneralNames!=null){
                copy.pathToNames=cloneSet(pathToNames);
                copy.pathToGeneralNames=cloneSet(pathToGeneralNames);
            }
            return copy;
        }catch(CloneNotSupportedException e){
            /** Cannot happen */
            throw new InternalError(e.toString(),e);
        }
    }

    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append("X509CertSelector: [\n");
        if(x509Cert!=null){
            sb.append("  Certificate: "+x509Cert.toString()+"\n");
        }
        if(serialNumber!=null){
            sb.append("  Serial Number: "+serialNumber.toString()+"\n");
        }
        if(issuer!=null){
            sb.append("  Issuer: "+getIssuerAsString()+"\n");
        }
        if(subject!=null){
            sb.append("  Subject: "+getSubjectAsString()+"\n");
        }
        sb.append("  matchAllSubjectAltNames flag: "
                +String.valueOf(matchAllSubjectAltNames)+"\n");
        if(subjectAlternativeNames!=null){
            sb.append("  SubjectAlternativeNames:\n");
            Iterator<List<?>> i=subjectAlternativeNames.iterator();
            while(i.hasNext()){
                List<?> list=i.next();
                sb.append("    type "+list.get(0)+
                        ", name "+list.get(1)+"\n");
            }
        }
        if(subjectKeyID!=null){
            HexDumpEncoder enc=new HexDumpEncoder();
            sb.append("  Subject Key Identifier: "+
                    enc.encodeBuffer(subjectKeyID)+"\n");
        }
        if(authorityKeyID!=null){
            HexDumpEncoder enc=new HexDumpEncoder();
            sb.append("  Authority Key Identifier: "+
                    enc.encodeBuffer(authorityKeyID)+"\n");
        }
        if(certificateValid!=null){
            sb.append("  Certificate Valid: "+
                    certificateValid.toString()+"\n");
        }
        if(privateKeyValid!=null){
            sb.append("  Private Key Valid: "+
                    privateKeyValid.toString()+"\n");
        }
        if(subjectPublicKeyAlgID!=null){
            sb.append("  Subject Public Key AlgID: "+
                    subjectPublicKeyAlgID.toString()+"\n");
        }
        if(subjectPublicKey!=null){
            sb.append("  Subject Public Key: "+
                    subjectPublicKey.toString()+"\n");
        }
        if(keyUsage!=null){
            sb.append("  Key Usage: "+keyUsageToString(keyUsage)+"\n");
        }
        if(keyPurposeSet!=null){
            sb.append("  Extended Key Usage: "+
                    keyPurposeSet.toString()+"\n");
        }
        if(policy!=null){
            sb.append("  Policy: "+policy.toString()+"\n");
        }
        if(pathToGeneralNames!=null){
            sb.append("  Path to names:\n");
            Iterator<GeneralNameInterface> i=pathToGeneralNames.iterator();
            while(i.hasNext()){
                sb.append("    "+i.next()+"\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String getIssuerAsString(){
        return (issuer==null?null:issuer.getName());
    }

    public String getSubjectAsString(){
        return (subject==null?null:subject.getName());
    }

    // Copied from sun.security.x509.KeyUsageExtension
    // (without calling the superclass)
    private static String keyUsageToString(boolean[] k){
        String s="KeyUsage [\n";
        try{
            if(k[0]){
                s+="  DigitalSignature\n";
            }
            if(k[1]){
                s+="  Non_repudiation\n";
            }
            if(k[2]){
                s+="  Key_Encipherment\n";
            }
            if(k[3]){
                s+="  Data_Encipherment\n";
            }
            if(k[4]){
                s+="  Key_Agreement\n";
            }
            if(k[5]){
                s+="  Key_CertSign\n";
            }
            if(k[6]){
                s+="  Crl_Sign\n";
            }
            if(k[7]){
                s+="  Encipher_Only\n";
            }
            if(k[8]){
                s+="  Decipher_Only\n";
            }
        }catch(ArrayIndexOutOfBoundsException ex){
        }
        s+="]\n";
        return (s);
    }

    @SuppressWarnings("unchecked") // Safe casts assuming clone() works correctly
    private static <T> Set<T> cloneSet(Set<T> set){
        if(set instanceof HashSet){
            Object clone=((HashSet<T>)set).clone();
            return (Set<T>)clone;
        }else{
            return new HashSet<T>(set);
        }
    }
}
