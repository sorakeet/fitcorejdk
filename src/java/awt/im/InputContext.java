/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.im;

import java.awt.*;
import java.beans.Transient;
import java.lang.Character.Subset;
import java.util.Locale;

public class InputContext{
    protected InputContext(){
        // real implementation is in sun.awt.im.InputContext
    }

    public static InputContext getInstance(){
        return new sun.awt.im.InputMethodContext();
    }

    public boolean selectInputMethod(Locale locale){
        // real implementation is in sun.awt.im.InputContext
        return false;
    }

    public Locale getLocale(){
        // real implementation is in sun.awt.im.InputContext
        return null;
    }

    public void setCharacterSubsets(Subset[] subsets){
        // real implementation is in sun.awt.im.InputContext
    }

    @Transient
    public boolean isCompositionEnabled(){
        // real implementation is in sun.awt.im.InputContext
        return false;
    }

    public void setCompositionEnabled(boolean enable){
        // real implementation is in sun.awt.im.InputContext
    }

    public void reconvert(){
        // real implementation is in sun.awt.im.InputContext
    }

    public void dispatchEvent(AWTEvent event){
        // real implementation is in sun.awt.im.InputContext
    }

    public void removeNotify(Component client){
        // real implementation is in sun.awt.im.InputContext
    }

    public void endComposition(){
        // real implementation is in sun.awt.im.InputContext
    }

    public void dispose(){
        // real implementation is in sun.awt.im.InputContext
    }

    public Object getInputMethodControlObject(){
        // real implementation is in sun.awt.im.InputContext
        return null;
    }
}
