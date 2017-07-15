/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.*;

class NoFramesView extends BlockView{
    boolean visible;

    public NoFramesView(Element elem,int axis){
        super(elem,axis);
        visible=false;
    }

    public void setParent(View p){
        if(p!=null){
            Container host=p.getContainer();
            if(host!=null){
                visible=((JTextComponent)host).isEditable();
            }
        }
        super.setParent(p);
    }

    public void paint(Graphics g,Shape allocation){
        Container host=getContainer();
        if(host!=null&&
                visible!=((JTextComponent)host).isEditable()){
            visible=((JTextComponent)host).isEditable();
        }
        if(!isVisible()){
            return;
        }
        super.paint(g,allocation);
    }

    public boolean isVisible(){
        return visible;
    }

    public float getPreferredSpan(int axis){
        if(!visible){
            return 0;
        }
        return super.getPreferredSpan(axis);
    }

    public float getMinimumSpan(int axis){
        if(!visible){
            return 0;
        }
        return super.getMinimumSpan(axis);
    }

    public float getMaximumSpan(int axis){
        if(!visible){
            return 0;
        }
        return super.getMaximumSpan(axis);
    }

    protected void layout(int width,int height){
        if(!isVisible()){
            return;
        }
        super.layout(width,height);
    }
}
