/**
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface MemoryManagerMXBean extends PlatformManagedObject{
    public String getName();

    public boolean isValid();

    public String[] getMemoryPoolNames();
}
