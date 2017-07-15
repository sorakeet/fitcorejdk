/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ParagraphView extends javax.swing.text.ParagraphView{
    private AttributeSet attr;
    private StyleSheet.BoxPainter painter;
    private CSS.LengthValue cssWidth;
    private CSS.LengthValue cssHeight;

    public ParagraphView(Element elem){
        super(elem);
    }

    public void setParent(View parent){
        super.setParent(parent);
        if(parent!=null){
            setPropertiesFromAttributes();
        }
    }

    protected void setPropertiesFromAttributes(){
        StyleSheet sheet=getStyleSheet();
        attr=sheet.getViewAttributes(this);
        painter=sheet.getBoxPainter(attr);
        if(attr!=null){
            super.setPropertiesFromAttributes();
            setInsets((short)painter.getInset(TOP,this),
                    (short)painter.getInset(LEFT,this),
                    (short)painter.getInset(BOTTOM,this),
                    (short)painter.getInset(RIGHT,this));
            Object o=attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
            if(o!=null){
                // set horizontal alignment
                String ta=o.toString();
                if(ta.equals("left")){
                    setJustification(StyleConstants.ALIGN_LEFT);
                }else if(ta.equals("center")){
                    setJustification(StyleConstants.ALIGN_CENTER);
                }else if(ta.equals("right")){
                    setJustification(StyleConstants.ALIGN_RIGHT);
                }else if(ta.equals("justify")){
                    setJustification(StyleConstants.ALIGN_JUSTIFIED);
                }
            }
            // Get the width/height
            cssWidth=(CSS.LengthValue)attr.getAttribute(
                    CSS.Attribute.WIDTH);
            cssHeight=(CSS.LengthValue)attr.getAttribute(
                    CSS.Attribute.HEIGHT);
        }
    }

    protected StyleSheet getStyleSheet(){
        HTMLDocument doc=(HTMLDocument)getDocument();
        return doc.getStyleSheet();
    }

    public void paint(Graphics g,Shape a){
        if(a==null){
            return;
        }
        Rectangle r;
        if(a instanceof Rectangle){
            r=(Rectangle)a;
        }else{
            r=a.getBounds();
        }
        painter.paint(g,r.x,r.y,r.width,r.height,this);
        super.paint(g,a);
    }

    protected SizeRequirements calculateMinorAxisRequirements(
            int axis,SizeRequirements r){
        r=super.calculateMinorAxisRequirements(axis,r);
        if(BlockView.spanSetFromAttributes(axis,r,cssWidth,cssHeight)){
            // Offset by the margins so that pref/min/max return the
            // right value.
            int margin=(axis==X_AXIS)?getLeftInset()+getRightInset():
                    getTopInset()+getBottomInset();
            r.minimum-=margin;
            r.preferred-=margin;
            r.maximum-=margin;
        }
        return r;
    }

    public float getPreferredSpan(int axis){
        if(!isVisible()){
            return 0;
        }
        return super.getPreferredSpan(axis);
    }

    public boolean isVisible(){
        int n=getLayoutViewCount()-1;
        for(int i=0;i<n;i++){
            View v=getLayoutView(i);
            if(v.isVisible()){
                return true;
            }
        }
        if(n>0){
            View v=getLayoutView(n);
            if((v.getEndOffset()-v.getStartOffset())==1){
                return false;
            }
        }
        // If it's the last paragraph and not editable, it shouldn't
        // be visible.
        if(getStartOffset()==getDocument().getLength()){
            boolean editable=false;
            Component c=getContainer();
            if(c instanceof JTextComponent){
                editable=((JTextComponent)c).isEditable();
            }
            if(!editable){
                return false;
            }
        }
        return true;
    }

    public AttributeSet getAttributes(){
        if(attr==null){
            StyleSheet sheet=getStyleSheet();
            attr=sheet.getViewAttributes(this);
        }
        return attr;
    }

    public float getMinimumSpan(int axis){
        if(!isVisible()){
            return 0;
        }
        return super.getMinimumSpan(axis);
    }

    public float getMaximumSpan(int axis){
        if(!isVisible()){
            return 0;
        }
        return super.getMaximumSpan(axis);
    }
}
