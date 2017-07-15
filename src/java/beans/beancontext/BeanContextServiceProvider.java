/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.util.Iterator;

public interface BeanContextServiceProvider{
    Object getService(BeanContextServices bcs,Object requestor,Class serviceClass,Object serviceSelector);

    public void releaseService(BeanContextServices bcs,Object requestor,Object service);

    Iterator getCurrentServiceSelectors(BeanContextServices bcs,Class serviceClass);
}
