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
import java.util.Vector;

public interface RenderedImage{
    Vector<RenderedImage> getSources();

    Object getProperty(String name);

    String[] getPropertyNames();

    ColorModel getColorModel();

    SampleModel getSampleModel();

    int getWidth();

    int getHeight();

    int getMinX();

    int getMinY();

    int getNumXTiles();

    int getNumYTiles();

    int getMinTileX();

    int getMinTileY();

    int getTileWidth();

    int getTileHeight();

    int getTileGridXOffset();

    int getTileGridYOffset();

    Raster getTile(int tileX,int tileY);

    Raster getData();

    Raster getData(Rectangle rect);

    WritableRaster copyData(WritableRaster raster);
}
