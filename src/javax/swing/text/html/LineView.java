/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.*;
import java.awt.*;

class LineView extends ParagraphView{
    int tabBase;

    public LineView(Element elem){
        super(elem);
    }

    public boolean isVisible(){
        return true;
    }

    public float getMinimumSpan(int axis){
        return getPreferredSpan(axis);
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

    protected void layout(int width,int height){
        super.layout(Integer.MAX_VALUE-1,height);
    }

    public float nextTabStop(float x,int tabOffset){
        // If the text isn't left justified, offset by 10 pixels!
        if(getTabSet()==null&&
                StyleConstants.getAlignment(getAttributes())==
                        StyleConstants.ALIGN_LEFT){
            return getPreTab(x,tabOffset);
        }
        return super.nextTabStop(x,tabOffset);
    }

    public float getAlignment(int axis){
        if(axis==View.X_AXIS){
            return 0;
        }
        return super.getAlignment(axis);
    }

    protected float getPreTab(float x,int tabOffset){
        Document d=getDocument();
        View v=getViewAtPosition(tabOffset,null);
        if((d instanceof StyledDocument)&&v!=null){
            // Assume f is fixed point.
            Font f=((StyledDocument)d).getFont(v.getAttributes());
            Container c=getContainer();
            FontMetrics fm=(c!=null)?c.getFontMetrics(f):
                    Toolkit.getDefaultToolkit().getFontMetrics(f);
            int width=getCharactersPerTab()*fm.charWidth('W');
            int tb=(int)getTabBase();
            return (float)((((int)x-tb)/width+1)*width+tb);
        }
        return 10.0f+x;
    }

    protected int getCharactersPerTab(){
        return 8;
    }
}
