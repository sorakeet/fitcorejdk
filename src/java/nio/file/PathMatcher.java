/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

@FunctionalInterface
public interface PathMatcher{
    boolean matches(Path path);
}
