/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

public interface BeanContextServicesListener extends BeanContextServiceRevokedListener{
    void serviceAvailable(BeanContextServiceAvailableEvent bcsae);
}
