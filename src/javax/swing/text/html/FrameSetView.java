/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import java.util.StringTokenizer;

class FrameSetView extends BoxView{
    String[] children;
    int[] percentChildren;
    int[] absoluteChildren;
    int[] relativeChildren;
    int percentTotals;
    int absoluteTotals;
    int relativeTotals;

    public FrameSetView(Element elem,int axis){
        super(elem,axis);
        children=null;
    }

    protected void layoutMajorAxis(int targetSpan,int axis,int[] offsets,
                                   int[] spans){
        if(children==null){
            init();
        }
        SizeRequirements.calculateTiledPositions(targetSpan,null,
                getChildRequests(targetSpan,
                        axis),
                offsets,spans);
    }

    private void init(){
        if(getAxis()==View.Y_AXIS){
            children=parseRowColSpec(HTML.Attribute.ROWS);
        }else{
            children=parseRowColSpec(HTML.Attribute.COLS);
        }
        percentChildren=new int[children.length];
        relativeChildren=new int[children.length];
        absoluteChildren=new int[children.length];
        for(int i=0;i<children.length;i++){
            percentChildren[i]=-1;
            relativeChildren[i]=-1;
            absoluteChildren[i]=-1;
            if(children[i].endsWith("*")){
                if(children[i].length()>1){
                    relativeChildren[i]=
                            Integer.parseInt(children[i].substring(
                                    0,children[i].length()-1));
                    relativeTotals+=relativeChildren[i];
                }else{
                    relativeChildren[i]=1;
                    relativeTotals+=1;
                }
            }else if(children[i].indexOf('%')!=-1){
                percentChildren[i]=parseDigits(children[i]);
                percentTotals+=percentChildren[i];
            }else{
                absoluteChildren[i]=Integer.parseInt(children[i]);
            }
        }
        if(percentTotals>100){
            for(int i=0;i<percentChildren.length;i++){
                if(percentChildren[i]>0){
                    percentChildren[i]=
                            (percentChildren[i]*100)/percentTotals;
                }
            }
            percentTotals=100;
        }
    }

    private String[] parseRowColSpec(HTML.Attribute key){
        AttributeSet attributes=getElement().getAttributes();
        String spec="*";
        if(attributes!=null){
            if(attributes.getAttribute(key)!=null){
                spec=(String)attributes.getAttribute(key);
            }
        }
        StringTokenizer tokenizer=new StringTokenizer(spec,",");
        int nTokens=tokenizer.countTokens();
        int n=getViewCount();
        String[] items=new String[Math.max(nTokens,n)];
        int i=0;
        for(;i<nTokens;i++){
            items[i]=tokenizer.nextToken().trim();
            // As per the spec, 100% is the same as *
            // hence the mapping.
            //
            if(items[i].equals("100%")){
                items[i]="*";
            }
        }
        // extend spec if we have more children than specified
        // in ROWS or COLS attribute
        for(;i<items.length;i++){
            items[i]="*";
        }
        return items;
    }

    private int parseDigits(String mixedStr){
        int result=0;
        for(int i=0;i<mixedStr.length();i++){
            char ch=mixedStr.charAt(i);
            if(Character.isDigit(ch)){
                result=(result*10)+Character.digit(ch,10);
            }
        }
        return result;
    }

    protected SizeRequirements[] getChildRequests(int targetSpan,int axis){
        int span[]=new int[children.length];
        spread(targetSpan,span);
        int n=getViewCount();
        SizeRequirements[] reqs=new SizeRequirements[n];
        for(int i=0, sIndex=0;i<n;i++){
            View v=getView(i);
            if((v instanceof FrameView)||(v instanceof FrameSetView)){
                reqs[i]=new SizeRequirements((int)v.getMinimumSpan(axis),
                        span[sIndex],
                        (int)v.getMaximumSpan(axis),
                        0.5f);
                sIndex++;
            }else{
                int min=(int)v.getMinimumSpan(axis);
                int pref=(int)v.getPreferredSpan(axis);
                int max=(int)v.getMaximumSpan(axis);
                float a=v.getAlignment(axis);
                reqs[i]=new SizeRequirements(min,pref,max,a);
            }
        }
        return reqs;
    }

    private void spread(int targetSpan,int span[]){
        if(targetSpan==0){
            return;
        }
        int tempSpace=0;
        int remainingSpace=targetSpan;
        // allocate the absolute's first, they have
        // precedence
        //
        for(int i=0;i<span.length;i++){
            if(absoluteChildren[i]>0){
                span[i]=absoluteChildren[i];
                remainingSpace-=span[i];
            }
        }
        // then deal with percents.
        //
        tempSpace=remainingSpace;
        for(int i=0;i<span.length;i++){
            if(percentChildren[i]>0&&tempSpace>0){
                span[i]=(percentChildren[i]*tempSpace)/100;
                remainingSpace-=span[i];
            }else if(percentChildren[i]>0&&tempSpace<=0){
                span[i]=targetSpan/span.length;
                remainingSpace-=span[i];
            }
        }
        // allocate remainingSpace to relative
        if(remainingSpace>0&&relativeTotals>0){
            for(int i=0;i<span.length;i++){
                if(relativeChildren[i]>0){
                    span[i]=(remainingSpace*
                            relativeChildren[i])/relativeTotals;
                }
            }
        }else if(remainingSpace>0){
            // There are no relative columns and the space has been
            // under- or overallocated.  In this case, turn all the
            // percentage and pixel specified columns to percentage
            // columns based on the ratio of their pixel count to the
            // total "virtual" size. (In the case of percentage columns,
            // the pixel count would equal the specified percentage
            // of the screen size.
            // This action is in accordance with the HTML
            // 4.0 spec (see section 8.3, the end of the discussion of
            // the FRAMESET tag).  The precedence of percentage and pixel
            // specified columns is unclear (spec seems to indicate that
            // they share priority, however, unspecified what happens when
            // overallocation occurs.)
            // addendum is that we behave similar to netscape in that specified
            // widths have precedance over percentage widths...
            float vTotal=(float)(targetSpan-remainingSpace);
            float[] tempPercents=new float[span.length];
            remainingSpace=targetSpan;
            for(int i=0;i<span.length;i++){
                // ok we know what our total space is, and we know how large each
                // column should be relative to each other... therefore we can use
                // that relative information to deduce their percentages of a whole
                // and then scale them appropriately for the correct size
                tempPercents[i]=((float)span[i]/vTotal)*100.00f;
                span[i]=(int)(((float)targetSpan*tempPercents[i])/100.00f);
                remainingSpace-=span[i];
            }
            // this is for just in case there is something left over.. if there is we just
            // add it one pixel at a time to the frames in order.. We shouldn't really ever get
            // here and if we do it shouldn't be with more than 1 pixel, maybe two.
            int i=0;
            while(remainingSpace!=0){
                if(remainingSpace<0){
                    span[i++]--;
                    remainingSpace++;
                }else{
                    span[i++]++;
                    remainingSpace--;
                }
                // just in case there are more pixels than frames...should never happen..
                if(i==span.length) i=0;
            }
        }
    }
}
