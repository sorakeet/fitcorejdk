/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.beans.DesignMode;
import java.beans.Visibility;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

@SuppressWarnings("rawtypes")
public interface BeanContext extends BeanContextChild, Collection, DesignMode, Visibility{
    public static final Object globalHierarchyLock=new Object();

    Object instantiateChild(String beanName) throws IOException, ClassNotFoundException;

    InputStream getResourceAsStream(String name,BeanContextChild bcc) throws IllegalArgumentException;

    URL getResource(String name,BeanContextChild bcc) throws IllegalArgumentException;

    void addBeanContextMembershipListener(BeanContextMembershipListener bcml);

    void removeBeanContextMembershipListener(BeanContextMembershipListener bcml);
}
