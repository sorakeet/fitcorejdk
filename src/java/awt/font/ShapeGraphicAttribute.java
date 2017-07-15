/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * <p>
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 * <p>
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.awt.font;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public final class ShapeGraphicAttribute extends GraphicAttribute{
    public static final boolean STROKE=true;
    public static final boolean FILL=false;
    private Shape fShape;
    private boolean fStroke;
    // cache shape bounds, since GeneralPath doesn't
    private Rectangle2D fShapeBounds;

    public ShapeGraphicAttribute(Shape shape,
                                 int alignment,
                                 boolean stroke){
        super(alignment);
        fShape=shape;
        fStroke=stroke;
        fShapeBounds=fShape.getBounds2D();
    }

    public float getAscent(){
        return (float)Math.max(0,-fShapeBounds.getMinY());
    }

    public float getDescent(){
        return (float)Math.max(0,fShapeBounds.getMaxY());
    }

    public float getAdvance(){
        return (float)Math.max(0,fShapeBounds.getMaxX());
    }

    public Rectangle2D getBounds(){
        Rectangle2D.Float bounds=new Rectangle2D.Float();
        bounds.setRect(fShapeBounds);
        if(fStroke==STROKE){
            ++bounds.width;
            ++bounds.height;
        }
        return bounds;
    }

    public Shape getOutline(AffineTransform tx){
        return tx==null?fShape:tx.createTransformedShape(fShape);
    }

    public void draw(Graphics2D graphics,float x,float y){
        // translating graphics to draw Shape !!!
        graphics.translate((int)x,(int)y);
        try{
            if(fStroke==STROKE){
                // REMIND: set stroke to correct size
                graphics.draw(fShape);
            }else{
                graphics.fill(fShape);
            }
        }finally{
            graphics.translate(-(int)x,-(int)y);
        }
    }

    public int hashCode(){
        return fShape.hashCode();
    }

    public boolean equals(Object rhs){
        try{
            return equals((ShapeGraphicAttribute)rhs);
        }catch(ClassCastException e){
            return false;
        }
    }

    public boolean equals(ShapeGraphicAttribute rhs){
        if(rhs==null){
            return false;
        }
        if(this==rhs){
            return true;
        }
        if(fStroke!=rhs.fStroke){
            return false;
        }
        if(getAlignment()!=rhs.getAlignment()){
            return false;
        }
        if(!fShape.equals(rhs.fShape)){
            return false;
        }
        return true;
    }
}
