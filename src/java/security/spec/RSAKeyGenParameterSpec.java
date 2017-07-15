/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

import java.math.BigInteger;

public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec{
    public static final BigInteger F0=BigInteger.valueOf(3);
    public static final BigInteger F4=BigInteger.valueOf(65537);
    private int keysize;
    private BigInteger publicExponent;

    public RSAKeyGenParameterSpec(int keysize,BigInteger publicExponent){
        this.keysize=keysize;
        this.publicExponent=publicExponent;
    }

    public int getKeysize(){
        return keysize;
    }

    public BigInteger getPublicExponent(){
        return publicExponent;
    }
}
