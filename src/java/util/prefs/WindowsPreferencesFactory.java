/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

class WindowsPreferencesFactory implements PreferencesFactory{
    public Preferences systemRoot(){
        return WindowsPreferences.systemRoot;
    }

    public Preferences userRoot(){
        return WindowsPreferences.userRoot;
    }
}
