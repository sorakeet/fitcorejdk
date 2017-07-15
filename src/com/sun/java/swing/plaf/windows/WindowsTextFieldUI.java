/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.*;
import java.awt.*;

public class WindowsTextFieldUI extends BasicTextFieldUI{
    public static ComponentUI createUI(JComponent c){
        return new WindowsTextFieldUI();
    }

    protected Caret createCaret(){
        return new WindowsFieldCaret();
    }

    protected void paintBackground(Graphics g){
        super.paintBackground(g);
    }

    static class WindowsFieldCaret extends DefaultCaret implements UIResource{
        public WindowsFieldCaret(){
            super();
        }

        protected void adjustVisibility(Rectangle r){
            SwingUtilities.invokeLater(new SafeScroller(r));
        }

        protected Highlighter.HighlightPainter getSelectionPainter(){
            return WindowsTextUI.WindowsPainter;
        }

        private class SafeScroller implements Runnable{
            private Rectangle r;

            SafeScroller(Rectangle r){
                this.r=r;
            }

            public void run(){
                JTextField field=(JTextField)getComponent();
                if(field!=null){
                    TextUI ui=field.getUI();
                    int dot=getDot();
                    // PENDING: We need to expose the bias in DefaultCaret.
                    Position.Bias bias=Position.Bias.Forward;
                    Rectangle startRect=null;
                    try{
                        startRect=ui.modelToView(field,dot,bias);
                    }catch(BadLocationException ble){
                    }
                    Insets i=field.getInsets();
                    BoundedRangeModel vis=field.getHorizontalVisibility();
                    int x=r.x+vis.getValue()-i.left;
                    int quarterSpan=vis.getExtent()/4;
                    if(r.x<i.left){
                        vis.setValue(x-quarterSpan);
                    }else if(r.x+r.width>i.left+vis.getExtent()){
                        vis.setValue(x-(3*quarterSpan));
                    }
                    // If we scroll, our visual location will have changed,
                    // but we won't have updated our internal location as
                    // the model hasn't changed. This checks for the change,
                    // and if necessary, resets the internal location.
                    if(startRect!=null){
                        try{
                            Rectangle endRect;
                            endRect=ui.modelToView(field,dot,bias);
                            if(endRect!=null&&!endRect.equals(startRect)){
                                damage(endRect);
                            }
                        }catch(BadLocationException ble){
                        }
                    }
                }
            }
        }
    }
}
