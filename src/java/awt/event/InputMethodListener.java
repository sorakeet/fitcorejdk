/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface InputMethodListener extends EventListener{
    void inputMethodTextChanged(InputMethodEvent event);

    void caretPositionChanged(InputMethodEvent event);
}
