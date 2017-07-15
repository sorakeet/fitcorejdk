/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.im.spi;

import java.awt.*;
import java.lang.Character.Subset;
import java.util.Locale;

public interface InputMethod{
    public void setInputMethodContext(InputMethodContext context);

    public boolean setLocale(Locale locale);

    public Locale getLocale();

    public void setCharacterSubsets(Subset[] subsets);

    public boolean isCompositionEnabled();

    public void setCompositionEnabled(boolean enable);

    public void reconvert();

    public void dispatchEvent(AWTEvent event);

    public void notifyClientWindowChange(Rectangle bounds);

    public void activate();

    public void deactivate(boolean isTemporary);

    public void hideWindows();

    public void removeNotify();

    public void endComposition();

    public void dispose();

    public Object getControlObject();
}
