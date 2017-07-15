/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

import java.security.*;

public class CertPathBuilder{
    private static final String CPB_TYPE="certpathbuilder.type";
    private final CertPathBuilderSpi builderSpi;
    private final Provider provider;
    private final String algorithm;

    protected CertPathBuilder(CertPathBuilderSpi builderSpi,Provider provider,
                              String algorithm){
        this.builderSpi=builderSpi;
        this.provider=provider;
        this.algorithm=algorithm;
    }

    public static CertPathBuilder getInstance(String algorithm)
            throws NoSuchAlgorithmException{
        Instance instance=GetInstance.getInstance("CertPathBuilder",
                CertPathBuilderSpi.class,algorithm);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
                instance.provider,algorithm);
    }

    public static CertPathBuilder getInstance(String algorithm,String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException{
        Instance instance=GetInstance.getInstance("CertPathBuilder",
                CertPathBuilderSpi.class,algorithm,provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
                instance.provider,algorithm);
    }

    public static CertPathBuilder getInstance(String algorithm,
                                              Provider provider) throws NoSuchAlgorithmException{
        Instance instance=GetInstance.getInstance("CertPathBuilder",
                CertPathBuilderSpi.class,algorithm,provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
                instance.provider,algorithm);
    }

    public final static String getDefaultType(){
        String cpbtype=
                AccessController.doPrivileged(new PrivilegedAction<String>(){
                    public String run(){
                        return Security.getProperty(CPB_TYPE);
                    }
                });
        return (cpbtype==null)?"PKIX":cpbtype;
    }

    public final Provider getProvider(){
        return this.provider;
    }

    public final String getAlgorithm(){
        return this.algorithm;
    }

    public final CertPathBuilderResult build(CertPathParameters params)
            throws CertPathBuilderException, InvalidAlgorithmParameterException{
        return builderSpi.engineBuild(params);
    }

    public final CertPathChecker getRevocationChecker(){
        return builderSpi.engineGetRevocationChecker();
    }
}
