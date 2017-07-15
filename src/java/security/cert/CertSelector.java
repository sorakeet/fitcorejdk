/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

public interface CertSelector extends Cloneable{
    boolean match(Certificate cert);

    Object clone();
}
