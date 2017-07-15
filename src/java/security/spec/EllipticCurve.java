/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

import java.math.BigInteger;

public class EllipticCurve{
    private final ECField field;
    private final BigInteger a;
    private final BigInteger b;
    private final byte[] seed;

    public EllipticCurve(ECField field,BigInteger a,
                         BigInteger b){
        this(field,a,b,null);
    }

    public EllipticCurve(ECField field,BigInteger a,
                         BigInteger b,byte[] seed){
        if(field==null){
            throw new NullPointerException("field is null");
        }
        if(a==null){
            throw new NullPointerException("first coefficient is null");
        }
        if(b==null){
            throw new NullPointerException("second coefficient is null");
        }
        checkValidity(field,a,"first coefficient");
        checkValidity(field,b,"second coefficient");
        this.field=field;
        this.a=a;
        this.b=b;
        if(seed!=null){
            this.seed=seed.clone();
        }else{
            this.seed=null;
        }
    }

    // Check coefficient c is a valid element in ECField field.
    private static void checkValidity(ECField field,BigInteger c,
                                      String cName){
        // can only perform check if field is ECFieldFp or ECFieldF2m.
        if(field instanceof ECFieldFp){
            BigInteger p=((ECFieldFp)field).getP();
            if(p.compareTo(c)!=1){
                throw new IllegalArgumentException(cName+" is too large");
            }else if(c.signum()<0){
                throw new IllegalArgumentException(cName+" is negative");
            }
        }else if(field instanceof ECFieldF2m){
            int m=((ECFieldF2m)field).getM();
            if(c.bitLength()>m){
                throw new IllegalArgumentException(cName+" is too large");
            }
        }
    }

    public ECField getField(){
        return field;
    }

    public BigInteger getA(){
        return a;
    }

    public BigInteger getB(){
        return b;
    }

    public byte[] getSeed(){
        if(seed==null) return null;
        else return seed.clone();
    }

    public int hashCode(){
        return (field.hashCode()<<6+
                (a.hashCode()<<4)+
                (b.hashCode()<<2));
    }

    public boolean equals(Object obj){
        if(this==obj) return true;
        if(obj instanceof EllipticCurve){
            EllipticCurve curve=(EllipticCurve)obj;
            if((field.equals(curve.field))&&
                    (a.equals(curve.a))&&
                    (b.equals(curve.b))){
                return true;
            }
        }
        return false;
    }
}
