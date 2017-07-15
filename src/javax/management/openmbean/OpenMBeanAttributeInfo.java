/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//
// jmx import
//

public interface OpenMBeanAttributeInfo extends OpenMBeanParameterInfo{
    // Re-declares the methods that are in class MBeanAttributeInfo of JMX 1.0
    // (these will be removed when MBeanAttributeInfo is made a parent interface of this interface)

    public boolean isReadable();

    public boolean isWritable();

    public boolean isIs();
    // commodity methods
    //

    public boolean equals(Object obj);

    public int hashCode();

    public String toString();
    // methods specific to open MBeans are inherited from
    // OpenMBeanParameterInfo
    //
}
