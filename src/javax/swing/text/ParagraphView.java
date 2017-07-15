/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Arrays;

public class ParagraphView extends FlowView implements TabExpander{
    static Class i18nStrategy;
    static char[] tabChars;
    static char[] tabDecimalChars;

    static{
        tabChars=new char[1];
        tabChars[0]='\t';
        tabDecimalChars=new char[2];
        tabDecimalChars[0]='\t';
        tabDecimalChars[1]='.';
    }

    protected int firstLineIndent=0;
    // --- variables -----------------------------------------------
    private int justification;
    private float lineSpacing;
    private int tabBase;    protected int getNextNorthSouthVisualPositionFrom(int pos,Position.Bias b,
                                                      Shape a,int direction,
                                                      Position.Bias[] biasRet)
            throws BadLocationException{
        int vIndex;
        if(pos==-1){
            vIndex=(direction==NORTH)?
                    getViewCount()-1:0;
        }else{
            if(b==Position.Bias.Backward&&pos>0){
                vIndex=getViewIndexAtPosition(pos-1);
            }else{
                vIndex=getViewIndexAtPosition(pos);
            }
            if(direction==NORTH){
                if(vIndex==0){
                    return -1;
                }
                vIndex--;
            }else if(++vIndex>=getViewCount()){
                return -1;
            }
        }
        // vIndex gives index of row to look in.
        JTextComponent text=(JTextComponent)getContainer();
        Caret c=text.getCaret();
        Point magicPoint;
        magicPoint=(c!=null)?c.getMagicCaretPosition():null;
        int x;
        if(magicPoint==null){
            Shape posBounds;
            try{
                posBounds=text.getUI().modelToView(text,pos,b);
            }catch(BadLocationException exc){
                posBounds=null;
            }
            if(posBounds==null){
                x=0;
            }else{
                x=posBounds.getBounds().x;
            }
        }else{
            x=magicPoint.x;
        }
        return getClosestPositionTo(pos,b,a,direction,biasRet,vIndex,x);
    }

    public ParagraphView(Element elem){
        super(elem,View.Y_AXIS);
        setPropertiesFromAttributes();
        Document doc=elem.getDocument();
        Object i18nFlag=doc.getProperty(AbstractDocument.I18NProperty);
        if((i18nFlag!=null)&&i18nFlag.equals(Boolean.TRUE)){
            try{
                if(i18nStrategy==null){
                    // the classname should probably come from a property file.
                    String classname="javax.swing.text.TextLayoutStrategy";
                    ClassLoader loader=getClass().getClassLoader();
                    if(loader!=null){
                        i18nStrategy=loader.loadClass(classname);
                    }else{
                        i18nStrategy=Class.forName(classname);
                    }
                }
                Object o=i18nStrategy.newInstance();
                if(o instanceof FlowStrategy){
                    strategy=(FlowStrategy)o;
                }
            }catch(Throwable e){
                throw new StateInvariantError("ParagraphView: Can't create i18n strategy: "
                        +e.getMessage());
            }
        }
    }    // NOTE: This will not properly work if ParagraphView contains
    // other ParagraphViews. It won't raise, but this does not message
    // the children views with getNextVisualPositionFrom.
    protected int getClosestPositionTo(int pos,Position.Bias b,Shape a,
                                       int direction,Position.Bias[] biasRet,
                                       int rowIndex,int x)
            throws BadLocationException{
        JTextComponent text=(JTextComponent)getContainer();
        Document doc=getDocument();
        View row=getView(rowIndex);
        int lastPos=-1;
        // This could be made better to check backward positions too.
        biasRet[0]=Position.Bias.Forward;
        for(int vc=0, numViews=row.getViewCount();vc<numViews;vc++){
            View v=row.getView(vc);
            int start=v.getStartOffset();
            boolean ltr=AbstractDocument.isLeftToRight(doc,start,start+1);
            if(ltr){
                lastPos=start;
                for(int end=v.getEndOffset();lastPos<end;lastPos++){
                    float xx=text.modelToView(lastPos).getBounds().x;
                    if(xx>=x){
                        while(++lastPos<end&&
                                text.modelToView(lastPos).getBounds().x==xx){
                        }
                        return --lastPos;
                    }
                }
                lastPos--;
            }else{
                for(lastPos=v.getEndOffset()-1;lastPos>=start;
                    lastPos--){
                    float xx=text.modelToView(lastPos).getBounds().x;
                    if(xx>=x){
                        while(--lastPos>=start&&
                                text.modelToView(lastPos).getBounds().x==xx){
                        }
                        return ++lastPos;
                    }
                }
                lastPos++;
            }
        }
        if(lastPos==-1){
            return getStartOffset();
        }
        return lastPos;
    }

    protected void setPropertiesFromAttributes(){
        AttributeSet attr=getAttributes();
        if(attr!=null){
            setParagraphInsets(attr);
            Integer a=(Integer)attr.getAttribute(StyleConstants.Alignment);
            int alignment;
            if(a==null){
                Document doc=getElement().getDocument();
                Object o=doc.getProperty(TextAttribute.RUN_DIRECTION);
                if((o!=null)&&o.equals(TextAttribute.RUN_DIRECTION_RTL)){
                    alignment=StyleConstants.ALIGN_RIGHT;
                }else{
                    alignment=StyleConstants.ALIGN_LEFT;
                }
            }else{
                alignment=a.intValue();
            }
            setJustification(alignment);
            setLineSpacing(StyleConstants.getLineSpacing(attr));
            setFirstLineIndent(StyleConstants.getFirstLineIndent(attr));
        }
    }    protected boolean flipEastAndWestAtEnds(int position,
                                            Position.Bias bias){
        Document doc=getDocument();
        position=getStartOffset();
        return !AbstractDocument.isLeftToRight(doc,position,position+1);
    }
    // --- FlowView methods ---------------------------------------------

    protected void setJustification(int j){
        justification=j;
    }

    protected void setLineSpacing(float ls){
        lineSpacing=ls;
    }

    protected void setFirstLineIndent(float fi){
        firstLineIndent=(int)fi;
    }
    // --- TabExpander methods ------------------------------------------

    protected int getLayoutViewCount(){
        return layoutPool.getViewCount();
    }

    protected View getLayoutView(int index){
        return layoutPool.getView(index);
    }

    public int getFlowSpan(int index){
        View child=getView(index);
        int adjust=0;
        if(child instanceof Row){
            Row row=(Row)child;
            adjust=row.getLeftInset()+row.getRightInset();
        }
        return (layoutSpan==Integer.MAX_VALUE)?layoutSpan
                :(layoutSpan-adjust);
    }

    public int getFlowStart(int index){
        View child=getView(index);
        int adjust=0;
        if(child instanceof Row){
            Row row=(Row)child;
            adjust=row.getLeftInset();
        }
        return tabBase+adjust;
    }

    protected View createRow(){
        return new Row(getElement());
    }
    // ---- View methods ----------------------------------------------------

    @Override
    protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                              SizeRequirements r){
        r=super.calculateMinorAxisRequirements(axis,r);
        float min=0;
        float glue=0;
        int n=getLayoutViewCount();
        for(int i=0;i<n;i++){
            View v=getLayoutView(i);
            float span=v.getMinimumSpan(axis);
            if(v.getBreakWeight(axis,0,v.getMaximumSpan(axis))>View.BadBreakWeight){
                // find the longest non-breakable fragments at the view edges
                int p0=v.getStartOffset();
                int p1=v.getEndOffset();
                float start=findEdgeSpan(v,axis,p0,p0,p1);
                float end=findEdgeSpan(v,axis,p1,p0,p1);
                glue+=start;
                min=Math.max(min,Math.max(span,glue));
                glue=end;
            }else{
                // non-breakable view
                glue+=span;
                min=Math.max(min,glue);
            }
        }
        r.minimum=Math.max(r.minimum,(int)min);
        r.preferred=Math.max(r.minimum,r.preferred);
        r.maximum=Math.max(r.preferred,r.maximum);
        return r;
    }

    public void changedUpdate(DocumentEvent changes,Shape a,ViewFactory f){
        // update any property settings stored, and layout should be
        // recomputed
        setPropertiesFromAttributes();
        layoutChanged(X_AXIS);
        layoutChanged(Y_AXIS);
        super.changedUpdate(changes,a,f);
    }    public float getAlignment(int axis){
        switch(axis){
            case Y_AXIS:
                float a=0.5f;
                if(getViewCount()!=0){
                    int paragraphSpan=(int)getPreferredSpan(View.Y_AXIS);
                    View v=getView(0);
                    int rowSpan=(int)v.getPreferredSpan(View.Y_AXIS);
                    a=(paragraphSpan!=0)?((float)(rowSpan/2))/paragraphSpan:0;
                }
                return a;
            case X_AXIS:
                return 0.5f;
            default:
                throw new IllegalArgumentException("Invalid axis: "+axis);
        }
    }

    public float nextTabStop(float x,int tabOffset){
        // If the text isn't left justified, offset by 10 pixels!
        if(justification!=StyleConstants.ALIGN_LEFT)
            return x+10.0f;
        x-=tabBase;
        TabSet tabs=getTabSet();
        if(tabs==null){
            // a tab every 72 pixels.
            return (float)(tabBase+(((int)x/72+1)*72));
        }
        TabStop tab=tabs.getTabAfter(x+.01f);
        if(tab==null){
            // no tab, do a default of 5 pixels.
            // Should this cause a wrapping of the line?
            return tabBase+x+5.0f;
        }
        int alignment=tab.getAlignment();
        int offset;
        switch(alignment){
            default:
            case TabStop.ALIGN_LEFT:
                // Simple case, left tab.
                return tabBase+tab.getPosition();
            case TabStop.ALIGN_BAR:
                // PENDING: what does this mean?
                return tabBase+tab.getPosition();
            case TabStop.ALIGN_RIGHT:
            case TabStop.ALIGN_CENTER:
                offset=findOffsetToCharactersInString(tabChars,
                        tabOffset+1);
                break;
            case TabStop.ALIGN_DECIMAL:
                offset=findOffsetToCharactersInString(tabDecimalChars,
                        tabOffset+1);
                break;
        }
        if(offset==-1){
            offset=getEndOffset();
        }
        float charsSize=getPartialSize(tabOffset+1,offset);
        switch(alignment){
            case TabStop.ALIGN_RIGHT:
            case TabStop.ALIGN_DECIMAL:
                // right and decimal are treated the same way, the new
                // position will be the location of the tab less the
                // partialSize.
                return tabBase+Math.max(x,tab.getPosition()-charsSize);
            case TabStop.ALIGN_CENTER:
                // Similar to right, but half the partialSize.
                return tabBase+Math.max(x,tab.getPosition()-charsSize/2.0f);
        }
        // will never get here!
        return x;
    }

    protected TabSet getTabSet(){
        return StyleConstants.getTabSet(getElement().getAttributes());
    }

    protected float getPartialSize(int startOffset,int endOffset){
        float size=0.0f;
        int viewIndex;
        int numViews=getViewCount();
        View view;
        int viewEnd;
        int tempEnd;
        // Have to search layoutPool!
        // PENDING: when ParagraphView supports breaking location
        // into layoutPool will have to change!
        viewIndex=getElement().getElementIndex(startOffset);
        numViews=layoutPool.getViewCount();
        while(startOffset<endOffset&&viewIndex<numViews){
            view=layoutPool.getView(viewIndex++);
            viewEnd=view.getEndOffset();
            tempEnd=Math.min(endOffset,viewEnd);
            if(view instanceof TabableView)
                size+=((TabableView)view).getPartialSpan(startOffset,tempEnd);
            else if(startOffset==view.getStartOffset()&&
                    tempEnd==view.getEndOffset())
                size+=view.getPreferredSpan(View.X_AXIS);
            else
                // PENDING: should we handle this better?
                return 0.0f;
            startOffset=viewEnd;
        }
        return size;
    }

    protected int findOffsetToCharactersInString(char[] string,
                                                 int start){
        int stringLength=string.length;
        int end=getEndOffset();
        Segment seg=new Segment();
        try{
            getDocument().getText(start,end-start,seg);
        }catch(BadLocationException ble){
            return -1;
        }
        for(int counter=seg.offset, maxCounter=seg.offset+seg.count;
            counter<maxCounter;counter++){
            char currentChar=seg.array[counter];
            for(int subCounter=0;subCounter<stringLength;
                subCounter++){
                if(currentChar==string[subCounter])
                    return counter-seg.offset+start;
            }
        }
        // No match.
        return -1;
    }

    protected float getTabBase(){
        return (float)tabBase;
    }

    public void paint(Graphics g,Shape a){
        Rectangle alloc=(a instanceof Rectangle)?(Rectangle)a:a.getBounds();
        tabBase=alloc.x+getLeftInset();
        super.paint(g,a);
        // line with the negative firstLineIndent value needs
        // special handling
        if(firstLineIndent<0){
            Shape sh=getChildAllocation(0,a);
            if((sh!=null)&&sh.intersects(alloc)){
                int x=alloc.x+getLeftInset()+firstLineIndent;
                int y=alloc.y+getTopInset();
                Rectangle clip=g.getClipBounds();
                tempRect.x=x+getOffset(X_AXIS,0);
                tempRect.y=y+getOffset(Y_AXIS,0);
                tempRect.width=getSpan(X_AXIS,0)-firstLineIndent;
                tempRect.height=getSpan(Y_AXIS,0);
                if(tempRect.intersects(clip)){
                    tempRect.x=tempRect.x-firstLineIndent;
                    paintChild(g,tempRect,0);
                }
            }
        }
    }

    public View breakView(int axis,float len,Shape a){
        if(axis==View.Y_AXIS){
            if(a!=null){
                Rectangle alloc=a.getBounds();
                setSize(alloc.width,alloc.height);
            }
            // Determine what row to break on.
            // PENDING(prinz) add break support
            return this;
        }
        return this;
    }

    public int getBreakWeight(int axis,float len){
        if(axis==View.Y_AXIS){
            // PENDING(prinz) make this return a reasonable value
            // when paragraph breaking support is re-implemented.
            // If less than one row, bad weight value should be
            // returned.
            //return GoodBreakWeight;
            return BadBreakWeight;
        }
        return BadBreakWeight;
    }

    private float findEdgeSpan(View v,int axis,int fp,int p0,int p1){
        int len=p1-p0;
        if(len<=1){
            // further fragmentation is not possible
            return v.getMinimumSpan(axis);
        }else{
            int mid=p0+len/2;
            boolean startEdge=mid>fp;
            // initial view is breakable hence must support fragmentation
            View f=startEdge?
                    v.createFragment(fp,mid):v.createFragment(mid,fp);
            boolean breakable=f.getBreakWeight(
                    axis,0,f.getMaximumSpan(axis))>View.BadBreakWeight;
            if(breakable==startEdge){
                p1=mid;
            }else{
                p0=mid;
            }
            return findEdgeSpan(f,axis,fp,p0,p1);
        }
    }

    class Row extends BoxView{
        final static int SPACE_ADDON=0;
        final static int SPACE_ADDON_LEFTOVER_END=1;
        final static int START_JUSTIFIABLE=2;        public AttributeSet getAttributes(){
            View p=getParent();
            return (p!=null)?p.getAttributes():null;
        }
        //this should be the last index in justificationData
        final static int END_JUSTIFIABLE=3;        public float getAlignment(int axis){
            if(axis==View.X_AXIS){
                switch(justification){
                    case StyleConstants.ALIGN_LEFT:
                        return 0;
                    case StyleConstants.ALIGN_RIGHT:
                        return 1;
                    case StyleConstants.ALIGN_CENTER:
                        return 0.5f;
                    case StyleConstants.ALIGN_JUSTIFIED:
                        float rv=0.5f;
                        //if we can justifiy the content always align to
                        //the left.
                        if(isJustifiableDocument()){
                            rv=0f;
                        }
                        return rv;
                }
            }
            return super.getAlignment(axis);
        }
        int justificationData[]=null;        public Shape modelToView(int pos,Shape a,Position.Bias b) throws BadLocationException{
            Rectangle r=a.getBounds();
            View v=getViewAtPosition(pos,r);
            if((v!=null)&&(!v.getElement().isLeaf())){
                // Don't adjust the height if the view represents a branch.
                return super.modelToView(pos,a,b);
            }
            r=a.getBounds();
            int height=r.height;
            int y=r.y;
            Shape loc=super.modelToView(pos,a,b);
            r=loc.getBounds();
            r.height=height;
            r.y=y;
            return r;
        }

        Row(Element elem){
            super(elem,View.X_AXIS);
        }        public int getStartOffset(){
            int offs=Integer.MAX_VALUE;
            int n=getViewCount();
            for(int i=0;i<n;i++){
                View v=getView(i);
                offs=Math.min(offs,v.getStartOffset());
            }
            return offs;
        }

        protected void loadChildren(ViewFactory f){
        }        public int getEndOffset(){
            int offs=0;
            int n=getViewCount();
            for(int i=0;i<n;i++){
                View v=getView(i);
                offs=Math.max(offs,v.getEndOffset());
            }
            return offs;
        }

        protected int getViewIndexAtPosition(int pos){
            // This is expensive, but are views are not necessarily layed
            // out in model order.
            if(pos<getStartOffset()||pos>=getEndOffset())
                return -1;
            for(int counter=getViewCount()-1;counter>=0;counter--){
                View v=getView(counter);
                if(pos>=v.getStartOffset()&&
                        pos<v.getEndOffset()){
                    return counter;
                }
            }
            return -1;
        }        protected void layoutMinorAxis(int targetSpan,int axis,int[] offsets,int[] spans){
            baselineLayout(targetSpan,axis,offsets,spans);
        }

        protected short getLeftInset(){
            View parentView;
            int adjustment=0;
            if((parentView=getParent())!=null){ //use firstLineIdent for the first row
                if(this==parentView.getView(0)){
                    adjustment=firstLineIndent;
                }
            }
            return (short)(super.getLeftInset()+adjustment);
        }        protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                                  SizeRequirements r){
            return baselineRequirements(axis,r);
        }

        protected short getBottomInset(){
            return (short)(super.getBottomInset()+
                    ((minorRequest!=null)?minorRequest.preferred:0)*
                            lineSpacing);
        }        private boolean isLastRow(){
            View parent;
            return ((parent=getParent())==null
                    ||this==parent.getView(parent.getViewCount()-1));
        }

        private boolean isBrokenRow(){
            boolean rv=false;
            int viewsCount=getViewCount();
            if(viewsCount>0){
                View lastView=getView(viewsCount-1);
                if(lastView.getBreakWeight(X_AXIS,0,0)>=
                        ForcedBreakWeight){
                    rv=true;
                }
            }
            return rv;
        }

        private boolean isJustifiableDocument(){
            return (!Boolean.TRUE.equals(getDocument().getProperty(
                    AbstractDocument.I18NProperty)));
        }

        private boolean isJustifyEnabled(){
            boolean ret=(justification==StyleConstants.ALIGN_JUSTIFIED);
            //no justification for i18n documents
            ret=ret&&isJustifiableDocument();
            //no justification for the last row
            ret=ret&&!isLastRow();
            //no justification for the broken rows
            ret=ret&&!isBrokenRow();
            return ret;
        }

        //Calls super method after setting spaceAddon to 0.
        //Justification should not affect MajorAxisRequirements
        @Override
        protected SizeRequirements calculateMajorAxisRequirements(int axis,
                                                                  SizeRequirements r){
            int oldJustficationData[]=justificationData;
            justificationData=null;
            SizeRequirements ret=super.calculateMajorAxisRequirements(axis,r);
            if(isJustifyEnabled()){
                justificationData=oldJustficationData;
            }
            return ret;
        }

        @Override
        protected void layoutMajorAxis(int targetSpan,int axis,
                                       int[] offsets,int[] spans){
            int oldJustficationData[]=justificationData;
            justificationData=null;
            super.layoutMajorAxis(targetSpan,axis,offsets,spans);
            if(!isJustifyEnabled()){
                return;
            }
            int currentSpan=0;
            for(int span : spans){
                currentSpan+=span;
            }
            if(currentSpan==targetSpan){
                //no need to justify
                return;
            }
            // we justify text by enlarging spaces by the {@code spaceAddon}.
            // justification is started to the right of the rightmost TAB.
            // leading and trailing spaces are not extendable.
            //
            // GlyphPainter1 uses
            // justificationData
            // for all painting and measurement.
            int extendableSpaces=0;
            int startJustifiableContent=-1;
            int endJustifiableContent=-1;
            int lastLeadingSpaces=0;
            int rowStartOffset=getStartOffset();
            int rowEndOffset=getEndOffset();
            int spaceMap[]=new int[rowEndOffset-rowStartOffset];
            Arrays.fill(spaceMap,0);
            for(int i=getViewCount()-1;i>=0;i--){
                View view=getView(i);
                if(view instanceof GlyphView){
                    GlyphView.JustificationInfo justificationInfo=
                            ((GlyphView)view).getJustificationInfo(rowStartOffset);
                    final int viewStartOffset=view.getStartOffset();
                    final int offset=viewStartOffset-rowStartOffset;
                    for(int j=0;j<justificationInfo.spaceMap.length();j++){
                        if(justificationInfo.spaceMap.get(j)){
                            spaceMap[j+offset]=1;
                        }
                    }
                    if(startJustifiableContent>0){
                        if(justificationInfo.end>=0){
                            extendableSpaces+=justificationInfo.trailingSpaces;
                        }else{
                            lastLeadingSpaces+=justificationInfo.trailingSpaces;
                        }
                    }
                    if(justificationInfo.start>=0){
                        startJustifiableContent=
                                justificationInfo.start+viewStartOffset;
                        extendableSpaces+=lastLeadingSpaces;
                    }
                    if(justificationInfo.end>=0
                            &&endJustifiableContent<0){
                        endJustifiableContent=
                                justificationInfo.end+viewStartOffset;
                    }
                    extendableSpaces+=justificationInfo.contentSpaces;
                    lastLeadingSpaces=justificationInfo.leadingSpaces;
                    if(justificationInfo.hasTab){
                        break;
                    }
                }
            }
            if(extendableSpaces<=0){
                //there is nothing we can do to justify
                return;
            }
            int adjustment=(targetSpan-currentSpan);
            int spaceAddon=(extendableSpaces>0)
                    ?adjustment/extendableSpaces
                    :0;
            int spaceAddonLeftoverEnd=-1;
            for(int i=startJustifiableContent-rowStartOffset,
                leftover=adjustment-spaceAddon*extendableSpaces;
                leftover>0;
                leftover-=spaceMap[i],
                        i++){
                spaceAddonLeftoverEnd=i;
            }
            if(spaceAddon>0||spaceAddonLeftoverEnd>=0){
                justificationData=(oldJustficationData!=null)
                        ?oldJustficationData
                        :new int[END_JUSTIFIABLE+1];
                justificationData[SPACE_ADDON]=spaceAddon;
                justificationData[SPACE_ADDON_LEFTOVER_END]=
                        spaceAddonLeftoverEnd;
                justificationData[START_JUSTIFIABLE]=
                        startJustifiableContent-rowStartOffset;
                justificationData[END_JUSTIFIABLE]=
                        endJustifiableContent-rowStartOffset;
                super.layoutMajorAxis(targetSpan,axis,offsets,spans);
            }
        }

        //for justified row we assume the maximum horizontal span
        //is MAX_VALUE.
        @Override
        public float getMaximumSpan(int axis){
            float ret;
            if(View.X_AXIS==axis
                    &&isJustifyEnabled()){
                ret=Float.MAX_VALUE;
            }else{
                ret=super.getMaximumSpan(axis);
            }
            return ret;
        }












    }






}
