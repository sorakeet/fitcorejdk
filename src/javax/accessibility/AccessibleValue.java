/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleValue{
    public Number getCurrentAccessibleValue();

    public boolean setCurrentAccessibleValue(Number n);
//    /**
//     * Get the description of the value of this object.
//     *
//     * @return description of the value of the object
//     */
//    public String getAccessibleValueDescription();

    public Number getMinimumAccessibleValue();

    public Number getMaximumAccessibleValue();
}
