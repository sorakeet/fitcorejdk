/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

interface EventFilter{
    FilterAction acceptEvent(AWTEvent ev);

    ;

    static enum FilterAction{
        ACCEPT,
        REJECT,
        ACCEPT_IMMEDIATELY
    }
}
