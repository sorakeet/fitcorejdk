/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.util.EventListener;

public interface BeanContextServiceRevokedListener extends EventListener{
    void serviceRevoked(BeanContextServiceRevokedEvent bcsre);
}
