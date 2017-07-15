/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

abstract class MultipleGradientPaintContext implements PaintContext{
    protected static final int GRADIENT_SIZE=256;
    protected static final int GRADIENT_SIZE_INDEX=GRADIENT_SIZE-1;
    private static final int SRGBtoLinearRGB[]=new int[256];
    private static final int LinearRGBtoSRGB[]=new int[256];
    private static final int MAX_GRADIENT_ARRAY_SIZE=5000;
    protected static ColorModel cachedModel;
    protected static WeakReference<Raster> cached;
    private static ColorModel xrgbmodel=
            new DirectColorModel(24,0x00ff0000,0x0000ff00,0x000000ff);

    static{
        // build the tables
        for(int k=0;k<256;k++){
            SRGBtoLinearRGB[k]=convertSRGBtoLinearRGB(k);
            LinearRGBtoSRGB[k]=convertLinearRGBtoSRGB(k);
        }
    }

    protected ColorModel model;
    protected Raster saved;
    protected CycleMethod cycleMethod;
    protected ColorSpaceType colorSpace;
    protected float a00, a01, a10, a11, a02, a12;
    protected boolean isSimpleLookup;
    protected int fastGradientArraySize;
    protected int[] gradient;
    private int[][] gradients;
    private float[] normalizedIntervals;
    private float[] fractions;
    private int transparencyTest;

    protected MultipleGradientPaintContext(MultipleGradientPaint mgp,
                                           ColorModel cm,
                                           Rectangle deviceBounds,
                                           Rectangle2D userBounds,
                                           AffineTransform t,
                                           RenderingHints hints,
                                           float[] fractions,
                                           Color[] colors,
                                           CycleMethod cycleMethod,
                                           ColorSpaceType colorSpace){
        if(deviceBounds==null){
            throw new NullPointerException("Device bounds cannot be null");
        }
        if(userBounds==null){
            throw new NullPointerException("User bounds cannot be null");
        }
        if(t==null){
            throw new NullPointerException("Transform cannot be null");
        }
        if(hints==null){
            throw new NullPointerException("RenderingHints cannot be null");
        }
        // The inverse transform is needed to go from device to user space.
        // Get all the components of the inverse transform matrix.
        AffineTransform tInv;
        try{
            // the following assumes that the caller has copied the incoming
            // transform and is not concerned about it being modified
            t.invert();
            tInv=t;
        }catch(NoninvertibleTransformException e){
            // just use identity transform in this case; better to show
            // (incorrect) results than to throw an exception and/or no-op
            tInv=new AffineTransform();
        }
        double m[]=new double[6];
        tInv.getMatrix(m);
        a00=(float)m[0];
        a10=(float)m[1];
        a01=(float)m[2];
        a11=(float)m[3];
        a02=(float)m[4];
        a12=(float)m[5];
        // copy some flags
        this.cycleMethod=cycleMethod;
        this.colorSpace=colorSpace;
        // we can avoid copying this array since we do not modify its values
        this.fractions=fractions;
        // note that only one of these values can ever be non-null (we either
        // store the fast gradient array or the slow one, but never both
        // at the same time)
        int[] gradient=
                (mgp.gradient!=null)?mgp.gradient.get():null;
        int[][] gradients=
                (mgp.gradients!=null)?mgp.gradients.get():null;
        if(gradient==null&&gradients==null){
            // we need to (re)create the appropriate values
            calculateLookupData(colors);
            // now cache the calculated values in the
            // MultipleGradientPaint instance for future use
            mgp.model=this.model;
            mgp.normalizedIntervals=this.normalizedIntervals;
            mgp.isSimpleLookup=this.isSimpleLookup;
            if(isSimpleLookup){
                // only cache the fast array
                mgp.fastGradientArraySize=this.fastGradientArraySize;
                mgp.gradient=new SoftReference<int[]>(this.gradient);
            }else{
                // only cache the slow array
                mgp.gradients=new SoftReference<int[][]>(this.gradients);
            }
        }else{
            // use the values cached in the MultipleGradientPaint instance
            this.model=mgp.model;
            this.normalizedIntervals=mgp.normalizedIntervals;
            this.isSimpleLookup=mgp.isSimpleLookup;
            this.gradient=gradient;
            this.fastGradientArraySize=mgp.fastGradientArraySize;
            this.gradients=gradients;
        }
    }

    private void calculateLookupData(Color[] colors){
        Color[] normalizedColors;
        if(colorSpace==ColorSpaceType.LINEAR_RGB){
            // create a new colors array
            normalizedColors=new Color[colors.length];
            // convert the colors using the lookup table
            for(int i=0;i<colors.length;i++){
                int argb=colors[i].getRGB();
                int a=argb>>>24;
                int r=SRGBtoLinearRGB[(argb>>16)&0xff];
                int g=SRGBtoLinearRGB[(argb>>8)&0xff];
                int b=SRGBtoLinearRGB[(argb)&0xff];
                normalizedColors[i]=new Color(r,g,b,a);
            }
        }else{
            // we can just use this array by reference since we do not
            // modify its values in the case of SRGB
            normalizedColors=colors;
        }
        // this will store the intervals (distances) between gradient stops
        normalizedIntervals=new float[fractions.length-1];
        // convert from fractions into intervals
        for(int i=0;i<normalizedIntervals.length;i++){
            // interval distance is equal to the difference in positions
            normalizedIntervals[i]=this.fractions[i+1]-this.fractions[i];
        }
        // initialize to be fully opaque for ANDing with colors
        transparencyTest=0xff000000;
        // array of interpolation arrays
        gradients=new int[normalizedIntervals.length][];
        // find smallest interval
        float Imin=1;
        for(int i=0;i<normalizedIntervals.length;i++){
            Imin=(Imin>normalizedIntervals[i])?
                    normalizedIntervals[i]:Imin;
        }
        // Estimate the size of the entire gradients array.
        // This is to prevent a tiny interval from causing the size of array
        // to explode.  If the estimated size is too large, break to using
        // separate arrays for each interval, and using an indexing scheme at
        // look-up time.
        int estimatedSize=0;
        for(int i=0;i<normalizedIntervals.length;i++){
            estimatedSize+=(normalizedIntervals[i]/Imin)*GRADIENT_SIZE;
        }
        if(estimatedSize>MAX_GRADIENT_ARRAY_SIZE){
            // slow method
            calculateMultipleArrayGradient(normalizedColors);
        }else{
            // fast method
            calculateSingleArrayGradient(normalizedColors,Imin);
        }
        // use the most "economical" model
        if((transparencyTest>>>24)==0xff){
            model=xrgbmodel;
        }else{
            model=ColorModel.getRGBdefault();
        }
    }

    private void calculateSingleArrayGradient(Color[] colors,float Imin){
        // set the flag so we know later it is a simple (fast) lookup
        isSimpleLookup=true;
        // 2 colors to interpolate
        int rgb1, rgb2;
        //the eventual size of the single array
        int gradientsTot=1;
        // for every interval (transition between 2 colors)
        for(int i=0;i<gradients.length;i++){
            // create an array whose size is based on the ratio to the
            // smallest interval
            int nGradients=(int)((normalizedIntervals[i]/Imin)*255f);
            gradientsTot+=nGradients;
            gradients[i]=new int[nGradients];
            // the 2 colors (keyframes) to interpolate between
            rgb1=colors[i].getRGB();
            rgb2=colors[i+1].getRGB();
            // fill this array with the colors in between rgb1 and rgb2
            interpolate(rgb1,rgb2,gradients[i]);
            // if the colors are opaque, transparency should still
            // be 0xff000000
            transparencyTest&=rgb1;
            transparencyTest&=rgb2;
        }
        // put all gradients in a single array
        gradient=new int[gradientsTot];
        int curOffset=0;
        for(int i=0;i<gradients.length;i++){
            System.arraycopy(gradients[i],0,gradient,
                    curOffset,gradients[i].length);
            curOffset+=gradients[i].length;
        }
        gradient[gradient.length-1]=colors[colors.length-1].getRGB();
        // if interpolation occurred in Linear RGB space, convert the
        // gradients back to sRGB using the lookup table
        if(colorSpace==ColorSpaceType.LINEAR_RGB){
            for(int i=0;i<gradient.length;i++){
                gradient[i]=convertEntireColorLinearRGBtoSRGB(gradient[i]);
            }
        }
        fastGradientArraySize=gradient.length-1;
    }

    private void calculateMultipleArrayGradient(Color[] colors){
        // set the flag so we know later it is a non-simple lookup
        isSimpleLookup=false;
        // 2 colors to interpolate
        int rgb1, rgb2;
        // for every interval (transition between 2 colors)
        for(int i=0;i<gradients.length;i++){
            // create an array of the maximum theoretical size for
            // each interval
            gradients[i]=new int[GRADIENT_SIZE];
            // get the the 2 colors
            rgb1=colors[i].getRGB();
            rgb2=colors[i+1].getRGB();
            // fill this array with the colors in between rgb1 and rgb2
            interpolate(rgb1,rgb2,gradients[i]);
            // if the colors are opaque, transparency should still
            // be 0xff000000
            transparencyTest&=rgb1;
            transparencyTest&=rgb2;
        }
        // if interpolation occurred in Linear RGB space, convert the
        // gradients back to SRGB using the lookup table
        if(colorSpace==ColorSpaceType.LINEAR_RGB){
            for(int j=0;j<gradients.length;j++){
                for(int i=0;i<gradients[j].length;i++){
                    gradients[j][i]=
                            convertEntireColorLinearRGBtoSRGB(gradients[j][i]);
                }
            }
        }
    }

    private void interpolate(int rgb1,int rgb2,int[] output){
        // color components
        int a1, r1, g1, b1, da, dr, dg, db;
        // step between interpolated values
        float stepSize=1.0f/output.length;
        // extract color components from packed integer
        a1=(rgb1>>24)&0xff;
        r1=(rgb1>>16)&0xff;
        g1=(rgb1>>8)&0xff;
        b1=(rgb1)&0xff;
        // calculate the total change in alpha, red, green, blue
        da=((rgb2>>24)&0xff)-a1;
        dr=((rgb2>>16)&0xff)-r1;
        dg=((rgb2>>8)&0xff)-g1;
        db=((rgb2)&0xff)-b1;
        // for each step in the interval calculate the in-between color by
        // multiplying the normalized current position by the total color
        // change (0.5 is added to prevent truncation round-off error)
        for(int i=0;i<output.length;i++){
            output[i]=
                    (((int)((a1+i*da*stepSize)+0.5)<<24))|
                            (((int)((r1+i*dr*stepSize)+0.5)<<16))|
                            (((int)((g1+i*dg*stepSize)+0.5)<<8))|
                            (((int)((b1+i*db*stepSize)+0.5)));
        }
    }

    private int convertEntireColorLinearRGBtoSRGB(int rgb){
        // color components
        int a1, r1, g1, b1;
        // extract red, green, blue components
        a1=(rgb>>24)&0xff;
        r1=(rgb>>16)&0xff;
        g1=(rgb>>8)&0xff;
        b1=(rgb)&0xff;
        // use the lookup table
        r1=LinearRGBtoSRGB[r1];
        g1=LinearRGBtoSRGB[g1];
        b1=LinearRGBtoSRGB[b1];
        // re-compact the components
        return ((a1<<24)|
                (r1<<16)|
                (g1<<8)|
                (b1));
    }

    private static int convertSRGBtoLinearRGB(int color){
        float input, output;
        input=color/255.0f;
        if(input<=0.04045f){
            output=input/12.92f;
        }else{
            output=(float)Math.pow((input+0.055)/1.055,2.4);
        }
        return Math.round(output*255.0f);
    }

    private static int convertLinearRGBtoSRGB(int color){
        float input, output;
        input=color/255.0f;
        if(input<=0.0031308){
            output=input*12.92f;
        }else{
            output=(1.055f*
                    ((float)Math.pow(input,(1.0/2.4))))-0.055f;
        }
        return Math.round(output*255.0f);
    }

    protected final int indexIntoGradientsArrays(float position){
        // first, manipulate position value depending on the cycle method
        if(cycleMethod==CycleMethod.NO_CYCLE){
            if(position>1){
                // upper bound is 1
                position=1;
            }else if(position<0){
                // lower bound is 0
                position=0;
            }
        }else if(cycleMethod==CycleMethod.REPEAT){
            // get the fractional part
            // (modulo behavior discards integer component)
            position=position-(int)position;
            //position should now be between -1 and 1
            if(position<0){
                // force it to be in the range 0-1
                position=position+1;
            }
        }else{ // cycleMethod == CycleMethod.REFLECT
            if(position<0){
                // take absolute value
                position=-position;
            }
            // get the integer part
            int part=(int)position;
            // get the fractional part
            position=position-part;
            if((part&1)==1){
                // integer part is odd, get reflected color instead
                position=1-position;
            }
        }
        // now, get the color based on this 0-1 position...
        if(isSimpleLookup){
            // easy to compute: just scale index by array size
            return gradient[(int)(position*fastGradientArraySize)];
        }else{
            // more complicated computation, to save space
            // for all the gradient interval arrays
            for(int i=0;i<gradients.length;i++){
                if(position<fractions[i+1]){
                    // this is the array we want
                    float delta=position-fractions[i];
                    // this is the interval we want
                    int index=(int)((delta/normalizedIntervals[i])
                            *(GRADIENT_SIZE_INDEX));
                    return gradients[i][index];
                }
            }
        }
        return gradients[gradients.length-1][GRADIENT_SIZE_INDEX];
    }

    public final void dispose(){
        if(saved!=null){
            putCachedRaster(model,saved);
            saved=null;
        }
    }

    private static synchronized void putCachedRaster(ColorModel cm,
                                                     Raster ras){
        if(cached!=null){
            Raster cras=(Raster)cached.get();
            if(cras!=null){
                int cw=cras.getWidth();
                int ch=cras.getHeight();
                int iw=ras.getWidth();
                int ih=ras.getHeight();
                if(cw>=iw&&ch>=ih){
                    return;
                }
                if(cw*ch>=iw*ih){
                    return;
                }
            }
        }
        cachedModel=cm;
        cached=new WeakReference<Raster>(ras);
    }

    public final ColorModel getColorModel(){
        return model;
    }

    public final Raster getRaster(int x,int y,int w,int h){
        // If working raster is big enough, reuse it. Otherwise,
        // build a large enough new one.
        Raster raster=saved;
        if(raster==null||
                raster.getWidth()<w||raster.getHeight()<h){
            raster=getCachedRaster(model,w,h);
            saved=raster;
        }
        // Access raster internal int array. Because we use a DirectColorModel,
        // we know the DataBuffer is of type DataBufferInt and the SampleModel
        // is SinglePixelPackedSampleModel.
        // Adjust for initial offset in DataBuffer and also for the scanline
        // stride.
        // These calls make the DataBuffer non-acceleratable, but the
        // Raster is never Stable long enough to accelerate anyway...
        DataBufferInt rasterDB=(DataBufferInt)raster.getDataBuffer();
        int[] pixels=rasterDB.getData(0);
        int off=rasterDB.getOffset();
        int scanlineStride=((SinglePixelPackedSampleModel)
                raster.getSampleModel()).getScanlineStride();
        int adjust=scanlineStride-w;
        fillRaster(pixels,off,adjust,x,y,w,h); // delegate to subclass
        return raster;
    }

    protected abstract void fillRaster(int pixels[],int off,int adjust,
                                       int x,int y,int w,int h);

    private static synchronized Raster getCachedRaster(ColorModel cm,
                                                       int w,int h){
        if(cm==cachedModel){
            if(cached!=null){
                Raster ras=(Raster)cached.get();
                if(ras!=null&&
                        ras.getWidth()>=w&&
                        ras.getHeight()>=h){
                    cached=null;
                    return ras;
                }
            }
        }
        return cm.createCompatibleWritableRaster(w,h);
    }
}
