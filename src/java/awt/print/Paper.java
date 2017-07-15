/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

import java.awt.geom.Rectangle2D;

public class Paper implements Cloneable{
    private static final int INCH=72;
    private static final double LETTER_WIDTH=8.5*INCH;
    private static final double LETTER_HEIGHT=11*INCH;
    private double mHeight;
    private double mWidth;
    private Rectangle2D mImageableArea;

    public Paper(){
        mHeight=LETTER_HEIGHT;
        mWidth=LETTER_WIDTH;
        mImageableArea=new Rectangle2D.Double(INCH,INCH,
                mWidth-2*INCH,
                mHeight-2*INCH);
    }

    public Object clone(){
        Paper newPaper;
        try{
            /** It's okay to copy the reference to the imageable
             * area into the clone since we always return a copy
             * of the imageable area when asked for it.
             */
            newPaper=(Paper)super.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
            newPaper=null;    // should never happen.
        }
        return newPaper;
    }

    public double getHeight(){
        return mHeight;
    }

    public void setSize(double width,double height){
        mWidth=width;
        mHeight=height;
    }

    public double getWidth(){
        return mWidth;
    }

    public void setImageableArea(double x,double y,
                                 double width,double height){
        mImageableArea=new Rectangle2D.Double(x,y,width,height);
    }

    public double getImageableX(){
        return mImageableArea.getX();
    }

    public double getImageableY(){
        return mImageableArea.getY();
    }

    public double getImageableWidth(){
        return mImageableArea.getWidth();
    }

    public double getImageableHeight(){
        return mImageableArea.getHeight();
    }
}
