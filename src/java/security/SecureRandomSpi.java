/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

public abstract class SecureRandomSpi implements java.io.Serializable{
    private static final long serialVersionUID=-2991854161009191830L;

    protected abstract void engineSetSeed(byte[] seed);

    protected abstract void engineNextBytes(byte[] bytes);

    protected abstract byte[] engineGenerateSeed(int numBytes);
}
