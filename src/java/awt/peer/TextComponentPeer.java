/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.im.InputMethodRequests;

public interface TextComponentPeer extends ComponentPeer{
    void setEditable(boolean editable);

    String getText();

    void setText(String text);

    int getSelectionStart();

    int getSelectionEnd();

    void select(int selStart,int selEnd);

    int getCaretPosition();

    void setCaretPosition(int pos);

    InputMethodRequests getInputMethodRequests();
}
