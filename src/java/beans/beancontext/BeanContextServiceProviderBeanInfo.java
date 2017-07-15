/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.beans.BeanInfo;

public interface BeanContextServiceProviderBeanInfo extends BeanInfo{
    BeanInfo[] getServicesBeanInfo();
}
