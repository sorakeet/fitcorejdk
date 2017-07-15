/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author Charlton Innovations, Inc.
 */
/**
 * @author Charlton Innovations, Inc.
 */
package java.awt.font;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class GlyphVector implements Cloneable{
    //
    // methods associated with creation-time state
    //
    public static final int FLAG_HAS_TRANSFORMS=1;
    public static final int FLAG_HAS_POSITION_ADJUSTMENTS=2;
    //
    // methods associated with the GlyphVector as a whole
    //
    public static final int FLAG_RUN_RTL=4;
    public static final int FLAG_COMPLEX_GLYPHS=8;
    public static final int FLAG_MASK=
            FLAG_HAS_TRANSFORMS|
                    FLAG_HAS_POSITION_ADJUSTMENTS|
                    FLAG_RUN_RTL|
                    FLAG_COMPLEX_GLYPHS;

    public abstract Font getFont();

    public abstract FontRenderContext getFontRenderContext();

    public abstract void performDefaultLayout();

    public abstract int getNumGlyphs();

    public abstract int getGlyphCode(int glyphIndex);

    public abstract int[] getGlyphCodes(int beginGlyphIndex,int numEntries,
                                        int[] codeReturn);

    public int[] getGlyphCharIndices(int beginGlyphIndex,int numEntries,
                                     int[] codeReturn){
        if(codeReturn==null){
            codeReturn=new int[numEntries];
        }
        for(int i=0, j=beginGlyphIndex;i<numEntries;++i,++j){
            codeReturn[i]=getGlyphCharIndex(j);
        }
        return codeReturn;
    }

    public int getGlyphCharIndex(int glyphIndex){
        return glyphIndex;
    }

    public abstract Rectangle2D getLogicalBounds();

    public Rectangle getPixelBounds(FontRenderContext renderFRC,float x,float y){
        Rectangle2D rect=getVisualBounds();
        int l=(int)Math.floor(rect.getX()+x);
        int t=(int)Math.floor(rect.getY()+y);
        int r=(int)Math.ceil(rect.getMaxX()+x);
        int b=(int)Math.ceil(rect.getMaxY()+y);
        return new Rectangle(l,t,r-l,b-t);
    }

    public abstract Rectangle2D getVisualBounds();

    public abstract Shape getOutline();

    public abstract Shape getOutline(float x,float y);

    public Shape getGlyphOutline(int glyphIndex,float x,float y){
        Shape s=getGlyphOutline(glyphIndex);
        AffineTransform at=AffineTransform.getTranslateInstance(x,y);
        return at.createTransformedShape(s);
    }

    public abstract Shape getGlyphOutline(int glyphIndex);

    public abstract Point2D getGlyphPosition(int glyphIndex);

    public abstract void setGlyphPosition(int glyphIndex,Point2D newPos);

    public abstract AffineTransform getGlyphTransform(int glyphIndex);

    public abstract void setGlyphTransform(int glyphIndex,AffineTransform newTX);

    public int getLayoutFlags(){
        return 0;
    }

    public abstract float[] getGlyphPositions(int beginGlyphIndex,int numEntries,
                                              float[] positionReturn);

    public abstract Shape getGlyphLogicalBounds(int glyphIndex);

    public Rectangle getGlyphPixelBounds(int index,FontRenderContext renderFRC,float x,float y){
        Rectangle2D rect=getGlyphVisualBounds(index).getBounds2D();
        int l=(int)Math.floor(rect.getX()+x);
        int t=(int)Math.floor(rect.getY()+y);
        int r=(int)Math.ceil(rect.getMaxX()+x);
        int b=(int)Math.ceil(rect.getMaxY()+y);
        return new Rectangle(l,t,r-l,b-t);
    }

    public abstract Shape getGlyphVisualBounds(int glyphIndex);

    public abstract GlyphMetrics getGlyphMetrics(int glyphIndex);

    public abstract GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex);
    //
    // general utility methods
    //

    public abstract boolean equals(GlyphVector set);
}
