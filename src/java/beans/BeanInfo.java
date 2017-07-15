/**
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.awt.*;

public interface BeanInfo{
    final static int ICON_COLOR_16x16=1;
    final static int ICON_COLOR_32x32=2;
    final static int ICON_MONO_16x16=3;
    final static int ICON_MONO_32x32=4;

    BeanDescriptor getBeanDescriptor();

    EventSetDescriptor[] getEventSetDescriptors();

    int getDefaultEventIndex();

    PropertyDescriptor[] getPropertyDescriptors();

    int getDefaultPropertyIndex();

    MethodDescriptor[] getMethodDescriptors();

    BeanInfo[] getAdditionalBeanInfo();

    Image getIcon(int iconKind);
}
