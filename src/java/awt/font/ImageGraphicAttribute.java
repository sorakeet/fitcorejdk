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
import java.awt.geom.Rectangle2D;

public final class ImageGraphicAttribute extends GraphicAttribute{
    private Image fImage;
    private float fImageWidth, fImageHeight;
    private float fOriginX, fOriginY;

    public ImageGraphicAttribute(Image image,int alignment){
        this(image,alignment,0,0);
    }

    public ImageGraphicAttribute(Image image,
                                 int alignment,
                                 float originX,
                                 float originY){
        super(alignment);
        // Can't clone image
        // fImage = (Image) image.clone();
        fImage=image;
        fImageWidth=image.getWidth(null);
        fImageHeight=image.getHeight(null);
        // ensure origin is in Image?
        fOriginX=originX;
        fOriginY=originY;
    }

    public float getAscent(){
        return Math.max(0,fOriginY);
    }

    public float getDescent(){
        return Math.max(0,fImageHeight-fOriginY);
    }

    public float getAdvance(){
        return Math.max(0,fImageWidth-fOriginX);
    }

    public Rectangle2D getBounds(){
        return new Rectangle2D.Float(
                -fOriginX,-fOriginY,fImageWidth,fImageHeight);
    }

    public void draw(Graphics2D graphics,float x,float y){
        graphics.drawImage(fImage,(int)(x-fOriginX),(int)(y-fOriginY),null);
    }

    public int hashCode(){
        return fImage.hashCode();
    }

    public boolean equals(Object rhs){
        try{
            return equals((ImageGraphicAttribute)rhs);
        }catch(ClassCastException e){
            return false;
        }
    }

    public boolean equals(ImageGraphicAttribute rhs){
        if(rhs==null){
            return false;
        }
        if(this==rhs){
            return true;
        }
        if(fOriginX!=rhs.fOriginX||fOriginY!=rhs.fOriginY){
            return false;
        }
        if(getAlignment()!=rhs.getAlignment()){
            return false;
        }
        if(!fImage.equals(rhs.fImage)){
            return false;
        }
        return true;
    }
}
