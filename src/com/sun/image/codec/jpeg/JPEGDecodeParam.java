/**
 *
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) 1997-1998 Eastman Kodak Company.                 ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package com.sun.image.codec.jpeg;

public interface JPEGDecodeParam extends Cloneable{
    public final static int COLOR_ID_UNKNOWN=0;
    public final static int COLOR_ID_GRAY=1;
    public final static int COLOR_ID_RGB=2;
    public final static int COLOR_ID_YCbCr=3;
    public final static int COLOR_ID_CMYK=4;
    public final static int COLOR_ID_PYCC=5;
    public final static int COLOR_ID_RGBA=6;
    public final static int COLOR_ID_YCbCrA=7;
    public final static int COLOR_ID_RGBA_INVERTED=8;
    public final static int COLOR_ID_YCbCrA_INVERTED=9;
    public final static int COLOR_ID_PYCCA=10;
    public final static int COLOR_ID_YCCK=11;
    final static int NUM_COLOR_ID=12;
    final static int NUM_TABLES=4;
    public final static int DENSITY_UNIT_ASPECT_RATIO=0;
    public final static int DENSITY_UNIT_DOTS_INCH=1;
    public final static int DENSITY_UNIT_DOTS_CM=2;
    final static int NUM_DENSITY_UNIT=3;
    public final static int APP0_MARKER=0xE0;
    public final static int APP1_MARKER=0xE1;
    public final static int APP2_MARKER=0xE2;
    public final static int APP3_MARKER=0xE3;
    public final static int APP4_MARKER=0xE4;
    public final static int APP5_MARKER=0xE5;
    public final static int APP6_MARKER=0xE6;
    public final static int APP7_MARKER=0xE7;
    public final static int APP8_MARKER=0xE8;
    public final static int APP9_MARKER=0xE9;
    public final static int APPA_MARKER=0xEA;
    public final static int APPB_MARKER=0xEB;
    public final static int APPC_MARKER=0xEC;
    public final static int APPD_MARKER=0xED;
    public final static int APPE_MARKER=0xEE;
    public final static int APPF_MARKER=0xEF;
    public final static int COMMENT_MARKER=0XFE;

    public Object clone();

    public int getWidth();

    public int getHeight();

    public int getHorizontalSubsampling(int component);

    public int getVerticalSubsampling(int component);

    public JPEGQTable getQTable(int tableNum);

    public JPEGQTable getQTableForComponent(int component);

    public JPEGHuffmanTable getDCHuffmanTable(int tableNum);

    public JPEGHuffmanTable getDCHuffmanTableForComponent(int component);

    public JPEGHuffmanTable getACHuffmanTable(int tableNum);

    public JPEGHuffmanTable getACHuffmanTableForComponent(int component);

    public int getDCHuffmanComponentMapping(int component);

    public int getACHuffmanComponentMapping(int component);

    public int getQTableComponentMapping(int component);

    public boolean isImageInfoValid();

    public boolean isTableInfoValid();

    public boolean getMarker(int marker);

    public byte[][] getMarkerData(int marker);

    public int getEncodedColorID();

    public int getNumComponents();

    public int getRestartInterval();

    public int getDensityUnit();

    public int getXDensity();

    public int getYDensity();
}
