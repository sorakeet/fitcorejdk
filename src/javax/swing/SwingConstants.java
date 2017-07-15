/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

public interface SwingConstants{
    public static final int CENTER=0;
    //
    // Box-orientation constant used to specify locations in a box.
    //
    public static final int TOP=1;
    public static final int LEFT=2;
    public static final int BOTTOM=3;
    public static final int RIGHT=4;
    //
    // Compass-direction constants used to specify a position.
    //
    public static final int NORTH=1;
    public static final int NORTH_EAST=2;
    public static final int EAST=3;
    public static final int SOUTH_EAST=4;
    public static final int SOUTH=5;
    public static final int SOUTH_WEST=6;
    public static final int WEST=7;
    public static final int NORTH_WEST=8;
    //
    // These constants specify a horizontal or
    // vertical orientation. For example, they are
    // used by scrollbars and sliders.
    //
    public static final int HORIZONTAL=0;
    public static final int VERTICAL=1;
    //
    // Constants for orientation support, since some languages are
    // left-to-right oriented and some are right-to-left oriented.
    // This orientation is currently used by buttons and labels.
    //
    public static final int LEADING=10;
    public static final int TRAILING=11;
    public static final int NEXT=12;
    public static final int PREVIOUS=13;
}
