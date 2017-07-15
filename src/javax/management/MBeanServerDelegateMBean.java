/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface MBeanServerDelegateMBean{
    public String getMBeanServerId();

    public String getSpecificationName();

    public String getSpecificationVersion();

    public String getSpecificationVendor();

    public String getImplementationName();

    public String getImplementationVersion();

    public String getImplementationVendor();
}
