/**
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface TextListener extends EventListener{
    public void textValueChanged(TextEvent e);
}
