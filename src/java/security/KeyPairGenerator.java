/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;

import java.security.Provider.Service;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.List;

public abstract class KeyPairGenerator extends KeyPairGeneratorSpi{
    private static final Debug pdebug=
            Debug.getInstance("provider","Provider");
    private static final boolean skipDebug=
            Debug.isOn("engine=")&&!Debug.isOn("keypairgenerator");
    private final String algorithm;
    // The provider
    Provider provider;

    protected KeyPairGenerator(String algorithm){
        this.algorithm=algorithm;
    }

    public static KeyPairGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException{
        List<Service> list=
                GetInstance.getServices("KeyPairGenerator",algorithm);
        Iterator<Service> t=list.iterator();
        if(t.hasNext()==false){
            throw new NoSuchAlgorithmException
                    (algorithm+" KeyPairGenerator not available");
        }
        // find a working Spi or KeyPairGenerator subclass
        NoSuchAlgorithmException failure=null;
        do{
            Service s=t.next();
            try{
                Instance instance=
                        GetInstance.getInstance(s,KeyPairGeneratorSpi.class);
                if(instance.impl instanceof KeyPairGenerator){
                    return getInstance(instance,algorithm);
                }else{
                    return new Delegate(instance,t,algorithm);
                }
            }catch(NoSuchAlgorithmException e){
                if(failure==null){
                    failure=e;
                }
            }
        }while(t.hasNext());
        throw failure;
    }

    private static KeyPairGenerator getInstance(Instance instance,
                                                String algorithm){
        KeyPairGenerator kpg;
        if(instance.impl instanceof KeyPairGenerator){
            kpg=(KeyPairGenerator)instance.impl;
        }else{
            KeyPairGeneratorSpi spi=(KeyPairGeneratorSpi)instance.impl;
            kpg=new Delegate(spi,algorithm);
        }
        kpg.provider=instance.provider;
        if(!skipDebug&&pdebug!=null){
            pdebug.println("KeyPairGenerator."+algorithm+
                    " algorithm from: "+kpg.provider.getName());
        }
        return kpg;
    }

    public static KeyPairGenerator getInstance(String algorithm,
                                               String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException{
        Instance instance=GetInstance.getInstance("KeyPairGenerator",
                KeyPairGeneratorSpi.class,algorithm,provider);
        return getInstance(instance,algorithm);
    }

    public static KeyPairGenerator getInstance(String algorithm,
                                               Provider provider) throws NoSuchAlgorithmException{
        Instance instance=GetInstance.getInstance("KeyPairGenerator",
                KeyPairGeneratorSpi.class,algorithm,provider);
        return getInstance(instance,algorithm);
    }

    public String getAlgorithm(){
        return this.algorithm;
    }

    public final Provider getProvider(){
        disableFailover();
        return this.provider;
    }

    void disableFailover(){
        // empty, overridden in Delegate
    }

    public void initialize(int keysize){
        initialize(keysize,JCAUtil.getSecureRandom());
    }

    public void initialize(int keysize,SecureRandom random){
        // This does nothing, because either
        // 1. the implementation object returned by getInstance() is an
        //    instance of KeyPairGenerator which has its own
        //    initialize(keysize, random) method, so the application would
        //    be calling that method directly, or
        // 2. the implementation returned by getInstance() is an instance
        //    of Delegate, in which case initialize(keysize, random) is
        //    overridden to call the corresponding SPI method.
        // (This is a special case, because the API and SPI method have the
        // same name.)
    }

    public void initialize(AlgorithmParameterSpec params,
                           SecureRandom random)
            throws InvalidAlgorithmParameterException{
        // This does nothing, because either
        // 1. the implementation object returned by getInstance() is an
        //    instance of KeyPairGenerator which has its own
        //    initialize(params, random) method, so the application would
        //    be calling that method directly, or
        // 2. the implementation returned by getInstance() is an instance
        //    of Delegate, in which case initialize(params, random) is
        //    overridden to call the corresponding SPI method.
        // (This is a special case, because the API and SPI method have the
        // same name.)
    }

    public KeyPair generateKeyPair(){
        // This does nothing (except returning null), because either:
        //
        // 1. the implementation object returned by getInstance() is an
        //    instance of KeyPairGenerator which has its own implementation
        //    of generateKeyPair (overriding this one), so the application
        //    would be calling that method directly, or
        //
        // 2. the implementation returned by getInstance() is an instance
        //    of Delegate, in which case generateKeyPair is
        //    overridden to invoke the corresponding SPI method.
        //
        // (This is a special case, because in JDK 1.1.x the generateKeyPair
        // method was used both as an API and a SPI method.)
        return null;
    }

    public void initialize(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException{
        initialize(params,JCAUtil.getSecureRandom());
    }

    public final KeyPair genKeyPair(){
        return generateKeyPair();
    }
    //
    // error failover notes:
    //
    //  . we failover if the implementation throws an error during init
    //    by retrying the init on other providers
    //
    //  . we also failover if the init succeeded but the subsequent call
    //    to generateKeyPair() fails. In order for this to work, we need
    //    to remember the parameters to the last successful call to init
    //    and initialize() the next spi using them.
    //
    //  . although not specified, KeyPairGenerators could be thread safe,
    //    so we make sure we do not interfere with that
    //
    //  . failover is not available, if:
    //    . getInstance(algorithm, provider) was used
    //    . a provider extends KeyPairGenerator rather than
    //      KeyPairGeneratorSpi (JDK 1.1 style)
    //    . once getProvider() is called
    //

    private static final class Delegate extends KeyPairGenerator{
        private final static int I_NONE=1;
        private final static int I_SIZE=2;
        private final static int I_PARAMS=3;
        private final Object lock=new Object();
        // The provider implementation (delegate)
        private volatile KeyPairGeneratorSpi spi;
        private Iterator<Service> serviceIterator;
        private int initType;
        private int initKeySize;
        private AlgorithmParameterSpec initParams;
        private SecureRandom initRandom;

        // constructor
        Delegate(KeyPairGeneratorSpi spi,String algorithm){
            super(algorithm);
            this.spi=spi;
        }

        Delegate(Instance instance,Iterator<Service> serviceIterator,
                 String algorithm){
            super(algorithm);
            spi=(KeyPairGeneratorSpi)instance.impl;
            provider=instance.provider;
            this.serviceIterator=serviceIterator;
            initType=I_NONE;
            if(!skipDebug&&pdebug!=null){
                pdebug.println("KeyPairGenerator."+algorithm+
                        " algorithm from: "+provider.getName());
            }
        }

        private KeyPairGeneratorSpi nextSpi(KeyPairGeneratorSpi oldSpi,
                                            boolean reinit){
            synchronized(lock){
                // somebody else did a failover concurrently
                // try that spi now
                if((oldSpi!=null)&&(oldSpi!=spi)){
                    return spi;
                }
                if(serviceIterator==null){
                    return null;
                }
                while(serviceIterator.hasNext()){
                    Service s=serviceIterator.next();
                    try{
                        Object inst=s.newInstance(null);
                        // ignore non-spis
                        if(inst instanceof KeyPairGeneratorSpi==false){
                            continue;
                        }
                        if(inst instanceof KeyPairGenerator){
                            continue;
                        }
                        KeyPairGeneratorSpi spi=(KeyPairGeneratorSpi)inst;
                        if(reinit){
                            if(initType==I_SIZE){
                                spi.initialize(initKeySize,initRandom);
                            }else if(initType==I_PARAMS){
                                spi.initialize(initParams,initRandom);
                            }else if(initType!=I_NONE){
                                throw new AssertionError
                                        ("KeyPairGenerator initType: "+initType);
                            }
                        }
                        provider=s.getProvider();
                        this.spi=spi;
                        return spi;
                    }catch(Exception e){
                        // ignore
                    }
                }
                disableFailover();
                return null;
            }
        }

        void disableFailover(){
            serviceIterator=null;
            initType=0;
            initParams=null;
            initRandom=null;
        }

        // engine method
        public void initialize(int keysize,SecureRandom random){
            if(serviceIterator==null){
                spi.initialize(keysize,random);
                return;
            }
            RuntimeException failure=null;
            KeyPairGeneratorSpi mySpi=spi;
            do{
                try{
                    mySpi.initialize(keysize,random);
                    initType=I_SIZE;
                    initKeySize=keysize;
                    initParams=null;
                    initRandom=random;
                    return;
                }catch(RuntimeException e){
                    if(failure==null){
                        failure=e;
                    }
                    mySpi=nextSpi(mySpi,false);
                }
            }while(mySpi!=null);
            throw failure;
        }

        // engine method
        public void initialize(AlgorithmParameterSpec params,
                               SecureRandom random) throws InvalidAlgorithmParameterException{
            if(serviceIterator==null){
                spi.initialize(params,random);
                return;
            }
            Exception failure=null;
            KeyPairGeneratorSpi mySpi=spi;
            do{
                try{
                    mySpi.initialize(params,random);
                    initType=I_PARAMS;
                    initKeySize=0;
                    initParams=params;
                    initRandom=random;
                    return;
                }catch(Exception e){
                    if(failure==null){
                        failure=e;
                    }
                    mySpi=nextSpi(mySpi,false);
                }
            }while(mySpi!=null);
            if(failure instanceof RuntimeException){
                throw (RuntimeException)failure;
            }
            // must be an InvalidAlgorithmParameterException
            throw (InvalidAlgorithmParameterException)failure;
        }

        // engine method
        public KeyPair generateKeyPair(){
            if(serviceIterator==null){
                return spi.generateKeyPair();
            }
            RuntimeException failure=null;
            KeyPairGeneratorSpi mySpi=spi;
            do{
                try{
                    return mySpi.generateKeyPair();
                }catch(RuntimeException e){
                    if(failure==null){
                        failure=e;
                    }
                    mySpi=nextSpi(mySpi,true);
                }
            }while(mySpi!=null);
            throw failure;
        }
    }
}
