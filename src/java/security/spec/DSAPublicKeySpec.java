/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

import java.math.BigInteger;

public class DSAPublicKeySpec implements KeySpec{
    private BigInteger y;
    private BigInteger p;
    private BigInteger q;
    private BigInteger g;

    public DSAPublicKeySpec(BigInteger y,BigInteger p,BigInteger q,
                            BigInteger g){
        this.y=y;
        this.p=p;
        this.q=q;
        this.g=g;
    }

    public BigInteger getY(){
        return this.y;
    }

    public BigInteger getP(){
        return this.p;
    }

    public BigInteger getQ(){
        return this.q;
    }

    public BigInteger getG(){
        return this.g;
    }
}
