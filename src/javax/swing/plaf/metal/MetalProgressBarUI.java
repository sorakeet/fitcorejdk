/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class MetalProgressBarUI extends BasicProgressBarUI{
    private Rectangle innards;
    private Rectangle box;

    public static ComponentUI createUI(JComponent c){
        return new MetalProgressBarUI();
    }

    public void paintIndeterminate(Graphics g,JComponent c){
        super.paintIndeterminate(g,c);
        if(!progressBar.isBorderPainted()||(!(g instanceof Graphics2D))){
            return;
        }
        Insets b=progressBar.getInsets(); // area for border
        int barRectWidth=progressBar.getWidth()-(b.left+b.right);
        int barRectHeight=progressBar.getHeight()-(b.top+b.bottom);
        int amountFull=getAmountFull(b,barRectWidth,barRectHeight);
        boolean isLeftToRight=MetalUtils.isLeftToRight(c);
        int startX, startY, endX, endY;
        Rectangle box=null;
        box=getBox(box);
        // The progress bar border is painted according to a light source.
        // This light source is stationary and does not change when the
        // component orientation changes.
        startX=b.left;
        startY=b.top;
        endX=b.left+barRectWidth-1;
        endY=b.top+barRectHeight-1;
        Graphics2D g2=(Graphics2D)g;
        g2.setStroke(new BasicStroke(1.f));
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            // Draw light line lengthwise across the progress bar.
            g2.setColor(MetalLookAndFeel.getControlShadow());
            g2.drawLine(startX,startY,endX,startY);
            g2.drawLine(startX,startY,startX,endY);
            // Draw darker lengthwise line over filled area.
            g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            g2.drawLine(box.x,startY,box.x+box.width-1,startY);
        }else{ // VERTICAL
            // Draw light line lengthwise across the progress bar.
            g2.setColor(MetalLookAndFeel.getControlShadow());
            g2.drawLine(startX,startY,startX,endY);
            g2.drawLine(startX,startY,endX,startY);
            // Draw darker lengthwise line over filled area.
            g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            g2.drawLine(startX,box.y,startX,box.y+box.height-1);
        }
    }

    public void paintDeterminate(Graphics g,JComponent c){
        super.paintDeterminate(g,c);
        if(!(g instanceof Graphics2D)){
            return;
        }
        if(progressBar.isBorderPainted()){
            Insets b=progressBar.getInsets(); // area for border
            int barRectWidth=progressBar.getWidth()-(b.left+b.right);
            int barRectHeight=progressBar.getHeight()-(b.top+b.bottom);
            int amountFull=getAmountFull(b,barRectWidth,barRectHeight);
            boolean isLeftToRight=MetalUtils.isLeftToRight(c);
            int startX, startY, endX, endY;
            // The progress bar border is painted according to a light source.
            // This light source is stationary and does not change when the
            // component orientation changes.
            startX=b.left;
            startY=b.top;
            endX=b.left+barRectWidth-1;
            endY=b.top+barRectHeight-1;
            Graphics2D g2=(Graphics2D)g;
            g2.setStroke(new BasicStroke(1.f));
            if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
                // Draw light line lengthwise across the progress bar.
                g2.setColor(MetalLookAndFeel.getControlShadow());
                g2.drawLine(startX,startY,endX,startY);
                if(amountFull>0){
                    // Draw darker lengthwise line over filled area.
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                    if(isLeftToRight){
                        g2.drawLine(startX,startY,
                                startX+amountFull-1,startY);
                    }else{
                        g2.drawLine(endX,startY,
                                endX-amountFull+1,startY);
                        if(progressBar.getPercentComplete()!=1.f){
                            g2.setColor(MetalLookAndFeel.getControlShadow());
                        }
                    }
                }
                // Draw a line across the width.  The color is determined by
                // the code above.
                g2.drawLine(startX,startY,startX,endY);
            }else{ // VERTICAL
                // Draw light line lengthwise across the progress bar.
                g2.setColor(MetalLookAndFeel.getControlShadow());
                g2.drawLine(startX,startY,startX,endY);
                if(amountFull>0){
                    // Draw darker lengthwise line over filled area.
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                    g2.drawLine(startX,endY,
                            startX,endY-amountFull+1);
                }
                // Draw a line across the width.  The color is determined by
                // the code above.
                g2.setColor(MetalLookAndFeel.getControlShadow());
                if(progressBar.getPercentComplete()==1.f){
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                }
                g2.drawLine(startX,startY,endX,startY);
            }
        }
    }
}
