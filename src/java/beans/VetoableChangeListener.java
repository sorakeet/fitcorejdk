/**
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public interface VetoableChangeListener extends java.util.EventListener{
    void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException;
}
