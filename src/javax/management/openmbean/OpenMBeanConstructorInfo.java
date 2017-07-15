/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//
// jmx import
//

import javax.management.MBeanParameterInfo;

public interface OpenMBeanConstructorInfo{
    // Re-declares the methods that are in class MBeanConstructorInfo of JMX 1.0
    // (methods will be removed when MBeanConstructorInfo is made a parent interface of this interface)

    public String getDescription();

    public String getName();

    public MBeanParameterInfo[] getSignature();
    // commodity methods
    //

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
