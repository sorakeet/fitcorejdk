/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;

public class InlineView extends LabelView{
    private boolean nowrap;
    private AttributeSet attr;

    public InlineView(Element elem){
        super(elem);
        StyleSheet sheet=getStyleSheet();
        attr=sheet.getViewAttributes(this);
    }

    protected StyleSheet getStyleSheet(){
        HTMLDocument doc=(HTMLDocument)getDocument();
        return doc.getStyleSheet();
    }

    public int getBreakWeight(int axis,float pos,float len){
        if(nowrap){
            return BadBreakWeight;
        }
        return super.getBreakWeight(axis,pos,len);
    }

    public View breakView(int axis,int offset,float pos,float len){
        return super.breakView(axis,offset,pos,len);
    }

    public void insertUpdate(DocumentEvent e,Shape a,ViewFactory f){
        super.insertUpdate(e,a,f);
    }

    public void removeUpdate(DocumentEvent e,Shape a,ViewFactory f){
        super.removeUpdate(e,a,f);
    }

    protected void setPropertiesFromAttributes(){
        super.setPropertiesFromAttributes();
        AttributeSet a=getAttributes();
        Object decor=a.getAttribute(CSS.Attribute.TEXT_DECORATION);
        boolean u=(decor!=null)?
                (decor.toString().indexOf("underline")>=0):false;
        setUnderline(u);
        boolean s=(decor!=null)?
                (decor.toString().indexOf("line-through")>=0):false;
        setStrikeThrough(s);
        Object vAlign=a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
        s=(vAlign!=null)?(vAlign.toString().indexOf("sup")>=0):false;
        setSuperscript(s);
        s=(vAlign!=null)?(vAlign.toString().indexOf("sub")>=0):false;
        setSubscript(s);
        Object whitespace=a.getAttribute(CSS.Attribute.WHITE_SPACE);
        if((whitespace!=null)&&whitespace.equals("nowrap")){
            nowrap=true;
        }else{
            nowrap=false;
        }
        HTMLDocument doc=(HTMLDocument)getDocument();
        // fetches background color from stylesheet if specified
        Color bg=doc.getBackground(a);
        if(bg!=null){
            setBackground(bg);
        }
    }

    public void changedUpdate(DocumentEvent e,Shape a,ViewFactory f){
        super.changedUpdate(e,a,f);
        StyleSheet sheet=getStyleSheet();
        attr=sheet.getViewAttributes(this);
        preferenceChanged(null,true,true);
    }

    public AttributeSet getAttributes(){
        return attr;
    }
}
