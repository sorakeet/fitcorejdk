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
/**
 * JPEGImageEncoder Interface
 * <p>
 * JPEGImageEncoder compresses images into JPEG data streams and
 * writes the JPEG stream to an OutputStream.  Image data that is to
 * be encoded can be passed in as a Raster of image data or as a
 * BufferedImage.  Encoding or the image data into the output JPEG
 * stream is controlled by the parameters setting found in the
 * JPEGEncodeParam object.<P>
 * <p>
 * ColorSpace comments: First off JPEG by specification is color
 * blind!  That said, this interface will perform some color space
 * conversion in the name of better compression ratios.  There is no
 * explicit mechanism in the JPEGEncodeParam interface for controlling
 * the Encoded ColorSpace of the data when it is written to the JPEG
 * data stream.  If an approriate colorspace setting is not already
 * defined it is recommended that colorspace unknown is used.  Some
 * updates to the standard color space designations have been made to
 * allow this decoder to handle alpha channels.  See the
 * JPEGEncodeParam description for more details on additional color
 * space designations ( @see JPEGEncodeParam ).<P>
 * <p>
 * This encoder will process interchange, and abbreviated JPEG
 * streams.
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;

public interface JPEGImageEncoder{
    public OutputStream getOutputStream();

    public JPEGEncodeParam getJPEGEncodeParam();

    public void setJPEGEncodeParam(JPEGEncodeParam jep);

    public JPEGEncodeParam getDefaultJPEGEncodeParam(BufferedImage bi)
            throws ImageFormatException;

    public void encode(BufferedImage bi)
            throws IOException, ImageFormatException;

    public void encode(BufferedImage bi,JPEGEncodeParam jep)
            throws IOException, ImageFormatException;

    public int getDefaultColorId(ColorModel cm);

    public JPEGEncodeParam getDefaultJPEGEncodeParam(Raster ras,int colorID)
            throws ImageFormatException;

    public JPEGEncodeParam getDefaultJPEGEncodeParam(int numBands,
                                                     int colorID)
            throws ImageFormatException;

    public JPEGEncodeParam getDefaultJPEGEncodeParam(JPEGDecodeParam jdp)
            throws ImageFormatException;

    public void encode(Raster ras)
            throws IOException, ImageFormatException;

    public void encode(Raster ras,JPEGEncodeParam jep)
            throws IOException, ImageFormatException;
}
