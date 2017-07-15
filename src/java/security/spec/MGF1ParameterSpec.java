/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

public class MGF1ParameterSpec implements AlgorithmParameterSpec{
    public static final MGF1ParameterSpec SHA1=
            new MGF1ParameterSpec("SHA-1");
    public static final MGF1ParameterSpec SHA224=
            new MGF1ParameterSpec("SHA-224");
    public static final MGF1ParameterSpec SHA256=
            new MGF1ParameterSpec("SHA-256");
    public static final MGF1ParameterSpec SHA384=
            new MGF1ParameterSpec("SHA-384");
    public static final MGF1ParameterSpec SHA512=
            new MGF1ParameterSpec("SHA-512");
    private String mdName;

    public MGF1ParameterSpec(String mdName){
        if(mdName==null){
            throw new NullPointerException("digest algorithm is null");
        }
        this.mdName=mdName;
    }

    public String getDigestAlgorithm(){
        return mdName;
    }
}
