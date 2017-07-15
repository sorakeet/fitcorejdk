/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

public interface RegisterableService{
    void onRegistration(ServiceRegistry registry,Class<?> category);

    void onDeregistration(ServiceRegistry registry,Class<?> category);
}
