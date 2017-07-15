/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import static com.sun.java.swing.plaf.windows.TMSchema.*;
import static com.sun.java.swing.plaf.windows.XPStyle.Skin;

public class WindowsScrollBarUI extends BasicScrollBarUI{
    private Grid thumbGrid;
    private Grid highlightGrid;
    private Dimension horizontalThumbSize;
    private Dimension verticalThumbSize;

    public static ComponentUI createUI(JComponent c){
        return new WindowsScrollBarUI();
    }

    protected void configureScrollBarColors(){
        super.configureScrollBarColors();
        Color color=UIManager.getColor("ScrollBar.trackForeground");
        if(color!=null&&trackColor!=null){
            thumbGrid=Grid.getGrid(color,trackColor);
        }
        color=UIManager.getColor("ScrollBar.trackHighlightForeground");
        if(color!=null&&trackHighlightColor!=null){
            highlightGrid=Grid.getGrid(color,trackHighlightColor);
        }
    }

    public void uninstallUI(JComponent c){
        super.uninstallUI(c);
        thumbGrid=highlightGrid=null;
    }

    protected void installDefaults(){
        super.installDefaults();
        XPStyle xp=XPStyle.getXP();
        if(xp!=null){
            scrollbar.setBorder(null);
            horizontalThumbSize=getSize(scrollbar,xp,Part.SBP_THUMBBTNHORZ);
            verticalThumbSize=getSize(scrollbar,xp,Part.SBP_THUMBBTNVERT);
        }else{
            horizontalThumbSize=null;
            verticalThumbSize=null;
        }
    }

    private static Dimension getSize(Component component,XPStyle xp,Part part){
        Skin skin=xp.getSkin(component,part);
        return new Dimension(skin.getWidth(),skin.getHeight());
    }

    @Override
    protected ArrowButtonListener createArrowButtonListener(){
        // we need to repaint the entire scrollbar because state change for each
        // button causes a state change for the thumb and other button on Vista
        if(XPStyle.isVista()){
            return new ArrowButtonListener(){
                public void mouseEntered(MouseEvent evt){
                    repaint();
                    super.mouseEntered(evt);
                }

                public void mouseExited(MouseEvent evt){
                    repaint();
                    super.mouseExited(evt);
                }

                private void repaint(){
                    scrollbar.repaint();
                }
            };
        }else{
            return super.createArrowButtonListener();
        }
    }

    @Override
    protected void setThumbRollover(boolean active){
        boolean old=isThumbRollover();
        super.setThumbRollover(active);
        // we need to repaint the entire scrollbar because state change for thumb
        // causes state change for incr and decr buttons on Vista
        if(XPStyle.isVista()&&active!=old){
            scrollbar.repaint();
        }
    }

    protected JButton createDecreaseButton(int orientation){
        return new WindowsArrowButton(orientation,
                UIManager.getColor("ScrollBar.thumb"),
                UIManager.getColor("ScrollBar.thumbShadow"),
                UIManager.getColor("ScrollBar.thumbDarkShadow"),
                UIManager.getColor("ScrollBar.thumbHighlight"));
    }

    protected JButton createIncreaseButton(int orientation){
        return new WindowsArrowButton(orientation,
                UIManager.getColor("ScrollBar.thumb"),
                UIManager.getColor("ScrollBar.thumbShadow"),
                UIManager.getColor("ScrollBar.thumbDarkShadow"),
                UIManager.getColor("ScrollBar.thumbHighlight"));
    }

    protected void paintDecreaseHighlight(Graphics g){
        if(highlightGrid==null){
            super.paintDecreaseHighlight(g);
        }else{
            Insets insets=scrollbar.getInsets();
            Rectangle thumbR=getThumbBounds();
            int x, y, w, h;
            if(scrollbar.getOrientation()==JScrollBar.VERTICAL){
                x=insets.left;
                y=decrButton.getY()+decrButton.getHeight();
                w=scrollbar.getWidth()-(insets.left+insets.right);
                h=thumbR.y-y;
            }else{
                x=decrButton.getX()+decrButton.getHeight();
                y=insets.top;
                w=thumbR.x-x;
                h=scrollbar.getHeight()-(insets.top+insets.bottom);
            }
            highlightGrid.paint(g,x,y,w,h);
        }
    }

    protected void paintIncreaseHighlight(Graphics g){
        if(highlightGrid==null){
            super.paintDecreaseHighlight(g);
        }else{
            Insets insets=scrollbar.getInsets();
            Rectangle thumbR=getThumbBounds();
            int x, y, w, h;
            if(scrollbar.getOrientation()==JScrollBar.VERTICAL){
                x=insets.left;
                y=thumbR.y+thumbR.height;
                w=scrollbar.getWidth()-(insets.left+insets.right);
                h=incrButton.getY()-y;
            }else{
                x=thumbR.x+thumbR.width;
                y=insets.top;
                w=incrButton.getX()-x;
                h=scrollbar.getHeight()-(insets.top+insets.bottom);
            }
            highlightGrid.paint(g,x,y,w,h);
        }
    }

    protected void paintTrack(Graphics g,JComponent c,Rectangle trackBounds){
        boolean v=(scrollbar.getOrientation()==JScrollBar.VERTICAL);
        XPStyle xp=XPStyle.getXP();
        if(xp!=null){
            JScrollBar sb=(JScrollBar)c;
            State state=State.NORMAL;
            // Pending: Implement rollover (hot) and pressed
            if(!sb.isEnabled()){
                state=State.DISABLED;
            }
            Part part=v?Part.SBP_LOWERTRACKVERT:Part.SBP_LOWERTRACKHORZ;
            xp.getSkin(sb,part).paintSkin(g,trackBounds,state);
        }else if(thumbGrid==null){
            super.paintTrack(g,c,trackBounds);
        }else{
            thumbGrid.paint(g,trackBounds.x,trackBounds.y,trackBounds.width,
                    trackBounds.height);
            if(trackHighlight==DECREASE_HIGHLIGHT){
                paintDecreaseHighlight(g);
            }else if(trackHighlight==INCREASE_HIGHLIGHT){
                paintIncreaseHighlight(g);
            }
        }
    }

    protected void paintThumb(Graphics g,JComponent c,Rectangle thumbBounds){
        boolean v=(scrollbar.getOrientation()==JScrollBar.VERTICAL);
        XPStyle xp=XPStyle.getXP();
        if(xp!=null){
            JScrollBar sb=(JScrollBar)c;
            State state=State.NORMAL;
            if(!sb.isEnabled()){
                state=State.DISABLED;
            }else if(isDragging){
                state=State.PRESSED;
            }else if(isThumbRollover()){
                state=State.HOT;
            }else if(XPStyle.isVista()){
                if((incrButton!=null&&incrButton.getModel().isRollover())||
                        (decrButton!=null&&decrButton.getModel().isRollover())){
                    state=State.HOVER;
                }
            }
            // Paint thumb
            Part thumbPart=v?Part.SBP_THUMBBTNVERT:Part.SBP_THUMBBTNHORZ;
            xp.getSkin(sb,thumbPart).paintSkin(g,thumbBounds,state);
            // Paint gripper
            Part gripperPart=v?Part.SBP_GRIPPERVERT:Part.SBP_GRIPPERHORZ;
            Skin skin=xp.getSkin(sb,gripperPart);
            Insets gripperInsets=xp.getMargin(c,thumbPart,null,Prop.CONTENTMARGINS);
            if(gripperInsets==null||
                    (v&&(thumbBounds.height-gripperInsets.top-
                            gripperInsets.bottom>=skin.getHeight()))||
                    (!v&&(thumbBounds.width-gripperInsets.left-
                            gripperInsets.right>=skin.getWidth()))){
                skin.paintSkin(g,
                        thumbBounds.x+(thumbBounds.width-skin.getWidth())/2,
                        thumbBounds.y+(thumbBounds.height-skin.getHeight())/2,
                        skin.getWidth(),skin.getHeight(),state);
            }
        }else{
            super.paintThumb(g,c,thumbBounds);
        }
    }

    @Override
    protected Dimension getMinimumThumbSize(){
        if((horizontalThumbSize==null)||(verticalThumbSize==null)){
            return super.getMinimumThumbSize();
        }
        return JScrollBar.HORIZONTAL==scrollbar.getOrientation()
                ?horizontalThumbSize
                :verticalThumbSize;
    }

    private static class Grid{
        private static final int BUFFER_SIZE=64;
        private static HashMap<String,WeakReference<Grid>> map;

        static{
            map=new HashMap<String,WeakReference<Grid>>();
        }

        private BufferedImage image;

        public Grid(Color fg,Color bg){
            int cmap[]={fg.getRGB(),bg.getRGB()};
            IndexColorModel icm=new IndexColorModel(8,2,cmap,0,false,-1,
                    DataBuffer.TYPE_BYTE);
            image=new BufferedImage(BUFFER_SIZE,BUFFER_SIZE,
                    BufferedImage.TYPE_BYTE_INDEXED,icm);
            Graphics g=image.getGraphics();
            try{
                g.setClip(0,0,BUFFER_SIZE,BUFFER_SIZE);
                paintGrid(g,fg,bg);
            }finally{
                g.dispose();
            }
        }

        private void paintGrid(Graphics g,Color fg,Color bg){
            Rectangle clipRect=g.getClipBounds();
            g.setColor(bg);
            g.fillRect(clipRect.x,clipRect.y,clipRect.width,
                    clipRect.height);
            g.setColor(fg);
            g.translate(clipRect.x,clipRect.y);
            int width=clipRect.width;
            int height=clipRect.height;
            int xCounter=clipRect.x%2;
            for(int end=width-height;xCounter<end;xCounter+=2){
                g.drawLine(xCounter,0,xCounter+height,height);
            }
            for(int end=width;xCounter<end;xCounter+=2){
                g.drawLine(xCounter,0,width,width-xCounter);
            }
            int yCounter=((clipRect.x%2)==0)?2:1;
            for(int end=height-width;yCounter<end;yCounter+=2){
                g.drawLine(0,yCounter,width,yCounter+width);
            }
            for(int end=height;yCounter<end;yCounter+=2){
                g.drawLine(0,yCounter,height-yCounter,height);
            }
            g.translate(-clipRect.x,-clipRect.y);
        }

        public static Grid getGrid(Color fg,Color bg){
            String key=fg.getRGB()+" "+bg.getRGB();
            WeakReference<Grid> ref=map.get(key);
            Grid grid=(ref==null)?null:ref.get();
            if(grid==null){
                grid=new Grid(fg,bg);
                map.put(key,new WeakReference<Grid>(grid));
            }
            return grid;
        }

        public void paint(Graphics g,int x,int y,int w,int h){
            Rectangle clipRect=g.getClipBounds();
            int minX=Math.max(x,clipRect.x);
            int minY=Math.max(y,clipRect.y);
            int maxX=Math.min(clipRect.x+clipRect.width,x+w);
            int maxY=Math.min(clipRect.y+clipRect.height,y+h);
            if(maxX<=minX||maxY<=minY){
                return;
            }
            int xOffset=(minX-x)%2;
            for(int xCounter=minX;xCounter<maxX;
                xCounter+=BUFFER_SIZE){
                int yOffset=(minY-y)%2;
                int width=Math.min(BUFFER_SIZE-xOffset,
                        maxX-xCounter);
                for(int yCounter=minY;yCounter<maxY;
                    yCounter+=BUFFER_SIZE){
                    int height=Math.min(BUFFER_SIZE-yOffset,
                            maxY-yCounter);
                    g.drawImage(image,xCounter,yCounter,
                            xCounter+width,yCounter+height,
                            xOffset,yOffset,
                            xOffset+width,yOffset+height,null);
                    if(yOffset!=0){
                        yCounter-=yOffset;
                        yOffset=0;
                    }
                }
                if(xOffset!=0){
                    xCounter-=xOffset;
                    xOffset=0;
                }
            }
        }
    }

    private class WindowsArrowButton extends BasicArrowButton{
        public WindowsArrowButton(int direction,Color background,Color shadow,
                                  Color darkShadow,Color highlight){
            super(direction,background,shadow,darkShadow,highlight);
        }

        public WindowsArrowButton(int direction){
            super(direction);
        }

        public void paint(Graphics g){
            XPStyle xp=XPStyle.getXP();
            if(xp!=null){
                ButtonModel model=getModel();
                Skin skin=xp.getSkin(this,Part.SBP_ARROWBTN);
                State state=null;
                boolean jointRollover=XPStyle.isVista()&&(isThumbRollover()||
                        (this==incrButton&&decrButton.getModel().isRollover())||
                        (this==decrButton&&incrButton.getModel().isRollover()));
                // normal, rollover, pressed, disabled
                if(model.isArmed()&&model.isPressed()){
                    switch(direction){
                        case NORTH:
                            state=State.UPPRESSED;
                            break;
                        case SOUTH:
                            state=State.DOWNPRESSED;
                            break;
                        case WEST:
                            state=State.LEFTPRESSED;
                            break;
                        case EAST:
                            state=State.RIGHTPRESSED;
                            break;
                    }
                }else if(!model.isEnabled()){
                    switch(direction){
                        case NORTH:
                            state=State.UPDISABLED;
                            break;
                        case SOUTH:
                            state=State.DOWNDISABLED;
                            break;
                        case WEST:
                            state=State.LEFTDISABLED;
                            break;
                        case EAST:
                            state=State.RIGHTDISABLED;
                            break;
                    }
                }else if(model.isRollover()||model.isPressed()){
                    switch(direction){
                        case NORTH:
                            state=State.UPHOT;
                            break;
                        case SOUTH:
                            state=State.DOWNHOT;
                            break;
                        case WEST:
                            state=State.LEFTHOT;
                            break;
                        case EAST:
                            state=State.RIGHTHOT;
                            break;
                    }
                }else if(jointRollover){
                    switch(direction){
                        case NORTH:
                            state=State.UPHOVER;
                            break;
                        case SOUTH:
                            state=State.DOWNHOVER;
                            break;
                        case WEST:
                            state=State.LEFTHOVER;
                            break;
                        case EAST:
                            state=State.RIGHTHOVER;
                            break;
                    }
                }else{
                    switch(direction){
                        case NORTH:
                            state=State.UPNORMAL;
                            break;
                        case SOUTH:
                            state=State.DOWNNORMAL;
                            break;
                        case WEST:
                            state=State.LEFTNORMAL;
                            break;
                        case EAST:
                            state=State.RIGHTNORMAL;
                            break;
                    }
                }
                skin.paintSkin(g,0,0,getWidth(),getHeight(),state);
            }else{
                super.paint(g);
            }
        }

        public Dimension getPreferredSize(){
            int size=16;
            if(scrollbar!=null){
                switch(scrollbar.getOrientation()){
                    case JScrollBar.VERTICAL:
                        size=scrollbar.getWidth();
                        break;
                    case JScrollBar.HORIZONTAL:
                        size=scrollbar.getHeight();
                        break;
                }
                size=Math.max(size,5);
            }
            return new Dimension(size,size);
        }
    }
}