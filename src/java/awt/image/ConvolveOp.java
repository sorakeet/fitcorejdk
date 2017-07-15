/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import sun.awt.image.ImagingLib;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.annotation.Native;

public class ConvolveOp implements BufferedImageOp, RasterOp{
    @Native
    public static final int EDGE_ZERO_FILL=0;
    @Native
    public static final int EDGE_NO_OP=1;
    Kernel kernel;
    int edgeHint;
    RenderingHints hints;

    public ConvolveOp(Kernel kernel,int edgeCondition,RenderingHints hints){
        this.kernel=kernel;
        this.edgeHint=edgeCondition;
        this.hints=hints;
    }

    public ConvolveOp(Kernel kernel){
        this.kernel=kernel;
        this.edgeHint=EDGE_ZERO_FILL;
    }

    public int getEdgeCondition(){
        return edgeHint;
    }

    public final Kernel getKernel(){
        return (Kernel)kernel.clone();
    }

    public final BufferedImage filter(BufferedImage src,BufferedImage dst){
        if(src==null){
            throw new NullPointerException("src image is null");
        }
        if(src==dst){
            throw new IllegalArgumentException("src image cannot be the "+
                    "same as the dst image");
        }
        boolean needToConvert=false;
        ColorModel srcCM=src.getColorModel();
        ColorModel dstCM;
        BufferedImage origDst=dst;
        // Can't convolve an IndexColorModel.  Need to expand it
        if(srcCM instanceof IndexColorModel){
            IndexColorModel icm=(IndexColorModel)srcCM;
            src=icm.convertToIntDiscrete(src.getRaster(),false);
            srcCM=src.getColorModel();
        }
        if(dst==null){
            dst=createCompatibleDestImage(src,null);
            dstCM=srcCM;
            origDst=dst;
        }else{
            dstCM=dst.getColorModel();
            if(srcCM.getColorSpace().getType()!=
                    dstCM.getColorSpace().getType()){
                needToConvert=true;
                dst=createCompatibleDestImage(src,null);
                dstCM=dst.getColorModel();
            }else if(dstCM instanceof IndexColorModel){
                dst=createCompatibleDestImage(src,null);
                dstCM=dst.getColorModel();
            }
        }
        if(ImagingLib.filter(this,src,dst)==null){
            throw new ImagingOpException("Unable to convolve src image");
        }
        if(needToConvert){
            ColorConvertOp ccop=new ColorConvertOp(hints);
            ccop.filter(dst,origDst);
        }else if(origDst!=dst){
            Graphics2D g=origDst.createGraphics();
            try{
                g.drawImage(dst,0,0,null);
            }finally{
                g.dispose();
            }
        }
        return origDst;
    }

    public final Rectangle2D getBounds2D(BufferedImage src){
        return getBounds2D(src.getRaster());
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel destCM){
        BufferedImage image;
        int w=src.getWidth();
        int h=src.getHeight();
        WritableRaster wr=null;
        if(destCM==null){
            destCM=src.getColorModel();
            // Not much support for ICM
            if(destCM instanceof IndexColorModel){
                destCM=ColorModel.getRGBdefault();
            }else{
                /** Create destination image as similar to the source
                 *  as it possible...
                 */
                wr=src.getData().createCompatibleWritableRaster(w,h);
            }
        }
        if(wr==null){
            /** This is the case when destination color model
             * was explicitly specified (and it may be not compatible
             * with source raster structure) or source is indexed image.
             * We should use destination color model to create compatible
             * destination raster here.
             */
            wr=destCM.createCompatibleWritableRaster(w,h);
        }
        image=new BufferedImage(destCM,wr,
                destCM.isAlphaPremultiplied(),null);
        return image;
    }

    public final Point2D getPoint2D(Point2D srcPt,Point2D dstPt){
        if(dstPt==null){
            dstPt=new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(),srcPt.getY());
        return dstPt;
    }

    public final RenderingHints getRenderingHints(){
        return hints;
    }

    public final WritableRaster filter(Raster src,WritableRaster dst){
        if(dst==null){
            dst=createCompatibleDestRaster(src);
        }else if(src==dst){
            throw new IllegalArgumentException("src image cannot be the "+
                    "same as the dst image");
        }else if(src.getNumBands()!=dst.getNumBands()){
            throw new ImagingOpException("Different number of bands in src "+
                    " and dst Rasters");
        }
        if(ImagingLib.filter(this,src,dst)==null){
            throw new ImagingOpException("Unable to convolve src image");
        }
        return dst;
    }

    public final Rectangle2D getBounds2D(Raster src){
        return src.getBounds();
    }

    public WritableRaster createCompatibleDestRaster(Raster src){
        return src.createCompatibleWritableRaster();
    }
}
