/**
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface BufferPoolMXBean extends PlatformManagedObject{
    String getName();

    long getCount();

    long getTotalCapacity();

    long getMemoryUsed();
}
