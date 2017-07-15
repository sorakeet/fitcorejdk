/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.View;
import java.awt.*;

public class SynthPasswordFieldUI extends SynthTextFieldUI{
    public static ComponentUI createUI(JComponent c){
        return new SynthPasswordFieldUI();
    }

    @Override
    protected String getPropertyPrefix(){
        return "PasswordField";
    }

    @Override
    public View create(Element elem){
        return new PasswordView(elem);
    }

    @Override
    void paintBackground(SynthContext context,Graphics g,JComponent c){
        context.getPainter().paintPasswordFieldBackground(context,g,0,0,
                c.getWidth(),c.getHeight());
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintPasswordFieldBorder(context,g,x,y,w,h);
    }

    @Override
    protected void installKeyboardActions(){
        super.installKeyboardActions();
        ActionMap map=SwingUtilities.getUIActionMap(getComponent());
        if(map!=null&&map.get(DefaultEditorKit.selectWordAction)!=null){
            Action a=map.get(DefaultEditorKit.selectLineAction);
            if(a!=null){
                map.put(DefaultEditorKit.selectWordAction,a);
            }
        }
    }
}
