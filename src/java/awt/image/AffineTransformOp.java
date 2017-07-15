/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import sun.awt.image.ImagingLib;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.annotation.Native;

public class AffineTransformOp implements BufferedImageOp, RasterOp{
    @Native
    public static final int TYPE_NEAREST_NEIGHBOR=1;
    @Native
    public static final int TYPE_BILINEAR=2;
    @Native
    public static final int TYPE_BICUBIC=3;
    RenderingHints hints;
    int interpolationType=TYPE_NEAREST_NEIGHBOR;
    private AffineTransform xform;

    public AffineTransformOp(AffineTransform xform,RenderingHints hints){
        validateTransform(xform);
        this.xform=(AffineTransform)xform.clone();
        this.hints=hints;
        if(hints!=null){
            Object value=hints.get(hints.KEY_INTERPOLATION);
            if(value==null){
                value=hints.get(hints.KEY_RENDERING);
                if(value==hints.VALUE_RENDER_SPEED){
                    interpolationType=TYPE_NEAREST_NEIGHBOR;
                }else if(value==hints.VALUE_RENDER_QUALITY){
                    interpolationType=TYPE_BILINEAR;
                }
            }else if(value==hints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                interpolationType=TYPE_NEAREST_NEIGHBOR;
            }else if(value==hints.VALUE_INTERPOLATION_BILINEAR){
                interpolationType=TYPE_BILINEAR;
            }else if(value==hints.VALUE_INTERPOLATION_BICUBIC){
                interpolationType=TYPE_BICUBIC;
            }
        }else{
            interpolationType=TYPE_NEAREST_NEIGHBOR;
        }
    }

    // We need to be able to invert the transform if we want to
    // transform the image.  If the determinant of the matrix is 0,
    // then we can't invert the transform.
    void validateTransform(AffineTransform xform){
        if(Math.abs(xform.getDeterminant())<=Double.MIN_VALUE){
            throw new ImagingOpException("Unable to invert transform "+xform);
        }
    }

    public AffineTransformOp(AffineTransform xform,int interpolationType){
        validateTransform(xform);
        this.xform=(AffineTransform)xform.clone();
        switch(interpolationType){
            case TYPE_NEAREST_NEIGHBOR:
            case TYPE_BILINEAR:
            case TYPE_BICUBIC:
                break;
            default:
                throw new IllegalArgumentException("Unknown interpolation type: "+
                        interpolationType);
        }
        this.interpolationType=interpolationType;
    }

    public final int getInterpolationType(){
        return interpolationType;
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
        if(dst==null){
            dst=createCompatibleDestImage(src,null);
            dstCM=srcCM;
            origDst=dst;
        }else{
            dstCM=dst.getColorModel();
            if(srcCM.getColorSpace().getType()!=
                    dstCM.getColorSpace().getType()){
                int type=xform.getType();
                boolean needTrans=((type&
                        (xform.TYPE_MASK_ROTATION|
                                xform.TYPE_GENERAL_TRANSFORM))
                        !=0);
                if(!needTrans&&type!=xform.TYPE_TRANSLATION&&type!=xform.TYPE_IDENTITY){
                    double[] mtx=new double[4];
                    xform.getMatrix(mtx);
                    // Check out the matrix.  A non-integral scale will force ARGB
                    // since the edge conditions can't be guaranteed.
                    needTrans=(mtx[0]!=(int)mtx[0]||mtx[3]!=(int)mtx[3]);
                }
                if(needTrans&&
                        srcCM.getTransparency()==Transparency.OPAQUE){
                    // Need to convert first
                    ColorConvertOp ccop=new ColorConvertOp(hints);
                    BufferedImage tmpSrc=null;
                    int sw=src.getWidth();
                    int sh=src.getHeight();
                    if(dstCM.getTransparency()==Transparency.OPAQUE){
                        tmpSrc=new BufferedImage(sw,sh,
                                BufferedImage.TYPE_INT_ARGB);
                    }else{
                        WritableRaster r=
                                dstCM.createCompatibleWritableRaster(sw,sh);
                        tmpSrc=new BufferedImage(dstCM,r,
                                dstCM.isAlphaPremultiplied(),
                                null);
                    }
                    src=ccop.filter(src,tmpSrc);
                }else{
                    needToConvert=true;
                    dst=createCompatibleDestImage(src,null);
                }
            }
        }
        if(interpolationType!=TYPE_NEAREST_NEIGHBOR&&
                dst.getColorModel() instanceof IndexColorModel){
            dst=new BufferedImage(dst.getWidth(),dst.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }
        if(ImagingLib.filter(this,src,dst)==null){
            throw new ImagingOpException("Unable to transform src image");
        }
        if(needToConvert){
            ColorConvertOp ccop=new ColorConvertOp(hints);
            ccop.filter(dst,origDst);
        }else if(origDst!=dst){
            Graphics2D g=origDst.createGraphics();
            try{
                g.setComposite(AlphaComposite.Src);
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
        Rectangle r=getBounds2D(src).getBounds();
        // If r.x (or r.y) is < 0, then we want to only create an image
        // that is in the positive range.
        // If r.x (or r.y) is > 0, then we need to create an image that
        // includes the translation.
        int w=r.x+r.width;
        int h=r.y+r.height;
        if(w<=0){
            throw new RasterFormatException("Transformed width ("+w+
                    ") is less than or equal to 0.");
        }
        if(h<=0){
            throw new RasterFormatException("Transformed height ("+h+
                    ") is less than or equal to 0.");
        }
        if(destCM==null){
            ColorModel cm=src.getColorModel();
            if(interpolationType!=TYPE_NEAREST_NEIGHBOR&&
                    (cm instanceof IndexColorModel||
                            cm.getTransparency()==Transparency.OPAQUE)){
                image=new BufferedImage(w,h,
                        BufferedImage.TYPE_INT_ARGB);
            }else{
                image=new BufferedImage(cm,
                        src.getRaster().createCompatibleWritableRaster(w,h),
                        cm.isAlphaPremultiplied(),null);
            }
        }else{
            image=new BufferedImage(destCM,
                    destCM.createCompatibleWritableRaster(w,h),
                    destCM.isAlphaPremultiplied(),null);
        }
        return image;
    }

    public final Point2D getPoint2D(Point2D srcPt,Point2D dstPt){
        return xform.transform(srcPt,dstPt);
    }

    public final RenderingHints getRenderingHints(){
        if(hints==null){
            Object val;
            switch(interpolationType){
                case TYPE_NEAREST_NEIGHBOR:
                    val=RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    break;
                case TYPE_BILINEAR:
                    val=RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                    break;
                case TYPE_BICUBIC:
                    val=RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    break;
                default:
                    // Should never get here
                    throw new InternalError("Unknown interpolation type "+
                            interpolationType);
            }
            hints=new RenderingHints(RenderingHints.KEY_INTERPOLATION,val);
        }
        return hints;
    }

    public final WritableRaster filter(Raster src,WritableRaster dst){
        if(src==null){
            throw new NullPointerException("src image is null");
        }
        if(dst==null){
            dst=createCompatibleDestRaster(src);
        }
        if(src==dst){
            throw new IllegalArgumentException("src image cannot be the "+
                    "same as the dst image");
        }
        if(src.getNumBands()!=dst.getNumBands()){
            throw new IllegalArgumentException("Number of src bands ("+
                    src.getNumBands()+
                    ") does not match number of "+
                    " dst bands ("+
                    dst.getNumBands()+")");
        }
        if(ImagingLib.filter(this,src,dst)==null){
            throw new ImagingOpException("Unable to transform src image");
        }
        return dst;
    }

    public final Rectangle2D getBounds2D(Raster src){
        int w=src.getWidth();
        int h=src.getHeight();
        // Get the bounding box of the src and transform the corners
        float[] pts={0,0,w,0,w,h,0,h};
        xform.transform(pts,0,pts,0,4);
        // Get the min, max of the dst
        float fmaxX=pts[0];
        float fmaxY=pts[1];
        float fminX=pts[0];
        float fminY=pts[1];
        for(int i=2;i<8;i+=2){
            if(pts[i]>fmaxX){
                fmaxX=pts[i];
            }else if(pts[i]<fminX){
                fminX=pts[i];
            }
            if(pts[i+1]>fmaxY){
                fmaxY=pts[i+1];
            }else if(pts[i+1]<fminY){
                fminY=pts[i+1];
            }
        }
        return new Rectangle2D.Float(fminX,fminY,fmaxX-fminX,fmaxY-fminY);
    }

    public WritableRaster createCompatibleDestRaster(Raster src){
        Rectangle2D r=getBounds2D(src);
        return src.createCompatibleWritableRaster((int)r.getX(),
                (int)r.getY(),
                (int)r.getWidth(),
                (int)r.getHeight());
    }

    public final AffineTransform getTransform(){
        return (AffineTransform)xform.clone();
    }
}
