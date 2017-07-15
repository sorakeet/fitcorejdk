/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.MouseEvent;

public class MotifSplitPaneDivider extends BasicSplitPaneDivider{
    public static final int minimumThumbSize=6;
    public static final int defaultDividerSize=18;
    protected static final int pad=6;
    private static final Cursor defaultCursor=
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    protected int hThumbWidth=12;
    protected int hThumbHeight=18;
    protected int vThumbWidth=18;
    protected int vThumbHeight=12;
    protected Color highlightColor;
    protected Color shadowColor;
    protected Color focusedColor;
    private int hThumbOffset=30;
    private int vThumbOffset=40;

    public MotifSplitPaneDivider(BasicSplitPaneUI ui){
        super(ui);
        highlightColor=UIManager.getColor("SplitPane.highlight");
        shadowColor=UIManager.getColor("SplitPane.shadow");
        focusedColor=UIManager.getColor("SplitPane.activeThumb");
        setDividerSize(hThumbWidth+pad);
    }

    public void setBasicSplitPaneUI(BasicSplitPaneUI newUI){
        if(splitPane!=null){
            splitPane.removePropertyChangeListener(this);
            if(mouseHandler!=null){
                splitPane.removeMouseListener(mouseHandler);
                splitPane.removeMouseMotionListener(mouseHandler);
                removeMouseListener(mouseHandler);
                removeMouseMotionListener(mouseHandler);
                mouseHandler=null;
            }
        }
        splitPaneUI=newUI;
        if(newUI!=null){
            splitPane=newUI.getSplitPane();
            if(splitPane!=null){
                if(mouseHandler==null) mouseHandler=new MotifMouseHandler();
                splitPane.addMouseListener(mouseHandler);
                splitPane.addMouseMotionListener(mouseHandler);
                addMouseListener(mouseHandler);
                addMouseMotionListener(mouseHandler);
                splitPane.addPropertyChangeListener(this);
                if(splitPane.isOneTouchExpandable()){
                    oneTouchExpandableChanged();
                }
            }
        }else{
            splitPane=null;
        }
    }

    public void setDividerSize(int newSize){
        Insets insets=getInsets();
        int borderSize=0;
        if(getBasicSplitPaneUI().getOrientation()==
                JSplitPane.HORIZONTAL_SPLIT){
            if(insets!=null){
                borderSize=insets.left+insets.right;
            }
        }else if(insets!=null){
            borderSize=insets.top+insets.bottom;
        }
        if(newSize<pad+minimumThumbSize+borderSize){
            setDividerSize(pad+minimumThumbSize+borderSize);
        }else{
            vThumbHeight=hThumbWidth=newSize-pad-borderSize;
            super.setDividerSize(newSize);
        }
    }

    public Dimension getMinimumSize(){
        return getPreferredSize();
    }

    // PENDING(jeff) - the thumb's location and size is currently hard coded.
    // It should be dynamic.
    public void paint(Graphics g){
        Color bgColor=getBackground();
        Dimension size=getSize();
        // fill
        g.setColor(getBackground());
        g.fillRect(0,0,size.width,size.height);
        if(getBasicSplitPaneUI().getOrientation()==
                JSplitPane.HORIZONTAL_SPLIT){
            int center=size.width/2;
            int x=center-hThumbWidth/2;
            int y=hThumbOffset;
            // split line
            g.setColor(shadowColor);
            g.drawLine(center-1,0,center-1,size.height);
            g.setColor(highlightColor);
            g.drawLine(center,0,center,size.height);
            // draw thumb
            g.setColor((splitPane.hasFocus())?focusedColor:
                    getBackground());
            g.fillRect(x+1,y+1,hThumbWidth-2,hThumbHeight-1);
            g.setColor(highlightColor);
            g.drawLine(x,y,x+hThumbWidth-1,y);       // top
            g.drawLine(x,y+1,x,y+hThumbHeight-1);    // left
            g.setColor(shadowColor);
            g.drawLine(x+1,y+hThumbHeight-1,
                    x+hThumbWidth-1,y+hThumbHeight-1);      // bottom
            g.drawLine(x+hThumbWidth-1,y+1,
                    x+hThumbWidth-1,y+hThumbHeight-2);      // right
        }else{
            int center=size.height/2;
            int x=size.width-vThumbOffset;
            int y=size.height/2-vThumbHeight/2;
            // split line
            g.setColor(shadowColor);
            g.drawLine(0,center-1,size.width,center-1);
            g.setColor(highlightColor);
            g.drawLine(0,center,size.width,center);
            // draw thumb
            g.setColor((splitPane.hasFocus())?focusedColor:
                    getBackground());
            g.fillRect(x+1,y+1,vThumbWidth-1,vThumbHeight-1);
            g.setColor(highlightColor);
            g.drawLine(x,y,x+vThumbWidth,y);    // top
            g.drawLine(x,y+1,x,y+vThumbHeight); // left
            g.setColor(shadowColor);
            g.drawLine(x+1,y+vThumbHeight,
                    x+vThumbWidth,y+vThumbHeight);          // bottom
            g.drawLine(x+vThumbWidth,y+1,
                    x+vThumbWidth,y+vThumbHeight-1);        // right
        }
        super.paint(g);
    }

    private boolean isInThumb(int x,int y){
        Dimension size=getSize();
        int thumbX;
        int thumbY;
        int thumbWidth;
        int thumbHeight;
        if(getBasicSplitPaneUI().getOrientation()==
                JSplitPane.HORIZONTAL_SPLIT){
            int center=size.width/2;
            thumbX=center-hThumbWidth/2;
            thumbY=hThumbOffset;
            thumbWidth=hThumbWidth;
            thumbHeight=hThumbHeight;
        }else{
            int center=size.height/2;
            thumbX=size.width-vThumbOffset;
            thumbY=size.height/2-vThumbHeight/2;
            thumbWidth=vThumbWidth;
            thumbHeight=vThumbHeight;
        }
        return (x>=thumbX&&x<(thumbX+thumbWidth)&&
                y>=thumbY&&y<(thumbY+thumbHeight));
    }
    //
    // Two methods are exposed so that MotifMouseHandler can see the
    // superclass protected ivars
    //

    private DragController getDragger(){
        return dragger;
    }

    private JSplitPane getSplitPane(){
        return splitPane;
    }

    private class MotifMouseHandler extends MouseHandler{
        public void mousePressed(MouseEvent e){
            // Constrain the mouse pressed to the thumb.
            if(e.getSource()==MotifSplitPaneDivider.this&&
                    getDragger()==null&&getSplitPane().isEnabled()&&
                    isInThumb(e.getX(),e.getY())){
                super.mousePressed(e);
            }
        }

        public void mouseMoved(MouseEvent e){
            if(getDragger()!=null){
                return;
            }
            if(!isInThumb(e.getX(),e.getY())){
                if(getCursor()!=defaultCursor){
                    setCursor(defaultCursor);
                }
                return;
            }
            super.mouseMoved(e);
        }
    }
}
