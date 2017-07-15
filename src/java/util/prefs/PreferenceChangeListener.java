/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

@FunctionalInterface
public interface PreferenceChangeListener extends java.util.EventListener{
    void preferenceChange(PreferenceChangeEvent evt);
}
