/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class BoxView extends CompositeView{
    // --- variables ------------------------------------------------
    int majorAxis;
    int majorSpan;
    int minorSpan;
    boolean majorReqValid;
    boolean minorReqValid;
    SizeRequirements majorRequest;
    // --- View methods ---------------------------------------------
    SizeRequirements minorRequest;
    boolean majorAllocValid;
    int[] majorOffsets;
    int[] majorSpans;
    boolean minorAllocValid;
    int[] minorOffsets;
    int[] minorSpans;
    Rectangle tempRect;

    public BoxView(Element elem,int axis){
        super(elem);
        tempRect=new Rectangle();
        this.majorAxis=axis;
        majorOffsets=new int[0];
        majorSpans=new int[0];
        majorReqValid=false;
        majorAllocValid=false;
        minorOffsets=new int[0];
        minorSpans=new int[0];
        minorReqValid=false;
        minorAllocValid=false;
    }

    public int getAxis(){
        return majorAxis;
    }

    public void setAxis(int axis){
        boolean axisChanged=(axis!=majorAxis);
        majorAxis=axis;
        if(axisChanged){
            preferenceChanged(null,true,true);
        }
    }

    public void layoutChanged(int axis){
        if(axis==majorAxis){
            majorAllocValid=false;
        }else{
            minorAllocValid=false;
        }
    }

    public void replace(int index,int length,View[] elems){
        super.replace(index,length,elems);
        // invalidate cache
        int nInserted=(elems!=null)?elems.length:0;
        majorOffsets=updateLayoutArray(majorOffsets,index,nInserted);
        majorSpans=updateLayoutArray(majorSpans,index,nInserted);
        majorReqValid=false;
        majorAllocValid=false;
        minorOffsets=updateLayoutArray(minorOffsets,index,nInserted);
        minorSpans=updateLayoutArray(minorSpans,index,nInserted);
        minorReqValid=false;
        minorAllocValid=false;
    }

    int[] updateLayoutArray(int[] oldArray,int offset,int nInserted){
        int n=getViewCount();
        int[] newArray=new int[n];
        System.arraycopy(oldArray,0,newArray,0,offset);
        System.arraycopy(oldArray,offset,
                newArray,offset+nInserted,n-nInserted-offset);
        return newArray;
    }

    public Shape getChildAllocation(int index,Shape a){
        if(a!=null){
            Shape ca=super.getChildAllocation(index,a);
            if((ca!=null)&&(!isAllocationValid())){
                // The child allocation may not have been set yet.
                Rectangle r=(ca instanceof Rectangle)?
                        (Rectangle)ca:ca.getBounds();
                if((r.width==0)&&(r.height==0)){
                    return null;
                }
            }
            return ca;
        }
        return null;
    }

    public Shape modelToView(int pos,Shape a,Position.Bias b) throws BadLocationException{
        if(!isAllocationValid()){
            Rectangle alloc=a.getBounds();
            setSize(alloc.width,alloc.height);
        }
        return super.modelToView(pos,a,b);
    }

    public int viewToModel(float x,float y,Shape a,Position.Bias[] bias){
        if(!isAllocationValid()){
            Rectangle alloc=a.getBounds();
            setSize(alloc.width,alloc.height);
        }
        return super.viewToModel(x,y,a,bias);
    }
    // --- local methods ----------------------------------------------------

    protected boolean isBefore(int x,int y,Rectangle innerAlloc){
        if(majorAxis==View.X_AXIS){
            return (x<innerAlloc.x);
        }else{
            return (y<innerAlloc.y);
        }
    }

    protected boolean isAfter(int x,int y,Rectangle innerAlloc){
        if(majorAxis==View.X_AXIS){
            return (x>(innerAlloc.width+innerAlloc.x));
        }else{
            return (y>(innerAlloc.height+innerAlloc.y));
        }
    }

    protected View getViewAtPoint(int x,int y,Rectangle alloc){
        int n=getViewCount();
        if(majorAxis==View.X_AXIS){
            if(x<(alloc.x+majorOffsets[0])){
                childAllocation(0,alloc);
                return getView(0);
            }
            for(int i=0;i<n;i++){
                if(x<(alloc.x+majorOffsets[i])){
                    childAllocation(i-1,alloc);
                    return getView(i-1);
                }
            }
            childAllocation(n-1,alloc);
            return getView(n-1);
        }else{
            if(y<(alloc.y+majorOffsets[0])){
                childAllocation(0,alloc);
                return getView(0);
            }
            for(int i=0;i<n;i++){
                if(y<(alloc.y+majorOffsets[i])){
                    childAllocation(i-1,alloc);
                    return getView(i-1);
                }
            }
            childAllocation(n-1,alloc);
            return getView(n-1);
        }
    }

    protected void childAllocation(int index,Rectangle alloc){
        alloc.x+=getOffset(X_AXIS,index);
        alloc.y+=getOffset(Y_AXIS,index);
        alloc.width=getSpan(X_AXIS,index);
        alloc.height=getSpan(Y_AXIS,index);
    }

    protected boolean flipEastAndWestAtEnds(int position,
                                            Position.Bias bias){
        if(majorAxis==Y_AXIS){
            int testPos=(bias==Position.Bias.Backward)?
                    Math.max(0,position-1):position;
            int index=getViewIndexAtPosition(testPos);
            if(index!=-1){
                View v=getView(index);
                if(v!=null&&v instanceof CompositeView){
                    return ((CompositeView)v).flipEastAndWestAtEnds(position,
                            bias);
                }
            }
        }
        return false;
    }

    protected boolean isAllocationValid(){
        return (majorAllocValid&&minorAllocValid);
    }

    float getSpanOnAxis(int axis){
        if(axis==majorAxis){
            return majorSpan;
        }else{
            return minorSpan;
        }
    }

    public float getPreferredSpan(int axis){
        checkRequests(axis);
        float marginSpan=(axis==X_AXIS)?getLeftInset()+getRightInset():
                getTopInset()+getBottomInset();
        if(axis==majorAxis){
            return ((float)majorRequest.preferred)+marginSpan;
        }else{
            return ((float)minorRequest.preferred)+marginSpan;
        }
    }

    public float getMinimumSpan(int axis){
        checkRequests(axis);
        float marginSpan=(axis==X_AXIS)?getLeftInset()+getRightInset():
                getTopInset()+getBottomInset();
        if(axis==majorAxis){
            return ((float)majorRequest.minimum)+marginSpan;
        }else{
            return ((float)minorRequest.minimum)+marginSpan;
        }
    }

    public float getMaximumSpan(int axis){
        checkRequests(axis);
        float marginSpan=(axis==X_AXIS)?getLeftInset()+getRightInset():
                getTopInset()+getBottomInset();
        if(axis==majorAxis){
            return ((float)majorRequest.maximum)+marginSpan;
        }else{
            return ((float)minorRequest.maximum)+marginSpan;
        }
    }

    public void preferenceChanged(View child,boolean width,boolean height){
        boolean majorChanged=(majorAxis==X_AXIS)?width:height;
        boolean minorChanged=(majorAxis==X_AXIS)?height:width;
        if(majorChanged){
            majorReqValid=false;
            majorAllocValid=false;
        }
        if(minorChanged){
            minorReqValid=false;
            minorAllocValid=false;
        }
        super.preferenceChanged(child,width,height);
    }

    public float getAlignment(int axis){
        checkRequests(axis);
        if(axis==majorAxis){
            return majorRequest.alignment;
        }else{
            return minorRequest.alignment;
        }
    }

    public void paint(Graphics g,Shape allocation){
        Rectangle alloc=(allocation instanceof Rectangle)?
                (Rectangle)allocation:allocation.getBounds();
        int n=getViewCount();
        int x=alloc.x+getLeftInset();
        int y=alloc.y+getTopInset();
        Rectangle clip=g.getClipBounds();
        for(int i=0;i<n;i++){
            tempRect.x=x+getOffset(X_AXIS,i);
            tempRect.y=y+getOffset(Y_AXIS,i);
            tempRect.width=getSpan(X_AXIS,i);
            tempRect.height=getSpan(Y_AXIS,i);
            int trx0=tempRect.x, trx1=trx0+tempRect.width;
            int try0=tempRect.y, try1=try0+tempRect.height;
            int crx0=clip.x, crx1=crx0+clip.width;
            int cry0=clip.y, cry1=cry0+clip.height;
            // We should paint views that intersect with clipping region
            // even if the intersection has no inside points (is a line).
            // This is needed for supporting views that have zero width, like
            // views that contain only combining marks.
            if((trx1>=crx0)&&(try1>=cry0)&&(crx1>=trx0)&&(cry1>=try0)){
                paintChild(g,tempRect,i);
            }
        }
    }

    protected void paintChild(Graphics g,Rectangle alloc,int index){
        View child=getView(index);
        child.paint(g,alloc);
    }

    public int getResizeWeight(int axis){
        checkRequests(axis);
        if(axis==majorAxis){
            if((majorRequest.preferred!=majorRequest.minimum)||
                    (majorRequest.preferred!=majorRequest.maximum)){
                return 1;
            }
        }else{
            if((minorRequest.preferred!=minorRequest.minimum)||
                    (minorRequest.preferred!=minorRequest.maximum)){
                return 1;
            }
        }
        return 0;
    }

    public void setSize(float width,float height){
        layout(Math.max(0,(int)(width-getLeftInset()-getRightInset())),
                Math.max(0,(int)(height-getTopInset()-getBottomInset())));
    }

    protected void forwardUpdate(DocumentEvent.ElementChange ec,
                                 DocumentEvent e,Shape a,ViewFactory f){
        boolean wasValid=isLayoutValid(majorAxis);
        super.forwardUpdate(ec,e,a,f);
        // determine if a repaint is needed
        if(wasValid&&(!isLayoutValid(majorAxis))){
            // Repaint is needed because one of the tiled children
            // have changed their span along the major axis.  If there
            // is a hosting component and an allocated shape we repaint.
            Component c=getContainer();
            if((a!=null)&&(c!=null)){
                int pos=e.getOffset();
                int index=getViewIndexAtPosition(pos);
                Rectangle alloc=getInsideAllocation(a);
                if(majorAxis==X_AXIS){
                    alloc.x+=majorOffsets[index];
                    alloc.width-=majorOffsets[index];
                }else{
                    alloc.y+=minorOffsets[index];
                    alloc.height-=minorOffsets[index];
                }
                c.repaint(alloc.x,alloc.y,alloc.width,alloc.height);
            }
        }
    }

    protected boolean isLayoutValid(int axis){
        if(axis==majorAxis){
            return majorAllocValid;
        }else{
            return minorAllocValid;
        }
    }

    protected void layout(int width,int height){
        setSpanOnAxis(X_AXIS,width);
        setSpanOnAxis(Y_AXIS,height);
    }

    void setSpanOnAxis(int axis,float span){
        if(axis==majorAxis){
            if(majorSpan!=(int)span){
                majorAllocValid=false;
            }
            if(!majorAllocValid){
                // layout the major axis
                majorSpan=(int)span;
                checkRequests(majorAxis);
                layoutMajorAxis(majorSpan,axis,majorOffsets,majorSpans);
                majorAllocValid=true;
                // flush changes to the children
                updateChildSizes();
            }
        }else{
            if(((int)span)!=minorSpan){
                minorAllocValid=false;
            }
            if(!minorAllocValid){
                // layout the minor axis
                minorSpan=(int)span;
                checkRequests(axis);
                layoutMinorAxis(minorSpan,axis,minorOffsets,minorSpans);
                minorAllocValid=true;
                // flush changes to the children
                updateChildSizes();
            }
        }
    }

    void updateChildSizes(){
        int n=getViewCount();
        if(majorAxis==X_AXIS){
            for(int i=0;i<n;i++){
                View v=getView(i);
                v.setSize((float)majorSpans[i],(float)minorSpans[i]);
            }
        }else{
            for(int i=0;i<n;i++){
                View v=getView(i);
                v.setSize((float)minorSpans[i],(float)majorSpans[i]);
            }
        }
    }

    protected void layoutMajorAxis(int targetSpan,int axis,int[] offsets,int[] spans){
        /**
         * first pass, calculate the preferred sizes
         * and the flexibility to adjust the sizes.
         */
        long preferred=0;
        int n=getViewCount();
        for(int i=0;i<n;i++){
            View v=getView(i);
            spans[i]=(int)v.getPreferredSpan(axis);
            preferred+=spans[i];
        }
        /**
         * Second pass, expand or contract by as much as possible to reach
         * the target span.
         */
        // determine the adjustment to be made
        long desiredAdjustment=targetSpan-preferred;
        float adjustmentFactor=0.0f;
        int[] diffs=null;
        if(desiredAdjustment!=0){
            long totalSpan=0;
            diffs=new int[n];
            for(int i=0;i<n;i++){
                View v=getView(i);
                int tmp;
                if(desiredAdjustment<0){
                    tmp=(int)v.getMinimumSpan(axis);
                    diffs[i]=spans[i]-tmp;
                }else{
                    tmp=(int)v.getMaximumSpan(axis);
                    diffs[i]=tmp-spans[i];
                }
                totalSpan+=tmp;
            }
            float maximumAdjustment=Math.abs(totalSpan-preferred);
            adjustmentFactor=desiredAdjustment/maximumAdjustment;
            adjustmentFactor=Math.min(adjustmentFactor,1.0f);
            adjustmentFactor=Math.max(adjustmentFactor,-1.0f);
        }
        // make the adjustments
        int totalOffset=0;
        for(int i=0;i<n;i++){
            offsets[i]=totalOffset;
            if(desiredAdjustment!=0){
                float adjF=adjustmentFactor*diffs[i];
                spans[i]+=Math.round(adjF);
            }
            totalOffset=(int)Math.min((long)totalOffset+(long)spans[i],Integer.MAX_VALUE);
        }
    }

    protected void layoutMinorAxis(int targetSpan,int axis,int[] offsets,int[] spans){
        int n=getViewCount();
        for(int i=0;i<n;i++){
            View v=getView(i);
            int max=(int)v.getMaximumSpan(axis);
            if(max<targetSpan){
                // can't make the child this wide, align it
                float align=v.getAlignment(axis);
                offsets[i]=(int)((targetSpan-max)*align);
                spans[i]=max;
            }else{
                // make it the target width, or as small as it can get.
                int min=(int)v.getMinimumSpan(axis);
                offsets[i]=0;
                spans[i]=Math.max(min,targetSpan);
            }
        }
    }

    void checkRequests(int axis){
        if((axis!=X_AXIS)&&(axis!=Y_AXIS)){
            throw new IllegalArgumentException("Invalid axis: "+axis);
        }
        if(axis==majorAxis){
            if(!majorReqValid){
                majorRequest=calculateMajorAxisRequirements(axis,
                        majorRequest);
                majorReqValid=true;
            }
        }else if(!minorReqValid){
            minorRequest=calculateMinorAxisRequirements(axis,minorRequest);
            minorReqValid=true;
        }
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis,SizeRequirements r){
        // calculate tiled request
        float min=0;
        float pref=0;
        float max=0;
        int n=getViewCount();
        for(int i=0;i<n;i++){
            View v=getView(i);
            min+=v.getMinimumSpan(axis);
            pref+=v.getPreferredSpan(axis);
            max+=v.getMaximumSpan(axis);
        }
        if(r==null){
            r=new SizeRequirements();
        }
        r.alignment=0.5f;
        r.minimum=(int)min;
        r.preferred=(int)pref;
        r.maximum=(int)max;
        return r;
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis,SizeRequirements r){
        int min=0;
        long pref=0;
        int max=Integer.MAX_VALUE;
        int n=getViewCount();
        for(int i=0;i<n;i++){
            View v=getView(i);
            min=Math.max((int)v.getMinimumSpan(axis),min);
            pref=Math.max((int)v.getPreferredSpan(axis),pref);
            max=Math.max((int)v.getMaximumSpan(axis),max);
        }
        if(r==null){
            r=new SizeRequirements();
            r.alignment=0.5f;
        }
        r.preferred=(int)pref;
        r.minimum=min;
        r.maximum=max;
        return r;
    }

    protected int getOffset(int axis,int childIndex){
        int[] offsets=(axis==majorAxis)?majorOffsets:minorOffsets;
        return offsets[childIndex];
    }

    protected int getSpan(int axis,int childIndex){
        int[] spans=(axis==majorAxis)?majorSpans:minorSpans;
        return spans[childIndex];
    }

    public int getWidth(){
        int span;
        if(majorAxis==X_AXIS){
            span=majorSpan;
        }else{
            span=minorSpan;
        }
        span+=getLeftInset()-getRightInset();
        return span;
    }

    public int getHeight(){
        int span;
        if(majorAxis==Y_AXIS){
            span=majorSpan;
        }else{
            span=minorSpan;
        }
        span+=getTopInset()-getBottomInset();
        return span;
    }

    protected void baselineLayout(int targetSpan,int axis,int[] offsets,int[] spans){
        int totalAscent=(int)(targetSpan*getAlignment(axis));
        int totalDescent=targetSpan-totalAscent;
        int n=getViewCount();
        for(int i=0;i<n;i++){
            View v=getView(i);
            float align=v.getAlignment(axis);
            float viewSpan;
            if(v.getResizeWeight(axis)>0){
                // if resizable then resize to the best fit
                // the smallest span possible
                float minSpan=v.getMinimumSpan(axis);
                // the largest span possible
                float maxSpan=v.getMaximumSpan(axis);
                if(align==0.0f){
                    // if the alignment is 0 then we need to fit into the descent
                    viewSpan=Math.max(Math.min(maxSpan,totalDescent),minSpan);
                }else if(align==1.0f){
                    // if the alignment is 1 then we need to fit into the ascent
                    viewSpan=Math.max(Math.min(maxSpan,totalAscent),minSpan);
                }else{
                    // figure out the span that we must fit into
                    float fitSpan=Math.min(totalAscent/align,
                            totalDescent/(1.0f-align));
                    // fit into the calculated span
                    viewSpan=Math.max(Math.min(maxSpan,fitSpan),minSpan);
                }
            }else{
                // otherwise use the preferred spans
                viewSpan=v.getPreferredSpan(axis);
            }
            offsets[i]=totalAscent-(int)(viewSpan*align);
            spans[i]=(int)viewSpan;
        }
    }

    protected SizeRequirements baselineRequirements(int axis,SizeRequirements r){
        SizeRequirements totalAscent=new SizeRequirements();
        SizeRequirements totalDescent=new SizeRequirements();
        if(r==null){
            r=new SizeRequirements();
        }
        r.alignment=0.5f;
        int n=getViewCount();
        // loop through all children calculating the max of all their ascents and
        // descents at minimum, preferred, and maximum sizes
        for(int i=0;i<n;i++){
            View v=getView(i);
            float align=v.getAlignment(axis);
            float span;
            int ascent;
            int descent;
            // find the maximum of the preferred ascents and descents
            span=v.getPreferredSpan(axis);
            ascent=(int)(align*span);
            descent=(int)(span-ascent);
            totalAscent.preferred=Math.max(ascent,totalAscent.preferred);
            totalDescent.preferred=Math.max(descent,totalDescent.preferred);
            if(v.getResizeWeight(axis)>0){
                // if the view is resizable then do the same for the minimum and
                // maximum ascents and descents
                span=v.getMinimumSpan(axis);
                ascent=(int)(align*span);
                descent=(int)(span-ascent);
                totalAscent.minimum=Math.max(ascent,totalAscent.minimum);
                totalDescent.minimum=Math.max(descent,totalDescent.minimum);
                span=v.getMaximumSpan(axis);
                ascent=(int)(align*span);
                descent=(int)(span-ascent);
                totalAscent.maximum=Math.max(ascent,totalAscent.maximum);
                totalDescent.maximum=Math.max(descent,totalDescent.maximum);
            }else{
                // otherwise use the preferred
                totalAscent.minimum=Math.max(ascent,totalAscent.minimum);
                totalDescent.minimum=Math.max(descent,totalDescent.minimum);
                totalAscent.maximum=Math.max(ascent,totalAscent.maximum);
                totalDescent.maximum=Math.max(descent,totalDescent.maximum);
            }
        }
        // we now have an overall preferred, minimum, and maximum ascent and descent
        // calculate the preferred span as the sum of the preferred ascent and preferred descent
        r.preferred=(int)Math.min((long)totalAscent.preferred+(long)totalDescent.preferred,
                Integer.MAX_VALUE);
        // calculate the preferred alignment as the preferred ascent divided by the preferred span
        if(r.preferred>0){
            r.alignment=(float)totalAscent.preferred/r.preferred;
        }
        if(r.alignment==0.0f){
            // if the preferred alignment is 0 then the minimum and maximum spans are simply
            // the minimum and maximum descents since there's nothing above the baseline
            r.minimum=totalDescent.minimum;
            r.maximum=totalDescent.maximum;
        }else if(r.alignment==1.0f){
            // if the preferred alignment is 1 then the minimum and maximum spans are simply
            // the minimum and maximum ascents since there's nothing below the baseline
            r.minimum=totalAscent.minimum;
            r.maximum=totalAscent.maximum;
        }else{
            // we want to honor the preferred alignment so we calculate two possible minimum
            // span values using 1) the minimum ascent and the alignment, and 2) the minimum
            // descent and the alignment. We'll choose the larger of these two numbers.
            r.minimum=Math.round(Math.max(totalAscent.minimum/r.alignment,
                    totalDescent.minimum/(1.0f-r.alignment)));
            // a similar calculation is made for the maximum but we choose the smaller number.
            r.maximum=Math.round(Math.min(totalAscent.maximum/r.alignment,
                    totalDescent.maximum/(1.0f-r.alignment)));
        }
        return r;
    }
}
