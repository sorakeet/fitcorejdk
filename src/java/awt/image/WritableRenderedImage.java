/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * ***************************************************************
 * *****************************************************************
 * *****************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997
 * ** As  an unpublished  work pursuant to Title 17 of the United
 * ** States Code.  All rights reserved.
 * *****************************************************************
 * *****************************************************************
 ******************************************************************/
/** ****************************************************************
 ******************************************************************
 ******************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997
 *** As  an unpublished  work pursuant to Title 17 of the United
 *** States Code.  All rights reserved.
 ******************************************************************
 ******************************************************************
 ******************************************************************/
package java.awt.image;

import java.awt.*;

public interface WritableRenderedImage extends RenderedImage{
    public void addTileObserver(TileObserver to);

    public void removeTileObserver(TileObserver to);

    public WritableRaster getWritableTile(int tileX,int tileY);

    public void releaseWritableTile(int tileX,int tileY);

    public boolean isTileWritable(int tileX,int tileY);

    public Point[] getWritableTileIndices();

    public boolean hasTileWriters();

    public void setData(Raster r);
}
