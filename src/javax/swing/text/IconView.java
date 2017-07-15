/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.awt.*;

public class IconView extends View{
    // --- member variables ------------------------------------------------
    private Icon c;
    // --- View methods ---------------------------------------------

    public IconView(Element elem){
        super(elem);
        AttributeSet attr=elem.getAttributes();
        c=StyleConstants.getIcon(attr);
    }

    public float getPreferredSpan(int axis){
        switch(axis){
            case View.X_AXIS:
                return c.getIconWidth();
            case View.Y_AXIS:
                return c.getIconHeight();
            default:
                throw new IllegalArgumentException("Invalid axis: "+axis);
        }
    }

    public float getAlignment(int axis){
        switch(axis){
            case View.Y_AXIS:
                return 1;
            default:
                return super.getAlignment(axis);
        }
    }

    public void paint(Graphics g,Shape a){
        Rectangle alloc=a.getBounds();
        c.paintIcon(getContainer(),g,alloc.x,alloc.y);
    }

    public Shape modelToView(int pos,Shape a,Position.Bias b) throws BadLocationException{
        int p0=getStartOffset();
        int p1=getEndOffset();
        if((pos>=p0)&&(pos<=p1)){
            Rectangle r=a.getBounds();
            if(pos==p1){
                r.x+=r.width;
            }
            r.width=0;
            return r;
        }
        throw new BadLocationException(pos+" not in range "+p0+","+p1,pos);
    }

    public int viewToModel(float x,float y,Shape a,Position.Bias[] bias){
        Rectangle alloc=(Rectangle)a;
        if(x<alloc.x+(alloc.width/2)){
            bias[0]=Position.Bias.Forward;
            return getStartOffset();
        }
        bias[0]=Position.Bias.Backward;
        return getEndOffset();
    }
}
