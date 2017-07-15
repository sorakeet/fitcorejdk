/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.beans.ConstructorProperties;

public final class LinearGradientPaint extends MultipleGradientPaint{
    private final Point2D start, end;

    public LinearGradientPaint(float startX,float startY,
                               float endX,float endY,
                               float[] fractions,Color[] colors){
        this(new Point2D.Float(startX,startY),
                new Point2D.Float(endX,endY),
                fractions,
                colors,
                CycleMethod.NO_CYCLE);
    }

    public LinearGradientPaint(Point2D start,Point2D end,
                               float[] fractions,Color[] colors,
                               CycleMethod cycleMethod){
        this(start,end,
                fractions,colors,
                cycleMethod,
                ColorSpaceType.SRGB,
                new AffineTransform());
    }

    @ConstructorProperties({"startPoint","endPoint","fractions","colors","cycleMethod","colorSpace","transform"})
    public LinearGradientPaint(Point2D start,Point2D end,
                               float[] fractions,Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace,
                               AffineTransform gradientTransform){
        super(fractions,colors,cycleMethod,colorSpace,gradientTransform);
        // check input parameters
        if(start==null||end==null){
            throw new NullPointerException("Start and end points must be"+
                    "non-null");
        }
        if(start.equals(end)){
            throw new IllegalArgumentException("Start point cannot equal"+
                    "endpoint");
        }
        // copy the points...
        this.start=new Point2D.Double(start.getX(),start.getY());
        this.end=new Point2D.Double(end.getX(),end.getY());
    }

    public LinearGradientPaint(float startX,float startY,
                               float endX,float endY,
                               float[] fractions,Color[] colors,
                               CycleMethod cycleMethod){
        this(new Point2D.Float(startX,startY),
                new Point2D.Float(endX,endY),
                fractions,
                colors,
                cycleMethod);
    }

    public LinearGradientPaint(Point2D start,Point2D end,
                               float[] fractions,Color[] colors){
        this(start,end,
                fractions,colors,
                CycleMethod.NO_CYCLE);
    }

    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform transform,
                                      RenderingHints hints){
        // avoid modifying the user's transform...
        transform=new AffineTransform(transform);
        // incorporate the gradient transform
        transform.concatenate(gradientTransform);
        if((fractions.length==2)&&
                (cycleMethod!=CycleMethod.REPEAT)&&
                (colorSpace==ColorSpaceType.SRGB)){
            // faster to use the basic GradientPaintContext for this
            // common case
            boolean cyclic=(cycleMethod!=CycleMethod.NO_CYCLE);
            return new GradientPaintContext(cm,start,end,
                    transform,
                    colors[0],colors[1],
                    cyclic);
        }else{
            return new LinearGradientPaintContext(this,cm,
                    deviceBounds,userBounds,
                    transform,hints,
                    start,end,
                    fractions,colors,
                    cycleMethod,colorSpace);
        }
    }

    public Point2D getStartPoint(){
        return new Point2D.Double(start.getX(),start.getY());
    }

    public Point2D getEndPoint(){
        return new Point2D.Double(end.getX(),end.getY());
    }
}
