/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

import java.math.BigInteger;

public class ECPoint{
    public static final ECPoint POINT_INFINITY=new ECPoint();
    private final BigInteger x;
    private final BigInteger y;

    // private constructor for constructing point at infinity
    private ECPoint(){
        this.x=null;
        this.y=null;
    }

    public ECPoint(BigInteger x,BigInteger y){
        if((x==null)||(y==null)){
            throw new NullPointerException("affine coordinate x or y is null");
        }
        this.x=x;
        this.y=y;
    }

    public BigInteger getAffineX(){
        return x;
    }

    public BigInteger getAffineY(){
        return y;
    }

    public int hashCode(){
        if(this==POINT_INFINITY) return 0;
        return x.hashCode()<<5+y.hashCode();
    }

    public boolean equals(Object obj){
        if(this==obj) return true;
        if(this==POINT_INFINITY) return false;
        if(obj instanceof ECPoint){
            return ((x.equals(((ECPoint)obj).x))&&
                    (y.equals(((ECPoint)obj).y)));
        }
        return false;
    }
}
