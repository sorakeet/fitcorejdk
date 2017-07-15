/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.text.ParagraphView.Row;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;

public class Utilities{
    public static final int drawTabbedText(Segment s,int x,int y,Graphics g,
                                           TabExpander e,int startOffset){
        return drawTabbedText(null,s,x,y,g,e,startOffset);
    }

    static final int drawTabbedText(View view,
                                    Segment s,int x,int y,Graphics g,
                                    TabExpander e,int startOffset){
        return drawTabbedText(view,s,x,y,g,e,startOffset,null);
    }

    // In addition to the previous method it can extend spaces for
    // justification.
    //
    // all params are the same as in the preious method except the last
    // one:
    // @param justificationData justificationData for the row.
    // if null not justification is needed
    static final int drawTabbedText(View view,
                                    Segment s,int x,int y,Graphics g,
                                    TabExpander e,int startOffset,
                                    int[] justificationData){
        JComponent component=getJComponent(view);
        FontMetrics metrics=SwingUtilities2.getFontMetrics(component,g);
        int nextX=x;
        char[] txt=s.array;
        int txtOffset=s.offset;
        int flushLen=0;
        int flushIndex=s.offset;
        int spaceAddon=0;
        int spaceAddonLeftoverEnd=-1;
        int startJustifiableContent=0;
        int endJustifiableContent=0;
        if(justificationData!=null){
            int offset=-startOffset+txtOffset;
            View parent=null;
            if(view!=null
                    &&(parent=view.getParent())!=null){
                offset+=parent.getStartOffset();
            }
            spaceAddon=
                    justificationData[Row.SPACE_ADDON];
            spaceAddonLeftoverEnd=
                    justificationData[Row.SPACE_ADDON_LEFTOVER_END]+offset;
            startJustifiableContent=
                    justificationData[Row.START_JUSTIFIABLE]+offset;
            endJustifiableContent=
                    justificationData[Row.END_JUSTIFIABLE]+offset;
        }
        int n=s.offset+s.count;
        for(int i=txtOffset;i<n;i++){
            if(txt[i]=='\t'
                    ||((spaceAddon!=0||i<=spaceAddonLeftoverEnd)
                    &&(txt[i]==' ')
                    &&startJustifiableContent<=i
                    &&i<=endJustifiableContent
            )){
                if(flushLen>0){
                    nextX=SwingUtilities2.drawChars(component,g,txt,
                            flushIndex,flushLen,x,y);
                    flushLen=0;
                }
                flushIndex=i+1;
                if(txt[i]=='\t'){
                    if(e!=null){
                        nextX=(int)e.nextTabStop((float)nextX,startOffset+i-txtOffset);
                    }else{
                        nextX+=metrics.charWidth(' ');
                    }
                }else if(txt[i]==' '){
                    nextX+=metrics.charWidth(' ')+spaceAddon;
                    if(i<=spaceAddonLeftoverEnd){
                        nextX++;
                    }
                }
                x=nextX;
            }else if((txt[i]=='\n')||(txt[i]=='\r')){
                if(flushLen>0){
                    nextX=SwingUtilities2.drawChars(component,g,txt,
                            flushIndex,flushLen,x,y);
                    flushLen=0;
                }
                flushIndex=i+1;
                x=nextX;
            }else{
                flushLen+=1;
            }
        }
        if(flushLen>0){
            nextX=SwingUtilities2.drawChars(component,g,txt,flushIndex,
                    flushLen,x,y);
        }
        return nextX;
    }

    static JComponent getJComponent(View view){
        if(view!=null){
            Component component=view.getContainer();
            if(component instanceof JComponent){
                return (JComponent)component;
            }
        }
        return null;
    }

    public static final int getTabbedTextWidth(Segment s,FontMetrics metrics,int x,
                                               TabExpander e,int startOffset){
        return getTabbedTextWidth(null,s,metrics,x,e,startOffset,null);
    }

    // In addition to the previous method it can extend spaces for
    // justification.
    //
    // all params are the same as in the preious method except the last
    // one:
    // @param justificationData justificationData for the row.
    // if null not justification is needed
    static final int getTabbedTextWidth(View view,Segment s,FontMetrics metrics,int x,
                                        TabExpander e,int startOffset,
                                        int[] justificationData){
        int nextX=x;
        char[] txt=s.array;
        int txtOffset=s.offset;
        int n=s.offset+s.count;
        int charCount=0;
        int spaceAddon=0;
        int spaceAddonLeftoverEnd=-1;
        int startJustifiableContent=0;
        int endJustifiableContent=0;
        if(justificationData!=null){
            int offset=-startOffset+txtOffset;
            View parent=null;
            if(view!=null
                    &&(parent=view.getParent())!=null){
                offset+=parent.getStartOffset();
            }
            spaceAddon=
                    justificationData[Row.SPACE_ADDON];
            spaceAddonLeftoverEnd=
                    justificationData[Row.SPACE_ADDON_LEFTOVER_END]+offset;
            startJustifiableContent=
                    justificationData[Row.START_JUSTIFIABLE]+offset;
            endJustifiableContent=
                    justificationData[Row.END_JUSTIFIABLE]+offset;
        }
        for(int i=txtOffset;i<n;i++){
            if(txt[i]=='\t'
                    ||((spaceAddon!=0||i<=spaceAddonLeftoverEnd)
                    &&(txt[i]==' ')
                    &&startJustifiableContent<=i
                    &&i<=endJustifiableContent
            )){
                nextX+=metrics.charsWidth(txt,i-charCount,charCount);
                charCount=0;
                if(txt[i]=='\t'){
                    if(e!=null){
                        nextX=(int)e.nextTabStop((float)nextX,
                                startOffset+i-txtOffset);
                    }else{
                        nextX+=metrics.charWidth(' ');
                    }
                }else if(txt[i]==' '){
                    nextX+=metrics.charWidth(' ')+spaceAddon;
                    if(i<=spaceAddonLeftoverEnd){
                        nextX++;
                    }
                }
            }else if(txt[i]=='\n'){
                // Ignore newlines, they take up space and we shouldn't be
                // counting them.
                nextX+=metrics.charsWidth(txt,i-charCount,charCount);
                charCount=0;
            }else{
                charCount++;
            }
        }
        nextX+=metrics.charsWidth(txt,n-charCount,charCount);
        return nextX-x;
    }

    public static final int getTabbedTextOffset(Segment s,FontMetrics metrics,
                                                int x0,int x,TabExpander e,
                                                int startOffset){
        return getTabbedTextOffset(s,metrics,x0,x,e,startOffset,true);
    }

    public static final int getTabbedTextOffset(Segment s,
                                                FontMetrics metrics,
                                                int x0,int x,TabExpander e,
                                                int startOffset,
                                                boolean round){
        return getTabbedTextOffset(null,s,metrics,x0,x,e,startOffset,round,null);
    }

    // In addition to the previous method it can extend spaces for
    // justification.
    //
    // all params are the same as in the preious method except the last
    // one:
    // @param justificationData justificationData for the row.
    // if null not justification is needed
    static final int getTabbedTextOffset(View view,
                                         Segment s,
                                         FontMetrics metrics,
                                         int x0,int x,TabExpander e,
                                         int startOffset,
                                         boolean round,
                                         int[] justificationData){
        if(x0>=x){
            // x before x0, return.
            return 0;
        }
        int nextX=x0;
        // s may be a shared segment, so it is copied prior to calling
        // the tab expander
        char[] txt=s.array;
        int txtOffset=s.offset;
        int txtCount=s.count;
        int spaceAddon=0;
        int spaceAddonLeftoverEnd=-1;
        int startJustifiableContent=0;
        int endJustifiableContent=0;
        if(justificationData!=null){
            int offset=-startOffset+txtOffset;
            View parent=null;
            if(view!=null
                    &&(parent=view.getParent())!=null){
                offset+=parent.getStartOffset();
            }
            spaceAddon=
                    justificationData[Row.SPACE_ADDON];
            spaceAddonLeftoverEnd=
                    justificationData[Row.SPACE_ADDON_LEFTOVER_END]+offset;
            startJustifiableContent=
                    justificationData[Row.START_JUSTIFIABLE]+offset;
            endJustifiableContent=
                    justificationData[Row.END_JUSTIFIABLE]+offset;
        }
        int n=s.offset+s.count;
        for(int i=s.offset;i<n;i++){
            if(txt[i]=='\t'
                    ||((spaceAddon!=0||i<=spaceAddonLeftoverEnd)
                    &&(txt[i]==' ')
                    &&startJustifiableContent<=i
                    &&i<=endJustifiableContent
            )){
                if(txt[i]=='\t'){
                    if(e!=null){
                        nextX=(int)e.nextTabStop((float)nextX,
                                startOffset+i-txtOffset);
                    }else{
                        nextX+=metrics.charWidth(' ');
                    }
                }else if(txt[i]==' '){
                    nextX+=metrics.charWidth(' ')+spaceAddon;
                    if(i<=spaceAddonLeftoverEnd){
                        nextX++;
                    }
                }
            }else{
                nextX+=metrics.charWidth(txt[i]);
            }
            if(x<nextX){
                // found the hit position... return the appropriate side
                int offset;
                // the length of the string measured as a whole may differ from
                // the sum of individual character lengths, for example if
                // fractional metrics are enabled; and we must guard from this.
                if(round){
                    offset=i+1-txtOffset;
                    int width=metrics.charsWidth(txt,txtOffset,offset);
                    int span=x-x0;
                    if(span<width){
                        while(offset>0){
                            int nextWidth=offset>1?metrics.charsWidth(txt,txtOffset,offset-1):0;
                            if(span>=nextWidth){
                                if(span-nextWidth<width-span){
                                    offset--;
                                }
                                break;
                            }
                            width=nextWidth;
                            offset--;
                        }
                    }
                }else{
                    offset=i-txtOffset;
                    while(offset>0&&metrics.charsWidth(txt,txtOffset,offset)>(x-x0)){
                        offset--;
                    }
                }
                return offset;
            }
        }
        // didn't find, return end offset
        return txtCount;
    }

    static final int getTabbedTextOffset(View view,Segment s,FontMetrics metrics,
                                         int x0,int x,TabExpander e,
                                         int startOffset,
                                         int[] justificationData){
        return getTabbedTextOffset(view,s,metrics,x0,x,e,startOffset,true,
                justificationData);
    }

    public static final int getBreakLocation(Segment s,FontMetrics metrics,
                                             int x0,int x,TabExpander e,
                                             int startOffset){
        char[] txt=s.array;
        int txtOffset=s.offset;
        int txtCount=s.count;
        int index=Utilities.getTabbedTextOffset(s,metrics,x0,x,
                e,startOffset,false);
        if(index>=txtCount-1){
            return txtCount;
        }
        for(int i=txtOffset+index;i>=txtOffset;i--){
            char ch=txt[i];
            if(ch<256){
                // break on whitespace
                if(Character.isWhitespace(ch)){
                    index=i-txtOffset+1;
                    break;
                }
            }else{
                // a multibyte char found; use BreakIterator to find line break
                BreakIterator bit=BreakIterator.getLineInstance();
                bit.setText(s);
                int breakPos=bit.preceding(i+1);
                if(breakPos>txtOffset){
                    index=breakPos-txtOffset;
                }
                break;
            }
        }
        return index;
    }

    public static final int getPositionAbove(JTextComponent c,int offs,int x) throws BadLocationException{
        int lastOffs=getRowStart(c,offs)-1;
        if(lastOffs<0){
            return -1;
        }
        int bestSpan=Integer.MAX_VALUE;
        int y=0;
        Rectangle r=null;
        if(lastOffs>=0){
            r=c.modelToView(lastOffs);
            y=r.y;
        }
        while((r!=null)&&(y==r.y)){
            int span=Math.abs(r.x-x);
            if(span<bestSpan){
                offs=lastOffs;
                bestSpan=span;
            }
            lastOffs-=1;
            r=(lastOffs>=0)?c.modelToView(lastOffs):null;
        }
        return offs;
    }

    public static final int getRowStart(JTextComponent c,int offs) throws BadLocationException{
        Rectangle r=c.modelToView(offs);
        if(r==null){
            return -1;
        }
        int lastOffs=offs;
        int y=r.y;
        while((r!=null)&&(y==r.y)){
            // Skip invisible elements
            if(r.height!=0){
                offs=lastOffs;
            }
            lastOffs-=1;
            r=(lastOffs>=0)?c.modelToView(lastOffs):null;
        }
        return offs;
    }

    public static final int getPositionBelow(JTextComponent c,int offs,int x) throws BadLocationException{
        int lastOffs=getRowEnd(c,offs)+1;
        if(lastOffs<=0){
            return -1;
        }
        int bestSpan=Integer.MAX_VALUE;
        int n=c.getDocument().getLength();
        int y=0;
        Rectangle r=null;
        if(lastOffs<=n){
            r=c.modelToView(lastOffs);
            y=r.y;
        }
        while((r!=null)&&(y==r.y)){
            int span=Math.abs(x-r.x);
            if(span<bestSpan){
                offs=lastOffs;
                bestSpan=span;
            }
            lastOffs+=1;
            r=(lastOffs<=n)?c.modelToView(lastOffs):null;
        }
        return offs;
    }

    public static final int getRowEnd(JTextComponent c,int offs) throws BadLocationException{
        Rectangle r=c.modelToView(offs);
        if(r==null){
            return -1;
        }
        int n=c.getDocument().getLength();
        int lastOffs=offs;
        int y=r.y;
        while((r!=null)&&(y==r.y)){
            // Skip invisible elements
            if(r.height!=0){
                offs=lastOffs;
            }
            lastOffs+=1;
            r=(lastOffs<=n)?c.modelToView(lastOffs):null;
        }
        return offs;
    }

    public static final int getWordStart(JTextComponent c,int offs) throws BadLocationException{
        Document doc=c.getDocument();
        Element line=getParagraphElement(c,offs);
        if(line==null){
            throw new BadLocationException("No word at "+offs,offs);
        }
        int lineStart=line.getStartOffset();
        int lineEnd=Math.min(line.getEndOffset(),doc.getLength());
        Segment seg=SegmentCache.getSharedSegment();
        doc.getText(lineStart,lineEnd-lineStart,seg);
        if(seg.count>0){
            BreakIterator words=BreakIterator.getWordInstance(c.getLocale());
            words.setText(seg);
            int wordPosition=seg.offset+offs-lineStart;
            if(wordPosition>=words.last()){
                wordPosition=words.last()-1;
            }
            words.following(wordPosition);
            offs=lineStart+words.previous()-seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return offs;
    }

    public static final Element getParagraphElement(JTextComponent c,int offs){
        Document doc=c.getDocument();
        if(doc instanceof StyledDocument){
            return ((StyledDocument)doc).getParagraphElement(offs);
        }
        Element map=doc.getDefaultRootElement();
        int index=map.getElementIndex(offs);
        Element paragraph=map.getElement(index);
        if((offs>=paragraph.getStartOffset())&&(offs<paragraph.getEndOffset())){
            return paragraph;
        }
        return null;
    }

    public static final int getWordEnd(JTextComponent c,int offs) throws BadLocationException{
        Document doc=c.getDocument();
        Element line=getParagraphElement(c,offs);
        if(line==null){
            throw new BadLocationException("No word at "+offs,offs);
        }
        int lineStart=line.getStartOffset();
        int lineEnd=Math.min(line.getEndOffset(),doc.getLength());
        Segment seg=SegmentCache.getSharedSegment();
        doc.getText(lineStart,lineEnd-lineStart,seg);
        if(seg.count>0){
            BreakIterator words=BreakIterator.getWordInstance(c.getLocale());
            words.setText(seg);
            int wordPosition=offs-lineStart+seg.offset;
            if(wordPosition>=words.last()){
                wordPosition=words.last()-1;
            }
            offs=lineStart+words.following(wordPosition)-seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return offs;
    }

    public static final int getNextWord(JTextComponent c,int offs) throws BadLocationException{
        int nextWord;
        Element line=getParagraphElement(c,offs);
        for(nextWord=getNextWordInParagraph(c,line,offs,false);
            nextWord==BreakIterator.DONE;
            nextWord=getNextWordInParagraph(c,line,offs,true)){
            // didn't find in this line, try the next line
            offs=line.getEndOffset();
            line=getParagraphElement(c,offs);
        }
        return nextWord;
    }

    static int getNextWordInParagraph(JTextComponent c,Element line,int offs,boolean first) throws BadLocationException{
        if(line==null){
            throw new BadLocationException("No more words",offs);
        }
        Document doc=line.getDocument();
        int lineStart=line.getStartOffset();
        int lineEnd=Math.min(line.getEndOffset(),doc.getLength());
        if((offs>=lineEnd)||(offs<lineStart)){
            throw new BadLocationException("No more words",offs);
        }
        Segment seg=SegmentCache.getSharedSegment();
        doc.getText(lineStart,lineEnd-lineStart,seg);
        BreakIterator words=BreakIterator.getWordInstance(c.getLocale());
        words.setText(seg);
        if((first&&(words.first()==(seg.offset+offs-lineStart)))&&
                (!Character.isWhitespace(seg.array[words.first()]))){
            return offs;
        }
        int wordPosition=words.following(seg.offset+offs-lineStart);
        if((wordPosition==BreakIterator.DONE)||
                (wordPosition>=seg.offset+seg.count)){
            // there are no more words on this line.
            return BreakIterator.DONE;
        }
        // if we haven't shot past the end... check to
        // see if the current boundary represents whitespace.
        // if so, we need to try again
        char ch=seg.array[wordPosition];
        if(!Character.isWhitespace(ch)){
            return lineStart+wordPosition-seg.offset;
        }
        // it was whitespace, try again.  The assumption
        // is that it must be a word start if the last
        // one had whitespace following it.
        wordPosition=words.next();
        if(wordPosition!=BreakIterator.DONE){
            offs=lineStart+wordPosition-seg.offset;
            if(offs!=lineEnd){
                return offs;
            }
        }
        SegmentCache.releaseSharedSegment(seg);
        return BreakIterator.DONE;
    }

    public static final int getPreviousWord(JTextComponent c,int offs) throws BadLocationException{
        int prevWord;
        Element line=getParagraphElement(c,offs);
        for(prevWord=getPrevWordInParagraph(c,line,offs);
            prevWord==BreakIterator.DONE;
            prevWord=getPrevWordInParagraph(c,line,offs)){
            // didn't find in this line, try the prev line
            offs=line.getStartOffset()-1;
            line=getParagraphElement(c,offs);
        }
        return prevWord;
    }

    static int getPrevWordInParagraph(JTextComponent c,Element line,int offs) throws BadLocationException{
        if(line==null){
            throw new BadLocationException("No more words",offs);
        }
        Document doc=line.getDocument();
        int lineStart=line.getStartOffset();
        int lineEnd=line.getEndOffset();
        if((offs>lineEnd)||(offs<lineStart)){
            throw new BadLocationException("No more words",offs);
        }
        Segment seg=SegmentCache.getSharedSegment();
        doc.getText(lineStart,lineEnd-lineStart,seg);
        BreakIterator words=BreakIterator.getWordInstance(c.getLocale());
        words.setText(seg);
        if(words.following(seg.offset+offs-lineStart)==BreakIterator.DONE){
            words.last();
        }
        int wordPosition=words.previous();
        if(wordPosition==(seg.offset+offs-lineStart)){
            wordPosition=words.previous();
        }
        if(wordPosition==BreakIterator.DONE){
            // there are no more words on this line.
            return BreakIterator.DONE;
        }
        // if we haven't shot past the end... check to
        // see if the current boundary represents whitespace.
        // if so, we need to try again
        char ch=seg.array[wordPosition];
        if(!Character.isWhitespace(ch)){
            return lineStart+wordPosition-seg.offset;
        }
        // it was whitespace, try again.  The assumption
        // is that it must be a word start if the last
        // one had whitespace following it.
        wordPosition=words.previous();
        if(wordPosition!=BreakIterator.DONE){
            return lineStart+wordPosition-seg.offset;
        }
        SegmentCache.releaseSharedSegment(seg);
        return BreakIterator.DONE;
    }

    static boolean isComposedTextElement(Document doc,int offset){
        Element elem=doc.getDefaultRootElement();
        while(!elem.isLeaf()){
            elem=elem.getElement(elem.getElementIndex(offset));
        }
        return isComposedTextElement(elem);
    }

    static boolean isComposedTextElement(Element elem){
        AttributeSet as=elem.getAttributes();
        return isComposedTextAttributeDefined(as);
    }

    static boolean isComposedTextAttributeDefined(AttributeSet as){
        return ((as!=null)&&
                (as.isDefined(StyleConstants.ComposedTextAttribute)));
    }

    static int drawComposedText(View view,AttributeSet attr,Graphics g,
                                int x,int y,int p0,int p1)
            throws BadLocationException{
        Graphics2D g2d=(Graphics2D)g;
        AttributedString as=(AttributedString)attr.getAttribute(
                StyleConstants.ComposedTextAttribute);
        as.addAttribute(TextAttribute.FONT,g.getFont());
        if(p0>=p1)
            return x;
        AttributedCharacterIterator aci=as.getIterator(null,p0,p1);
        return x+(int)SwingUtilities2.drawString(
                getJComponent(view),g2d,aci,x,y);
    }

    static void paintComposedText(Graphics g,Rectangle alloc,GlyphView v){
        if(g instanceof Graphics2D){
            Graphics2D g2d=(Graphics2D)g;
            int p0=v.getStartOffset();
            int p1=v.getEndOffset();
            AttributeSet attrSet=v.getElement().getAttributes();
            AttributedString as=
                    (AttributedString)attrSet.getAttribute(StyleConstants.ComposedTextAttribute);
            int start=v.getElement().getStartOffset();
            int y=alloc.y+alloc.height-(int)v.getGlyphPainter().getDescent(v);
            int x=alloc.x;
            //Add text attributes
            as.addAttribute(TextAttribute.FONT,v.getFont());
            as.addAttribute(TextAttribute.FOREGROUND,v.getForeground());
            if(StyleConstants.isBold(v.getAttributes())){
                as.addAttribute(TextAttribute.WEIGHT,TextAttribute.WEIGHT_BOLD);
            }
            if(StyleConstants.isItalic(v.getAttributes())){
                as.addAttribute(TextAttribute.POSTURE,TextAttribute.POSTURE_OBLIQUE);
            }
            if(v.isUnderline()){
                as.addAttribute(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON);
            }
            if(v.isStrikeThrough()){
                as.addAttribute(TextAttribute.STRIKETHROUGH,TextAttribute.STRIKETHROUGH_ON);
            }
            if(v.isSuperscript()){
                as.addAttribute(TextAttribute.SUPERSCRIPT,TextAttribute.SUPERSCRIPT_SUPER);
            }
            if(v.isSubscript()){
                as.addAttribute(TextAttribute.SUPERSCRIPT,TextAttribute.SUPERSCRIPT_SUB);
            }
            // draw
            AttributedCharacterIterator aci=as.getIterator(null,p0-start,p1-start);
            SwingUtilities2.drawString(getJComponent(v),
                    g2d,aci,x,y);
        }
    }

    static boolean isLeftToRight(Component c){
        return c.getComponentOrientation().isLeftToRight();
    }

    static int getNextVisualPositionFrom(View v,int pos,Position.Bias b,
                                         Shape alloc,int direction,
                                         Position.Bias[] biasRet)
            throws BadLocationException{
        if(v.getViewCount()==0){
            // Nothing to do.
            return pos;
        }
        boolean top=(direction==SwingConstants.NORTH||
                direction==SwingConstants.WEST);
        int retValue;
        if(pos==-1){
            // Start from the first View.
            int childIndex=(top)?v.getViewCount()-1:0;
            View child=v.getView(childIndex);
            Shape childBounds=v.getChildAllocation(childIndex,alloc);
            retValue=child.getNextVisualPositionFrom(pos,b,childBounds,
                    direction,biasRet);
            if(retValue==-1&&!top&&v.getViewCount()>1){
                // Special case that should ONLY happen if first view
                // isn't valid (can happen when end position is put at
                // beginning of line.
                child=v.getView(1);
                childBounds=v.getChildAllocation(1,alloc);
                retValue=child.getNextVisualPositionFrom(-1,biasRet[0],
                        childBounds,
                        direction,biasRet);
            }
        }else{
            int increment=(top)?-1:1;
            int childIndex;
            if(b==Position.Bias.Backward&&pos>0){
                childIndex=v.getViewIndex(pos-1,Position.Bias.Forward);
            }else{
                childIndex=v.getViewIndex(pos,Position.Bias.Forward);
            }
            View child=v.getView(childIndex);
            Shape childBounds=v.getChildAllocation(childIndex,alloc);
            retValue=child.getNextVisualPositionFrom(pos,b,childBounds,
                    direction,biasRet);
            if((direction==SwingConstants.EAST||
                    direction==SwingConstants.WEST)&&
                    (v instanceof CompositeView)&&
                    ((CompositeView)v).flipEastAndWestAtEnds(pos,b)){
                increment*=-1;
            }
            childIndex+=increment;
            if(retValue==-1&&childIndex>=0&&
                    childIndex<v.getViewCount()){
                child=v.getView(childIndex);
                childBounds=v.getChildAllocation(childIndex,alloc);
                retValue=child.getNextVisualPositionFrom(
                        -1,b,childBounds,direction,biasRet);
                // If there is a bias change, it is a fake position
                // and we should skip it. This is usually the result
                // of two elements side be side flowing the same way.
                if(retValue==pos&&biasRet[0]!=b){
                    return getNextVisualPositionFrom(v,pos,biasRet[0],
                            alloc,direction,
                            biasRet);
                }
            }else if(retValue!=-1&&biasRet[0]!=b&&
                    ((increment==1&&child.getEndOffset()==retValue)||
                            (increment==-1&&
                                    child.getStartOffset()==retValue))&&
                    childIndex>=0&&childIndex<v.getViewCount()){
                // Reached the end of a view, make sure the next view
                // is a different direction.
                child=v.getView(childIndex);
                childBounds=v.getChildAllocation(childIndex,alloc);
                Position.Bias originalBias=biasRet[0];
                int nextPos=child.getNextVisualPositionFrom(
                        -1,b,childBounds,direction,biasRet);
                if(biasRet[0]==b){
                    retValue=nextPos;
                }else{
                    biasRet[0]=originalBias;
                }
            }
        }
        return retValue;
    }
}
