/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public abstract class FocusManager extends DefaultKeyboardFocusManager{
    public static final String FOCUS_MANAGER_CLASS_PROPERTY=
            "FocusManagerClassName";
    private static boolean enabled=true;

    public static FocusManager getCurrentManager(){
        KeyboardFocusManager manager=
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if(manager instanceof FocusManager){
            return (FocusManager)manager;
        }else{
            return new DelegatingDefaultFocusManager(manager);
        }
    }

    public static void setCurrentManager(FocusManager aFocusManager)
            throws SecurityException{
        // Note: This method is not backward-compatible with 1.3 and earlier
        // releases. It now throws a SecurityException in an applet, whereas
        // in previous releases, it did not. This issue was discussed at
        // length, and ultimately approved by Hans.
        KeyboardFocusManager toSet=
                (aFocusManager instanceof DelegatingDefaultFocusManager)
                        ?((DelegatingDefaultFocusManager)aFocusManager).getDelegate()
                        :aFocusManager;
        KeyboardFocusManager.setCurrentKeyboardFocusManager(toSet);
    }

    @Deprecated
    public static void disableSwingFocusManager(){
        if(enabled){
            enabled=false;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    setDefaultFocusTraversalPolicy(
                            new DefaultFocusTraversalPolicy());
        }
    }

    @Deprecated
    public static boolean isFocusManagerEnabled(){
        return enabled;
    }
}
