/**
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.jca.ServiceId;
import sun.security.util.Debug;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.Provider.Service;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Signature extends SignatureSpi{
    protected final static int UNINITIALIZED=0;
    protected final static int SIGN=2;
    protected final static int VERIFY=3;
    private static final Debug debug=
            Debug.getInstance("jca","Signature");
    private static final Debug pdebug=
            Debug.getInstance("provider","Provider");
    private static final boolean skipDebug=
            Debug.isOn("engine=")&&!Debug.isOn("signature");
    // name of the special signature alg
    private final static String RSA_SIGNATURE="NONEwithRSA";
    // name of the equivalent cipher alg
    private final static String RSA_CIPHER="RSA/ECB/PKCS1Padding";
    // all the services we need to lookup for compatibility with Cipher
    private final static List<ServiceId> rsaIds=Arrays.asList(
            new ServiceId[]{
                    new ServiceId("Signature","NONEwithRSA"),
                    new ServiceId("Cipher","RSA/ECB/PKCS1Padding"),
                    new ServiceId("Cipher","RSA/ECB"),
                    new ServiceId("Cipher","RSA//PKCS1Padding"),
                    new ServiceId("Cipher","RSA"),
            }
    );
    private final static Map<String,Boolean> signatureInfo;

    static{
        signatureInfo=new ConcurrentHashMap<String,Boolean>();
        Boolean TRUE=Boolean.TRUE;
        // pre-initialize with values for our SignatureSpi implementations
        signatureInfo.put("sun.security.provider.DSA$RawDSA",TRUE);
        signatureInfo.put("sun.security.provider.DSA$SHA1withDSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD2withRSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD5withRSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA1withRSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA256withRSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA384withRSA",TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA512withRSA",TRUE);
        signatureInfo.put("com.sun.net.ssl.internal.ssl.RSASignature",TRUE);
        signatureInfo.put("sun.security.pkcs11.P11Signature",TRUE);
    }

    protected int state=UNINITIALIZED;
    // The provider
    Provider provider;
    private String algorithm;

    protected Signature(String algorithm){
        this.algorithm=algorithm;
    }

    public static Signature getInstance(String algorithm)
            throws NoSuchAlgorithmException{
        List<Service> list;
        if(algorithm.equalsIgnoreCase(RSA_SIGNATURE)){
            list=GetInstance.getServices(rsaIds);
        }else{
            list=GetInstance.getServices("Signature",algorithm);
        }
        Iterator<Service> t=list.iterator();
        if(t.hasNext()==false){
            throw new NoSuchAlgorithmException
                    (algorithm+" Signature not available");
        }
        // try services until we find an Spi or a working Signature subclass
        NoSuchAlgorithmException failure;
        do{
            Service s=t.next();
            if(isSpi(s)){
                return new Delegate(s,t,algorithm);
            }else{
                // must be a subclass of Signature, disable dynamic selection
                try{
                    Instance instance=
                            GetInstance.getInstance(s,SignatureSpi.class);
                    return getInstance(instance,algorithm);
                }catch(NoSuchAlgorithmException e){
                    failure=e;
                }
            }
        }while(t.hasNext());
        throw failure;
    }

    private static Signature getInstance(Instance instance,String algorithm){
        Signature sig;
        if(instance.impl instanceof Signature){
            sig=(Signature)instance.impl;
            sig.algorithm=algorithm;
        }else{
            SignatureSpi spi=(SignatureSpi)instance.impl;
            sig=new Delegate(spi,algorithm);
        }
        sig.provider=instance.provider;
        return sig;
    }

    private static boolean isSpi(Service s){
        if(s.getType().equals("Cipher")){
            // must be a CipherSpi, which we can wrap with the CipherAdapter
            return true;
        }
        String className=s.getClassName();
        Boolean result=signatureInfo.get(className);
        if(result==null){
            try{
                Object instance=s.newInstance(null);
                // Signature extends SignatureSpi
                // so it is a "real" Spi if it is an
                // instance of SignatureSpi but not Signature
                boolean r=(instance instanceof SignatureSpi)
                        &&(instance instanceof Signature==false);
                if((debug!=null)&&(r==false)){
                    debug.println("Not a SignatureSpi "+className);
                    debug.println("Delayed provider selection may not be "
                            +"available for algorithm "+s.getAlgorithm());
                }
                result=Boolean.valueOf(r);
                signatureInfo.put(className,result);
            }catch(Exception e){
                // something is wrong, assume not an SPI
                return false;
            }
        }
        return result.booleanValue();
    }

    public static Signature getInstance(String algorithm,String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException{
        if(algorithm.equalsIgnoreCase(RSA_SIGNATURE)){
            // exception compatibility with existing code
            if((provider==null)||(provider.length()==0)){
                throw new IllegalArgumentException("missing provider");
            }
            Provider p=Security.getProvider(provider);
            if(p==null){
                throw new NoSuchProviderException
                        ("no such provider: "+provider);
            }
            return getInstanceRSA(p);
        }
        Instance instance=GetInstance.getInstance
                ("Signature",SignatureSpi.class,algorithm,provider);
        return getInstance(instance,algorithm);
    }

    // return an implementation for NONEwithRSA, which is a special case
    // because of the Cipher.RSA/ECB/PKCS1Padding compatibility wrapper
    private static Signature getInstanceRSA(Provider p)
            throws NoSuchAlgorithmException{
        // try Signature first
        Service s=p.getService("Signature",RSA_SIGNATURE);
        if(s!=null){
            Instance instance=GetInstance.getInstance(s,SignatureSpi.class);
            return getInstance(instance,RSA_SIGNATURE);
        }
        // check Cipher
        try{
            Cipher c=Cipher.getInstance(RSA_CIPHER,p);
            return new Delegate(new CipherAdapter(c),RSA_SIGNATURE);
        }catch(GeneralSecurityException e){
            // throw Signature style exception message to avoid confusion,
            // but append Cipher exception as cause
            throw new NoSuchAlgorithmException("no such algorithm: "
                    +RSA_SIGNATURE+" for provider "+p.getName(),e);
        }
    }

    public static Signature getInstance(String algorithm,Provider provider)
            throws NoSuchAlgorithmException{
        if(algorithm.equalsIgnoreCase(RSA_SIGNATURE)){
            // exception compatibility with existing code
            if(provider==null){
                throw new IllegalArgumentException("missing provider");
            }
            return getInstanceRSA(provider);
        }
        Instance instance=GetInstance.getInstance
                ("Signature",SignatureSpi.class,algorithm,provider);
        return getInstance(instance,algorithm);
    }

    public final Provider getProvider(){
        chooseFirstProvider();
        return this.provider;
    }

    void chooseFirstProvider(){
        // empty, overridden in Delegate
    }

    public final void initVerify(PublicKey publicKey)
            throws InvalidKeyException{
        engineInitVerify(publicKey);
        state=VERIFY;
        if(!skipDebug&&pdebug!=null){
            pdebug.println("Signature."+algorithm+
                    " verification algorithm from: "+this.provider.getName());
        }
    }

    public final void initVerify(Certificate certificate)
            throws InvalidKeyException{
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        if(certificate instanceof X509Certificate){
            // Check whether the cert has a key usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            X509Certificate cert=(X509Certificate)certificate;
            Set<String> critSet=cert.getCriticalExtensionOIDs();
            if(critSet!=null&&!critSet.isEmpty()
                    &&critSet.contains("2.5.29.15")){
                boolean[] keyUsageInfo=cert.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if((keyUsageInfo!=null)&&(keyUsageInfo[0]==false))
                    throw new InvalidKeyException("Wrong key usage");
            }
        }
        PublicKey publicKey=certificate.getPublicKey();
        engineInitVerify(publicKey);
        state=VERIFY;
        if(!skipDebug&&pdebug!=null){
            pdebug.println("Signature."+algorithm+
                    " verification algorithm from: "+this.provider.getName());
        }
    }

    public final void initSign(PrivateKey privateKey)
            throws InvalidKeyException{
        engineInitSign(privateKey);
        state=SIGN;
        if(!skipDebug&&pdebug!=null){
            pdebug.println("Signature."+algorithm+
                    " signing algorithm from: "+this.provider.getName());
        }
    }

    public final void initSign(PrivateKey privateKey,SecureRandom random)
            throws InvalidKeyException{
        engineInitSign(privateKey,random);
        state=SIGN;
        if(!skipDebug&&pdebug!=null){
            pdebug.println("Signature."+algorithm+
                    " signing algorithm from: "+this.provider.getName());
        }
    }

    public final byte[] sign() throws SignatureException{
        if(state==SIGN){
            return engineSign();
        }
        throw new SignatureException("object not initialized for "+
                "signing");
    }

    public final int sign(byte[] outbuf,int offset,int len)
            throws SignatureException{
        if(outbuf==null){
            throw new IllegalArgumentException("No output buffer given");
        }
        if(offset<0||len<0){
            throw new IllegalArgumentException("offset or len is less than 0");
        }
        if(outbuf.length-offset<len){
            throw new IllegalArgumentException
                    ("Output buffer too small for specified offset and length");
        }
        if(state!=SIGN){
            throw new SignatureException("object not initialized for "+
                    "signing");
        }
        return engineSign(outbuf,offset,len);
    }

    public final boolean verify(byte[] signature) throws SignatureException{
        if(state==VERIFY){
            return engineVerify(signature);
        }
        throw new SignatureException("object not initialized for "+
                "verification");
    }

    public final boolean verify(byte[] signature,int offset,int length)
            throws SignatureException{
        if(state==VERIFY){
            if(signature==null){
                throw new IllegalArgumentException("signature is null");
            }
            if(offset<0||length<0){
                throw new IllegalArgumentException
                        ("offset or length is less than 0");
            }
            if(signature.length-offset<length){
                throw new IllegalArgumentException
                        ("signature too small for specified offset and length");
            }
            return engineVerify(signature,offset,length);
        }
        throw new SignatureException("object not initialized for "+
                "verification");
    }

    public final void update(byte b) throws SignatureException{
        if(state==VERIFY||state==SIGN){
            engineUpdate(b);
        }else{
            throw new SignatureException("object not initialized for "
                    +"signature or verification");
        }
    }

    public final void update(byte[] data) throws SignatureException{
        update(data,0,data.length);
    }

    public final void update(byte[] data,int off,int len)
            throws SignatureException{
        if(state==SIGN||state==VERIFY){
            if(data==null){
                throw new IllegalArgumentException("data is null");
            }
            if(off<0||len<0){
                throw new IllegalArgumentException("off or len is less than 0");
            }
            if(data.length-off<len){
                throw new IllegalArgumentException
                        ("data too small for specified offset and length");
            }
            engineUpdate(data,off,len);
        }else{
            throw new SignatureException("object not initialized for "
                    +"signature or verification");
        }
    }

    public final void update(ByteBuffer data) throws SignatureException{
        if((state!=SIGN)&&(state!=VERIFY)){
            throw new SignatureException("object not initialized for "
                    +"signature or verification");
        }
        if(data==null){
            throw new NullPointerException();
        }
        engineUpdate(data);
    }

    public String toString(){
        String initState="";
        switch(state){
            case UNINITIALIZED:
                initState="<not initialized>";
                break;
            case VERIFY:
                initState="<initialized for verifying>";
                break;
            case SIGN:
                initState="<initialized for signing>";
                break;
        }
        return "Signature object: "+getAlgorithm()+initState;
    }

    public final String getAlgorithm(){
        return this.algorithm;
    }

    @Deprecated
    public final void setParameter(String param,Object value)
            throws InvalidParameterException{
        engineSetParameter(param,value);
    }

    public final void setParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException{
        engineSetParameter(params);
    }

    public final AlgorithmParameters getParameters(){
        return engineGetParameters();
    }

    @Deprecated
    public final Object getParameter(String param)
            throws InvalidParameterException{
        return engineGetParameter(param);
    }

    @SuppressWarnings("deprecation")
    private static class Delegate extends Signature{
        private final static int I_PUB=1;
        private final static int I_PRIV=2;
        private final static int I_PRIV_SR=3;
        // max number of debug warnings to print from chooseFirstProvider()
        private static int warnCount=10;
        // lock for mutex during provider selection
        private final Object lock;
        // The provider implementation (delegate)
        // filled in once the provider is selected
        private SignatureSpi sigSpi;
        // next service to try in provider selection
        // null once provider is selected
        private Service firstService;
        // remaining services to try in provider selection
        // null once provider is selected
        private Iterator<Service> serviceIterator;        public Object clone() throws CloneNotSupportedException{
            chooseFirstProvider();
            if(sigSpi instanceof Cloneable){
                SignatureSpi sigSpiClone=(SignatureSpi)sigSpi.clone();
                // Because 'algorithm' and 'provider' are private
                // members of our supertype, we must perform a cast to
                // access them.
                Signature that=
                        new Delegate(sigSpiClone,((Signature)this).algorithm);
                that.provider=((Signature)this).provider;
                return that;
            }else{
                throw new CloneNotSupportedException();
            }
        }

        // constructor
        Delegate(SignatureSpi sigSpi,String algorithm){
            super(algorithm);
            this.sigSpi=sigSpi;
            this.lock=null; // no lock needed
        }

        // used with delayed provider selection
        Delegate(Service service,
                 Iterator<Service> iterator,String algorithm){
            super(algorithm);
            this.firstService=service;
            this.serviceIterator=iterator;
            this.lock=new Object();
        }        private static SignatureSpi newInstance(Service s)
                throws NoSuchAlgorithmException{
            if(s.getType().equals("Cipher")){
                // must be NONEwithRSA
                try{
                    Cipher c=Cipher.getInstance(RSA_CIPHER,s.getProvider());
                    return new CipherAdapter(c);
                }catch(NoSuchPaddingException e){
                    throw new NoSuchAlgorithmException(e);
                }
            }else{
                Object o=s.newInstance(null);
                if(o instanceof SignatureSpi==false){
                    throw new NoSuchAlgorithmException
                            ("Not a SignatureSpi: "+o.getClass().getName());
                }
                return (SignatureSpi)o;
            }
        }

        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException{
            if(sigSpi!=null){
                sigSpi.engineInitVerify(publicKey);
            }else{
                chooseProvider(I_PUB,publicKey,null);
            }
        }

        private void chooseProvider(int type,Key key,SecureRandom random)
                throws InvalidKeyException{
            synchronized(lock){
                if(sigSpi!=null){
                    init(sigSpi,type,key,random);
                    return;
                }
                Exception lastException=null;
                while((firstService!=null)||serviceIterator.hasNext()){
                    Service s;
                    if(firstService!=null){
                        s=firstService;
                        firstService=null;
                    }else{
                        s=serviceIterator.next();
                    }
                    // if provider says it does not support this key, ignore it
                    if(s.supportsParameter(key)==false){
                        continue;
                    }
                    // if instance is not a SignatureSpi, ignore it
                    if(isSpi(s)==false){
                        continue;
                    }
                    try{
                        SignatureSpi spi=newInstance(s);
                        init(spi,type,key,random);
                        provider=s.getProvider();
                        sigSpi=spi;
                        firstService=null;
                        serviceIterator=null;
                        return;
                    }catch(Exception e){
                        // NoSuchAlgorithmException from newInstance()
                        // InvalidKeyException from init()
                        // RuntimeException (ProviderException) from init()
                        if(lastException==null){
                            lastException=e;
                        }
                    }
                }
                // no working provider found, fail
                if(lastException instanceof InvalidKeyException){
                    throw (InvalidKeyException)lastException;
                }
                if(lastException instanceof RuntimeException){
                    throw (RuntimeException)lastException;
                }
                String k=(key!=null)?key.getClass().getName():"(null)";
                throw new InvalidKeyException
                        ("No installed provider supports this key: "
                                +k,lastException);
            }
        }

        private void init(SignatureSpi spi,int type,Key key,
                          SecureRandom random) throws InvalidKeyException{
            switch(type){
                case I_PUB:
                    spi.engineInitVerify((PublicKey)key);
                    break;
                case I_PRIV:
                    spi.engineInitSign((PrivateKey)key);
                    break;
                case I_PRIV_SR:
                    spi.engineInitSign((PrivateKey)key,random);
                    break;
                default:
                    throw new AssertionError("Internal error: "+type);
            }
        }        void chooseFirstProvider(){
            if(sigSpi!=null){
                return;
            }
            synchronized(lock){
                if(sigSpi!=null){
                    return;
                }
                if(debug!=null){
                    int w=--warnCount;
                    if(w>=0){
                        debug.println("Signature.init() not first method "
                                +"called, disabling delayed provider selection");
                        if(w==0){
                            debug.println("Further warnings of this type will "
                                    +"be suppressed");
                        }
                        new Exception("Call trace").printStackTrace();
                    }
                }
                Exception lastException=null;
                while((firstService!=null)||serviceIterator.hasNext()){
                    Service s;
                    if(firstService!=null){
                        s=firstService;
                        firstService=null;
                    }else{
                        s=serviceIterator.next();
                    }
                    if(isSpi(s)==false){
                        continue;
                    }
                    try{
                        sigSpi=newInstance(s);
                        provider=s.getProvider();
                        // not needed any more
                        firstService=null;
                        serviceIterator=null;
                        return;
                    }catch(NoSuchAlgorithmException e){
                        lastException=e;
                    }
                }
                ProviderException e=new ProviderException
                        ("Could not construct SignatureSpi instance");
                if(lastException!=null){
                    e.initCause(lastException);
                }
                throw e;
            }
        }







        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException{
            if(sigSpi!=null){
                sigSpi.engineInitSign(privateKey);
            }else{
                chooseProvider(I_PRIV,privateKey,null);
            }
        }

        protected void engineInitSign(PrivateKey privateKey,SecureRandom sr)
                throws InvalidKeyException{
            if(sigSpi!=null){
                sigSpi.engineInitSign(privateKey,sr);
            }else{
                chooseProvider(I_PRIV_SR,privateKey,sr);
            }
        }

        protected void engineUpdate(byte b) throws SignatureException{
            chooseFirstProvider();
            sigSpi.engineUpdate(b);
        }

        protected void engineUpdate(byte[] b,int off,int len)
                throws SignatureException{
            chooseFirstProvider();
            sigSpi.engineUpdate(b,off,len);
        }

        protected void engineUpdate(ByteBuffer data){
            chooseFirstProvider();
            sigSpi.engineUpdate(data);
        }

        protected byte[] engineSign() throws SignatureException{
            chooseFirstProvider();
            return sigSpi.engineSign();
        }

        protected int engineSign(byte[] outbuf,int offset,int len)
                throws SignatureException{
            chooseFirstProvider();
            return sigSpi.engineSign(outbuf,offset,len);
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException{
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes);
        }

        protected boolean engineVerify(byte[] sigBytes,int offset,int length)
                throws SignatureException{
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes,offset,length);
        }

        protected void engineSetParameter(String param,Object value)
                throws InvalidParameterException{
            chooseFirstProvider();
            sigSpi.engineSetParameter(param,value);
        }

        protected void engineSetParameter(AlgorithmParameterSpec params)
                throws InvalidAlgorithmParameterException{
            chooseFirstProvider();
            sigSpi.engineSetParameter(params);
        }

        protected Object engineGetParameter(String param)
                throws InvalidParameterException{
            chooseFirstProvider();
            return sigSpi.engineGetParameter(param);
        }

        protected AlgorithmParameters engineGetParameters(){
            chooseFirstProvider();
            return sigSpi.engineGetParameters();
        }
    }

    // adapter for RSA/ECB/PKCS1Padding ciphers
    @SuppressWarnings("deprecation")
    private static class CipherAdapter extends SignatureSpi{
        private final Cipher cipher;
        private ByteArrayOutputStream data;

        CipherAdapter(Cipher cipher){
            this.cipher=cipher;
        }

        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException{
            cipher.init(Cipher.DECRYPT_MODE,publicKey);
            if(data==null){
                data=new ByteArrayOutputStream(128);
            }else{
                data.reset();
            }
        }

        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException{
            cipher.init(Cipher.ENCRYPT_MODE,privateKey);
            data=null;
        }

        protected void engineInitSign(PrivateKey privateKey,
                                      SecureRandom random) throws InvalidKeyException{
            cipher.init(Cipher.ENCRYPT_MODE,privateKey,random);
            data=null;
        }

        protected void engineUpdate(byte b) throws SignatureException{
            engineUpdate(new byte[]{b},0,1);
        }

        protected void engineUpdate(byte[] b,int off,int len)
                throws SignatureException{
            if(data!=null){
                data.write(b,off,len);
                return;
            }
            byte[] out=cipher.update(b,off,len);
            if((out!=null)&&(out.length!=0)){
                throw new SignatureException
                        ("Cipher unexpectedly returned data");
            }
        }

        protected byte[] engineSign() throws SignatureException{
            try{
                return cipher.doFinal();
            }catch(IllegalBlockSizeException e){
                throw new SignatureException("doFinal() failed",e);
            }catch(BadPaddingException e){
                throw new SignatureException("doFinal() failed",e);
            }
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException{
            try{
                byte[] out=cipher.doFinal(sigBytes);
                byte[] dataBytes=data.toByteArray();
                data.reset();
                return MessageDigest.isEqual(out,dataBytes);
            }catch(BadPaddingException e){
                // e.g. wrong public key used
                // return false rather than throwing exception
                return false;
            }catch(IllegalBlockSizeException e){
                throw new SignatureException("doFinal() failed",e);
            }
        }

        protected void engineSetParameter(String param,Object value)
                throws InvalidParameterException{
            throw new InvalidParameterException("Parameters not supported");
        }

        protected Object engineGetParameter(String param)
                throws InvalidParameterException{
            throw new InvalidParameterException("Parameters not supported");
        }
    }    public Object clone() throws CloneNotSupportedException{
        if(this instanceof Cloneable){
            return super.clone();
        }else{
            throw new CloneNotSupportedException();
        }
    }


}
