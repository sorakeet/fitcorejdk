/**
 * Copyright (c) 2001, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;
import java.util.EventListenerProxy;

public class AWTEventListenerProxy
        extends EventListenerProxy<AWTEventListener>
        implements AWTEventListener{
    private final long eventMask;

    public AWTEventListenerProxy(long eventMask,AWTEventListener listener){
        super(listener);
        this.eventMask=eventMask;
    }

    public void eventDispatched(AWTEvent event){
        getListener().eventDispatched(event);
    }

    public long getEventMask(){
        return this.eventMask;
    }
}
