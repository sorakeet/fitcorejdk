/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.im.spi;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;

public interface InputMethodContext extends InputMethodRequests{
    public void dispatchInputMethodEvent(int id,
                                         AttributedCharacterIterator text,int committedCharacterCount,
                                         TextHitInfo caret,TextHitInfo visiblePosition);

    public Window createInputMethodWindow(String title,boolean attachToInputContext);

    public JFrame createInputMethodJFrame(String title,boolean attachToInputContext);

    public void enableClientWindowNotification(InputMethod inputMethod,boolean enable);
}
