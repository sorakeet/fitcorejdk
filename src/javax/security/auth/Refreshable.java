/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

public interface Refreshable{
    boolean isCurrent();

    void refresh() throws RefreshFailedException;
}
