/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.lang.ref.SoftReference;

public class WrappedPlainView extends BoxView implements TabExpander{
    // --- variables -------------------------------------------
    FontMetrics metrics;
    Segment lineBuffer;
    boolean widthChanging;
    int tabBase;
    int tabSize;
    boolean wordWrap;
    int sel0;
    int sel1;
    Color unselected;
    Color selected;

    public WrappedPlainView(Element elem){
        this(elem,false);
    }    void updateChildren(DocumentEvent e,Shape a){
        Element elem=getElement();
        DocumentEvent.ElementChange ec=e.getChange(elem);
        if(ec!=null){
            // the structure of this element changed.
            Element[] removedElems=ec.getChildrenRemoved();
            Element[] addedElems=ec.getChildrenAdded();
            View[] added=new View[addedElems.length];
            for(int i=0;i<addedElems.length;i++){
                added[i]=new WrappedLine(addedElems[i]);
            }
            replace(ec.getIndex(),removedElems.length,added);
            // should damge a little more intelligently.
            if(a!=null){
                preferenceChanged(null,true,true);
                getContainer().repaint();
            }
        }
        // update font metrics which may be used by the child views
        updateMetrics();
    }

    public WrappedPlainView(Element elem,boolean wordWrap){
        super(elem,Y_AXIS);
        this.wordWrap=wordWrap;
    }

    protected void drawLine(int p0,int p1,Graphics g,int x,int y){
        Element lineMap=getElement();
        Element line=lineMap.getElement(lineMap.getElementIndex(p0));
        Element elem;
        try{
            if(line.isLeaf()){
                drawText(line,p0,p1,g,x,y);
            }else{
                // this line contains the composed text.
                int idx=line.getElementIndex(p0);
                int lastIdx=line.getElementIndex(p1);
                for(;idx<=lastIdx;idx++){
                    elem=line.getElement(idx);
                    int start=Math.max(elem.getStartOffset(),p0);
                    int end=Math.min(elem.getEndOffset(),p1);
                    x=drawText(elem,start,end,g,x,y);
                }
            }
        }catch(BadLocationException e){
            throw new StateInvariantError("Can't render: "+p0+","+p1);
        }
    }
    // --- TabExpander methods ------------------------------------------

    private int drawText(Element elem,int p0,int p1,Graphics g,int x,int y) throws BadLocationException{
        p1=Math.min(getDocument().getLength(),p1);
        AttributeSet attr=elem.getAttributes();
        if(Utilities.isComposedTextAttributeDefined(attr)){
            g.setColor(unselected);
            x=Utilities.drawComposedText(this,attr,g,x,y,
                    p0-elem.getStartOffset(),
                    p1-elem.getStartOffset());
        }else{
            if(sel0==sel1||selected==unselected){
                // no selection, or it is invisible
                x=drawUnselectedText(g,x,y,p0,p1);
            }else if((p0>=sel0&&p0<=sel1)&&(p1>=sel0&&p1<=sel1)){
                x=drawSelectedText(g,x,y,p0,p1);
            }else if(sel0>=p0&&sel0<=p1){
                if(sel1>=p0&&sel1<=p1){
                    x=drawUnselectedText(g,x,y,p0,sel0);
                    x=drawSelectedText(g,x,y,sel0,sel1);
                    x=drawUnselectedText(g,x,y,sel1,p1);
                }else{
                    x=drawUnselectedText(g,x,y,p0,sel0);
                    x=drawSelectedText(g,x,y,sel0,p1);
                }
            }else if(sel1>=p0&&sel1<=p1){
                x=drawSelectedText(g,x,y,p0,sel1);
                x=drawUnselectedText(g,x,y,sel1,p1);
            }else{
                x=drawUnselectedText(g,x,y,p0,p1);
            }
        }
        return x;
    }
    // --- View methods -------------------------------------

    protected int drawUnselectedText(Graphics g,int x,int y,
                                     int p0,int p1) throws BadLocationException{
        g.setColor(unselected);
        Document doc=getDocument();
        Segment segment=SegmentCache.getSharedSegment();
        doc.getText(p0,p1-p0,segment);
        int ret=Utilities.drawTabbedText(this,segment,x,y,g,this,p0);
        SegmentCache.releaseSharedSegment(segment);
        return ret;
    }

    protected int drawSelectedText(Graphics g,int x,
                                   int y,int p0,int p1) throws BadLocationException{
        g.setColor(selected);
        Document doc=getDocument();
        Segment segment=SegmentCache.getSharedSegment();
        doc.getText(p0,p1-p0,segment);
        int ret=Utilities.drawTabbedText(this,segment,x,y,g,this,p0);
        SegmentCache.releaseSharedSegment(segment);
        return ret;
    }

    protected final Segment getLineBuffer(){
        if(lineBuffer==null){
            lineBuffer=new Segment();
        }
        return lineBuffer;
    }

    protected int calculateBreakPosition(int p0,int p1){
        int p;
        Segment segment=SegmentCache.getSharedSegment();
        loadText(segment,p0,p1);
        int currentWidth=getWidth();
        if(wordWrap){
            p=p0+Utilities.getBreakLocation(segment,metrics,
                    tabBase,tabBase+currentWidth,
                    this,p0);
        }else{
            p=p0+Utilities.getTabbedTextOffset(segment,metrics,
                    tabBase,tabBase+currentWidth,
                    this,p0,false);
        }
        SegmentCache.releaseSharedSegment(segment);
        return p;
    }

    final void loadText(Segment segment,int p0,int p1){
        try{
            Document doc=getDocument();
            doc.getText(p0,p1-p0,segment);
        }catch(BadLocationException bl){
            throw new StateInvariantError("Can't get line text");
        }
    }

    protected void loadChildren(ViewFactory f){
        Element e=getElement();
        int n=e.getElementCount();
        if(n>0){
            View[] added=new View[n];
            for(int i=0;i<n;i++){
                added[i]=new WrappedLine(e.getElement(i));
            }
            replace(0,0,added);
        }
    }    public void insertUpdate(DocumentEvent e,Shape a,ViewFactory f){
        updateChildren(e,a);
        Rectangle alloc=((a!=null)&&isAllocationValid())?
                getInsideAllocation(a):null;
        int pos=e.getOffset();
        View v=getViewAtPosition(pos,alloc);
        if(v!=null){
            v.insertUpdate(e,alloc,f);
        }
    }

    public float nextTabStop(float x,int tabOffset){
        if(tabSize==0)
            return x;
        int ntabs=((int)x-tabBase)/tabSize;
        return tabBase+((ntabs+1)*tabSize);
    }    public void removeUpdate(DocumentEvent e,Shape a,ViewFactory f){
        updateChildren(e,a);
        Rectangle alloc=((a!=null)&&isAllocationValid())?
                getInsideAllocation(a):null;
        int pos=e.getOffset();
        View v=getViewAtPosition(pos,alloc);
        if(v!=null){
            v.removeUpdate(e,alloc,f);
        }
    }

    public void setSize(float width,float height){
        updateMetrics();
        if((int)width!=getWidth()){
            // invalidate the view itself since the desired widths
            // of the children will be based upon this views width.
            preferenceChanged(null,true,true);
            widthChanging=true;
        }
        super.setSize(width,height);
        widthChanging=false;
    }    public void changedUpdate(DocumentEvent e,Shape a,ViewFactory f){
        updateChildren(e,a);
    }

    final void updateMetrics(){
        Component host=getContainer();
        Font f=host.getFont();
        metrics=host.getFontMetrics(f);
        tabSize=getTabSize()*metrics.charWidth('m');
    }

    protected int getTabSize(){
        Integer i=(Integer)getDocument().getProperty(PlainDocument.tabSizeAttribute);
        int size=(i!=null)?i.intValue():8;
        return size;
    }

    public void paint(Graphics g,Shape a){
        Rectangle alloc=(Rectangle)a;
        tabBase=alloc.x;
        JTextComponent host=(JTextComponent)getContainer();
        sel0=host.getSelectionStart();
        sel1=host.getSelectionEnd();
        unselected=(host.isEnabled())?
                host.getForeground():host.getDisabledTextColor();
        Caret c=host.getCaret();
        selected=c.isSelectionVisible()&&host.getHighlighter()!=null?
                host.getSelectedTextColor():unselected;
        g.setFont(host.getFont());
        // superclass paints the children
        super.paint(g,a);
    }

    public float getPreferredSpan(int axis){
        updateMetrics();
        return super.getPreferredSpan(axis);
    }

    public float getMinimumSpan(int axis){
        updateMetrics();
        return super.getMinimumSpan(axis);
    }

    public float getMaximumSpan(int axis){
        updateMetrics();
        return super.getMaximumSpan(axis);
    }

    class WrappedLine extends View{
        int lineCount;
        SoftReference<int[]> lineCache=null;

        WrappedLine(Element elem){
            super(elem);
            lineCount=-1;
        }

        public float getPreferredSpan(int axis){
            switch(axis){
                case View.X_AXIS:
                    float width=getWidth();
                    if(width==Integer.MAX_VALUE){
                        // We have been initially set to MAX_VALUE, but we don't
                        // want this as our preferred.
                        return 100f;
                    }
                    return width;
                case View.Y_AXIS:
                    if(lineCount<0||widthChanging){
                        breakLines(getStartOffset());
                    }
                    return lineCount*metrics.getHeight();
                default:
                    throw new IllegalArgumentException("Invalid axis: "+axis);
            }
        }

        public void paint(Graphics g,Shape a){
            Rectangle alloc=(Rectangle)a;
            int y=alloc.y+metrics.getAscent();
            int x=alloc.x;
            JTextComponent host=(JTextComponent)getContainer();
            Highlighter h=host.getHighlighter();
            LayeredHighlighter dh=(h instanceof LayeredHighlighter)?
                    (LayeredHighlighter)h:null;
            int start=getStartOffset();
            int end=getEndOffset();
            int p0=start;
            int[] lineEnds=getLineEnds();
            for(int i=0;i<lineCount;i++){
                int p1=(lineEnds==null)?end:
                        start+lineEnds[i];
                if(dh!=null){
                    int hOffset=(p1==end)
                            ?(p1-1)
                            :p1;
                    dh.paintLayeredHighlights(g,p0,hOffset,a,host,this);
                }
                drawLine(p0,p1,g,x,y);
                p0=p1;
                y+=metrics.getHeight();
            }
        }

        public Shape modelToView(int pos,Shape a,Position.Bias b)
                throws BadLocationException{
            Rectangle alloc=a.getBounds();
            alloc.height=metrics.getHeight();
            alloc.width=1;
            int p0=getStartOffset();
            if(pos<p0||pos>getEndOffset()){
                throw new BadLocationException("Position out of range",pos);
            }
            int testP=(b==Position.Bias.Forward)?pos:
                    Math.max(p0,pos-1);
            int line=0;
            int[] lineEnds=getLineEnds();
            if(lineEnds!=null){
                line=findLine(testP-p0);
                if(line>0){
                    p0+=lineEnds[line-1];
                }
                alloc.y+=alloc.height*line;
            }
            if(pos>p0){
                Segment segment=SegmentCache.getSharedSegment();
                loadText(segment,p0,pos);
                alloc.x+=Utilities.getTabbedTextWidth(segment,metrics,
                        alloc.x,WrappedPlainView.this,p0);
                SegmentCache.releaseSharedSegment(segment);
            }
            return alloc;
        }        public void insertUpdate(DocumentEvent e,Shape a,ViewFactory f){
            update(e,a);
        }

        public int viewToModel(float fx,float fy,Shape a,Position.Bias[] bias){
            // PENDING(prinz) implement bias properly
            bias[0]=Position.Bias.Forward;
            Rectangle alloc=(Rectangle)a;
            int x=(int)fx;
            int y=(int)fy;
            if(y<alloc.y){
                // above the area covered by this icon, so the the position
                // is assumed to be the start of the coverage for this view.
                return getStartOffset();
            }else if(y>alloc.y+alloc.height){
                // below the area covered by this icon, so the the position
                // is assumed to be the end of the coverage for this view.
                return getEndOffset()-1;
            }else{
                // positioned within the coverage of this view vertically,
                // so we figure out which line the point corresponds to.
                // if the line is greater than the number of lines contained, then
                // simply use the last line as it represents the last possible place
                // we can position to.
                alloc.height=metrics.getHeight();
                int line=(alloc.height>0?
                        (y-alloc.y)/alloc.height:lineCount-1);
                if(line>=lineCount){
                    return getEndOffset()-1;
                }else{
                    int p0=getStartOffset();
                    int p1;
                    if(lineCount==1){
                        p1=getEndOffset();
                    }else{
                        int[] lineEnds=getLineEnds();
                        p1=p0+lineEnds[line];
                        if(line>0){
                            p0+=lineEnds[line-1];
                        }
                    }
                    if(x<alloc.x){
                        // point is to the left of the line
                        return p0;
                    }else if(x>alloc.x+alloc.width){
                        // point is to the right of the line
                        return p1-1;
                    }else{
                        // Determine the offset into the text
                        Segment segment=SegmentCache.getSharedSegment();
                        loadText(segment,p0,p1);
                        int n=Utilities.getTabbedTextOffset(segment,metrics,
                                alloc.x,x,
                                WrappedPlainView.this,p0);
                        SegmentCache.releaseSharedSegment(segment);
                        return Math.min(p0+n,p1-1);
                    }
                }
            }
        }        public void removeUpdate(DocumentEvent e,Shape a,ViewFactory f){
            update(e,a);
        }

        private void update(DocumentEvent ev,Shape a){
            int oldCount=lineCount;
            breakLines(ev.getOffset());
            if(oldCount!=lineCount){
                WrappedPlainView.this.preferenceChanged(this,false,true);
                // have to repaint any views after the receiver.
                getContainer().repaint();
            }else if(a!=null){
                Component c=getContainer();
                Rectangle alloc=(Rectangle)a;
                c.repaint(alloc.x,alloc.y,alloc.width,alloc.height);
            }
        }

        final int[] getLineEnds(){
            if(lineCache==null){
                return null;
            }else{
                int[] lineEnds=lineCache.get();
                if(lineEnds==null){
                    // Cache was GC'ed, so rebuild it
                    return breakLines(getStartOffset());
                }else{
                    return lineEnds;
                }
            }
        }

        final int[] breakLines(int startPos){
            int[] lineEnds=(lineCache==null)?null:lineCache.get();
            int[] oldLineEnds=lineEnds;
            int start=getStartOffset();
            int lineIndex=0;
            if(lineEnds!=null){
                lineIndex=findLine(startPos-start);
                if(lineIndex>0){
                    lineIndex--;
                }
            }
            int p0=(lineIndex==0)?start:start+lineEnds[lineIndex-1];
            int p1=getEndOffset();
            while(p0<p1){
                int p=calculateBreakPosition(p0,p1);
                p0=(p==p0)?++p:p;      // 4410243
                if(lineIndex==0&&p0>=p1){
                    // do not use cache if there's only one line
                    lineCache=null;
                    lineEnds=null;
                    lineIndex=1;
                    break;
                }else if(lineEnds==null||lineIndex>=lineEnds.length){
                    // we have 2+ lines, and the cache is not big enough
                    // we try to estimate total number of lines
                    double growFactor=((double)(p1-start)/(p0-start));
                    int newSize=(int)Math.ceil((lineIndex+1)*growFactor);
                    newSize=Math.max(newSize,lineIndex+2);
                    int[] tmp=new int[newSize];
                    if(lineEnds!=null){
                        System.arraycopy(lineEnds,0,tmp,0,lineIndex);
                    }
                    lineEnds=tmp;
                }
                lineEnds[lineIndex++]=p0-start;
            }
            lineCount=lineIndex;
            if(lineCount>1){
                // check if the cache is too big
                int maxCapacity=lineCount+lineCount/3;
                if(lineEnds.length>maxCapacity){
                    int[] tmp=new int[maxCapacity];
                    System.arraycopy(lineEnds,0,tmp,0,lineCount);
                    lineEnds=tmp;
                }
            }
            if(lineEnds!=null&&lineEnds!=oldLineEnds){
                lineCache=new SoftReference<int[]>(lineEnds);
            }
            return lineEnds;
        }

        private int findLine(int offset){
            int[] lineEnds=lineCache.get();
            if(offset<lineEnds[0]){
                return 0;
            }else if(offset>lineEnds[lineCount-1]){
                return lineCount;
            }else{
                return findLine(lineEnds,offset,0,lineCount-1);
            }
        }

        private int findLine(int[] array,int offset,int min,int max){
            if(max-min<=1){
                return max;
            }else{
                int mid=(max+min)/2;
                return (offset<array[mid])?
                        findLine(array,offset,min,mid):
                        findLine(array,offset,mid,max);
            }
        }



    }





}
