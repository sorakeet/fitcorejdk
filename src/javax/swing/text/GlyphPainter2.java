/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

class GlyphPainter2 extends GlyphView.GlyphPainter{
    // --- variables ---------------------------------------------
    TextLayout layout;

    public GlyphPainter2(TextLayout layout){
        this.layout=layout;
    }

    public float getSpan(GlyphView v,int p0,int p1,
                         TabExpander e,float x){
        if((p0==v.getStartOffset())&&(p1==v.getEndOffset())){
            return layout.getAdvance();
        }
        int p=v.getStartOffset();
        int index0=p0-p;
        int index1=p1-p;
        TextHitInfo hit0=TextHitInfo.afterOffset(index0);
        TextHitInfo hit1=TextHitInfo.beforeOffset(index1);
        float[] locs=layout.getCaretInfo(hit0);
        float x0=locs[0];
        locs=layout.getCaretInfo(hit1);
        float x1=locs[0];
        return (x1>x0)?x1-x0:x0-x1;
    }

    public float getHeight(GlyphView v){
        return layout.getAscent()+layout.getDescent()+layout.getLeading();
    }

    public float getAscent(GlyphView v){
        return layout.getAscent();
    }

    public float getDescent(GlyphView v){
        return layout.getDescent();
    }

    public void paint(GlyphView v,Graphics g,Shape a,int p0,int p1){
        if(g instanceof Graphics2D){
            Rectangle2D alloc=a.getBounds2D();
            Graphics2D g2d=(Graphics2D)g;
            float y=(float)alloc.getY()+layout.getAscent()+layout.getLeading();
            float x=(float)alloc.getX();
            if(p0>v.getStartOffset()||p1<v.getEndOffset()){
                try{
                    //TextLayout can't render only part of it's range, so if a
                    //partial range is required, add a clip region.
                    Shape s=v.modelToView(p0,Position.Bias.Forward,
                            p1,Position.Bias.Backward,a);
                    Shape savedClip=g.getClip();
                    g2d.clip(s);
                    layout.draw(g2d,x,y);
                    g.setClip(savedClip);
                }catch(BadLocationException e){
                }
            }else{
                layout.draw(g2d,x,y);
            }
        }
    }

    public Shape modelToView(GlyphView v,int pos,Position.Bias bias,
                             Shape a) throws BadLocationException{
        int offs=pos-v.getStartOffset();
        Rectangle2D alloc=a.getBounds2D();
        TextHitInfo hit=(bias==Position.Bias.Forward)?
                TextHitInfo.afterOffset(offs):TextHitInfo.beforeOffset(offs);
        float[] locs=layout.getCaretInfo(hit);
        // vertical at the baseline, should use slope and check if glyphs
        // are being rendered vertically.
        alloc.setRect(alloc.getX()+locs[0],alloc.getY(),1,alloc.getHeight());
        return alloc;
    }

    public int viewToModel(GlyphView v,float x,float y,Shape a,
                           Position.Bias[] biasReturn){
        Rectangle2D alloc=(a instanceof Rectangle2D)?(Rectangle2D)a:a.getBounds2D();
        //Move the y co-ord of the hit onto the baseline.  This is because TextLayout supports
        //italic carets and we do not.
        TextHitInfo hit=layout.hitTestChar(x-(float)alloc.getX(),0);
        int pos=hit.getInsertionIndex();
        if(pos==v.getEndOffset()){
            pos--;
        }
        biasReturn[0]=hit.isLeadingEdge()?Position.Bias.Forward:Position.Bias.Backward;
        return pos+v.getStartOffset();
    }

    public int getBoundedPosition(GlyphView v,int p0,float x,float len){
        if(len<0)
            throw new IllegalArgumentException("Length must be >= 0.");
        // note: this only works because swing uses TextLayouts that are
        // only pure rtl or pure ltr
        TextHitInfo hit;
        if(layout.isLeftToRight()){
            hit=layout.hitTestChar(len,0);
        }else{
            hit=layout.hitTestChar(layout.getAdvance()-len,0);
        }
        return v.getStartOffset()+hit.getCharIndex();
    }

    public GlyphView.GlyphPainter getPainter(GlyphView v,int p0,int p1){
        return null;
    }

    public int getNextVisualPositionFrom(GlyphView v,int pos,
                                         Position.Bias b,Shape a,
                                         int direction,
                                         Position.Bias[] biasRet)
            throws BadLocationException{
        Document doc=v.getDocument();
        int startOffset=v.getStartOffset();
        int endOffset=v.getEndOffset();
        Segment text;
        boolean viewIsLeftToRight;
        TextHitInfo currentHit, nextHit;
        switch(direction){
            case View.NORTH:
                break;
            case View.SOUTH:
                break;
            case View.EAST:
                viewIsLeftToRight=AbstractDocument.isLeftToRight(doc,startOffset,endOffset);
                if(startOffset==doc.getLength()){
                    if(pos==-1){
                        biasRet[0]=Position.Bias.Forward;
                        return startOffset;
                    }
                    // End case for bidi text where newline is at beginning
                    // of line.
                    return -1;
                }
                if(pos==-1){
                    // Entering view from the left.
                    if(viewIsLeftToRight){
                        biasRet[0]=Position.Bias.Forward;
                        return startOffset;
                    }else{
                        text=v.getText(endOffset-1,endOffset);
                        char c=text.array[text.offset];
                        SegmentCache.releaseSharedSegment(text);
                        if(c=='\n'){
                            biasRet[0]=Position.Bias.Forward;
                            return endOffset-1;
                        }
                        biasRet[0]=Position.Bias.Backward;
                        return endOffset;
                    }
                }
                if(b==Position.Bias.Forward)
                    currentHit=TextHitInfo.afterOffset(pos-startOffset);
                else
                    currentHit=TextHitInfo.beforeOffset(pos-startOffset);
                nextHit=layout.getNextRightHit(currentHit);
                if(nextHit==null){
                    return -1;
                }
                if(viewIsLeftToRight!=layout.isLeftToRight()){
                    // If the layout's base direction is different from
                    // this view's run direction, we need to use the weak
                    // carrat.
                    nextHit=layout.getVisualOtherHit(nextHit);
                }
                pos=nextHit.getInsertionIndex()+startOffset;
                if(pos==endOffset){
                    // A move to the right from an internal position will
                    // only take us to the endOffset in a left to right run.
                    text=v.getText(endOffset-1,endOffset);
                    char c=text.array[text.offset];
                    SegmentCache.releaseSharedSegment(text);
                    if(c=='\n'){
                        return -1;
                    }
                    biasRet[0]=Position.Bias.Backward;
                }else{
                    biasRet[0]=Position.Bias.Forward;
                }
                return pos;
            case View.WEST:
                viewIsLeftToRight=AbstractDocument.isLeftToRight(doc,startOffset,endOffset);
                if(startOffset==doc.getLength()){
                    if(pos==-1){
                        biasRet[0]=Position.Bias.Forward;
                        return startOffset;
                    }
                    // End case for bidi text where newline is at beginning
                    // of line.
                    return -1;
                }
                if(pos==-1){
                    // Entering view from the right
                    if(viewIsLeftToRight){
                        text=v.getText(endOffset-1,endOffset);
                        char c=text.array[text.offset];
                        SegmentCache.releaseSharedSegment(text);
                        if((c=='\n')||Character.isSpaceChar(c)){
                            biasRet[0]=Position.Bias.Forward;
                            return endOffset-1;
                        }
                        biasRet[0]=Position.Bias.Backward;
                        return endOffset;
                    }else{
                        biasRet[0]=Position.Bias.Forward;
                        return startOffset;
                    }
                }
                if(b==Position.Bias.Forward)
                    currentHit=TextHitInfo.afterOffset(pos-startOffset);
                else
                    currentHit=TextHitInfo.beforeOffset(pos-startOffset);
                nextHit=layout.getNextLeftHit(currentHit);
                if(nextHit==null){
                    return -1;
                }
                if(viewIsLeftToRight!=layout.isLeftToRight()){
                    // If the layout's base direction is different from
                    // this view's run direction, we need to use the weak
                    // carrat.
                    nextHit=layout.getVisualOtherHit(nextHit);
                }
                pos=nextHit.getInsertionIndex()+startOffset;
                if(pos==endOffset){
                    // A move to the left from an internal position will
                    // only take us to the endOffset in a right to left run.
                    text=v.getText(endOffset-1,endOffset);
                    char c=text.array[text.offset];
                    SegmentCache.releaseSharedSegment(text);
                    if(c=='\n'){
                        return -1;
                    }
                    biasRet[0]=Position.Bias.Backward;
                }else{
                    biasRet[0]=Position.Bias.Forward;
                }
                return pos;
            default:
                throw new IllegalArgumentException("Bad direction: "+direction);
        }
        return pos;
    }
}
