/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public interface CompositeContext{
    public void dispose();

    public void compose(Raster src,
                        Raster dstIn,
                        WritableRaster dstOut);
}
