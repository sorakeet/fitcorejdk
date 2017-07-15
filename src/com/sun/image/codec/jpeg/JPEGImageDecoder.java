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
 * JPEGImageDecoder Interface
 * <p>
 * JPEGImageDecoder decompresses an JPEG InputStream into a Raster or
 * a BufferedImage depending upon the method invoked. Decoding the
 * JPEG input stream is controlled by the parameters in the
 * JPEGDecodeParam object.  If no JPEGDecodeParam object has been
 * specified then one is created to contain information about a
 * decompressed JPEG stream.<P>
 * <p>
 * The JPEGDecodeParam object is updated with information from the
 * file header during decompression. If the input stream contains
 * tables only information (no image data), the JPEGDecodeParam object
 * will be updated and NULL returned for the output image. If the
 * input stream contains only image data, the parameters and tables in
 * the current JPEGDecodeParam object will be used to decode in
 * decoding the JPEG stream. If no tables are set in the
 * JPEGDecodeParam object, an exception will be thrown.<P>
 * <p>
 * ColorSpace comments: First off JPEG by specification is color
 * blind!  That said, some color space conversion is done in the name
 * of better compression ratios.  If a BufferedImage is requested
 * common color conversions will be applied. Some updates to the
 * standard color space designations have been made to allow this
 * decoder to handle alpha channels.  See the JPEGDecodeParam
 * description for more details on additional color space
 * designations ( @see JPEGDecodeParam ).<P>
 * <p>
 * This decoder can process interchange, abbreviated and progressive
 * jpeg streams.  However, progressive jpeg streams are treated as
 * interchange streams.  They return once with the entire image in the
 * image buffer.
 */

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

public interface JPEGImageDecoder{
    public JPEGDecodeParam getJPEGDecodeParam();

    public void setJPEGDecodeParam(JPEGDecodeParam jdp);

    public InputStream getInputStream();

    public Raster decodeAsRaster()
            throws IOException, ImageFormatException;

    public BufferedImage decodeAsBufferedImage()
            throws IOException, ImageFormatException;
} // end class JPEGImageDecoder
