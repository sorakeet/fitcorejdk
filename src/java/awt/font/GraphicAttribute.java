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

public abstract class GraphicAttribute{
    public static final int TOP_ALIGNMENT=-1;
    public static final int BOTTOM_ALIGNMENT=-2;
    public static final int ROMAN_BASELINE=Font.ROMAN_BASELINE;
    public static final int CENTER_BASELINE=Font.CENTER_BASELINE;
    public static final int HANGING_BASELINE=Font.HANGING_BASELINE;
    private int fAlignment;

    protected GraphicAttribute(int alignment){
        if(alignment<BOTTOM_ALIGNMENT||alignment>HANGING_BASELINE){
            throw new IllegalArgumentException("bad alignment");
        }
        fAlignment=alignment;
    }

    public Shape getOutline(AffineTransform tx){
        Shape b=getBounds();
        if(tx!=null){
            b=tx.createTransformedShape(b);
        }
        return b;
    }

    public Rectangle2D getBounds(){
        float ascent=getAscent();
        return new Rectangle2D.Float(0,-ascent,
                getAdvance(),ascent+getDescent());
    }

    public abstract float getAscent();

    public abstract float getDescent();

    public abstract float getAdvance();

    public abstract void draw(Graphics2D graphics,float x,float y);

    public final int getAlignment(){
        return fAlignment;
    }

    public GlyphJustificationInfo getJustificationInfo(){
        // should we cache this?
        float advance=getAdvance();
        return new GlyphJustificationInfo(
                advance,   // weight
                false,     // growAbsorb
                2,         // growPriority
                advance/3, // growLeftLimit
                advance/3, // growRightLimit
                false,     // shrinkAbsorb
                1,         // shrinkPriority
                0,         // shrinkLeftLimit
                0);        // shrinkRightLimit
    }
}
