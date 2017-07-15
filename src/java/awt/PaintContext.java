/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.image.ColorModel;
import java.awt.image.Raster;

public interface PaintContext{
    public void dispose();

    ColorModel getColorModel();

    Raster getRaster(int x,
                     int y,
                     int w,
                     int h);
}
