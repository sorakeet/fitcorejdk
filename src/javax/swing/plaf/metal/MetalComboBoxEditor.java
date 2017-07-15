/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;

public class MetalComboBoxEditor extends BasicComboBoxEditor{
    protected static Insets editorBorderInsets=new Insets(2,2,2,0);

    public MetalComboBoxEditor(){
        super();
        //editor.removeFocusListener(this);
        editor=new JTextField("",9){
            // workaround for 4530952
            public void setText(String s){
                if(getText().equals(s)){
                    return;
                }
                super.setText(s);
            }

            // The preferred and minimum sizes are overriden and padded by
            // 4 to keep the size as it previously was.  Refer to bugs
            // 4775789 and 4517214 for details.
            public Dimension getPreferredSize(){
                Dimension pref=super.getPreferredSize();
                pref.height+=4;
                return pref;
            }

            public Dimension getMinimumSize(){
                Dimension min=super.getMinimumSize();
                min.height+=4;
                return min;
            }
        };
        editor.setBorder(new EditorBorder());
        //editor.addFocusListener(this);
    }

    public static class UIResource extends MetalComboBoxEditor
            implements javax.swing.plaf.UIResource{
    }

    class EditorBorder extends AbstractBorder{
        public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            g.translate(x,y);
            if(MetalLookAndFeel.usingOcean()){
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.drawRect(0,0,w,h-1);
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(1,1,w-2,h-3);
            }else{
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.drawLine(0,0,w-1,0);
                g.drawLine(0,0,0,h-2);
                g.drawLine(0,h-2,w-1,h-2);
                g.setColor(MetalLookAndFeel.getControlHighlight());
                g.drawLine(1,1,w-1,1);
                g.drawLine(1,1,1,h-1);
                g.drawLine(1,h-1,w-1,h-1);
                g.setColor(MetalLookAndFeel.getControl());
                g.drawLine(1,h-2,1,h-2);
            }
            g.translate(-x,-y);
        }

        public Insets getBorderInsets(Component c,Insets insets){
            insets.set(2,2,2,0);
            return insets;
        }
    }
}
