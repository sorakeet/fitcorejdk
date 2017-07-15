/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public interface Certificate{
    public abstract Principal getGuarantor();

    public abstract Principal getPrincipal();

    public abstract PublicKey getPublicKey();

    public abstract void encode(OutputStream stream)
            throws KeyException, IOException;

    public abstract void decode(InputStream stream)
            throws KeyException, IOException;

    public abstract String getFormat();

    public String toString(boolean detailed);
}
