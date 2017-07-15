/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

import java.security.*;

public class CertPathValidator{
    private static final String CPV_TYPE="certpathvalidator.type";
    private final CertPathValidatorSpi validatorSpi;
    private final Provider provider;
    private final String algorithm;

    protected CertPathValidator(CertPathValidatorSpi validatorSpi,
                                Provider provider,String algorithm){
        this.validatorSpi=validatorSpi;
        this.provider=provider;
        this.algorithm=algorithm;
    }

    public static CertPathValidator getInstance(String algorithm)
            throws NoSuchAlgorithmException{
        Instance instance=GetInstance.getInstance("CertPathValidator",
                CertPathValidatorSpi.class,algorithm);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
                instance.provider,algorithm);
    }

    public static CertPathValidator getInstance(String algorithm,
                                                String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException{
        Instance instance=GetInstance.getInstance("CertPathValidator",
                CertPathValidatorSpi.class,algorithm,provider);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
                instance.provider,algorithm);
    }

    public static CertPathValidator getInstance(String algorithm,
                                                Provider provider) throws NoSuchAlgorithmException{
        Instance instance=GetInstance.getInstance("CertPathValidator",
                CertPathValidatorSpi.class,algorithm,provider);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
                instance.provider,algorithm);
    }

    public final static String getDefaultType(){
        String cpvtype=
                AccessController.doPrivileged(new PrivilegedAction<String>(){
                    public String run(){
                        return Security.getProperty(CPV_TYPE);
                    }
                });
        return (cpvtype==null)?"PKIX":cpvtype;
    }

    public final Provider getProvider(){
        return this.provider;
    }

    public final String getAlgorithm(){
        return this.algorithm;
    }

    public final CertPathValidatorResult validate(CertPath certPath,
                                                  CertPathParameters params)
            throws CertPathValidatorException, InvalidAlgorithmParameterException{
        return validatorSpi.engineValidate(certPath,params);
    }

    public final CertPathChecker getRevocationChecker(){
        return validatorSpi.engineGetRevocationChecker();
    }
}
