/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class KeyFactorySpi{
    protected abstract PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException;

    protected abstract PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException;

    protected abstract <T extends KeySpec>
    T engineGetKeySpec(Key key,Class<T> keySpec)
            throws InvalidKeySpecException;

    protected abstract Key engineTranslateKey(Key key)
            throws InvalidKeyException;
}
