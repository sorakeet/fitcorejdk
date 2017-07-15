/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.text.AttributedCharacterIterator;
import java.text.BreakIterator;

public final class LineBreakMeasurer{
    private BreakIterator breakIter;
    private int start;
    private int pos;
    private int limit;
    private TextMeasurer measurer;
    private CharArrayIterator charIter;

    public LineBreakMeasurer(AttributedCharacterIterator text,FontRenderContext frc){
        this(text,BreakIterator.getLineInstance(),frc);
    }

    public LineBreakMeasurer(AttributedCharacterIterator text,
                             BreakIterator breakIter,
                             FontRenderContext frc){
        if(text.getEndIndex()-text.getBeginIndex()<1){
            throw new IllegalArgumentException("Text must contain at least one character.");
        }
        this.breakIter=breakIter;
        this.measurer=new TextMeasurer(text,frc);
        this.limit=text.getEndIndex();
        this.pos=this.start=text.getBeginIndex();
        charIter=new CharArrayIterator(measurer.getChars(),this.start);
        this.breakIter.setText(charIter);
    }

    public int nextOffset(float wrappingWidth){
        return nextOffset(wrappingWidth,limit,false);
    }

    public int nextOffset(float wrappingWidth,int offsetLimit,
                          boolean requireNextWord){
        int nextOffset=pos;
        if(pos<limit){
            if(offsetLimit<=pos){
                throw new IllegalArgumentException("offsetLimit must be after current position");
            }
            int charAtMaxAdvance=
                    measurer.getLineBreakIndex(pos,wrappingWidth);
            if(charAtMaxAdvance==limit){
                nextOffset=limit;
            }else if(Character.isWhitespace(measurer.getChars()[charAtMaxAdvance-start])){
                nextOffset=breakIter.following(charAtMaxAdvance);
            }else{
                // Break is in a word;  back up to previous break.
                // NOTE:  I think that breakIter.preceding(limit) should be
                // equivalent to breakIter.last(), breakIter.previous() but
                // the authors of BreakIterator thought otherwise...
                // If they were equivalent then the first branch would be
                // unnecessary.
                int testPos=charAtMaxAdvance+1;
                if(testPos==limit){
                    breakIter.last();
                    nextOffset=breakIter.previous();
                }else{
                    nextOffset=breakIter.preceding(testPos);
                }
                if(nextOffset<=pos){
                    // first word doesn't fit on line
                    if(requireNextWord){
                        nextOffset=pos;
                    }else{
                        nextOffset=Math.max(pos+1,charAtMaxAdvance);
                    }
                }
            }
        }
        if(nextOffset>offsetLimit){
            nextOffset=offsetLimit;
        }
        return nextOffset;
    }

    public TextLayout nextLayout(float wrappingWidth){
        return nextLayout(wrappingWidth,limit,false);
    }

    public TextLayout nextLayout(float wrappingWidth,int offsetLimit,
                                 boolean requireNextWord){
        if(pos<limit){
            int layoutLimit=nextOffset(wrappingWidth,offsetLimit,requireNextWord);
            if(layoutLimit==pos){
                return null;
            }
            TextLayout result=measurer.getLayout(pos,layoutLimit);
            pos=layoutLimit;
            return result;
        }else{
            return null;
        }
    }

    public int getPosition(){
        return pos;
    }

    public void setPosition(int newPosition){
        if(newPosition<start||newPosition>limit){
            throw new IllegalArgumentException("position is out of range");
        }
        pos=newPosition;
    }

    public void insertChar(AttributedCharacterIterator newParagraph,
                           int insertPos){
        measurer.insertChar(newParagraph,insertPos);
        limit=newParagraph.getEndIndex();
        pos=start=newParagraph.getBeginIndex();
        charIter.reset(measurer.getChars(),newParagraph.getBeginIndex());
        breakIter.setText(charIter);
    }

    public void deleteChar(AttributedCharacterIterator newParagraph,
                           int deletePos){
        measurer.deleteChar(newParagraph,deletePos);
        limit=newParagraph.getEndIndex();
        pos=start=newParagraph.getBeginIndex();
        charIter.reset(measurer.getChars(),start);
        breakIter.setText(charIter);
    }
}
