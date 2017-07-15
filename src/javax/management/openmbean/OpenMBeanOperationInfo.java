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

public interface OpenMBeanOperationInfo{
    // Re-declares fields and methods that are in class MBeanOperationInfo of JMX 1.0
    // (fields and methods will be removed when MBeanOperationInfo is made a parent interface of this interface)

    public String getDescription();

    public String getName();

    public MBeanParameterInfo[] getSignature();

    public int getImpact();

    public String getReturnType();
    // Now declares methods that are specific to open MBeans
    //

    public OpenType<?> getReturnOpenType(); // open MBean specific method
    // commodity methods
    //

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
