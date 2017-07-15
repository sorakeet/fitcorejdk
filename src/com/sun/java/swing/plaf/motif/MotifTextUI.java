/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MotifTextUI{
    static final JTextComponent.KeyBinding[] defaultBindings={
            new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,
                    InputEvent.CTRL_MASK),
                    DefaultEditorKit.copyAction),
            new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,
                    InputEvent.SHIFT_MASK),
                    DefaultEditorKit.pasteAction),
            new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                    InputEvent.SHIFT_MASK),
                    DefaultEditorKit.cutAction),
            new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                    InputEvent.SHIFT_MASK),
                    DefaultEditorKit.selectionBackwardAction),
            new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                    InputEvent.SHIFT_MASK),
                    DefaultEditorKit.selectionForwardAction),
    };

    public static Caret createCaret(){
        return new MotifCaret();
    }

    public static class MotifCaret extends DefaultCaret implements UIResource{
        static final int IBeamOverhang=2;

        protected void damage(Rectangle r){
            if(r!=null){
                x=r.x-IBeamOverhang-1;
                y=r.y;
                width=r.width+(2*IBeamOverhang)+3;
                height=r.height;
                repaint();
            }
        }

        public void focusGained(FocusEvent e){
            super.focusGained(e);
            getComponent().repaint();
        }

        public void focusLost(FocusEvent e){
            super.focusLost(e);
            getComponent().repaint();
        }

        public void paint(Graphics g){
            if(isVisible()){
                try{
                    JTextComponent c=getComponent();
                    Color fg=c.hasFocus()?c.getCaretColor():
                            c.getDisabledTextColor();
                    TextUI mapper=c.getUI();
                    int dot=getDot();
                    Rectangle r=mapper.modelToView(c,dot);
                    int x0=r.x-IBeamOverhang;
                    int x1=r.x+IBeamOverhang;
                    int y0=r.y+1;
                    int y1=r.y+r.height-2;
                    g.setColor(fg);
                    g.drawLine(r.x,y0,r.x,y1);
                    g.drawLine(x0,y0,x1,y0);
                    g.drawLine(x0,y1,x1,y1);
                }catch(BadLocationException e){
                    // can't render I guess
                    //System.err.println("Can't render caret");
                }
            }
        }
    }
}
