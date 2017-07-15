/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface RasterOp{
    public WritableRaster filter(Raster src,WritableRaster dest);

    public Rectangle2D getBounds2D(Raster src);

    public WritableRaster createCompatibleDestRaster(Raster src);

    public Point2D getPoint2D(Point2D srcPt,Point2D dstPt);

    public RenderingHints getRenderingHints();
}
