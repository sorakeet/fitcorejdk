/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

public abstract class EncodedKeySpec implements KeySpec{
    private byte[] encodedKey;

    public EncodedKeySpec(byte[] encodedKey){
        this.encodedKey=encodedKey.clone();
    }

    public byte[] getEncoded(){
        return this.encodedKey.clone();
    }

    public abstract String getFormat();
}
