/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.util.EventListener;

public interface BeanContextMembershipListener extends EventListener{
    void childrenAdded(BeanContextMembershipEvent bcme);

    void childrenRemoved(BeanContextMembershipEvent bcme);
}
