/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.Element;
import javax.swing.text.View;
import java.awt.*;

public class ListView extends BlockView{
    private StyleSheet.ListPainter listPainter;

    public ListView(Element elem){
        super(elem,View.Y_AXIS);
    }

    public void paint(Graphics g,Shape allocation){
        super.paint(g,allocation);
        Rectangle alloc=allocation.getBounds();
        Rectangle clip=g.getClipBounds();
        // Since listPainter paints in the insets we have to check for the
        // case where the child is not painted because the paint region is
        // to the left of the child. This assumes the ListPainter paints in
        // the left margin.
        if((clip.x+clip.width)<(alloc.x+getLeftInset())){
            Rectangle childRect=alloc;
            alloc=getInsideAllocation(allocation);
            int n=getViewCount();
            int endY=clip.y+clip.height;
            for(int i=0;i<n;i++){
                childRect.setBounds(alloc);
                childAllocation(i,childRect);
                if(childRect.y<endY){
                    if((childRect.y+childRect.height)>=clip.y){
                        listPainter.paint(g,childRect.x,childRect.y,
                                childRect.width,childRect.height,
                                this,i);
                    }
                }else{
                    break;
                }
            }
        }
    }

    public float getAlignment(int axis){
        switch(axis){
            case View.X_AXIS:
                return 0.5f;
            case View.Y_AXIS:
                return 0.5f;
            default:
                throw new IllegalArgumentException("Invalid axis: "+axis);
        }
    }

    protected void setPropertiesFromAttributes(){
        super.setPropertiesFromAttributes();
        listPainter=getStyleSheet().getListPainter(getAttributes());
    }

    protected void paintChild(Graphics g,Rectangle alloc,int index){
        listPainter.paint(g,alloc.x,alloc.y,alloc.width,alloc.height,this,index);
        super.paintChild(g,alloc,index);
    }
}
