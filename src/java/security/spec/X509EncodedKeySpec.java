/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

public class X509EncodedKeySpec extends EncodedKeySpec{
    public X509EncodedKeySpec(byte[] encodedKey){
        super(encodedKey);
    }

    public byte[] getEncoded(){
        return super.getEncoded();
    }

    public final String getFormat(){
        return "X.509";
    }
}
