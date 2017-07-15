/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

final class LinearGradientPaintContext extends MultipleGradientPaintContext{
    private float dgdX, dgdY, gc;

    LinearGradientPaintContext(LinearGradientPaint paint,
                               ColorModel cm,
                               Rectangle deviceBounds,
                               Rectangle2D userBounds,
                               AffineTransform t,
                               RenderingHints hints,
                               Point2D start,
                               Point2D end,
                               float[] fractions,
                               Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace){
        super(paint,cm,deviceBounds,userBounds,t,hints,fractions,
                colors,cycleMethod,colorSpace);
        // A given point in the raster should take on the same color as its
        // projection onto the gradient vector.
        // Thus, we want the projection of the current position vector
        // onto the gradient vector, then normalized with respect to the
        // length of the gradient vector, giving a value which can be mapped
        // into the range 0-1.
        //    projection =
        //        currentVector dot gradientVector / length(gradientVector)
        //    normalized = projection / length(gradientVector)
        float startx=(float)start.getX();
        float starty=(float)start.getY();
        float endx=(float)end.getX();
        float endy=(float)end.getY();
        float dx=endx-startx;  // change in x from start to end
        float dy=endy-starty;  // change in y from start to end
        float dSq=dx*dx+dy*dy; // total distance squared
        // avoid repeated calculations by doing these divides once
        float constX=dx/dSq;
        float constY=dy/dSq;
        // incremental change along gradient for +x
        dgdX=a00*constX+a10*constY;
        // incremental change along gradient for +y
        dgdY=a01*constX+a11*constY;
        // constant, incorporates the translation components from the matrix
        gc=(a02-startx)*constX+(a12-starty)*constY;
    }

    protected void fillRaster(int[] pixels,int off,int adjust,
                              int x,int y,int w,int h){
        // current value for row gradients
        float g=0;
        // used to end iteration on rows
        int rowLimit=off+w;
        // constant which can be pulled out of the inner loop
        float initConst=(dgdX*x)+gc;
        for(int i=0;i<h;i++){ // for every row
            // initialize current value to be start
            g=initConst+dgdY*(y+i);
            while(off<rowLimit){ // for every pixel in this row
                // get the color
                pixels[off++]=indexIntoGradientsArrays(g);
                // incremental change in g
                g+=dgdX;
            }
            // change in off from row to row
            off+=adjust;
            //rowlimit is width + offset
            rowLimit=off+w;
        }
    }
}
