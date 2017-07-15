/**
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public interface PropertyChangeListener extends java.util.EventListener{
    void propertyChange(PropertyChangeEvent evt);
}
