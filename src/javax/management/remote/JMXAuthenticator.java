/**
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import javax.security.auth.Subject;

public interface JMXAuthenticator{
    public Subject authenticate(Object credentials);
}
