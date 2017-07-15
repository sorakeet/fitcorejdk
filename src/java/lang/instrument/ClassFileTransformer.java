/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.instrument;

import java.security.ProtectionDomain;

public interface ClassFileTransformer{
    byte[]
    transform(ClassLoader loader,
              String className,
              Class<?> classBeingRedefined,
              ProtectionDomain protectionDomain,
              byte[] classfileBuffer)
            throws IllegalClassFormatException;
}
