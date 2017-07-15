/**
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * *******************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/** ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package java.awt.image.renderable;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class RenderContext implements Cloneable{
    RenderingHints hints;
    AffineTransform usr2dev;
    Shape aoi;
    // Various constructors that allow different levels of
    // specificity. If the Shape is missing the whole renderable area
    // is assumed. If hints is missing no hints are assumed.

    public RenderContext(AffineTransform usr2dev){
        this(usr2dev,null,null);
    }

    public RenderContext(AffineTransform usr2dev,
                         Shape aoi,
                         RenderingHints hints){
        this.hints=hints;
        this.aoi=aoi;
        this.usr2dev=(AffineTransform)usr2dev.clone();
    }

    public RenderContext(AffineTransform usr2dev,RenderingHints hints){
        this(usr2dev,null,hints);
    }

    public RenderContext(AffineTransform usr2dev,Shape aoi){
        this(usr2dev,aoi,null);
    }

    public RenderingHints getRenderingHints(){
        return hints;
    }

    public void setRenderingHints(RenderingHints hints){
        this.hints=hints;
    }

    public void preConcatenateTransform(AffineTransform modTransform){
        this.preConcetenateTransform(modTransform);
    }

    @Deprecated
    public void preConcetenateTransform(AffineTransform modTransform){
        usr2dev.preConcatenate(modTransform);
    }

    public void concatenateTransform(AffineTransform modTransform){
        this.concetenateTransform(modTransform);
    }

    @Deprecated
    public void concetenateTransform(AffineTransform modTransform){
        usr2dev.concatenate(modTransform);
    }

    public AffineTransform getTransform(){
        return (AffineTransform)usr2dev.clone();
    }

    public void setTransform(AffineTransform newTransform){
        usr2dev=(AffineTransform)newTransform.clone();
    }

    public Shape getAreaOfInterest(){
        return aoi;
    }

    public void setAreaOfInterest(Shape newAoi){
        aoi=newAoi;
    }

    public Object clone(){
        RenderContext newRenderContext=new RenderContext(usr2dev,
                aoi,hints);
        return newRenderContext;
    }
}
