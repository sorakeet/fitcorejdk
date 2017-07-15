/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;

public class BlockView extends BoxView{
    private AttributeSet attr;
    private StyleSheet.BoxPainter painter;
    private CSS.LengthValue cssWidth;
    private CSS.LengthValue cssHeight;

    public BlockView(Element elem,int axis){
        super(elem,axis);
    }

    public void setParent(View parent){
        super.setParent(parent);
        if(parent!=null){
            setPropertiesFromAttributes();
        }
    }

    protected void setPropertiesFromAttributes(){
        // update attributes
        StyleSheet sheet=getStyleSheet();
        attr=sheet.getViewAttributes(this);
        // Reset the painter
        painter=sheet.getBoxPainter(attr);
        if(attr!=null){
            setInsets((short)painter.getInset(TOP,this),
                    (short)painter.getInset(LEFT,this),
                    (short)painter.getInset(BOTTOM,this),
                    (short)painter.getInset(RIGHT,this));
        }
        // Get the width/height
        cssWidth=(CSS.LengthValue)attr.getAttribute(CSS.Attribute.WIDTH);
        cssHeight=(CSS.LengthValue)attr.getAttribute(CSS.Attribute.HEIGHT);
    }

    protected StyleSheet getStyleSheet(){
        HTMLDocument doc=(HTMLDocument)getDocument();
        return doc.getStyleSheet();
    }

    boolean isPercentage(int axis,AttributeSet a){
        if(axis==X_AXIS){
            if(cssWidth!=null){
                return cssWidth.isPercentage();
            }
        }else{
            if(cssHeight!=null){
                return cssHeight.isPercentage();
            }
        }
        return false;
    }

    public int getResizeWeight(int axis){
        switch(axis){
            case View.X_AXIS:
                return 1;
            case View.Y_AXIS:
                return 0;
            default:
                throw new IllegalArgumentException("Invalid axis: "+axis);
        }
    }

    public void paint(Graphics g,Shape allocation){
        Rectangle a=(Rectangle)allocation;
        painter.paint(g,a.x,a.y,a.width,a.height,this);
        super.paint(g,a);
    }

    public float getAlignment(int axis){
        switch(axis){
            case View.X_AXIS:
                return 0;
            case View.Y_AXIS:
                if(getViewCount()==0){
                    return 0;
                }
                float span=getPreferredSpan(View.Y_AXIS);
                View v=getView(0);
                float above=v.getPreferredSpan(View.Y_AXIS);
                float a=(((int)span)!=0)?(above*v.getAlignment(View.Y_AXIS))/span:0;
                return a;
            default:
                throw new IllegalArgumentException("Invalid axis: "+axis);
        }
    }

    public float getPreferredSpan(int axis){
        return super.getPreferredSpan(axis);
    }

    public float getMinimumSpan(int axis){
        return super.getMinimumSpan(axis);
    }

    public float getMaximumSpan(int axis){
        return super.getMaximumSpan(axis);
    }

    protected void layoutMinorAxis(int targetSpan,int axis,int[] offsets,int[] spans){
        int n=getViewCount();
        Object key=(axis==X_AXIS)?CSS.Attribute.WIDTH:CSS.Attribute.HEIGHT;
        for(int i=0;i<n;i++){
            View v=getView(i);
            int min=(int)v.getMinimumSpan(axis);
            int max;
            // check for percentage span
            AttributeSet a=v.getAttributes();
            CSS.LengthValue lv=(CSS.LengthValue)a.getAttribute(key);
            if((lv!=null)&&lv.isPercentage()){
                // bound the span to the percentage specified
                min=Math.max((int)lv.getValue(targetSpan),min);
                max=min;
            }else{
                max=(int)v.getMaximumSpan(axis);
            }
            // assign the offset and span for the child
            if(max<targetSpan){
                // can't make the child this wide, align it
                float align=v.getAlignment(axis);
                offsets[i]=(int)((targetSpan-max)*align);
                spans[i]=max;
            }else{
                // make it the target width, or as small as it can get.
                offsets[i]=0;
                spans[i]=Math.max(min,targetSpan);
            }
        }
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis,SizeRequirements r){
        if(r==null){
            r=new SizeRequirements();
        }
        if(!spanSetFromAttributes(axis,r,cssWidth,cssHeight)){
            r=super.calculateMajorAxisRequirements(axis,r);
        }else{
            // Offset by the margins so that pref/min/max return the
            // right value.
            SizeRequirements parentR=super.calculateMajorAxisRequirements(
                    axis,null);
            int margin=(axis==X_AXIS)?getLeftInset()+getRightInset():
                    getTopInset()+getBottomInset();
            r.minimum-=margin;
            r.preferred-=margin;
            r.maximum-=margin;
            constrainSize(axis,r,parentR);
        }
        return r;
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis,SizeRequirements r){
        if(r==null){
            r=new SizeRequirements();
        }
        if(!spanSetFromAttributes(axis,r,cssWidth,cssHeight)){
            /**
             * The requirements were not directly specified by attributes, so
             * compute the aggregate of the requirements of the children.  The
             * children that have a percentage value specified will be treated
             * as completely stretchable since that child is not limited in any
             * way.
             */
/**
 int min = 0;
 long pref = 0;
 int max = 0;
 int n = getViewCount();
 for (int i = 0; i < n; i++) {
 View v = getView(i);
 min = Math.max((int) v.getMinimumSpan(axis), min);
 pref = Math.max((int) v.getPreferredSpan(axis), pref);
 if (
 max = Math.max((int) v.getMaximumSpan(axis), max);

 }
 r.preferred = (int) pref;
 r.minimum = min;
 r.maximum = max;
 */
            r=super.calculateMinorAxisRequirements(axis,r);
        }else{
            // Offset by the margins so that pref/min/max return the
            // right value.
            SizeRequirements parentR=super.calculateMinorAxisRequirements(
                    axis,null);
            int margin=(axis==X_AXIS)?getLeftInset()+getRightInset():
                    getTopInset()+getBottomInset();
            r.minimum-=margin;
            r.preferred-=margin;
            r.maximum-=margin;
            constrainSize(axis,r,parentR);
        }
        /**
         * Set the alignment based upon the CSS properties if it is
         * specified.  For X_AXIS this would be text-align, for
         * Y_AXIS this would be vertical-align.
         */
        if(axis==X_AXIS){
            Object o=getAttributes().getAttribute(CSS.Attribute.TEXT_ALIGN);
            if(o!=null){
                String align=o.toString();
                if(align.equals("center")){
                    r.alignment=0.5f;
                }else if(align.equals("right")){
                    r.alignment=1.0f;
                }else{
                    r.alignment=0.0f;
                }
            }
        }
        // Y_AXIS TBD
        return r;
    }

    static boolean spanSetFromAttributes(int axis,SizeRequirements r,
                                         CSS.LengthValue cssWidth,
                                         CSS.LengthValue cssHeight){
        if(axis==X_AXIS){
            if((cssWidth!=null)&&(!cssWidth.isPercentage())){
                r.minimum=r.preferred=r.maximum=(int)cssWidth.getValue();
                return true;
            }
        }else{
            if((cssHeight!=null)&&(!cssHeight.isPercentage())){
                r.minimum=r.preferred=r.maximum=(int)cssHeight.getValue();
                return true;
            }
        }
        return false;
    }

    private void constrainSize(int axis,SizeRequirements want,
                               SizeRequirements min){
        if(min.minimum>want.minimum){
            want.minimum=want.preferred=min.minimum;
            want.maximum=Math.max(want.maximum,min.maximum);
        }
    }

    public void changedUpdate(DocumentEvent changes,Shape a,ViewFactory f){
        super.changedUpdate(changes,a,f);
        int pos=changes.getOffset();
        if(pos<=getStartOffset()&&(pos+changes.getLength())>=
                getEndOffset()){
            setPropertiesFromAttributes();
        }
    }

    public AttributeSet getAttributes(){
        if(attr==null){
            StyleSheet sheet=getStyleSheet();
            attr=sheet.getViewAttributes(this);
        }
        return attr;
    }
}
