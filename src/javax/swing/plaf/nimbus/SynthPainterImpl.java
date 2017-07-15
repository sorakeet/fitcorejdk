/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import javax.swing.*;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

class SynthPainterImpl extends SynthPainter{
    private NimbusStyle style;

    SynthPainterImpl(NimbusStyle style){
        this.style=style;
    }

    public void paintArrowButtonBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBackground(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBackground(context,g,0,0,w,h,transform);
        }
    }

    private void paintBackground(SynthContext ctx,Graphics g,int x,int y,
                                 int w,int h,AffineTransform transform){
        // if the background color of the component is 100% transparent
        // then we should not paint any background graphics. This is a solution
        // for there being no way of turning off Nimbus background painting as
        // basic components are all non-opaque by default.
        Component c=ctx.getComponent();
        Color bg=(c!=null)?c.getBackground():null;
        if(bg==null||bg.getAlpha()>0){
            Painter backgroundPainter=style.getBackgroundPainter(ctx);
            if(backgroundPainter!=null){
                paint(backgroundPainter,ctx,g,x,y,w,h,transform);
            }
        }
    }

    private void paint(Painter p,SynthContext ctx,Graphics g,int x,int y,
                       int w,int h,AffineTransform transform){
        if(p!=null){
            if(g instanceof Graphics2D){
                Graphics2D gfx=(Graphics2D)g;
                if(transform!=null){
                    gfx.transform(transform);
                }
                gfx.translate(x,y);
                p.paint(gfx,ctx.getComponent(),w,h);
                gfx.translate(-x,-y);
                if(transform!=null){
                    try{
                        gfx.transform(transform.createInverse());
                    }catch(NoninvertibleTransformException e){
                        // this should never happen as we are in control of all
                        // calls into this method and only ever pass in simple
                        // transforms of rotate, flip and translates
                        e.printStackTrace();
                    }
                }
            }else{
                // use image if we are printing to a Java 1.1 PrintGraphics as
                // it is not a instance of Graphics2D
                BufferedImage img=new BufferedImage(w,h,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D gfx=img.createGraphics();
                if(transform!=null){
                    gfx.transform(transform);
                }
                p.paint(gfx,ctx.getComponent(),w,h);
                gfx.dispose();
                g.drawImage(img,x,y,null);
                img=null;
            }
        }
    }

    public void paintArrowButtonBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    private void paintBorder(SynthContext ctx,Graphics g,int x,int y,int w,
                             int h,AffineTransform transform){
        Painter borderPainter=style.getBorderPainter(ctx);
        if(borderPainter!=null){
            paint(borderPainter,ctx,g,x,y,w,h,transform);
        }
    }

    public void paintArrowButtonForeground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h,
                                           int direction){
        //assume that the painter is arranged with the arrow pointing... LEFT?
        String compName=context.getComponent().getName();
        boolean ltr=context.getComponent().
                getComponentOrientation().isLeftToRight();
        // The hard coding for spinners here needs to be replaced by a more
        // general method for disabling rotation
        if("Spinner.nextButton".equals(compName)||
                "Spinner.previousButton".equals(compName)){
            if(ltr){
                paintForeground(context,g,x,y,w,h,null);
            }else{
                AffineTransform transform=new AffineTransform();
                transform.translate(w,0);
                transform.scale(-1,1);
                paintForeground(context,g,x,y,w,h,transform);
            }
        }else if(direction==SwingConstants.WEST){
            paintForeground(context,g,x,y,w,h,null);
        }else if(direction==SwingConstants.NORTH){
            if(ltr){
                AffineTransform transform=new AffineTransform();
                transform.scale(-1,1);
                transform.rotate(Math.toRadians(90));
                paintForeground(context,g,y,0,h,w,transform);
            }else{
                AffineTransform transform=new AffineTransform();
                transform.rotate(Math.toRadians(90));
                transform.translate(0,-(x+w));
                paintForeground(context,g,y,0,h,w,transform);
            }
        }else if(direction==SwingConstants.EAST){
            AffineTransform transform=new AffineTransform();
            transform.translate(w,0);
            transform.scale(-1,1);
            paintForeground(context,g,x,y,w,h,transform);
        }else if(direction==SwingConstants.SOUTH){
            if(ltr){
                AffineTransform transform=new AffineTransform();
                transform.rotate(Math.toRadians(-90));
                transform.translate(-h,0);
                paintForeground(context,g,y,x,h,w,transform);
            }else{
                AffineTransform transform=new AffineTransform();
                transform.scale(-1,1);
                transform.rotate(Math.toRadians(-90));
                transform.translate(-(h+y),-(w+x));
                paintForeground(context,g,y,x,h,w,transform);
            }
        }
    }

    private void paintForeground(SynthContext ctx,Graphics g,int x,int y,
                                 int w,int h,AffineTransform transform){
        Painter foregroundPainter=style.getForegroundPainter(ctx);
        if(foregroundPainter!=null){
            paint(foregroundPainter,ctx,g,x,y,w,h,transform);
        }
    }

    public void paintButtonBackground(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintButtonBorder(SynthContext context,
                                  Graphics g,int x,int y,
                                  int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintCheckBoxMenuItemBackground(SynthContext context,
                                                Graphics g,int x,int y,
                                                int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintCheckBoxMenuItemBorder(SynthContext context,
                                            Graphics g,int x,int y,
                                            int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintCheckBoxBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintCheckBoxBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintColorChooserBackground(SynthContext context,
                                            Graphics g,int x,int y,
                                            int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintColorChooserBorder(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintComboBoxBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBackground(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBackground(context,g,0,0,w,h,transform);
        }
    }

    public void paintComboBoxBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintDesktopIconBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintDesktopIconBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintDesktopPaneBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintDesktopPaneBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintEditorPaneBackground(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintEditorPaneBorder(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintFileChooserBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintFileChooserBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintFormattedTextFieldBackground(SynthContext context,
                                                  Graphics g,int x,int y,
                                                  int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBackground(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBackground(context,g,0,0,w,h,transform);
        }
    }

    public void paintFormattedTextFieldBorder(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBorder(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBorder(context,g,0,0,w,h,transform);
        }
    }

    public void paintInternalFrameTitlePaneBackground(SynthContext context,
                                                      Graphics g,int x,int y,
                                                      int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintInternalFrameTitlePaneBorder(SynthContext context,
                                                  Graphics g,int x,int y,
                                                  int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintInternalFrameBackground(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintInternalFrameBorder(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintLabelBackground(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintLabelBorder(SynthContext context,
                                 Graphics g,int x,int y,
                                 int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintListBackground(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintListBorder(SynthContext context,
                                Graphics g,int x,int y,
                                int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintMenuBarBackground(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintMenuBarBorder(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintMenuItemBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintMenuItemBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintMenuBackground(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintMenuBorder(SynthContext context,
                                Graphics g,int x,int y,
                                int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintOptionPaneBackground(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintOptionPaneBorder(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintPanelBackground(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintPanelBorder(SynthContext context,
                                 Graphics g,int x,int y,
                                 int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintPasswordFieldBackground(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintPasswordFieldBorder(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintPopupMenuBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintPopupMenuBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintProgressBarBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintProgressBarBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    private void paintBackground(SynthContext ctx,Graphics g,int x,int y,int w,int h,int orientation){
        Component c=ctx.getComponent();
        boolean ltr=c.getComponentOrientation().isLeftToRight();
        // Don't RTL flip JSpliders as they handle it internaly
        if(ctx.getComponent() instanceof JSlider) ltr=true;
        if(orientation==SwingConstants.VERTICAL&&ltr){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintBackground(ctx,g,y,x,h,w,transform);
        }else if(orientation==SwingConstants.VERTICAL){
            AffineTransform transform=new AffineTransform();
            transform.rotate(Math.toRadians(90));
            transform.translate(0,-(x+w));
            paintBackground(ctx,g,y,x,h,w,transform);
        }else if(orientation==SwingConstants.HORIZONTAL&&ltr){
            paintBackground(ctx,g,x,y,w,h,null);
        }else{
            //horizontal and right-to-left orientation
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBackground(ctx,g,0,0,w,h,transform);
        }
    }

    public void paintProgressBarBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintProgressBarBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    private void paintBorder(SynthContext ctx,Graphics g,int x,int y,int w,int h,int orientation){
        Component c=ctx.getComponent();
        boolean ltr=c.getComponentOrientation().isLeftToRight();
        if(orientation==SwingConstants.VERTICAL&&ltr){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintBorder(ctx,g,y,x,h,w,transform);
        }else if(orientation==SwingConstants.VERTICAL){
            AffineTransform transform=new AffineTransform();
            transform.rotate(Math.toRadians(90));
            transform.translate(0,-(x+w));
            paintBorder(ctx,g,y,0,h,w,transform);
        }else if(orientation==SwingConstants.HORIZONTAL&&ltr){
            paintBorder(ctx,g,x,y,w,h,null);
        }else{
            //horizontal and right-to-left orientation
            paintBorder(ctx,g,x,y,w,h,null);
        }
    }

    public void paintProgressBarForeground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h,int orientation){
        paintForeground(context,g,x,y,w,h,orientation);
    }

    private void paintForeground(SynthContext ctx,Graphics g,int x,int y,int w,int h,int orientation){
        Component c=ctx.getComponent();
        boolean ltr=c.getComponentOrientation().isLeftToRight();
        if(orientation==SwingConstants.VERTICAL&&ltr){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintForeground(ctx,g,y,x,h,w,transform);
        }else if(orientation==SwingConstants.VERTICAL){
            AffineTransform transform=new AffineTransform();
            transform.rotate(Math.toRadians(90));
            transform.translate(0,-(x+w));
            paintForeground(ctx,g,y,0,h,w,transform);
        }else if(orientation==SwingConstants.HORIZONTAL&&ltr){
            paintForeground(ctx,g,x,y,w,h,null);
        }else{
            //horizontal and right-to-left orientation
            paintForeground(ctx,g,x,y,w,h,null);
        }
    }

    public void paintRadioButtonMenuItemBackground(SynthContext context,
                                                   Graphics g,int x,int y,
                                                   int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintRadioButtonMenuItemBorder(SynthContext context,
                                               Graphics g,int x,int y,
                                               int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintRadioButtonBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintRadioButtonBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintRootPaneBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintRootPaneBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintScrollBarBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintScrollBarBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintScrollBarBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintScrollBarBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintScrollBarThumbBackground(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintScrollBarThumbBorder(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintScrollBarTrackBackground(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintScrollBarTrackBackground(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintScrollBarTrackBorder(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintScrollBarTrackBorder(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintScrollPaneBackground(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintScrollPaneBorder(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintSeparatorBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSeparatorBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintSeparatorBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintSeparatorBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintSeparatorForeground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h,int orientation){
        paintForeground(context,g,x,y,w,h,orientation);
    }

    public void paintSliderBackground(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSliderBackground(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintSliderBorder(SynthContext context,
                                  Graphics g,int x,int y,
                                  int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintSliderBorder(SynthContext context,
                                  Graphics g,int x,int y,
                                  int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintSliderThumbBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h,int orientation){
        if(context.getComponent().getClientProperty(
                "Slider.paintThumbArrowShape")==Boolean.TRUE){
            if(orientation==JSlider.HORIZONTAL){
                orientation=JSlider.VERTICAL;
            }else{
                orientation=JSlider.HORIZONTAL;
            }
            paintBackground(context,g,x,y,w,h,orientation);
        }else{
            paintBackground(context,g,x,y,w,h,orientation);
        }
    }

    public void paintSliderThumbBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintSliderTrackBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSliderTrackBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintSliderTrackBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintSliderTrackBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintSpinnerBackground(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSpinnerBorder(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintSplitPaneDividerBackground(SynthContext context,
                                                Graphics g,int x,int y,
                                                int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSplitPaneDividerBackground(SynthContext context,
                                                Graphics g,int x,int y,
                                                int w,int h,int orientation){
        if(orientation==JSplitPane.HORIZONTAL_SPLIT){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintBackground(context,g,y,x,h,w,transform);
        }else{
            paintBackground(context,g,x,y,w,h,null);
        }
    }

    public void paintSplitPaneDividerForeground(SynthContext context,
                                                Graphics g,int x,int y,
                                                int w,int h,int orientation){
        paintForeground(context,g,x,y,w,h,null);
    }

    public void paintSplitPaneDragDivider(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSplitPaneBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintSplitPaneBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneBackground(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneBorder(SynthContext context,
                                      Graphics g,int x,int y,
                                      int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabAreaBackground(SynthContext context,
                                                 Graphics g,int x,int y,
                                                 int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabAreaBackground(SynthContext context,
                                                 Graphics g,int x,int y,
                                                 int w,int h,int orientation){
        if(orientation==JTabbedPane.LEFT){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintBackground(context,g,y,x,h,w,transform);
        }else if(orientation==JTabbedPane.RIGHT){
            AffineTransform transform=new AffineTransform();
            transform.rotate(Math.toRadians(90));
            transform.translate(0,-(x+w));
            paintBackground(context,g,y,0,h,w,transform);
        }else if(orientation==JTabbedPane.BOTTOM){
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(1,-1);
            transform.translate(0,-h);
            paintBackground(context,g,0,0,w,h,transform);
        }else{
            paintBackground(context,g,x,y,w,h,null);
        }
    }

    public void paintTabbedPaneTabAreaBorder(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabAreaBorder(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabBackground(SynthContext context,Graphics g,
                                             int x,int y,int w,int h,
                                             int tabIndex){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabBackground(SynthContext context,Graphics g,
                                             int x,int y,int w,int h,
                                             int tabIndex,int orientation){
        if(orientation==JTabbedPane.LEFT){
            AffineTransform transform=new AffineTransform();
            transform.scale(-1,1);
            transform.rotate(Math.toRadians(90));
            paintBackground(context,g,y,x,h,w,transform);
        }else if(orientation==JTabbedPane.RIGHT){
            AffineTransform transform=new AffineTransform();
            transform.rotate(Math.toRadians(90));
            transform.translate(0,-(x+w));
            paintBackground(context,g,y,0,h,w,transform);
        }else if(orientation==JTabbedPane.BOTTOM){
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(1,-1);
            transform.translate(0,-h);
            paintBackground(context,g,0,0,w,h,transform);
        }else{
            paintBackground(context,g,x,y,w,h,null);
        }
    }

    public void paintTabbedPaneTabBorder(SynthContext context,Graphics g,
                                         int x,int y,int w,int h,
                                         int tabIndex){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneTabBorder(SynthContext context,Graphics g,
                                         int x,int y,int w,int h,
                                         int tabIndex,int orientation){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneContentBackground(SynthContext context,
                                                 Graphics g,int x,int y,int w,
                                                 int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTabbedPaneContentBorder(SynthContext context,Graphics g,
                                             int x,int y,int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTableHeaderBackground(SynthContext context,
                                           Graphics g,int x,int y,
                                           int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTableHeaderBorder(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTableBackground(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTableBorder(SynthContext context,
                                 Graphics g,int x,int y,
                                 int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTextAreaBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTextAreaBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTextPaneBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTextPaneBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTextFieldBackground(SynthContext context,
                                         Graphics g,int x,int y,
                                         int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBackground(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBackground(context,g,0,0,w,h,transform);
        }
    }

    public void paintTextFieldBorder(SynthContext context,
                                     Graphics g,int x,int y,
                                     int w,int h){
        if(context.getComponent().getComponentOrientation().isLeftToRight()){
            paintBorder(context,g,x,y,w,h,null);
        }else{
            AffineTransform transform=new AffineTransform();
            transform.translate(x,y);
            transform.scale(-1,1);
            transform.translate(-w,0);
            paintBorder(context,g,0,0,w,h,transform);
        }
    }

    public void paintToggleButtonBackground(SynthContext context,
                                            Graphics g,int x,int y,
                                            int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintToggleButtonBorder(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintToolBarBackground(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintToolBarBackground(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintToolBarBorder(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintToolBarBorder(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintToolBarContentBackground(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintToolBarContentBackground(SynthContext context,
                                              Graphics g,int x,int y,
                                              int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintToolBarContentBorder(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintToolBarContentBorder(SynthContext context,
                                          Graphics g,int x,int y,
                                          int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintToolBarDragWindowBackground(SynthContext context,
                                                 Graphics g,int x,int y,
                                                 int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintToolBarDragWindowBackground(SynthContext context,
                                                 Graphics g,int x,int y,
                                                 int w,int h,int orientation){
        paintBackground(context,g,x,y,w,h,orientation);
    }

    public void paintToolBarDragWindowBorder(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintToolBarDragWindowBorder(SynthContext context,
                                             Graphics g,int x,int y,
                                             int w,int h,int orientation){
        paintBorder(context,g,x,y,w,h,orientation);
    }

    public void paintToolTipBackground(SynthContext context,
                                       Graphics g,int x,int y,
                                       int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintToolTipBorder(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTreeBackground(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTreeBorder(SynthContext context,
                                Graphics g,int x,int y,
                                int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTreeCellBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintTreeCellBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }

    public void paintTreeCellFocus(SynthContext context,
                                   Graphics g,int x,int y,
                                   int w,int h){
        //TODO
    }

    public void paintViewportBackground(SynthContext context,
                                        Graphics g,int x,int y,
                                        int w,int h){
        paintBackground(context,g,x,y,w,h,null);
    }

    public void paintViewportBorder(SynthContext context,
                                    Graphics g,int x,int y,
                                    int w,int h){
        paintBorder(context,g,x,y,w,h,null);
    }
}
