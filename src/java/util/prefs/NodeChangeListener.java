/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

public interface NodeChangeListener extends java.util.EventListener{
    void childAdded(NodeChangeEvent evt);

    void childRemoved(NodeChangeEvent evt);
}
