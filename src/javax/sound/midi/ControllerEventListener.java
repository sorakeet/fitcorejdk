/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

import java.util.EventListener;

public interface ControllerEventListener extends EventListener{
    public void controlChange(ShortMessage event);
}
