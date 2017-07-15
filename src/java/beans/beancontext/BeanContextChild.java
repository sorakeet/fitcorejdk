/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

public interface BeanContextChild{
    BeanContext getBeanContext();

    void setBeanContext(BeanContext bc) throws PropertyVetoException;

    void addPropertyChangeListener(String name,PropertyChangeListener pcl);

    void removePropertyChangeListener(String name,PropertyChangeListener pcl);

    void addVetoableChangeListener(String name,VetoableChangeListener vcl);

    void removeVetoableChangeListener(String name,VetoableChangeListener vcl);
}
