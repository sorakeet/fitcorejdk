/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.geom.Rectangle2D;

public final class GlyphMetrics{
    public static final byte STANDARD=0;
    public static final byte LIGATURE=1;
    public static final byte COMBINING=2;
    public static final byte COMPONENT=3;
    public static final byte WHITESPACE=4;
    private boolean horizontal;
    private float advanceX;
    private float advanceY;
    private Rectangle2D.Float bounds;
    private byte glyphType;

    public GlyphMetrics(float advance,Rectangle2D bounds,byte glyphType){
        this.horizontal=true;
        this.advanceX=advance;
        this.advanceY=0;
        this.bounds=new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType=glyphType;
    }

    public GlyphMetrics(boolean horizontal,float advanceX,float advanceY,
                        Rectangle2D bounds,byte glyphType){
        this.horizontal=horizontal;
        this.advanceX=advanceX;
        this.advanceY=advanceY;
        this.bounds=new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType=glyphType;
    }

    public float getAdvance(){
        return horizontal?advanceX:advanceY;
    }

    public float getAdvanceX(){
        return advanceX;
    }

    public float getAdvanceY(){
        return advanceY;
    }

    public Rectangle2D getBounds2D(){
        return new Rectangle2D.Float(bounds.x,bounds.y,bounds.width,bounds.height);
    }

    public float getLSB(){
        return horizontal?bounds.x:bounds.y;
    }

    public float getRSB(){
        return horizontal?
                advanceX-bounds.x-bounds.width:
                advanceY-bounds.y-bounds.height;
    }

    public int getType(){
        return glyphType;
    }

    public boolean isStandard(){
        return (glyphType&0x3)==STANDARD;
    }

    public boolean isLigature(){
        return (glyphType&0x3)==LIGATURE;
    }

    public boolean isCombining(){
        return (glyphType&0x3)==COMBINING;
    }

    public boolean isComponent(){
        return (glyphType&0x3)==COMPONENT;
    }

    public boolean isWhitespace(){
        return (glyphType&0x4)==WHITESPACE;
    }
}
