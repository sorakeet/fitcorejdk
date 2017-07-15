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

import sun.awt.image.codec.JPEGImageDecoderImpl;
import sun.awt.image.codec.JPEGImageEncoderImpl;
import sun.awt.image.codec.JPEGParam;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.InputStream;
import java.io.OutputStream;

public class JPEGCodec{
    private JPEGCodec(){
    }

    public static JPEGImageDecoder createJPEGDecoder(InputStream src){
        return new JPEGImageDecoderImpl(src);
    }

    public static JPEGImageDecoder createJPEGDecoder(InputStream src,
                                                     JPEGDecodeParam jdp){
        return new JPEGImageDecoderImpl(src,jdp);
    }

    public static JPEGImageEncoder createJPEGEncoder(OutputStream dest){
        return new JPEGImageEncoderImpl(dest);
    }

    public static JPEGImageEncoder createJPEGEncoder(OutputStream dest,
                                                     JPEGEncodeParam jep){
        return new JPEGImageEncoderImpl(dest,jep);
    }

    public static JPEGEncodeParam getDefaultJPEGEncodeParam(BufferedImage bi){
        int colorID=JPEGParam.getDefaultColorId(bi.getColorModel());
        return getDefaultJPEGEncodeParam(bi.getRaster(),colorID);
    }

    public static JPEGEncodeParam getDefaultJPEGEncodeParam(Raster ras,
                                                            int colorID){
        JPEGParam ret=new JPEGParam(colorID,ras.getNumBands());
        ret.setWidth(ras.getWidth());
        ret.setHeight(ras.getHeight());
        return ret;
    }

    public static JPEGEncodeParam getDefaultJPEGEncodeParam(int numBands,
                                                            int colorID)
            throws ImageFormatException{
        return new JPEGParam(colorID,numBands);
    }

    public static JPEGEncodeParam getDefaultJPEGEncodeParam(JPEGDecodeParam jdp)
            throws ImageFormatException{
        return new JPEGParam(jdp);
    }
}
