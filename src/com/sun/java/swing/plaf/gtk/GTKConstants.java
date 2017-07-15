/**
 * Copyright (c) 2002, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.gtk;

public interface GTKConstants{
    public static final int UNDEFINED=-100;

    public enum IconSize{
        INVALID,
        MENU,
        SMALL_TOOLBAR,
        LARGE_TOOLBAR,
        BUTTON,
        DND,
        DIALOG
    }

    public enum TextDirection{
        NONE,
        LTR,
        RTL
    }

    public enum ShadowType{
        NONE,
        IN,
        OUT,
        ETCHED_IN,
        ETCHED_OUT
    }

    public enum StateType{
        NORMAL,
        ACTIVE,
        PRELIGHT,
        SELECTED,
        INSENSITIVE
    }

    public enum ExpanderStyle{
        COLLAPSED,
        SEMI_COLLAPSED,
        SEMI_EXPANDED,
        EXPANDED,
    }

    public enum PositionType{
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public enum ArrowType{
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public enum Orientation{
        HORIZONTAL,
        VERTICAL
    }
}
