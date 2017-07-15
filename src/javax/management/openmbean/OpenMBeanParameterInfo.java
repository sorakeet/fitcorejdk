/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import java.util.Set;
// jmx import
//

public interface OpenMBeanParameterInfo{
    // Re-declares methods that are in class MBeanParameterInfo of JMX 1.0
    // (these will be removed when MBeanParameterInfo is made a parent interface of this interface)

    public String getDescription();

    public String getName();
    // Now declares methods that are specific to open MBeans
    //

    public OpenType<?> getOpenType();

    public Object getDefaultValue();

    public Set<?> getLegalValues();

    public Comparable<?> getMinValue();

    public Comparable<?> getMaxValue();

    public boolean hasDefaultValue();

    public boolean hasLegalValues();

    public boolean hasMinValue();

    public boolean hasMaxValue();

    public boolean isValue(Object obj);

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
