/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.im.spi;

import java.awt.*;
import java.util.Locale;

public interface InputMethodDescriptor{
    Locale[] getAvailableLocales() throws AWTException;

    boolean hasDynamicLocaleList();

    String getInputMethodDisplayName(Locale inputLocale,Locale displayLanguage);

    Image getInputMethodIcon(Locale inputLocale);

    InputMethod createInputMethod() throws Exception;
}
